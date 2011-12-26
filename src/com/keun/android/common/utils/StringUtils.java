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
 * String에 대한 정보를 처리한다.
 * 
 * @author Keun-yang Son
 * @since 2011. 12. 8.
 * @version 1.0
 */
public class StringUtils {

    private static final char HANGUL_BASE_UNIT = 588;// 각자음 마다 가지는 글자수
    private static final char HANGUL_BEGIN_UNICODE = 44032; // 가 ~
    private static final char HANGUL_END_UNICODE = 55203; // ~ 힣
    private static final char[] HANGUL_CHOSUNG = {
            'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ',
            'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    };

    /**
     * 한글에서 초성을 추출한다.
     * 
     * @param value 초성을 추출 할 한글.
     * @return 초성.
     */
    public static final String chosung(final String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == ' ') { // 공백문자는 무시한다.
                continue;
            }
            if (isHangul(ch)) { // 한글인지 확인한다.
                sb.append(chosung(ch));
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * 한글에서 초성을 추출한다.
     * 
     * @param value 초성을 추출 할 한글.
     * @return 초성.
     */
    private static final char chosung(final char value) {
        int hanBegin = (value - HANGUL_BEGIN_UNICODE);
        int index = hanBegin / HANGUL_BASE_UNIT;
        return HANGUL_CHOSUNG[index];
    }

    /**
     * 한글 여부를 확인한다.
     */
    private static final boolean isHangul(final char value) {
        return HANGUL_BEGIN_UNICODE <= value && value <= HANGUL_END_UNICODE;
    }

}
