/*
 * Copyright (C) 2010 The Android Open Source Project
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

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author givenjazz
 */
public class RecycleUtils {

    /**
     * View에 붙어있는 View의 child를 리커시브로 null로 설정해준다.
     * 
     * <pre>
     * protected void onDestroy() {
     *     RecycleUtils.recursiveRecycle(getWindow().getDecorView());
     *     System.gc();
     *     super.onDestroy();
     * }
     * </pre>
     * 
     * @param view View
     */
    public static final void recursiveRecycle(View view) {
        if (view == null) {
            return;
        }
        view.setBackgroundDrawable(null);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                recursiveRecycle(group.getChildAt(i));
            }
            if (!(view instanceof AdapterView)) {
                group.removeAllViews();
            }

        }
        if (view instanceof ImageView) {
            ((ImageView) view).setImageDrawable(null);
        }
        view = null;
    }

    public static final void recursiveRecycle(List<WeakReference<View>> recycleList) {
        for (WeakReference<View> ref : recycleList) {
            recursiveRecycle(ref.get());
        }
    }
}
