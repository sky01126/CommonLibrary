/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.keun.android.common.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.ImageView;

import com.keun.android.common.net.HttpClientManager;
import com.keun.android.common.utils.Crc64Utils;
import com.keun.android.common.utils.Logger;
import com.keun.android.common.utils.StopWatchAverage;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.CookieStore;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This helper class download images from the Internet and binds those with the
 * provided ImageView.
 * <p>
 * It requires the INTERNET permission, which should be added to your
 * application's manifest file.
 * </p>
 * A local cache of downloaded images is maintained internally to improve
 * performance.
 */
public class ImageDownloader {
    private static final int HARD_CACHE_CAPACITY = 40;
    private static final int DELAY_BEFORE_PURGE = 30 * 1000; // in milliseconds

    private final Context mContext;
    private final String mUserAgent;

    private int mResourcesId;
    private SavePath mSavePath;

    public ImageDownloader(Context ctx, String userAgent) {
        this.mContext = ctx;
        this.mUserAgent = userAgent;
    }

    public ImageDownloader(Context ctx, String userAgent, SavePath savePath) {
        this.mContext = ctx;
        this.mUserAgent = userAgent;
        this.mSavePath = savePath;
    }

    /**
     * Default로 사용 할 Recources ID를 설정한다.
     * 
     * @param id Recources ID 설정.
     */
    public void setResourcesId(int id) {
        this.mResourcesId = id;
    }

    // Hard cache, with a fixed maximum capacity and a life duration
    private final HashMap<String, Bitmap> sHardBitmapCache =
            new LinkedHashMap<String, Bitmap>(HARD_CACHE_CAPACITY / 2, 0.75f, true) {
                private static final long serialVersionUID = -7190622541619388252L;

                @Override
                protected boolean removeEldestEntry(LinkedHashMap.Entry<String, Bitmap> eldest) {
                    if (size() > HARD_CACHE_CAPACITY) {
                        // Entries push-out of hard reference cache are
                        // transferred to soft reference cache
                        sSoftBitmapCache.put(eldest.getKey(),
                                new SoftReference<Bitmap>(eldest.getValue()));
                        return true;
                    } else {
                        return false;
                    }
                }
            };

    // Soft cache for bitmap kicked out of hard cache
    private final static ConcurrentHashMap<String, SoftReference<Bitmap>> sSoftBitmapCache =
            new ConcurrentHashMap<String, SoftReference<Bitmap>>(HARD_CACHE_CAPACITY / 2);

    private final Handler purgeHandler = new Handler();

    private final Runnable purger = new Runnable() {
        public void run() {
            clearCache();
        }
    };

    /**
     * Download the specified image from the Internet and binds it to the
     * provided ImageView. The binding is immediate if the image is found in the
     * cache and will be done asynchronously otherwise. A null bitmap will be
     * associated to the ImageView if an error occurs.
     * 
     * @param url The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     */
    public void download(String url, ImageView imageView) {
        download(url, imageView, null);
    }

    /**
     * Same as {@link #download(String, ImageView)}, with the possibility to
     * provide an additional cookie that will be used when the image will be
     * retrieved.
     * 
     * @param url The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     * @param cookie A cookie String that will be used by the http connection.
     */
    public void download(String url, ImageView imageView, String cookie) {
        resetPurgeTimer();
        Bitmap bitmap = getBitmapFromCache(url);

        if (bitmap == null) {
            forceDownload(url, imageView, cookie);
        } else {
            cancelPotentialDownload(url, imageView);
            imageView.setImageBitmap(bitmap);
        }
    }

    /**
     * Same as download but the image is always downloaded and the cache is not
     * used. Kept private at the moment as its interest is not clear.
     */
    private void forceDownload(String url, ImageView imageView, String cookie) {
        // State sanity: url is guaranteed to never be null in
        // DownloadedDrawable and cache keys.
        if (url == null) {
            if (mResourcesId > 0) {
                imageView.setImageDrawable(mContext.getResources().getDrawable(mResourcesId));
            } else {
                imageView.setImageDrawable(null);
            }
            return;
        }

        if (cancelPotentialDownload(url, imageView)) {
            BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
            DownloadedDrawable downloadedDrawable = null;
            if (mResourcesId > 0) {
                downloadedDrawable = new DownloadedDrawable(task, mContext, mResourcesId);
            } else {
                downloadedDrawable = new DownloadedDrawable(task);
            }
            imageView.setImageDrawable(downloadedDrawable);
            task.execute(url, cookie);
        }
    }

    /**
     * Clears the image cache used internally to improve performance. Note that
     * for memory efficiency reasons, the cache will automatically be cleared
     * after a certain inactivity delay.
     */
    public void clearCache() {
        sHardBitmapCache.clear();
        sSoftBitmapCache.clear();

        // Cache에 저장된 파일을 정리한다.
        if (mSavePath != null) {
            mSavePath.clear();
        }
    }

    private void resetPurgeTimer() {
        purgeHandler.removeCallbacks(purger);
        purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
    }

    /**
     * Returns true if the current download has been canceled or if there was no
     * download in progress on this image view. Returns false if the download in
     * progress deals with the same url. The download is not stopped in that
     * case.
     */
    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }

    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active download task (if any) associated
     *         with this imageView. null if there is no such task.
     */
    private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable) drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    /**
     * @param url The URL of the image that will be retrieved from the cache.
     * @return The cached bitmap or null if it was not found.
     */
    private Bitmap getBitmapFromCache(String url) {
        // First try the hard reference cache
        synchronized (sHardBitmapCache) {
            final Bitmap bitmap = sHardBitmapCache.get(url);
            if (bitmap != null) {
                // Bitmap found in hard cache
                // Move element to first position, so that it is removed last
                sHardBitmapCache.remove(url);
                sHardBitmapCache.put(url, bitmap);
                return bitmap;
            }
        }

        // Then try the soft reference cache
        SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(url);
        if (bitmapReference != null) {
            final Bitmap bitmap = bitmapReference.get();
            if (bitmap != null) {
                // Bitmap found in soft cache
                return bitmap;
            } else {
                // Soft reference has been Garbage Collected
                sSoftBitmapCache.remove(url);
            }
        }

        return null;
    }

    /**
     * The actual AsyncTask that will asynchronously download the image.
     */
    class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private static final int IO_BUFFER_SIZE = 4 * 1024;
        private String url;
        private final WeakReference<ImageView> imageViewReference;

        public BitmapDownloaderTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        /**
         * Actual download method.
         */
        @Override
        protected Bitmap doInBackground(String... params) {
            // TODO 다운로드 정보를 추가한다.
            url = params[0];
            String cookie = params[1];

            // 네트워크 연결을 수정한다.
            HttpEntity entity = null;
            HttpClientManager manager = null;
            try {
                if (url.startsWith("/") || url.startsWith("file://")) {
                    return getBitmap(new FileInputStream(url));
                }

                // 서버 URL인 경우 "http://" 또는 "https://"로 시작하지 않으면 "http://"를 붙여준다.
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "http://" + url;
                }

                // 로컬에 저장하지 않고 서버에서 이미지를 바로 가져온다.
                if (mSavePath == null || mSavePath.getPath() == null) {
                    // 네트워크를 연결한다.
                    manager = new HttpClientManager(mContext, mUserAgent);
                    entity = getRemoteImage(manager, cookie);
                    if (entity != null) {
                        return getBitmap(entity.getContent());
                    }
                }

                // Cache 파일을 로컬 디스크에 저장한 후 Bitmap을 읽어온다.
                String key = Crc64Utils.crc64(url);

                // Cache파일이 로컬에 존재하는지 확인한다.
                File file = new File(mSavePath.getPath(), key);
                if (file.exists()) { // 로컬에 파일이 존재한다.
                    return getBitmap(new FileInputStream(file));
                }

                // 서버에서 파일을 받아서 로컬에 저장 후 Cache에 등록한다.
                manager = new HttpClientManager(mContext, mUserAgent);
                entity = getRemoteImage(manager, cookie);
                if (entity != null) {
                    BufferedInputStream stream = new BufferedInputStream(entity.getContent());
                    boolean isSave = writeToFile(file, stream);
                    if (isSave == true) {
                        return getBitmap(new FileInputStream(file));
                    }
                }
            } catch (IOException e) {
                if (Logger.isWarnEnabled()) {
                    Logger.w(getClass(), "I/O error while retrieving bitmap from " + url, e);
                }
            } catch (IllegalStateException e) {
                if (Logger.isWarnEnabled()) {
                    Logger.w(getClass(), "Incorrect URL: " + url);
                }
            } catch (Exception e) {
                if (Logger.isWarnEnabled()) {
                    Logger.w(getClass(), "Error while retrieving bitmap from " + url, e);
                }
            } finally {
                if (entity != null) {
                    try {
                        entity.consumeContent();
                    } catch (IOException e) {
                        if (Logger.isVerboseEnabled()) {
                            Logger.v(getClass(), e.toString());
                        }
                    }
                }
                if (manager != null) {
                    manager.close();
                }
            }
            return null;
        }

        /**
         * Once the image is downloaded, associates it to the imageView
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            // Add bitmap to cache
            if (bitmap != null) {
                synchronized (sHardBitmapCache) {
                    sHardBitmapCache.put(url, bitmap);
                }
            }

            if (imageViewReference != null) {
                ImageView imageView = imageViewReference.get();
                BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                // Change bitmap only if this process is still associated with
                // it
                if (this == bitmapDownloaderTask) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }

        /**
         * 이미지파일을 디스크에 저장한다.
         * 
         * @param file 저장경로와 파일이름
         * @param bin BufferedInputStream
         * @return 저장 성공(true) / 실패(false)
         * @throws IOException
         */
        private boolean writeToFile(final File file, BufferedInputStream bin) throws IOException {
            StopWatchAverage swa = null;
            if (Logger.isDebugEnabled()) {
                swa = new StopWatchAverage("Image write (URL: " + file.toString() + ") - run time");
            }
            FileOutputStream fout = null;
            BufferedOutputStream bout = null;
            try {
                if (!file.getParentFile().isDirectory() && !file.getParentFile().mkdirs()) {
                    if (Logger.isErrorEnabled()) {
                        Logger.e(getClass(), "Unable to create cache directory " + file.getParent());
                    }
                    return false;
                }
                file.createNewFile();
                fout = new FileOutputStream(file);
                bout = new BufferedOutputStream(fout);
                int read = -1;
                while ((read = bin.read()) != -1) {
                    for (;;) {
                        try {
                            bout.write(read);
                            break;
                        } catch (IOException e) {
                            if (Logger.isVerboseEnabled()) {
                                Logger.v(getClass(), e.toString());
                            }
                        }
                    }
                }
                return true;
            } finally {
                if (bout != null) {
                    try {
                        bout.flush();
                        bout.close();
                        bout = null;
                    } catch (IOException e) {
                        if (Logger.isVerboseEnabled()) {
                            Logger.v(getClass(), e.toString());
                        }
                    }
                }
                if (fout != null) {
                    try {
                        fout.close();
                        fout = null;
                    } catch (IOException e) {
                        if (Logger.isVerboseEnabled()) {
                            Logger.v(getClass(), e.toString());
                        }
                    }
                }
                if (bin != null) {
                    try {
                        bin.close();
                        bin = null;
                    } catch (IOException e) {
                        if (Logger.isVerboseEnabled()) {
                            Logger.v(getClass(), e.toString());
                        }
                    }
                }
                if (Logger.isDebugEnabled() && swa != null) {
                    Logger.d(getClass(),
                            "[" + file.toString() + "] 이미지파일 저장 시간 - " + swa.toString());
                }
            }
        }

        /**
         * Remote 서버에서 이미지를 읽어온다.
         */
        private HttpEntity getRemoteImage(HttpClientManager manager, String cookie)
                throws IOException {
            // 이미지를 Cache할 경로를 설정하지 않은 경우에는 다운로드만 받고 종료한다.
            HttpResponse response = null;
            if (cookie != null) {
                CookieStore cookies = new BasicCookieStore();
                cookies.addCookie(new BasicClientCookie("cookie", cookie));
                response = manager.sendGet(url, cookies);
            } else {
                response = manager.sendGet(url);
            }
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                if (Logger.isWarnEnabled()) {
                    Logger.w(getClass(), "Error " + statusCode
                            + " while retrieving bitmap from " + url);
                }
                return null;
            }
            // 이미지인지 여부를 확인한다.
            Header contentType = response.getFirstHeader("Content-Type");
            if (contentType == null) {
                if (Logger.isWarnEnabled()) {
                    Logger.w(getClass(), "Content-Type Header 가 존재하지 않습니다.");
                }
                return null;
            }
            String value = contentType.getValue();
            if (value == null || !((value.toLowerCase()).startsWith("image"))) {
                if (Logger.isWarnEnabled()) {
                    Logger.w(getClass(), "(" + value + ")는(은) 이미지 파일이 아닙니다.");
                }
                return null;
            }
            return response.getEntity();
        }

        /**
         * InputStream을 Bitma으로 변환한다.
         */
        private Bitmap getBitmap(InputStream inputStream) throws IOException {
            OutputStream outputStream = null;
            try {
                final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
                outputStream = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
                copy(inputStream, outputStream);
                outputStream.flush();

                final byte[] data = dataStream.toByteArray();
                final Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                // FIXME : Should use
                // BitmapFactory.decodeStream(inputStream) instead.
                // final Bitmap bitmap =
                // BitmapFactory.decodeStream(inputStream);
                return bitmap;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        }

        public void copy(InputStream in, OutputStream out) throws IOException {
            byte[] b = new byte[IO_BUFFER_SIZE];
            int read;
            while ((read = in.read(b)) != -1) {
                out.write(b, 0, read);
            }
        }
    }

    /**
     * A fake Drawable that will be attached to the imageView while the download
     * is in progress.
     * <p>
     * Contains a reference to the actual download task, so that a download task
     * can be stopped if a new binding is required, and makes sure that only the
     * last started download process can bind its result, independently of the
     * download finish order.
     * </p>
     */
    static class DownloadedDrawable extends BitmapDrawable {
        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

        public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask) {
            bitmapDownloaderTaskReference =
                    new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
        }

        public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask, Context ctx, int id) {
            super(((BitmapDrawable) ctx.getResources().getDrawable(id)).getBitmap());
            bitmapDownloaderTaskReference =
                    new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
        }

        public BitmapDownloaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }

    /**
     * 파일을 내부/외부 디스크에 Cache한다.
     * 
     * @author Keun-yang Son
     * @since 2011. 12. 23.
     * @version 1.0
     * @see
     */
    public static interface SavePath {
        /**
         * Cache를 저장 할 경로를 가져온다.
         * 
         * @return Cache 저장 경로.
         */
        public String getPath();

        /**
         * Cache를 Clear한다.
         */
        public void clear();
    }
}
