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

import android.content.Context;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Android Device의 정보를 가져온다.
 * 
 * @author Keun-yang Son
 * @since 2011. 12. 8.
 * @version 1.0
 */
public class DeviceUtils {
    public static final int INT_ERROR_CODE = -1;

    /**
     * Mobile Country Code를 추출한다.
     * 
     * @param context Android Context.
     * @return Mobile Country Code.
     */
    public static final int mcc(final Context context) {
        try {
            TelephonyManager telephonyManager =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String networkOperator = telephonyManager.getNetworkOperator();
            if (networkOperator != null && !"".equals(networkOperator)) {
                if (Logger.isVerboseEnabled()) {
                    Logger.v(DeviceUtils.class, "Network Operator: " + networkOperator);
                }
                // int mcc = NumberUtils.toInt(networkOperator.substring(0, 3));
                // int mnc = Integer.parseInt(networkOperator.substring(3));
                return NumberUtils.toInt(networkOperator.substring(0, 3));
            }
        } catch (Exception e) {
            if (Logger.isErrorEnabled()) {
                Logger.e(DeviceUtils.class, e);
            }
        }
        return INT_ERROR_CODE;
    }

    /**
     * 안드로이드 키보드를 올려준다.
     * 
     * @param ctx Context
     * @param view View
     */
    public static final void upKeyboard(final Context ctx, final View view) {
        InputMethodManager mgr =
                (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }

    /**
     * 안드로이드 키보드를 내려준다.
     * 
     * @param ctx Context
     * @param view View
     */
    public static final void downKeyboard(final Context ctx, final View view) {
        InputMethodManager mgr =
                (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(view, 0);
    }
}
