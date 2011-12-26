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

package com.keun.android.common.lang;

import com.keun.android.common.utils.Logger;

import java.lang.Thread.UncaughtExceptionHandler;

/**
 * 예상치 못한(알수 없는) 에러가 발생하는 경우를 처리한다.
 * 
 * @author Keun-yang Son
 * @since 2011. 12. 8.
 * @version 1.0
 * @see UncaughtExceptionHandler
 */
public class UncaughtExceptionHandlerImpl implements UncaughtExceptionHandler {
    // private Context mContext;
    // public UncaughtExceptionHandlerImpl(Context context) {
    // mContext = context;
    // }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        // // 에러 로그를 잡아서 기록 한 후 Custom 에러 리포팅 Alert을 보여준다.
        // StringWriter stackTrace = new StringWriter();
        // throwable.printStackTrace(new PrintWriter(stackTrace));
        // if (Logger.isErrorEnabled()) {
        // Logger.e(thread.getClass(), throwable);
        // }
        //
        // // 로그 레벨이 Warning 이상인 경우에만 버그 리포팅을 할 수 있도록 한다.
        // // if (Logger.isWarnEnabled()) {
        // Intent intent = new Intent(mContext, ErrorReportActivity.class);
        // intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // intent.putExtra(ErrorReportActivity.EXTRA_STACK_TRACE,
        // stackTrace.toString());
        // mContext.startActivity(intent);
        //
        // android.os.Process.killProcess(android.os.Process.myPid());
        // System.exit(10);

        // 에러 로그를 기존에 사용하던 방식으로 Native에 넘겨준다.
        if (Logger.isErrorEnabled()) {
            Logger.e(getClass(), "알수 없는 에러 발생.");
        }
        UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
        handler.uncaughtException(thread, throwable);

    }
}
