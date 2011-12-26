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

package com.keun.android.common.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * URL을 Encoding, Decoding한다.
 * 
 * @author Keun-yang Son
 * @since 2011. 12. 8.
 * @version 1.0
 */
public class URLCodec {

    /**
     * URL 인코딩한다.
     * 
     * @param value 인코딩 될 String.
     * @return 인코딩된 String.
     * @throws UnsupportedEncodingException
     */
    public static final String encode(final String value) {
        return encode(value, "UTF-8");
    }

    /**
     * URL 인코딩한다.
     * 
     * @param value 인코딩 될 String.
     * @param encoding 인코딩 시 사용될 charset.
     * @return 인코딩된 String.
     * @throws UnsupportedEncodingException
     */
    public static final String encode(final String value, final String encoding) {
        try {
            return URLEncoder.encode(value, encoding != null ? encoding : "UTF-8");
        } catch (UnsupportedEncodingException e) {
            if (Logger.isWarnEnabled()) {
                Logger.w(URLCodec.class, e.toString());
            }
            return value;
        }
    }

    /**
     * URL 디코딩한다.
     * 
     * @param value 디코딩 될 String.
     * @return 디코딩된 String.
     * @throws UnsupportedEncodingException
     */
    public static final String decode(final String value) {
        return decode(value, "UTF-8");
    }

    /**
     * URL 디코딩한다.
     * 
     * @param value 디코딩 될 String.
     * @param encoding 디코딩 시 사용될 charset.
     * @return 디코딩된 String.
     * @throws UnsupportedEncodingException
     */
    public static final String decode(final String value, final String encoding) {
        try {
            return URLDecoder.decode(value, encoding != null ? encoding : "UTF-8");
        } catch (UnsupportedEncodingException e) {
            if (Logger.isWarnEnabled()) {
                Logger.w(URLCodec.class, e.toString());
            }
            return value;
        }
    }
}
