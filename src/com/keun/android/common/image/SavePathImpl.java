/*
 * Copyright (C) 2011 The Common Platform Team, KTH, Inc. All rights reserved.
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
import android.os.Build;
import android.os.Environment;

import com.keun.android.common.config.Config;
import com.keun.android.common.utils.Logger;
import com.keun.android.common.utils.StopWatchAverage;
import com.keun.android.common.utils.StorageUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 파일을 내부/외부 디스크에 Cache한다.
 *
 * @author Keun-yang Son
 * @since 2011. 12. 23.
 * @version 1.0
 * @see
 */
public class SavePathImpl implements ImageDownloader.SavePath {

    /** Cache 제한 용량 마지막 체크 시간. */
    private static long mTimeLastCheck;
    private static boolean sIsRunClearCache;

    private final Context mContext;

    /**
     * @param ctx Context
     * @param paths Cache된 파일을 삭제할 경로 설정.
     * @param size Cache 폴더의 최대 사이즈 설정.
     */
    public SavePathImpl(final Context ctx) {
        this.mContext = ctx;
    }

    /*
     * @see com.keun.android.common.image.ImageDownloader.SavePath#getPath()
     */
    @Override
    public String getPath() {
        try {
            // 외부 메모리가 10M 이상인 경우에만 저장한다.
            if (StorageUtils.availableExternalStorageSize() > Config.MIN_STORAGE_SIZE) {
                // 프로요 이전 버전은 외부 cache를 API에서 지원하지 않음.
                if (Build.VERSION.SDK_INT < Config.API_LEVEL_FROYO) {
                    return Environment.getExternalStorageDirectory() + File.separator + "Android"
                            + File.separator + mContext.getPackageName() + File.separator + "cache";
                } else {
                    return mContext.getExternalCacheDir().getAbsolutePath();
                }
            } else if (StorageUtils.availableInternalStorageSize() > Config.MIN_STORAGE_SIZE) {
                // 내부 메모리가 10M 이상인 경우에만 저장한다.
                return mContext.getCacheDir().getAbsolutePath();
            }
        } catch (Exception e) {
            if (Logger.isWarnEnabled()) {
                Logger.w(getClass(), e.toString());
            }
        }
        return null;
    }

    /*
     * @see com.keun.android.common.image.ImageDownloader.SavePath#clear()
     */
    @Override
    public void clear() {
        if (Logger.isDebugEnabled()) {
            Logger.d(getClass(), "clear.");
        }
        // Cache 디렉토리의 사이즈를 검사한다.
        long now = System.currentTimeMillis();
        if ((now - mTimeLastCheck) < Config.MIN_CACHE_CLEAR_TIME) {
            return;
        }

        mTimeLastCheck = now;
        if (sIsRunClearCache == false) {
            synchronized (SavePathImpl.class) {
                if (sIsRunClearCache == false) {
                    sIsRunClearCache = true;
                    new ImageCacheClear().start();
                }
            }
        }
    }

    class ImageCacheClear extends Thread {

        @Override
        public void run() {
            StopWatchAverage swa = null;
            try {
                String path = getPath();
                long size = size(0, path);
                if (Logger.isVerboseEnabled()) {
                    Logger.v(getClass(), "Dir Total Size : " + size);
                    swa = new StopWatchAverage("Image Dir Size Check - Run Time");
                }

                // 폴더가 아니면 작업을 중단한다.
                if (!((new File(path)).isDirectory())) {
                    if (Logger.isWarnEnabled()) {
                        Logger.w(getClass(), path + "는(은) 폴더가 아니다.");
                    }
                    return;
                }
                if (Config.MAX_CACHE_STORAGE_SIZE > 0
                        && (Config.MAX_CACHE_STORAGE_SIZE * 1024) < size) {
                    delete(path);
                }
            } catch (Exception e) {
                if (Logger.isWarnEnabled()) {
                    Logger.w(getClass(), e);
                }
            } finally {
                if (Logger.isVerboseEnabled() && swa != null) {
                    Logger.v(getClass(), swa.toString());
                }
                sIsRunClearCache = false;
            }
        }

        /**
         * Size 이하가 되도록 파일을 삭제한다.
         *
         * @param path 폴더 경로.
         */
        private void delete(final String path) {
            if (path == null) {
                if (Logger.isWarnEnabled()) {
                    Logger.w(getClass(), "폴더 경로가 Null이다.");
                }
                return;
            }
            if (Logger.isDebugEnabled()) {
                Logger.d(getClass(), path + " 폴더 검사.");
            }
            File[] files = new File(path).listFiles();
            sort(files, 0);
            for (File file : files) {
                if (file.isDirectory()) {
                    delete(file.getAbsolutePath());
                } else {
                    try {
                        // 이미지 폴더의 사이즈를 다시 계산한다.
                        long size = size(0, path); // Base 디렉토리에서 전체 사이즈를 구함.
                        if (Logger.isDebugEnabled()) {
                            StringBuilder sb = new StringBuilder();
                            sb.append("Image Dir Min Size : ");
                            sb.append(Config.MAX_CACHE_STORAGE_SIZE * 1024).append(" byte");
                            sb.append(", Image Dir Size: ").append(size).append(" byte");
                            Logger.d(getClass(), sb.toString());
                        }
                        if ((Config.MAX_CACHE_STORAGE_SIZE * 1024) > size) {
                            return; // Cache를 Clear했으면 종료한다.
                        }
                        file.delete(); // 파일 삭제.
                        if (Logger.isDebugEnabled()) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(file.toString()).append(" 파일 삭제");
                            Logger.d(getClass(), sb.toString());
                        }
                    } catch (Exception e) {
                        if (Logger.isWarnEnabled()) {
                            Logger.w(getClass(), e);
                        }
                    }
                }
            }
        }

        /**
         * 파일을 생성날짜 순으로 정렬한다.
         *
         * @param files 정렬하려는 파일들의 배열.
         * @param sort 정렬 기준, 0 : ASC, 1 : DESC
         */
        private void sort(File[] files, final int sort) {
            // 파일을 날짜로 정렬한다.
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    if (sort == 0) { // ASC로 정렬..
                        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
                    } else { // DESC로 정렬..
                        return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
                    }
                }
            });
        }

        /**
         * Cache 디렉토리의 전체 싸이즈를 가져온다.
         */
        private long size(long size, final String path) {
            File[] files = new File(path).listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    size += size(size, file.getAbsolutePath());
                } else {
                    size += file.length();
                }
            }
            return size;
        }

    }

}
