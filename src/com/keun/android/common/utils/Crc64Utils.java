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
 * Crc64 hash
 * 
 * @author Keun-yang Son
 * @since 2011. 12. 7.
 * @version 1.0
 */
public class Crc64Utils {
    private static final long POLY64REV = 0x95AC9329AC4BC9B5L;
    private static final long INITIALCRC = 0xFFFFFFFFFFFFFFFFL;
    private static boolean sInit = false;
    private static long[] sCrcTable = new long[256];

    /**
     * A function that returns a human readable hex string of a Crx64
     * 
     * @param in input string
     * @return hex string of the 64-bit CRC value
     */
    public static final String crc64(final String in) {
        if (in == null) {
            return null;
        }
        long crc = crc64Long(in);

        /*
         * The output is done in two parts to avoid problems with
         * architecture-dependent word order
         */
        int low = ((int) crc) & 0xffffffff;
        int high = ((int) (crc >> 32)) & 0xffffffff;
        String outVal = Integer.toHexString(high) + Integer.toHexString(low);
        return outVal;
    }

    /**
     * A function thats returns a 64-bit crc for string
     * 
     * @param in input string
     * @return 64-bit crc value
     */
    public static final long crc64Long(final String in) {
        if (in == null || in.length() == 0) {
            return 0;
        }
        // http://bioinf.cs.ucl.ac.uk/downloads/crc64/crc64.c
        long crc = INITIALCRC, part;
        if (!sInit) {
            for (int i = 0; i < 256; i++) {
                part = i;
                for (int j = 0; j < 8; j++) {
                    int value = ((int) part & 1);
                    if (value != 0) {
                        part = (part >> 1) ^ POLY64REV;
                    } else {
                        part >>= 1;
                    }
                }
                sCrcTable[i] = part;
            }
            sInit = true;
        }
        int length = in.length();
        for (int k = 0; k < length; ++k) {
            char c = in.charAt(k);
            crc = sCrcTable[(((int) crc) ^ c) & 0xff] ^ (crc >> 8);
        }
        return crc;
    }
}
