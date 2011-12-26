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
 * 프로그램의 실행 시간을 체크하는 Util.
 * 
 * @author Keun-yang Son
 * @since 2011. 12. 8.
 * @version 1.0
 */
public class StopWatchAverage {
    private long mStartTime; // 시작 시간.
    private long mElapsedTime = 0;
    private String mMessage;

    /**
     * 특별히 이름을 지정하지 않은 StopWatchAverage 객체를 생성하고, Timer를 시작한다.
     */
    public StopWatchAverage() {
        mStartTime = System.nanoTime();
    }

    /**
     * 메시지만을 지정하는 생성자
     * 
     * @param message 추가로 명시할 메시
     */
    public StopWatchAverage(String message) {
        mMessage = message;
        mStartTime = System.nanoTime();
    }

    /**
     * StopWatch를 멈추고 마지막에(혹은 현재까지) 수행된 시간을 리턴한다.
     * 
     * @return 마지막에 수행된 밀리초
     */
    public double getElapsedMS() {
        long runTime = System.nanoTime() - (mStartTime + mElapsedTime);
        mElapsedTime = mElapsedTime + runTime;
        return runTime / 1000000.0;
    }

    /**
     * StopWatch를 멈추고 마지막에(혹은 현재까지) 수행된 시간을 리턴한다.
     * 
     * @return 마지막에 수행된 나노초
     */
    public double getElapsedNano() {
        long runTime = System.nanoTime() - (mStartTime + mElapsedTime);
        mElapsedTime = mElapsedTime + runTime;
        return runTime;
    }

    /**
     * 현재까지 수집된 횟수, 전체 수행시간의 합, 평균 수행시간을 밀리초 단위로 리턴해준다.
     * 
     * @see java.lang.Object#toString()
     * @return 수행 횟수, 전체 수행시간 , 평균 수행시간
     */
    @Override
    public String toString() {
        long runTime = System.nanoTime() - mStartTime;
        return (mMessage != null && !"".equals(mMessage) ? "[" + mMessage + "] " : "")
                + "Run Time : " + runTime / 1000000.0 + " ms";
    }
}
