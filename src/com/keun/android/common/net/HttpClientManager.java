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

package com.keun.android.common.net;

import android.content.Context;
import android.os.Build;
import android.webkit.URLUtil;

import com.keun.android.common.net.CountingMultipartEntity.ProgressListener;
import com.keun.android.common.utils.Logger;
import com.keun.android.common.utils.StopWatchAverage;
import com.keun.android.common.utils.URLCodec;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.impl.cookie.CookieSpecBase;
import org.apache.http.message.HeaderGroup;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 * Http Clien를 이용한 네트워크 사용 설정. <br />
 * Native AndroidHttpClient는 Android API Level 8 이상에서만 사용이 가능하여 Android API
 * Level에 따라서 분기한다.
 * 
 * @author Keun-yang Son
 * @since 2011. 12. 19.
 * @version 1.0
 * @see
 */
public class HttpClientManager {

    private static final String UTF_8 = "UTF-8";

    /* Native AndroidHttpClien는 Froyo 이상에서만 사용이 가능하다. */
    private static final int API_LEVEL_FROYO = 8;

    public static enum Type {
        GET, PUT, POST, DELETE, UPLOAD
    }

    private final HttpClient mHttpClient;

    public HttpClient getHttpClient() {
        return mHttpClient;
    }

    /** Network Response 시 Gzip 사용 여부 체크 */
    private boolean mIsAcceptGzip;

    /**
     * HttpClientManager를 초기화한다.
     */
    public HttpClientManager() {
        if (Build.VERSION.SDK_INT < API_LEVEL_FROYO) {
            mHttpClient = HttpClientManager.connectLocal(getClass().getSimpleName(), null);
        } else {
            mHttpClient = HttpClientManager.connectNative(getClass().getSimpleName(), null);
        }
    }

    /**
     * HttpClientManager를 초기화한다.
     * 
     * @param userAgent Http request 시 User-Agent값을 정의한다.
     */
    public HttpClientManager(String userAgent) {
        if (Build.VERSION.SDK_INT < API_LEVEL_FROYO) {
            mHttpClient = HttpClientManager.connectLocal(userAgent, null);
        } else {
            mHttpClient = HttpClientManager.connectNative(userAgent, null);
        }
    }

    /**
     * HttpClientManager를 초기화한다.
     * 
     * @param context SSL sessions을 caching한다. (Null인 경우에는 caching하지 않음)
     * @param userAgent Http request 시 User-Agent값을 정의한다.
     */
    public HttpClientManager(Context context, String userAgent) {
        if (Build.VERSION.SDK_INT < API_LEVEL_FROYO) {
            mHttpClient = HttpClientManager.connectLocal(userAgent, context);
        } else {
            mHttpClient = HttpClientManager.connectNative(userAgent, context);
        }
    }

    /**
     * HttpClientManager를 초기화한다.
     * 
     * @param context SSL sessions을 caching한다. (Null인 경우에는 caching하지 않음)
     * @param userAgent Http request 시 User-Agent값을 정의한다.
     * @param timeout Connection/Socket Timeout 시간을 설정한다.
     */
    public HttpClientManager(Context context, String userAgent, int timeout) {
        if (Build.VERSION.SDK_INT < API_LEVEL_FROYO) {
            mHttpClient = HttpClientManager.connectLocal(userAgent, context);
        } else {
            mHttpClient = HttpClientManager.connectNative(userAgent, context);
        }

        // Connection Timeout과 Socket Timeout을 설정한다.
        HttpParams params = mHttpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, timeout);
        HttpConnectionParams.setSoTimeout(params, timeout);
    }

    /**
     * HttpClientManager를 초기화한다.
     * 
     * @param context SSL sessions을 caching한다. (Null인 경우에는 caching하지 않음)
     * @param userAgent Http request 시 User-Agent값을 정의한다.
     * @param conTimeout Connection Timeout 시간을 설정한다.
     * @param soTimeout Socket Timeout 시간을 설정한다.
     */
    public HttpClientManager(Context context, String userAgent, int conTimeout, int soTimeout) {
        if (Build.VERSION.SDK_INT < API_LEVEL_FROYO) {
            mHttpClient = HttpClientManager.connectLocal(userAgent, context);
        } else {
            mHttpClient = HttpClientManager.connectNative(userAgent, context);
        }

        // Connection Timeout과 Socket Timeout을 설정한다.
        HttpParams params = mHttpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, conTimeout);
        HttpConnectionParams.setSoTimeout(params, soTimeout);
    }

    /**
     * HttpClientManager를 초기화한다.
     * 
     * @param context SSL sessions을 caching한다. (Null인 경우에는 caching하지 않음)
     * @param userAgent Http request 시 User-Agent값을 정의한다.
     * @param conTimeout Connection Timeout 시간을 설정한다.
     * @param soTimeout Socket Timeout 시간을 설정한다.
     * @param bufferSize Buffer Size를 설정한다.
     */
    protected HttpClientManager(Context context, String userAgent, int conTimeout, int soTimeout,
            int bufferSize) {
        if (Build.VERSION.SDK_INT < API_LEVEL_FROYO) {
            mHttpClient = HttpClientManager.connectLocal(userAgent, context);
        } else {
            mHttpClient = HttpClientManager.connectNative(userAgent, context);
        }

        // Connection Timeout과 Socket Timeout을 설정한다.
        HttpParams params = mHttpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(params, conTimeout);
        HttpConnectionParams.setSoTimeout(params, soTimeout);
        HttpConnectionParams.setSocketBufferSize(params, bufferSize);
    }

    /**
     * Froyo 이전 버전은 내부에 있는 HttpClient를 사용한다.
     * 
     * @return HttpClient
     */
    public static HttpClient connectLocal(String userAgent, Context context) {
        if (Logger.isDebugEnabled()) {
            Logger.d(HttpClientManager.class, Build.VERSION.SDK + "는(은) 자체적으로 만든 HttpClient를 사용.");
        }
        if (context == null) {
            return com.keun.android.common.net.http.AndroidHttpClient
                    .newInstance(userAgent);
        } else {
            return com.keun.android.common.net.http.AndroidHttpClient
                    .newInstance(userAgent, context);
        }
    }

    /**
     * Android Native의 HttpClient를 사용한다.
     * 
     * @return HttpClient
     */
    public static HttpClient connectNative(String userAgent, Context context) {
        if (Logger.isDebugEnabled()) {
            Logger.d(HttpClientManager.class,
                    Build.VERSION.SDK + "는(은) Android Native에서 제공하는 HttpClient를 사용.");
        }
        if (context == null) {
            return android.net.http.AndroidHttpClient.newInstance(userAgent);
        } else {
            return android.net.http.AndroidHttpClient.newInstance(userAgent, context);
        }
    }

    /**
     * HttpClient를 Close한다.
     */
    public void close() {
        if (Logger.isDebugEnabled()) {
            Logger.d(getClass(), "close..");
        }
        if (mHttpClient instanceof com.keun.android.common.net.http.AndroidHttpClient) {
            ((com.keun.android.common.net.http.AndroidHttpClient) mHttpClient).close();
        } else {
            ((android.net.http.AndroidHttpClient) mHttpClient).close();
        }
    }

    /**
     * Network Response 시 Gzip 사용 여부를 설정한다.
     * 
     * @param isAcceptGzip Gzip 사용(true) / 미사용(false)
     */
    public void setAcceptGzip(boolean isAcceptGzip) {
        this.mIsAcceptGzip = isAcceptGzip;
    }

    /* ====== Http GET ====== */

    /**
     * Http Get으로 서버에 요청한다.
     * 
     * <pre>
     * manager.sendGet(url);
     * </pre>
     * 
     * @param url Http URL.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendGet(String url) throws IOException {
        return sendGet(url, UTF_8, null, null, null);
    }

    /**
     * Http Get으로 서버에 요청한다.
     * 
     * <pre>
     * manager.sendGet(url, encoding);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendGet(String url, String encoding) throws IOException {
        return sendGet(url, encoding, null, null, null);
    }

    /**
     * Http Get으로 서버에 요청한다.
     * 
     * <pre>
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendGet(url, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendGet(String url, List<NameValuePair> params) throws IOException {
        return sendGet(url, UTF_8, null, null, params);
    }

    /**
     * Http Get으로 서버에 요청한다.
     * 
     * <pre>
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendGet(url, encoding, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendGet(String url, String encoding, List<NameValuePair> params)
            throws IOException {
        return sendGet(url, encoding, null, null, params);
    }

    /**
     * Http Get으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     *  manager.sendGet(url, headers);
     * </pre>
     * 
     * @param url Http URL.
     * @param headers Header 정보.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendGet(String url, HeaderGroup headers) throws IOException {
        return sendGet(url, UTF_8, headers, null, null);
    }

    /**
     * Http Get으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendGet(url, headers, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param headers Header 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendGet(String url, HeaderGroup headers, List<NameValuePair> params)
            throws IOException {
        return sendGet(url, UTF_8, headers, null, params);
    }

    /**
     * Http Get으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendGet(url, encoding, headers, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param headers Header 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendGet(String url, String encoding, HeaderGroup headers,
            List<NameValuePair> params) throws IOException {
        return sendGet(url, encoding, headers, null, params);
    }

    /**
     * Http Get으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     *  manager.sendGet(url, encoding, headers);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param headers Header 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendGet(String url, String encoding, HeaderGroup headers)
            throws IOException {
        return sendGet(url, encoding, headers, null, null);
    }

    /**
     * Http Get으로 서버에 요청한다.
     * 
     * <pre>
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     *  manager.sendGet(url, cookies);
     * </pre>
     * 
     * @param url Http URL.
     * @param headers Header 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendGet(String url, CookieStore cookies) throws IOException {
        return sendGet(url, UTF_8, null, cookies, null);
    }

    /**
     * Http Get으로 서버에 요청한다.
     * 
     * <pre>
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendGet(url, cookies, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param headers Header 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendGet(String url, CookieStore cookies, List<NameValuePair> params)
            throws IOException {
        return sendGet(url, UTF_8, null, cookies, params);
    }

    /**
     * Http Get으로 서버에 요청한다.
     * 
     * <pre>
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     *  manager.sendGet(url, encoding, cookies);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param headers Header 정보.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendGet(String url, String encoding, CookieStore cookies)
            throws IOException {
        return sendGet(url, encoding, null, cookies, null);
    }

    /**
     * Http Get으로 서버에 요청한다.
     * 
     * <pre>
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendGet(url, encoding, cookies, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param headers Header 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendGet(String url, String encoding, CookieStore cookies,
            List<NameValuePair> params) throws IOException {
        return sendGet(url, encoding, null, cookies, params);
    }

    /**
     * Http Get으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     *  manager.sendGet(url, headers, cookies);
     * </pre>
     * 
     * @param url Http URL.
     * @param headers Header 정보.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendGet(String url, HeaderGroup headers, CookieStore cookies)
            throws IOException {
        return sendGet(url, UTF_8, headers, cookies, null);
    }

    /**
     * Http Get으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendGet(url, headers, cookies, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param headers Header 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendGet(String url, HeaderGroup headers, CookieStore cookies,
            List<NameValuePair> params) throws IOException {
        return sendGet(url, UTF_8, headers, cookies, params);
    }

    /**
     * Http Get으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     *  manager.sendGet(url, encoding, headers, cookies);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param headers Header 정보.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendGet(String url, String encoding, HeaderGroup headers,
            CookieStore cookies)
            throws IOException {
        return sendGet(url, encoding, headers, cookies, null);
    }

    /**
     * Http Get으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendGet(url, encoding, headers, cookies, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param headers Header 정보.
     * @param cookies Cookie 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendGet(String url, String encoding, HeaderGroup headers,
            CookieStore cookies, List<NameValuePair> params) throws IOException {
        if (params != null && params.size() > 0) { // Parameter가 존재하는지 확인한다.
            url = url + (url.lastIndexOf("?") > 0 ? "&" : "?") + format(params, encoding); // HTTP.UTF_8
        }
        return sendMethod(Type.GET, headers, cookies, params, new HttpGet(urlFilter(url)));
    }

    /* ====== Http PUT ====== */

    /**
     * Http Put으로 서버에 요청한다.
     * 
     * <pre>
     * manager.sendPut(url);
     * </pre>
     * 
     * @param url Http URL.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPut(String url) throws IOException {
        return sendPut(url, UTF_8, null, null, null, null, null);
    }

    /**
     * Http Put으로 서버에 요청한다.
     * 
     * <pre>
     * manager.sendPut(url, encoding);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPut(String url, String encoding) throws IOException {
        return sendPut(url, encoding, null, null, null, null, null);
    }

    /**
     * Http Put으로 서버에 요청한다.
     * 
     * <pre>
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendPut(url, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPut(String url, List<NameValuePair> params) throws IOException {
        return sendPut(url, UTF_8, null, null, null, null, params);
    }

    /**
     * Http Put으로 서버에 요청한다.
     * 
     * <pre>
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendPut(url, encoding, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPut(String url, String encoding, List<NameValuePair> params)
            throws IOException {
        return sendPut(url, encoding, null, null, null, null, params);
    }

    /**
     * Http Put으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendPut(url, headers, cookies, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param headers Header 정보.
     * @param cookies Cookie 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPut(String url, HeaderGroup headers, CookieStore cookies,
            List<NameValuePair> params) throws IOException {
        return sendPut(url, UTF_8, null, headers, cookies, null, params);
    }

    /**
     * Http PUt으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendPut(url, encoding, headers, cookies, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param headers Header 정보.
     * @param cookies Cookie 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPut(String url, String encoding, HeaderGroup headers,
            CookieStore cookies, List<NameValuePair> params) throws IOException {
        return sendPut(url, encoding, null, headers, cookies, null, params);
    }

    /**
     * Http Put으로 서버에 요청한다.
     * 
     * <pre>
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendPut(url, encoding, contextType, str.getBytes(), params);
     * </pre>
     * 
     * @param url Http URL.
     * @param contextType Context Type
     * @param body Http PUT Body에 전달 할 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPut(String url, String contextType, byte[] body,
            List<NameValuePair> params) throws IOException {
        return sendPut(url, UTF_8, contextType, null, null, body, params);
    }

    /**
     * Http Put으로 서버에 요청한다.
     * 
     * <pre>
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendPut(url, encoding, contextType, str.getBytes(), params);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param contextType Context Type
     * @param body Http PUT Body에 전달 할 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPut(String url, String encoding, String contextType, byte[] body,
            List<NameValuePair> params) throws IOException {
        return sendPut(url, encoding, contextType, null, null, body, params);
    }

    /**
     * Http Put으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendPut(url, contextType, headers, str.getBytes(), params);
     * </pre>
     * 
     * @param url Http URL.
     * @param contextType Context Type
     * @param headers Header 정보.
     * @param body Http PUT Body에 전달 할 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPut(String url, String contextType, HeaderGroup headers, byte[] body,
            List<NameValuePair> params) throws IOException {
        return sendPut(url, UTF_8, contextType, headers, null, body, params);
    }

    /**
     * Http Put으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendPut(url, encoding, contextType, headers, str.getBytes(), params);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param contextType Context Type
     * @param headers Header 정보.
     * @param body Http PUT Body에 전달 할 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPut(String url, String encoding, String contextType,
            HeaderGroup headers, byte[] body, List<NameValuePair> params) throws IOException {
        return sendPut(url, encoding, contextType, headers, null, body, params);
    }

    /**
     * Http Put으로 서버에 요청한다.
     * 
     * <pre>
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendPut(url, contextType, cookies, str.getBytes(), params);
     * </pre>
     * 
     * @param url Http URL.
     * @param contextType Context Type
     * @param cookies Cookie 정보.
     * @param body Http PUT Body에 전달 할 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPut(String url, String contextType, CookieStore cookies, byte[] body,
            List<NameValuePair> params) throws IOException {
        return sendPut(url, UTF_8, contextType, null, cookies, body, params);
    }

    /**
     * Http Put으로 서버에 요청한다.
     * 
     * <pre>
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendPut(url, encoding, contextType, cookies, str.getBytes(), params);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param contextType Context Type
     * @param cookies Cookie 정보.
     * @param body Http PUT Body에 전달 할 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPut(String url, String encoding, String contextType,
            CookieStore cookies, byte[] body, List<NameValuePair> params) throws IOException {
        return sendPut(url, encoding, contextType, null, cookies, body, params);
    }

    /**
     * Http Put으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendPut(url, contextType, headers, cookies, str.getBytes(), params);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param contextType Context Type
     * @param headers Header 정보.
     * @param cookies Cookie 정보.
     * @param body Http PUT Body에 전달 할 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPut(String url, String contextType, HeaderGroup headers,
            CookieStore cookies, byte[] body, List<NameValuePair> params) throws IOException {
        return sendPut(url, UTF_8, contextType, headers, cookies, body, params);
    }

    /**
     * Http Put으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendPut(url, encoding, contextType, headers, cookies, str.getBytes(), params);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param contextType Context Type
     * @param headers Header 정보.
     * @param cookies Cookie 정보.
     * @param body Http PUT Body에 전달 할 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPut(String url, String encoding, String contextType,
            HeaderGroup headers, CookieStore cookies, byte[] body, List<NameValuePair> params)
            throws IOException {
        if (params != null && params.size() > 0) { // Parameter가 존재하는지 확인한다.
            url = url + (url.lastIndexOf("?") > 0 ? "&" : "?") + format(params, encoding); // HTTP.UTF_8
        }

        // Body가 존재하면
        HttpPut put = new HttpPut(urlFilter(url));
        if (body != null) {
            if (Logger.isVerboseEnabled()) {
                Logger.v(getClass(), "HTTP PUT Method 통신 시 Body에 정보를 전달한다.");
            }
            contextType = (contextType == null ? "text/plain" : contextType);
            ByteArrayEntity entity = new ByteArrayEntity(body);
            entity.setContentType(contextType == null ? "text/plain" : contextType);

            put.setEntity(entity);
            put.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
            put.addHeader("Accept", contextType);
            put.addHeader("Content-Type", contextType);
        }
        return sendMethod(Type.PUT, headers, cookies, params, put);
    }

    /* ====== Http POST ====== */

    /**
     * Http Post으로 서버에 요청한다.
     * 
     * <pre>
     * manager.sendPost(url);
     * </pre>
     * 
     * @param url Http URL.
     * @param headers Header 정보.
     * @param cookies Cookie 정보.
     * @param params 파라미터 리스트.
     * @param files 업로드 하려는 파일 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPost(String url) throws IOException {
        return sendPost(url, null, null, null, null, null);
    }

    /**
     * Http Post으로 서버에 요청한다.
     * 
     * <pre>
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendPost(url, headers, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param headers Header 정보.
     * @param cookies Cookie 정보.
     * @param params 파라미터 리스트.
     * @param files 업로드 하려는 파일 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPost(String url, List<NameValuePair> params) throws IOException {
        return sendPost(url, null, null, params, null, null);
    }

    /**
     * Http Post으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     *  manager.sendPost(url, headers);
     * </pre>
     * 
     * @param url Http URL.
     * @param headers Header 정보.
     * @param cookies Cookie 정보.
     * @param params 파라미터 리스트.
     * @param files 업로드 하려는 파일 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPost(String url, HeaderGroup headers) throws IOException {
        return sendPost(url, headers, null, null, null, null);
    }

    /**
     * Http Post으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendPost(url, headers, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param headers Header 정보.
     * @param cookies Cookie 정보.
     * @param params 파라미터 리스트.
     * @param files 업로드 하려는 파일 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPost(String url, HeaderGroup headers, List<NameValuePair> params)
            throws IOException {
        return sendPost(url, headers, null, params, null, null);
    }

    /**
     * Http Post으로 서버에 요청한다.
     * 
     * <pre>
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     *  manager.sendPost(url, cookies, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param headers Header 정보.
     * @param cookies Cookie 정보.
     * @param params 파라미터 리스트.
     * @param files 업로드 하려는 파일 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPost(String url, CookieStore cookies) throws IOException {
        return sendPost(url, null, cookies, null, null, null);
    }

    /**
     * Http Post으로 서버에 요청한다.
     * 
     * <pre>
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendPost(url, cookies, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param headers Header 정보.
     * @param cookies Cookie 정보.
     * @param params 파라미터 리스트.
     * @param files 업로드 하려는 파일 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPost(String url, CookieStore cookies, List<NameValuePair> params)
            throws IOException {
        return sendPost(url, null, cookies, params, null, null);
    }

    /**
     * Http Post으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendPost(url, headers, cookies, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param headers Header 정보.
     * @param cookies Cookie 정보.
     * @param params 파라미터 리스트.
     * @param files 업로드 하려는 파일 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPost(String url, HeaderGroup headers, CookieStore cookies,
            List<NameValuePair> params) throws IOException {
        return sendPost(url, headers, cookies, params, null, null);
    }

    /**
     * Http Post으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     * -- 업로드 할 파일 리스트 생성.
     *  List&lt;NameValuePair&gt; files = new ArrayList&lt;NameValuePair&gt;();
     *  files.add(new BasicNameValuePair(name, value));
     *  files.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendPost(url, headers, cookies, params, files);
     * </pre>
     * 
     * @param url Http URL.
     * @param headers Header 정보.
     * @param cookies Cookie 정보.
     * @param params 파라미터 리스트.
     * @param files 업로드 하려는 파일 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPost(String url, HeaderGroup headers, CookieStore cookies,
            List<NameValuePair> params, List<NameValuePair> files) throws IOException {
        return sendPost(url, headers, cookies, params, files, null);
    }

    /**
     * Http Post으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     * -- 업로드 할 파일 리스트 생성.
     *  List&lt;NameValuePair&gt; files = new ArrayList&lt;NameValuePair&gt;();
     *  files.add(new BasicNameValuePair(name, value));
     *  files.add(new BasicNameValuePair(name, value));
     * 
     *  -- 업로드 진행률.
     *  ProgressListener listener = new CustomProgressListener();
     * 
     *  manager.sendPost(url, headers, cookies, params, files, listener);
     * </pre>
     * 
     * @param url Http URL.
     * @param headers Header 정보.
     * @param cookies Cookie 정보.
     * @param params 파라미터 리스트.
     * @param files 업로드 하려는 파일 리스트.
     * @param listener 업로드 진행률을 받을 Listener.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendPost(String url, HeaderGroup headers, CookieStore cookies,
            List<NameValuePair> params, List<NameValuePair> files, ProgressListener listener)
            throws IOException {
        HttpPost post = new HttpPost(urlFilter(url));
        if (files != null && files.size() > 0) {
            MultipartEntity entity = null;
            if (listener != null) {
                entity = new CountingMultipartEntity(listener);
            } else {
                entity = new MultipartEntity();
            }
            for (NameValuePair pair : files) {
                FileBody file = new FileBody(new File(pair.getValue()));
                entity.addPart(pair.getName(), file);
            }
            if (params != null && params.size() > 0) {
                for (NameValuePair pair : params) {
                    entity.addPart(pair.getName(), new StringBody(pair.getValue()));
                }
            }
            post.setEntity(entity);
        } else if (params != null && params.size() > 0) { // POST 통신.
            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, HTTP.UTF_8);
            post.setEntity(entity);
            post.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
        }
        post.setParams(mHttpClient.getParams());
        return sendMethod(Type.POST, headers, cookies, params, post);
    }

    /* ====== Http DELETE ====== */

    /**
     * Http Delete으로 서버에 요청한다.
     * 
     * <pre>
     * manager.sendDelete(url);
     * </pre>
     * 
     * @param url Http URL.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendDelete(String url) throws IOException {
        return sendDelete(url, UTF_8, null, null, null);
    }

    /**
     * Http Delete으로 서버에 요청한다.
     * 
     * <pre>
     * manager.sendDelete(url, encoding);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendDelete(String url, String encoding) throws IOException {
        return sendDelete(url, encoding, null, null, null);
    }

    /**
     * Http Delete으로 서버에 요청한다.
     * 
     * <pre>
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendDelete(url, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendDelete(String url, List<NameValuePair> params) throws IOException {
        return sendDelete(url, UTF_8, null, null, params);
    }

    /**
     * Http Delete으로 서버에 요청한다.
     * 
     * <pre>
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendDelete(url, encoding, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendDelete(String url, String encoding, List<NameValuePair> params)
            throws IOException {
        return sendDelete(url, encoding, null, null, params);
    }

    /**
     * Http Delete으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     *  manager.sendDelete(url, headers);
     * </pre>
     * 
     * @param url Http URL.
     * @param headers Header 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendDelete(String url, HeaderGroup headers) throws IOException {
        return sendDelete(url, UTF_8, headers, null, null);
    }

    /**
     * Http Delete으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendDelete(url, headers, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param headers Header 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendDelete(String url, HeaderGroup headers, List<NameValuePair> params)
            throws IOException {
        return sendDelete(url, UTF_8, headers, null, params);
    }

    /**
     * Http Delete으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     *  manager.sendDelete(url, encoding, headers);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param headers Header 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendDelete(String url, String encoding, HeaderGroup headers)
            throws IOException {
        return sendDelete(url, encoding, headers, null, null);
    }

    /**
     * Http Delete으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendDelete(url, encoding, headers, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param headers Header 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendDelete(String url, String encoding, HeaderGroup headers,
            List<NameValuePair> params) throws IOException {
        return sendDelete(url, encoding, headers, null, params);
    }

    /**
     * Http Delete으로 서버에 요청한다.
     * 
     * <pre>
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     *  manager.sendDelete(url, cookies);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param headers Header 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendDelete(String url, CookieStore cookies) throws IOException {
        return sendDelete(url, UTF_8, null, cookies, null);
    }

    /**
     * Http Delete으로 서버에 요청한다.
     * 
     * <pre>
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendDelete(url, cookies, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param headers Header 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendDelete(String url, CookieStore cookies, List<NameValuePair> params)
            throws IOException {
        return sendDelete(url, UTF_8, null, cookies, params);
    }

    /**
     * Http Delete으로 서버에 요청한다.
     * 
     * <pre>
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendDelete(url, encoding, cookies, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param headers Header 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendDelete(String url, String encoding, CookieStore cookies,
            List<NameValuePair> params) throws IOException {
        return sendDelete(url, encoding, null, cookies, params);
    }

    /**
     * Http Delete으로 서버에 요청한다.
     * 
     * <pre>
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     *  manager.sendDelete(url, encoding, cookies);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param headers Header 정보.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendDelete(String url, String encoding, CookieStore cookies)
            throws IOException {
        return sendDelete(url, encoding, null, cookies, null);
    }

    /**
     * Http Delete으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendDelete(url, headers, cookies, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param headers Header 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendDelete(String url, HeaderGroup headers, CookieStore cookies,
            List<NameValuePair> params) throws IOException {
        return sendDelete(url, UTF_8, headers, cookies, params);
    }

    /**
     * Http Delete으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     *  manager.sendDelete(url, headers, cookies);
     * </pre>
     * 
     * @param url Http URL.
     * @param headers Header 정보.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendDelete(String url, HeaderGroup headers, CookieStore cookies)
            throws IOException {
        return sendDelete(url, UTF_8, headers, cookies, null);
    }

    /**
     * Http Delete으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     *  manager.sendDelete(url, encoding, headers, cookies, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param headers Header 정보.
     * @param cookies Cookie 정보.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendDelete(String url, String encoding, HeaderGroup headers,
            CookieStore cookies) throws IOException {
        return sendDelete(url, encoding, headers, cookies, null);
    }

    /**
     * Http Delete으로 서버에 요청한다.
     * 
     * <pre>
     * -- Header 정보 생성.
     *  HeaderGroup headers = = new HeaderGroup();
     *  headers.addHeader(new BasicHeader(name, value));
     *  headers.addHeader(new BasicHeader(name, value));
     * 
     * -- Cookie 정보 생성.
     *  CookieStore cookies = new BasicCookieStore();
     *  BasicClientCookie cookie = new BasicClientCookie(name, value);
     *  if (domain != null &amp;&amp; !&quot;&quot;.equals(domain)) {
     *      cookie.setDomain(domain);
     *  }
     *  if (path != null &amp;&amp; !&quot;&quot;.equals(path)) {
     *      cookie.setPath(path);
     *  }
     *  if (expiry != null) {
     *      cookie.setExpiryDate(expiry);
     *  }
     *  cookies.addCookie(cookie);
     * 
     * -- 파리미터 리스트 생성.
     *  List&lt;NameValuePair&gt; params = new ArrayList&lt;NameValuePair&gt;();
     *  params.add(new BasicNameValuePair(name, value));
     *  params.add(new BasicNameValuePair(name, value));
     * 
     *  manager.sendDelete(url, encoding, headers, cookies, params);
     * </pre>
     * 
     * @param url Http URL.
     * @param encoding The encoding to use.
     * @param headers Header 정보.
     * @param cookies Cookie 정보.
     * @param params 파라미터 리스트.
     * @return HttpResponse.
     * @throws IOException
     */
    public HttpResponse sendDelete(String url, String encoding, HeaderGroup headers,
            CookieStore cookies, List<NameValuePair> params) throws IOException {
        if (params != null && params.size() > 0) { // Parameter가 존재하는지 확인한다.
            url = url + (url.lastIndexOf("?") > 0 ? "&" : "?") + format(params, encoding); // HTTP.UTF_8
        }
        return sendMethod(Type.DELETE, headers, cookies, params, new HttpDelete(urlFilter(url)));
    }

    /* ====== Http Method ====== */

    /**
     * @param type Get/Put/Post/Delete 타입.
     * @param url Http URL.
     * @param request Http Uri Request.
     * @return
     * @throws IOException
     */
    private HttpResponse sendMethod(Type type, HeaderGroup headers, CookieStore cookies,
            List<NameValuePair> params, HttpUriRequest request) throws IOException {
        StopWatchAverage swa = null;
        if (Logger.isDebugEnabled()) { // 실행시간 설정.
            swa = new StopWatchAverage();
        }
        HttpResponse response = null;
        try {
            // Cookie가 존재하면 Cookie를 Header에 설정한다.
            if (cookies != null) {
                List<Cookie> list = cookies.getCookies();
                CookieSpecBase cookieSpecBase = new BrowserCompatSpec();
                List<Header> cookieHeader = cookieSpecBase.formatCookies(list);
                if (headers == null) {
                    headers = new HeaderGroup();
                }
                for (Header h : cookieHeader) {
                    request.addHeader(h);
                }
            }

            if (mIsAcceptGzip) { // GZip을 이용해서 통신한다.
                modifyRequestToAcceptGzipResponse(request);
            }
            if (headers != null) { // Header가 존재하면 Header를 설정한다.
                for (Header header : headers.getAllHeaders()) {
                    request.addHeader(header);
                }
            }
            if (Logger.isDebugEnabled()) { // HTTP Request URI
                printConnectionLog(type, params, request);
            }
            return (response = mHttpClient.execute(request));
        } catch (ClientProtocolException e) {
            if (Logger.isErrorEnabled()) {
                Logger.e(getClass(), e);
            }
            request.abort();
            throw e;
        } catch (IOException e) {
            if (Logger.isErrorEnabled()) {
                Logger.e(getClass(), e);
            }
            request.abort();
            throw e;
        } finally {
            // Response 로그를 기록한다.
            if (Logger.isDebugEnabled()) {
                String url = printConnectionLog(type, params, request, response);
                if (swa != null) { // 실행시간 설정.
                    Logger.d(getClass(), "{" + url + "} " + swa.toString());
                }
            }
            swa = null;
        }
    }

    /* ====== URL Filter ====== */

    private static final Pattern ACCEPTED_URI_SCHEMA = Pattern.compile("(?i)"
            + "(" + "(?:http|https|file):\\/\\/"
            + "|(?:inline|data|about|content|javascript):" + ")" + "(.*)");

    /**
     * Attempts to determine whether user input is a URL or search terms.<br />
     * Anything with a space is passed to search.<br />
     * Converts to lowercase any mistakenly uppercased schema (i.e., "Http://"
     * converts to "http://"
     * 
     * @return Original or modified URL
     */
    private String urlFilter(String url) {
        String inUrl = url.trim();
        boolean hasSpace = inUrl.indexOf(' ') != -1;
        Matcher matcher = ACCEPTED_URI_SCHEMA.matcher(inUrl);
        if (matcher.matches()) {
            // force scheme to lowercase
            String scheme = matcher.group(1);
            String lcScheme = scheme.toLowerCase();
            if (!lcScheme.equals(scheme)) {
                inUrl = lcScheme + matcher.group(2);
            }
            if (hasSpace) {
                inUrl = inUrl.replace(" ", "%20");
            }
            return inUrl;
        }
        return URLUtil.guessUrl(inUrl);
    }

    /* ====== Param Encoding Format ====== */

    /**
     * Returns a String that is suitable for use as an
     * <code>application/x-www-form-urlencoded
     * list of parameters in an HTTP PUT or HTTP POST.
     * 
     * @param parameters The parameters to include.
     * @param encoding The encoding to use.
     */
    private String format(final List<? extends NameValuePair> parameters, String encoding) {
        final StringBuilder result = new StringBuilder();
        if (parameters == null || parameters.size() <= 0) {
            return "";
        }
        for (final NameValuePair parameter : parameters) {
            String name = parameter.getName();
            String value = parameter.getValue();
            name = URLCodec.encode(parameter.getName(), encoding);
            value = (value != null ? URLCodec.encode(value, encoding) : "");
            if (result.length() > 0) {
                result.append("&");
            }
            result.append(name).append("=").append(value);
        }
        return result.toString();
    }

    /* ====== Accept-Encoding ====== */

    /**
     * Modifies a request to indicate to the server that we would like a gzipped
     * response. (Uses the "Accept-Encoding" HTTP header.)
     * 
     * @param request the request to modify
     * @see #getUngzippedContent
     */
    public static void modifyRequestToAcceptGzipResponse(HttpRequest request) {
        request.addHeader("Accept-Encoding", "gzip");
    }

    /**
     * Gets the input stream from a response entity. If the entity is gzipped
     * then this will get a stream over the uncompressed data.
     * 
     * @param entity the entity whose content should be read
     * @return the input stream to read from
     * @throws IOException
     */
    public static InputStream getUngzippedContent(HttpEntity entity)
            throws IOException {
        InputStream responseStream = entity.getContent();
        if (responseStream == null) {
            return responseStream;
        }
        Header header = entity.getContentEncoding();
        if (header == null) {
            return responseStream;
        }
        String contentEncoding = header.getValue();
        if (contentEncoding == null) {
            return responseStream;
        }
        if (contentEncoding.contains("gzip")) {
            responseStream = new GZIPInputStream(responseStream);
        }
        return responseStream;
    }

    /* ====== Connect Log ====== */

    /** 서버에 Request 로그와 Response 로그를 기록한다. */
    private String printConnectionLog(Type type, List<NameValuePair> params,
            HttpUriRequest request) {
        return printConnectionLog(type, params, request, null);
    }

    private String printConnectionLog(Type type, List<NameValuePair> params,
            HttpUriRequest request, HttpResponse response) {
        String url = request.getURI().toString();
        switch (type) {
            case POST: {
                url = url + (params != null ? "?" + format(params, UTF_8) : "");
                if (response == null) {
                    Logger.d(getClass(), "HTTP Request URI (" + type.name() + ") : " + url);
                } else {
                    log(request, response, url, type.name());
                }
                break;
            }
            default: {
                if (response == null) {
                    Logger.d(getClass(), "HTTP Request URI (" + type.name() + ") : " + url);
                } else {
                    log(request, response, url, type.name());
                }
                break;
            }
        }
        return url;
    }

    private void log(final HttpRequest request, final HttpResponse response, final String url,
            final String method) {
        if (Logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n");
            if (response != null) {
                sb.append("========================= Request & Response =========================");
            } else {
                sb.append("========================= Request =========================");
            }
            sb.append("\n");
            sb.append("[ HTTP Method      ] ").append(method).append("\n");

            HttpParams params = mHttpClient.getParams();
            sb.append("[ Connect Timeout  ] ");
            sb.append(HttpConnectionParams.getConnectionTimeout(params)).append("\n");
            sb.append("[ Socket  Timeout  ] ");
            sb.append(HttpConnectionParams.getSoTimeout(params)).append("\n");
            sb.append("[ Buffer  Size     ] ");
            sb.append(HttpConnectionParams.getSocketBufferSize(params)).append("\n");

            // Proxy 서버를 확인한다.
            HttpHost proxy = ConnRouteParams.getDefaultProxy(params);
            if (proxy != null) {
                sb.append("[ Proxy Server     ] ").append(proxy).append("\n");
            }

            // User-Agent를 확인한다.
            String userAgent = HttpProtocolParams.getUserAgent(params);
            if (userAgent != null) {
                sb.append("[ User-Agent       ] ").append(userAgent).append("\n");
            }

            // HTTP 요청 URL을 확인한다.
            if (url != null) {
                sb.append("[ Request URL      ] ").append(url).append("\n");
            }

            if (request != null) {
                // Request의 Header 정보를 확인한다.
                Header[] headers = request.getAllHeaders();
                if (headers != null) {
                    sb.append("[ Request Header   ] ");
                    int size = headers.length;
                    for (int i = 0; i < size; i++) {
                        Header h = headers[i];
                        sb.append(h.getName()).append("=").append(h.getValue()).append("; ");
                    }
                    sb.append("\n");
                }

                // HTTP 프로토콜 버전을 확인한다.
                String protocol = request.getProtocolVersion().toString();
                if (protocol != null) {
                    sb.append("[ Request Protocol ] ").append(protocol).append("\n");
                }
            }

            if (response != null) {
                // Request의 Header 정보를 확인한다.
                Header[] headers = response.getAllHeaders();
                if (headers != null) {
                    sb.append("[ Response Header  ] ");
                    int size = headers.length;
                    for (int i = 0; i < size; i++) {
                        Header h = headers[i];
                        sb.append(h.getName()).append("=").append(h.getValue()).append("; ");
                    }
                    sb.append("\n");
                }

                // HTTP Responses Status 코드를 확인한다.
                int statusCode = response.getStatusLine().getStatusCode();
                sb.append("[ Response Status  ] ").append(statusCode);
            }
            Logger.d(getClass(), sb.toString());
        }
    }
}
