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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * 시간을 String으로 변환하며, GMT Time Zone을 읽어온다.
 * 
 * @author Keun-yang Son
 * @since 2011. 12. 8.
 * @version 1.0
 */
public class DateUtils {

    /**
     * 현재시간을 synchronized를 선택해서 "yyyy-MM-dd HH:mm:ss" Format으로 변환해서 반환한다.
     * 
     * <pre>
     * DateUtil.toDateString(false);
     * </pre>
     * 
     * @return String.
     */
    public static final String toDateString() {
        return toDateString("yyyy-MM-dd HH:mm:ss", new Date());
    }

    /**
     * 시간을 원하는 String Format으로 변환해서 반환한다.
     * 
     * <pre>
     * DateUtil.toDateString(true, &quot;yyyy-MM-dd HH:mm:ss mmmm&quot;, str, new Date());
     * </pre>
     * 
     * @param fmt Format.
     * @param date Date
     * @return Date String.
     */
    public static final String toDateString(final String fmt, final Date date) {
        return new SimpleDateFormat(fmt).format(date);
    }

    public static final int gmtTimeZone() {
        try {
            TimeZone tz = TimeZone.getDefault();
            if (Logger.isVerboseEnabled()) {
                Logger.v(DateUtils.class,
                        tz.getDisplayName() + " : " + (tz.getRawOffset() / 60 / 60 / 1000));
            }
            return (tz.getRawOffset() / 60 / 60 / 1000);
        } catch (Exception e) {
            if (Logger.isWarnEnabled()) {
                Logger.w(DateUtils.class, e);
            }
        }
        return -1;
    }

}
