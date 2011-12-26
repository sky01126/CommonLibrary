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

/**
 * String을 Integer 또는 Long 형으루 형변환한다.
 * 
 * @author Keun-yang Son
 * @since 2011. 12. 8.
 * @version 1.0
 */
public class NumberUtils {

    /**
     * String을 Integer형으로 변경한다.
     * 
     * @param str Integer형으로 변경할 String.
     * @return Integer
     */
    public static final int toInt(final String str) {
        return toInt(str, 0);
    }

    /**
     * String을 Integer형으로 변경한다.
     * 
     * @param str Integer형으로 변경할 String.
     * @param defaultValue Null 또는 에러 발생 시 리턴될 값.
     * @return Integer
     */
    public static final int toInt(final String str, final int defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Object를 Integer형으로 변경한다.
     * 
     * @param str Integer형으로 변경할 Object.
     * @return Integer
     */
    public static final int toInt(final Object obj) {
        return toInt(obj, 0);
    }

    /**
     * Object를 Integer형으로 변경한다.
     * 
     * @param str Integer형으로 변경할 Object.
     * @param defaultValue Null 또는 에러 발생 시 리턴될 값.
     * @return Integer
     */
    public static final int toInt(final Object obj, final int defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        try {
            return (Integer) obj;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * String을 Long형으로 변경한다.
     * 
     * @param str Long형으로 변경할 String.
     * @return Long
     */
    public static final long toLong(final String str) {
        return toLong(str, 0L);
    }

    /**
     * String을 Long형으로 변경한다.
     * 
     * @param str Long형으로 변경할 String.
     * @param defaultValue Null 또는 에러 발생 시 리턴될 값.
     * @return Long
     */
    public static final long toLong(final String str, final long defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(str);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Object를 Long형으로 변경한다.
     * 
     * @param str Long형으로 변경할 Object.
     * @return Long
     */
    public static final long toLong(final Object obj) {
        return toLong(obj, 0L);
    }

    /**
     * Object를 Long형으로 변경한다.
     * 
     * @param str Long형으로 변경할 Object.
     * @param defaultValue Null 또는 에러 발생 시 리턴될 값.
     * @return Long
     */
    public static final long toLong(final Object obj, final long defaultValue) {
        if (obj == null) {
            return defaultValue;
        }
        try {
            return (Long) obj;
        } catch (Exception nfe) {
            return defaultValue;
        }
    }

    /**
     * String을 Float형으로 변경한다.
     * 
     * @param str Float형으로 변경할 String.
     * @return Float
     */
    public static final float toFloat(final String str) {
        return toFloat(str, 0f);
    }

    /**
     * String을 Float형으로 변경한다.
     * 
     * @param str Float형으로 변경할 String.
     * @param defaultValue Null 또는 에러 발생 시 리턴될 값.
     * @return Float
     */
    public static final float toFloat(final String str, final float defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return Float.parseFloat(str);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Object를 Float형으로 변경한다.
     * 
     * @param str Float형으로 변경할 Object.
     * @return Float
     */
    public static final float toFloat(final Object str) {
        return toFloat(str, 0f);
    }

    /**
     * Object를 Float형으로 변경한다.
     * 
     * @param str Float형으로 변경할 Object.
     * @param defaultValue Null 또는 에러 발생 시 리턴될 값.
     * @return Float
     */
    public static final float toFloat(final Object str, final float defaultValue) {
        if (str == null) {
            return defaultValue;
        }
        try {
            return (Float) str;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
