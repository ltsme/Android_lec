/**
 * Copyright 2014-2017 Kakao Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.kakao.auth;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import com.kakao.auth.AuthService.AgeAuthStatus;
import com.kakao.auth.authorization.accesstoken.AccessToken;
import com.kakao.auth.authorization.accesstoken.AccessTokenManager;
import com.kakao.auth.authorization.authcode.AuthCodeManager;
import com.kakao.auth.helper.StartActivityWrapper;
import com.kakao.auth.network.response.ApiResponse.InsufficientScopeException;
import com.kakao.auth.network.response.ApiResponse.SessionClosedException;
import com.kakao.network.ErrorResult;
import com.kakao.network.INetwork;
import com.kakao.network.IRequest;
import com.kakao.network.KakaoNetworkImpl;
import com.kakao.network.NetworkTask;
import com.kakao.network.response.ResponseBody;
import com.kakao.network.response.ResponseBody.ResponseBodyException;
import com.kakao.network.response.ResponseData;
import com.kakao.util.AppConfig;
import com.kakao.util.helper.log.Logger;

/**
 * @author leo.shin
 */
public class SingleNetworkTask extends NetworkTask {
    final private INetwork network;

    public SingleNetworkTask() {
        this.network = new KakaoNetworkImpl();
    }

    public SingleNetworkTask(INetwork network) {
        this.network = network;
    }

    /**
     * Session??? ????????? ???????????? open????????? ??????????????? ????????? ??????.
     * @return true is succeed, false is otherwise.
     */
    private static boolean checkApiSession() {
        Session session = Session.getCurrentSession();
        // 1. session check.
        if (session.isOpened()) {
            return true;
        }

        Logger.e("access token expired... trying to refresh access token...");
        // 2. request accessToken with refreshToken.
        // 3. update original request authorization accessToken value on header.
        if (session.getTokenInfo().hasRefreshToken()) {
            try {
                AccessToken accessToken = AccessTokenManager.Factory.getInstance().refreshAccessToken(session.getTokenInfo().getRefreshToken(), session.getAccessTokenCallback()).get();
                return true;
            } catch (Exception e) {
                Logger.e("exception: " + e.toString());
                return false;
            }
        }

        return false;
    }

    private AccessToken requestScopesUpdateBlocking(final AuthType authType, final Activity topActivity, final ResponseBody result) throws Exception {
        List<String> requiredScopes = null;
        if (result.has(StringSet.required_scopes)) {
            try {
                requiredScopes = result.optConvertedList(StringSet.required_scopes, ResponseBody.STRING_CONVERTER, Collections.<String>emptyList());
            } catch (ResponseBodyException e) {
                throw new InsufficientScopeException(result);
            }
        }
        return requestScopesUpdate(authType, topActivity, requiredScopes);
    }

    private AccessToken requestScopesUpdate(final AuthType authType, final Activity topActivity, final List<String> scopes) throws Exception {
        final Session session = Session.getCurrentSession();
        final String refreshToken = session.getTokenInfo().getRefreshToken();
        final String appKey = AppConfig.getInstance(topActivity).getAppKey();
        final AuthCodeManager getter = AuthCodeManager.Factory.getInstance();

        final AtomicReference<String> authCodeResult = new AtomicReference<String>();
        final AtomicReference<Exception> exception = new AtomicReference<Exception>();
        final CountDownLatch lock = new CountDownLatch(1);

        final AuthCodeCallback callback = new AuthCodeCallback() {
            @Override
            public void onAuthCodeReceived(String authCode) {
                authCodeResult.set(authCode);
                lock.countDown();
            }

            @Override
            public void onAuthCodeFailure(ErrorResult errorResult) {
                exception.set(errorResult.getException());
                lock.countDown();
            }
        };

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                try {
                    // 4. Scope update Webview Dialog??????.(????????? ??????) - AuthCode ??????.
                    getter.requestAuthCodeWithScopes(authType, new StartActivityWrapper(KakaoSDK.getCurrentActivity()), scopes, callback);
                } catch (Exception e) {
                    lock.countDown();
                }
            }
        });

        // scope??? ?????????????????? ????????????.
        // ???????????? ????????? ????????? ??????.
        try {
            lock.await();
        } catch (InterruptedException ignored) {
        }

        String authCode = authCodeResult.get();
        AccessToken accessToken = null;
        if (authCode != null) {
            try {
                accessToken = AccessTokenManager.Factory.getInstance().requestAccessTokenByAuthCode(authCode, null).get();
            } catch (Exception e) {
                Logger.e(e);
                accessToken = AccessToken.createEmptyToken();
            }
        }

        if (exception.get() != null) {
            throw exception.get();
        }

        return accessToken;
    }

    private static ErrorCode getErrorCode(ResponseBody responseBody) {
        try {
            // 1. check scopes error.
            if (responseBody.has(StringSet.code)) {
                return ErrorCode.valueOf(responseBody.getInt(StringSet.code));
            }
        } catch (ResponseBodyException e) {
            Logger.e("exception while getting error code: " + e.toString());
        }
        return null;
    }

    Activity getTopActivity() {
        Activity topActivity = KakaoSDK.getCurrentActivity();
        if (topActivity == null) {
            // 3???????????? retry?????? ????????? ??????????????? ????????? ???????????? ?????????.
            int retryCount = 0;
            while(topActivity == null && retryCount < 3) {
                try {
                    retryCount++;
                    Thread.sleep(500);
                    topActivity = KakaoSDK.getCurrentActivity();
                } catch (InterruptedException e) {
                }
            }
        }
        return topActivity;
    }

    AuthType getAuthType() {
        return AuthType.KAKAO_ACCOUNT;
    }

    private boolean handleApiError(ResponseData result) {
        boolean retry = false;
        try {
            ResponseBody errResponseBody = new ResponseBody(result.getHttpStatusCode(), result.getData());
            if (getErrorCode(errResponseBody) == ErrorCode.INVALID_TOKEN_CODE) {
                Session session = Session.getCurrentSession();
                session.removeAccessToken();

                if (session.getTokenInfo().hasRefreshToken()) {
                    retry = AccessTokenManager.Factory.getInstance().refreshAccessToken(session.getTokenInfo().getRefreshToken(), session.getAccessTokenCallback()).get().hasValidAccessToken();
                }
            } else if (getErrorCode(errResponseBody) == ErrorCode.INVALID_SCOPE_CODE) {
                Activity topActivity = getTopActivity();
                AuthType authType = getAuthType();
                if (topActivity != null) {
                    retry = requestScopesUpdateBlocking(authType, topActivity, errResponseBody).hasValidAccessToken();
                }
            } else if (getErrorCode(errResponseBody) == ErrorCode.NEED_TO_AGE_AUTHENTICATION) {
                Activity topActivity = getTopActivity();
                if (topActivity != null) {
                    int state = AgeAuthManager.getInstance().requestShowAgeAuthDialog(topActivity);
                    retry = state == AgeAuthStatus.SUCCESS.getValue() || state == AgeAuthStatus.ALREADY_AGE_AUTHORIZED.getValue();
                }
            }
        } catch (Exception ignore) {
            Logger.w(ignore);
        }
        return retry;
    }

    /**
     * ?????? API ????????? ????????? ????????? ????????? ????????????.
     * Background Thread
     * 1. accessToken ???????????? ?????? ??? ???????????? ????????? refreshToken?????? accessToken??????.
     * 2. ????????? API ??????.
     *
     * UI Thread
     * 1. result??? scope error ??????.
     * 2. Scope update Webview Dialog??????.(????????? ??????)
     * 3. AuthCode?????? ??? accessToken??????.
     *
     * Backgound Thread
     * 1. ????????? accessToken??? ????????? ?????? retry.
     *
     * @param request API ??????.
     * @return request??? ?????? responseBody
     * @throws Exception if request fails
     */
    public synchronized ResponseData requestApi(final IRequest request) throws Exception {
        // 1. accessToken ???????????? ?????? ??? ???????????? ????????? refreshToken?????? accessToken??????.
        if (!checkApiSession()) {
            throw new SessionClosedException("Application Session is Closed.");
        }

        // 2. ????????? API ??????.
        ResponseData result = request(request);
        Logger.d("++ [%s]response : %s", result.getHttpStatusCode(), result.getStringData());
        if (result.getHttpStatusCode() != HttpURLConnection.HTTP_OK) {
            if (handleApiError(result)) {
                // ????????? accessToken??? ????????? ?????? retry.
                return requestApi(request);
            }
        }

        return result;
    }

    public synchronized ResponseBody requestAuth(IRequest request) throws Exception {
        ResponseData result = request(request);
        Logger.d("++ [%s]response : %s", result.getHttpStatusCode(), result.getStringData());
        return new ResponseBody(result.getHttpStatusCode(), result.getData());
    }
}
