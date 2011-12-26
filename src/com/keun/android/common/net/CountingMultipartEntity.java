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

package com.keun.android.common.net;

import com.keun.android.common.utils.Logger;

import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * 파일 업로드 시 업로드 진행률을 읽어온다.
 * 
 * @author Keun-yang Son
 * @since 2011. 12. 19.
 * @version 1.0
 * @see MultipartEntity
 */
public class CountingMultipartEntity extends MultipartEntity {

    private final ProgressListener mListener;

    public CountingMultipartEntity(final ProgressListener listener) {
        super();
        this.mListener = listener;
    }

    public CountingMultipartEntity(final HttpMultipartMode mode, final ProgressListener listener) {
        super(mode);
        this.mListener = listener;
    }

    public CountingMultipartEntity(HttpMultipartMode mode, final String boundary,
            final Charset charset, final ProgressListener listener) {
        super(mode, boundary, charset);
        this.mListener = listener;
    }

    @Override
    public void writeTo(final OutputStream outstream) throws IOException {
        Logger.i(getClass(), "ContentLength = " + getContentLength());
        super.writeTo(new CountingOutputStream(outstream, this.mListener, getContentLength()));
    }

    /**
     * 업로드 진행률을 받을 수 있는 Interface.
     * 
     * @author Keun-yang Son
     * @since 2011. 12. 19.
     * @version 1.0
     */
    public static interface ProgressListener {
        public void transferred(long length, long num);
    }

    public static class CountingOutputStream extends FilterOutputStream {

        /**
         * The minimum amount of progress that has to be done before the
         * progress bar gets updated
         */
        public static final int MIN_PROGRESS_STEP = 4096;

        /**
         * The minimum amount of time that has to elapse before the progress bar
         * gets updated, in ms
         */
        public static final long MIN_PROGRESS_TIME = 500;

        private long mCurrentBytes;
        private long mTransferred;
        private long mTimeLastNotification;
        private final long mContentLength;
        private final ProgressListener mListener;

        public CountingOutputStream(final OutputStream out, final ProgressListener listener,
                final long contentLength) {
            super(out);
            this.mListener = listener;
            this.mContentLength = contentLength;
            this.mTransferred = 0;
        }

        public void write(byte[] b, int off, int len) throws IOException {
            this.out.write(b, off, len);
            this.mTransferred += len;
            if (mListener != null) {
                // 업로드를 모두 진행하였거나 업로드 사이즈가 4096보다크고 500ms가 지났으면 업데이트한다.
                long now = System.currentTimeMillis();
                if ((mContentLength <= mTransferred)
                        || (mTransferred - mCurrentBytes) > MIN_PROGRESS_STEP
                        && now - mTimeLastNotification > MIN_PROGRESS_TIME) {
                    this.mCurrentBytes = this.mTransferred;
                    this.mTimeLastNotification = now;
                    this.mListener.transferred(this.mContentLength, this.mTransferred);
                }
            }
        }

        public void write(int b) throws IOException {
            this.out.write(b);
            this.mTransferred++;
            if (mListener != null) {
                this.mListener.transferred(this.mContentLength, this.mTransferred);
            }
        }
    }

}
