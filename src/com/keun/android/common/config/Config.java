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

package com.keun.android.common.config;

/**
 * @author Keun-yang Son
 * @since 2012. 1. 11.
 * @version 1.0
 * @see
 */
public class Config {

    public static final String UTF_8 = "UTF-8";

    /** Native AndroidHttpClien는 Froyo 이상에서만 사용이 가능하다. */
    public static final int API_LEVEL_FROYO = 8;

    /** 최소의 외부/내부 메모리 공간 (10 MByte) */
    public static final long MIN_STORAGE_SIZE = 10 * 1024 * 1024;

    /** Cache로 저장할 메모리의 기본 사이즈를 설정한다. */
    public static final long MAX_CACHE_STORAGE_SIZE = 20 * 1024;

    /** 이미지 Cache 제한 용량을 검사하는 최소 간격. */
    public static final long MIN_CACHE_CLEAR_TIME = 20 * 60 * 1000;

    /**
     * The minimum amount of progress that has to be done before the progress
     * bar gets updated
     */
    public static final int MIN_PROGRESS_STEP = 4096;

    /**
     * The minimum amount of time that has to elapse before the progress bar
     * gets updated, in ms
     */
    public static final long MIN_PROGRESS_TIME = 500;

}
