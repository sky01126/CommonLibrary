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

import com.keun.android.common.utils.Logger;
import com.keun.android.common.utils.StorageUtils;

import java.io.File;

/**
 * 파일을 내부/외부 디스크에 Cache한다.
 *
 * @author Keun-yang Son
 * @since 2011. 12. 23.
 * @version 1.0
 * @see
 */
public class SavePathImpl implements ImageDownloader.SavePath {

    /** Froyo의 API Level을 설정한다. */
    private static final int API_LEVEL_FROYO = 8;

    /** 최소의 외부/내부 메모리 공간 (10 MByte) */
    private static final long MIN_STORAGE_SIZE = 10 * 1024 * 1024;

    /** Cache로 저장할 메모리의 기본 사이즈를 설정한다. */
    public static final long MAX_CACHE_STORAGE_SIZE = 20 * 1024;

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
            if (StorageUtils.availableExternalStorageSize() > MIN_STORAGE_SIZE) {
                // 프로요 이전 버전은 외부 cache를 API에서 지원하지 않음.
                if (Build.VERSION.SDK_INT < API_LEVEL_FROYO) {
                    return Environment.getExternalStorageDirectory() + File.separator + "Android"
                            + File.separator + mContext.getPackageName() + File.separator + "cache";
                } else {
                    return mContext.getExternalCacheDir().getAbsolutePath();
                }
            } else if (StorageUtils.availableInternalStorageSize() > MIN_STORAGE_SIZE) {
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
        // TODO Auto-generated method stub
        if (Logger.isDebugEnabled()) {
            Logger.d(getClass(), "clear.");
        }
    }

}
