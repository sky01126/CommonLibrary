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

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Android 어플리케이션의 로그를 기록한다.<br />
 * 단말기의 기본 로그 레벨을 확인한 후 변경한다.<br />
 * <br />
 * <br />
 * 로그 레벨 확인
 * 
 * <pre>
 * Logger.isLoggable(TAG, Logger.VERBOSE) == false
 * Logger.isLoggable(TAG, Logger.DEBUG) == false
 * Logger.isLoggable(TAG, Logger.INFO) == true
 * Logger.isLoggable(TAG, Logger.WARN) == true
 * Logger.isLoggable(TAG, Logger.ERROR) == true
 * </pre>
 * 
 * 로그 레벨을 Shell에서 변경 방법 (TAG : KTH)
 * 
 * <pre>
 * shell> adb shell setprop log.tag.KTH VERBOSE
 * shell> adb shell getprop ## 설정값 확인.
 * </pre>
 * 
 * 버그 리포팅 방법.
 * 
 * <pre>
 * shell> adb shell bugreport > test.txt
 * </pre>
 * 
 * @author Keun-yang Son
 * @since 2011. 12. 8.
 * @version 1.0
 */
public class Logger {
    public static final String TAG = "KTH";

    /**
     * VERBOSE 로그를 기록한다.
     * 
     * @param clazz 로그 기록을 요청한 Class명.
     * @param msg 메시지.
     */
    public static final void v(final Class<?> clazz, final String msg) {
        if (Logger.isVerboseEnabled()) {
            Log.println(Log.VERBOSE, TAG, Logger.getClassLineNumber(clazz) + " - " + msg);
        }

        // 로그를 파일에 기록할 수 있는지 확인한다.
        if (Logger.isVerboseEnabled()) {
            write(Log.VERBOSE, Logger.getClassLineNumber(clazz), msg);
        }
    }

    /**
     * VERBOSE 로그를 기록한다.
     * 
     * @param clazz 로그 기록을 요청한 Class명.
     * @param tr Throwable.
     */
    public static final void v(final Class<?> clazz, final Throwable tr) {
        if (Logger.isVerboseEnabled()) {
            Log.println(Log.VERBOSE, TAG,
                    Logger.getClassLineNumber(clazz) + " - " + Log.getStackTraceString(tr));
        }

        // 로그를 파일에 기록할 수 있는지 확인한다.
        if (Logger.isVerboseEnabled()) {
            write(Log.VERBOSE, Logger.getClassLineNumber(clazz), tr);
        }
    }

    /**
     * VERBOSE 로그를 기록한다.
     * 
     * @param clazz 로그 기록을 요청한 Class명.
     * @param msg 메시지.
     * @param tr Throwable.
     */
    public static final void v(final Class<?> clazz, final String msg, final Throwable tr) {
        if (Logger.isVerboseEnabled()) {
            Log.println(Log.VERBOSE, TAG, Logger.getClassLineNumber(clazz) + " - " + msg + '\n'
                    + Log.getStackTraceString(tr));
        }

        // 로그를 파일에 기록할 수 있는지 확인한다.
        if (Logger.isVerboseEnabled()) {
            write(Log.VERBOSE, Logger.getClassLineNumber(clazz), msg, tr);
        }
    }

    /**
     * DEBUG 로그를 기록한다.
     * 
     * @param clazz 로그 기록을 요청한 Class명.
     * @param msg 메시지.
     */
    public static final void d(final Class<?> clazz, final String msg) {
        if (Logger.isDebugEnabled()) {
            Log.println(Log.DEBUG, TAG, Logger.getClassLineNumber(clazz) + " - " + msg);
        }

        // 로그를 파일에 기록할 수 있는지 확인한다.
        if (Logger.isDebugEnabled()) {
            write(Log.DEBUG, Logger.getClassLineNumber(clazz), msg);
        }
    }

    /**
     * DEBUG 로그를 기록한다.
     * 
     * @param clazz 로그 기록을 요청한 Class명.
     * @param tr Throwable.
     */
    public static final void d(final Class<?> clazz, final Throwable tr) {
        if (Logger.isDebugEnabled()) {
            Log.println(Log.DEBUG, TAG,
                    Logger.getClassLineNumber(clazz) + " - " + Log.getStackTraceString(tr));
        }

        // 로그를 파일에 기록할 수 있는지 확인한다.
        if (Logger.isDebugEnabled()) {
            write(Log.DEBUG, Logger.getClassLineNumber(clazz), tr);
        }
    }

    /**
     * DEBUG 로그를 기록한다.
     * 
     * @param clazz 로그 기록을 요청한 Class명.
     * @param msg 메시지.
     * @param tr Throwable.
     */
    public static final void d(final Class<?> clazz, final String msg, final Throwable tr) {
        if (Logger.isDebugEnabled()) {
            Log.println(Log.DEBUG, TAG, Logger.getClassLineNumber(clazz) + " - " + msg + '\n'
                    + Log.getStackTraceString(tr));
        }

        // 로그를 파일에 기록할 수 있는지 확인한다.
        if (Logger.isDebugEnabled()) {
            write(Log.DEBUG, Logger.getClassLineNumber(clazz), msg, tr);
        }
    }

    /**
     * INFO 로그를 기록한다.
     * 
     * @param clazz 로그 기록을 요청한 Class명.
     * @param msg 메시지.
     */
    public static final void i(final Class<?> clazz, final String msg) {
        if (Logger.isInfoEnabled()) {
            Log.println(Log.INFO, TAG, Logger.getClassLineNumber(clazz) + " - " + msg);
        }

        // 로그를 파일에 기록할 수 있는지 확인한다.
        if (Logger.isDebugEnabled()) {
            write(Log.INFO, Logger.getClassLineNumber(clazz), msg);
        }
    }

    /**
     * INFO 로그를 기록한다.
     * 
     * @param clazz 로그 기록을 요청한 Class명.
     * @param tr Throwable.
     */
    public static final void i(final Class<?> clazz, final Throwable tr) {
        if (Logger.isInfoEnabled()) {
            Log.println(Log.INFO, TAG,
                    Logger.getClassLineNumber(clazz) + " - " + Log.getStackTraceString(tr));
        }

        // 로그를 파일에 기록할 수 있는지 확인한다.
        if (Logger.isDebugEnabled()) {
            write(Log.INFO, Logger.getClassLineNumber(clazz), tr);
        }
    }

    /**
     * INFO 로그를 기록한다.
     * 
     * @param clazz 로그 기록을 요청한 Class명.
     * @param msg 메시지.
     * @param tr Throwable.
     */
    public static final void i(final Class<?> clazz, final String msg, final Throwable tr) {
        if (Logger.isInfoEnabled()) {
            Log.println(Log.INFO, TAG, Logger.getClassLineNumber(clazz) + " - " + msg + '\n'
                    + Log.getStackTraceString(tr));
        }

        // 로그를 파일에 기록할 수 있는지 확인한다.
        if (Logger.isDebugEnabled()) {
            write(Log.INFO, Logger.getClassLineNumber(clazz), msg, tr);
        }
    }

    /**
     * WARN 로그를 기록한다.
     * 
     * @param clazz 로그 기록을 요청한 Class명.
     * @param msg 메시지.
     */
    public static final void w(final Class<?> clazz, final String msg) {
        if (Logger.isWarnEnabled()) {
            Log.println(Log.WARN, TAG, Logger.getClassLineNumber(clazz) + " - " + msg);
        }

        // 로그를 파일에 기록할 수 있는지 확인한다.
        if (Logger.isDebugEnabled()) {
            write(Log.WARN, Logger.getClassLineNumber(clazz), msg);
        }
    }

    /**
     * WARN 로그를 기록한다.
     * 
     * @param clazz 로그 기록을 요청한 Class명.
     * @param tr Throwable.
     */
    public static final void w(final Class<?> clazz, final Throwable tr) {
        if (Logger.isWarnEnabled()) {
            Log.println(Log.WARN, TAG,
                    Logger.getClassLineNumber(clazz) + " - " + Log.getStackTraceString(tr));
        }

        // 로그를 파일에 기록할 수 있는지 확인한다.
        if (Logger.isDebugEnabled()) {
            write(Log.WARN, Logger.getClassLineNumber(clazz), tr);
        }
    }

    /**
     * WARN 로그를 기록한다.
     * 
     * @param clazz 로그 기록을 요청한 Class명.
     * @param msg 메시지.
     * @param tr Throwable.
     */
    public static final void w(final Class<?> clazz, final String msg, final Throwable tr) {
        if (Logger.isWarnEnabled()) {
            Log.println(Log.WARN, TAG, Logger.getClassLineNumber(clazz) + " - " + msg + '\n'
                    + Log.getStackTraceString(tr));
        }

        // 로그를 파일에 기록할 수 있는지 확인한다.
        if (Logger.isDebugEnabled()) {
            write(Log.WARN, Logger.getClassLineNumber(clazz), msg, tr);
        }
    }

    /**
     * ERROR 로그를 기록한다.
     * 
     * @param clazz 로그 기록을 요청한 Class명.
     * @param msg 메시지.
     */
    public static final void e(final Class<?> clazz, final String msg) {
        if (Logger.isErrorEnabled()) {
            Log.println(Log.ERROR, TAG, Logger.getClassLineNumber(clazz) + " - " + msg);
        }

        // 로그를 파일에 기록할 수 있는지 확인한다.
        if (Logger.isDebugEnabled()) {
            write(Log.ERROR, Logger.getClassLineNumber(clazz), msg);
        }
    }

    /**
     * ERROR 로그를 기록한다.
     * 
     * @param clazz 로그 기록을 요청한 Class명.
     * @param tr Throwable.
     */
    public static final void e(final Class<?> clazz, final Throwable tr) {
        if (Logger.isErrorEnabled()) {
            Log.println(Log.ERROR, TAG,
                    Logger.getClassLineNumber(clazz) + " - " + Log.getStackTraceString(tr));
        }

        // 로그를 파일에 기록할 수 있는지 확인한다.
        if (Logger.isDebugEnabled()) {
            write(Log.ERROR, Logger.getClassLineNumber(clazz), tr);
        }
    }

    /**
     * ERROR 로그를 기록한다.
     * 
     * @param clazz 로그 기록을 요청한 Class명.
     * @param msg 메시지.
     * @param tr Throwable.
     */
    public static final void e(final Class<?> clazz, final String msg, final Throwable tr) {
        if (Logger.isErrorEnabled()) {
            Log.println(Log.ERROR, TAG, Logger.getClassLineNumber(clazz) + " - " + msg + '\n'
                    + Log.getStackTraceString(tr));
        }

        // 로그를 파일에 기록할 수 있는지 확인한다.
        if (Logger.isDebugEnabled()) {
            write(Log.ERROR, Logger.getClassLineNumber(clazz), msg, tr);
        }
    }

    /**
     * Stack Trace 로그를 기록한다.
     * 
     * @param clazz 로그 기록을 요청한 Class명.
     */
    public static final void stackTrace(final Class<?> clazz) {
        stackTrace(clazz, Log.INFO, null);
    }

    /**
     * Stack Trace 로그를 기록한다.
     * 
     * @param clazz 로그 기록을 요청한 Class명.
     * @param msg 메시지.
     */
    public static final void stackTrace(final Class<?> clazz, final String msg) {
        stackTrace(clazz, Log.INFO, msg);
    }

    /**
     * Stack Trace 로그를 기록한다.
     * 
     * @param clazz 로그 기록을 요청한 Class명.
     * @param level 로그 레벨.
     * @param msg 메시지.
     */
    public static final void stackTrace(final Class<?> clazz, final int level, final String msg) {
        if (Log.isLoggable(TAG, level)) {
            Thread th = Thread.currentThread();
            StackTraceElement[] stack = th.getStackTrace();

            StringBuilder sb = new StringBuilder();
            if (msg != null && !"".equals(msg)) {
                sb.append(msg).append("\n");
            }
            for (StackTraceElement element : stack) {
                if (!"getStackTrace".equals(element.getMethodName())
                        && !"stackTrace".equals(element.getMethodName())) {
                    sb.append("\tat ").append(element.toString()).append("\n");
                }
            }
            Log.println(level, TAG,
                    Logger.getClassLineNumber(clazz) + " - " + sb.toString());
        }
    }

    /**
     * VERBOSE 로그 기록 확인 여부 판단.
     */
    public static final boolean isVerboseEnabled() {
        return Log.isLoggable(TAG, Log.VERBOSE);
    }

    /**
     * DEBUG 로그 기록 확인 여부 판단.
     */
    public static final boolean isDebugEnabled() {
        return Log.isLoggable(TAG, Log.DEBUG);
    }

    /**
     * INFO 로그 기록 확인 여부 판단.
     */
    public static final boolean isInfoEnabled() {
        return Log.isLoggable(TAG, Log.INFO);
    }

    /**
     * WARN 로그 기록 확인 여부 판단.
     */
    public static final boolean isWarnEnabled() {
        return Log.isLoggable(TAG, Log.WARN);
    }

    /**
     * ERROR 로그 기록 확인 여부 판단.
     */
    public static final boolean isErrorEnabled() {
        return Log.isLoggable(TAG, Log.ERROR);
    }

    /** 클래스명과 메소드명, 라인번호를 기록할 수 있도록한다. */
    private static final String getClassLineNumber(final Class<?> clazz) {
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        if (elements != null) {
            for (StackTraceElement e : elements) {
                if ((clazz.getName()).equals(e.getClassName())
                        || (clazz.getSimpleName()).equals(e.getClassName())) {
                    return e.getClassName()
                            + "(" + e.getMethodName() + ":" + e.getLineNumber() + ")";
                }
            }
        }
        return "";
    }

    /** 특정 로그를 외부 메모리에 저장한다. */
    private static final void write(final int level, final String clazz, final String msg) {
        new Thread(new Write(level, clazz, msg)).start();
    }

    private static final void write(final int level, final String clazz, final Throwable tr) {
        new Thread(new Write(level, clazz, tr)).start();
    }

    private static final void write(final int level, final String clazz, final String msg,
            final Throwable tr) {
        new Thread(new Write(level, clazz, tr)).start();
    }

    /**
     * Date를 원하는 String Format으로 변환한다.
     */
    public static final String toDateString(final String frm) {
        return new SimpleDateFormat(frm).format(new Date());
    }

    /**
     * 로그를 파일에 기록한다.
     * 
     * @author <a href="mailto:sky01126@paran.com"><b>손근양</b></a>
     * @since 2011. 9. 15.
     * @version 1.0.0
     * @see Runnable
     * @linkplain JDK 6.0
     */
    static class Write implements Runnable {
        private int mLevel;
        private String mClazz;
        private String mMessage;
        private Throwable mThrowable;

        public Write(final int level, final String clazz, final String msg) {
            this.mLevel = level;
            this.mClazz = clazz;
            this.mMessage = msg;
        }

        public Write(final int level, final String clazz, final Throwable tr) {
            this.mLevel = level;
            this.mClazz = clazz;
            this.mThrowable = tr;
        }

        public Write(final int level, final String clazz, final String msg, final Throwable tr) {
            this.mLevel = level;
            this.mClazz = clazz;
            this.mMessage = msg;
            this.mThrowable = tr;
        }

        @Override
        public void run() {
            FileChannel out = null;
            FileOutputStream fos = null;
            try {
                // 외부 메모리가 있는지 확인한다.
                File filePath = Environment.getExternalStorageDirectory().getAbsoluteFile();
                long size = StorageUtils.availableExternalStorageSize();

                // 내부/외부 메모리에 10M 이상의 여유공간이 존재하는지 확인한다.
                if (size < (10 * 1024 * 1024)) {
                    return;
                }

                String path = filePath + "/logs";

                // 로그를 기록 할 디렉토리가 없으면 생성한다.
                if (!new File(path).exists()) {
                    new File(path).mkdirs();
                }

                String fileName = TAG + "_" + toDateString("yyyyMMddHH") + ".log";

                // 파일에 생성한다.
                fos = new FileOutputStream(path + "/" + fileName, true);
                out = fos.getChannel();

                // 로그를 생성한다.
                StringBuilder sb = new StringBuilder();
                sb.append(toDateString("yyyy-MM-dd HH:mm:ss mmmm"));
                switch (mLevel) {
                    case Log.DEBUG:
                        sb.append(" [DEBUG  ] ");
                        break;
                    case Log.INFO:
                        sb.append(" [INFO   ] ");
                        break;
                    case Log.WARN:
                        sb.append(" [WARN   ] ");
                        break;
                    case Log.ERROR:
                        sb.append(" [ERROR  ] ");
                        break;
                    case Log.VERBOSE:
                    default:
                        sb.append(" [VERBOSE] ");
                        break;
                }

                sb.append(mClazz).append(" - ");
                if (mMessage != null && !"".equals(mMessage)) {
                    sb.append(mMessage);
                    if (mThrowable != null) {
                        sb.append("\n");
                        sb.append(Log.getStackTraceString(mThrowable));
                    }
                } else {
                    if (mThrowable != null) {
                        sb.append(Log.getStackTraceString(mThrowable));
                    }
                }

                sb.append("\n\n");
                byte[] b = (sb.toString()).getBytes();
                ByteBuffer buf = ByteBuffer.allocate(b.length);

                // 바이트배열을 버퍼에 넣는다.
                buf.put(b);

                // 버퍼의 위치(position)는 0으로 Limit와 Capacity값과 같게 설정한다.
                buf.clear();
                out.write(buf);
            } catch (Exception e) {
                Log.d(TAG, e.toString());
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        Log.d(TAG, e.toString());
                    }
                }
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        Log.d(TAG, e.toString());
                    }
                }
            }
        }
    }
}
