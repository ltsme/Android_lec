/*
  Copyright 2017 Kakao Corp.

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
 */
package com.kakao.auth.authorization.authcode;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;

import com.kakao.auth.ApprovalType;
import com.kakao.auth.AuthCodeCallback;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.Session;
import com.kakao.auth.StringSet;
import com.kakao.auth.helper.StartActivityWrapper;
import com.kakao.auth.authorization.AuthorizationResult;
import com.kakao.network.ErrorResult;
import com.kakao.util.AppConfig;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author leo.shin
 */
class KakaoAuthCodeManager implements AuthCodeManager, AuthCodeListener {
    private AtomicInteger authRequestCode;

    private Context context;
    private AppConfig appConfig;
    private SparseArray<AuthCodeRequest> requestMap;

    private final Queue<AuthCodeService> authCodeManagers = new LinkedList<AuthCodeService>();
    private StartActivityWrapper startActivityWrapper;

    private final ISessionConfig sessionConfig;
    private AuthCodeService kakaoManager;
    private AuthCodeService storyManager;
    private AuthCodeService webManager;

    @Override
    public void requestAuthCode(AuthType authType, Activity activity, AuthCodeCallback authCodeCallback) {
        requestAuthCode(authType, new StartActivityWrapper(activity), authCodeCallback);
    }

    @Override
    public void requestAuthCode(AuthType authType, Fragment fragment, AuthCodeCallback authCodeCallback) {
        requestAuthCode(authType, new StartActivityWrapper(fragment), authCodeCallback);
    }

    @Override
    public void requestAuthCode(AuthType authType, android.support.v4.app.Fragment fragment, AuthCodeCallback authCodeCallback) {
        requestAuthCode(authType, new StartActivityWrapper(fragment), authCodeCallback);
    }

    @Override
    public void requestAuthCode(final AuthType authType, final StartActivityWrapper wrapper, AuthCodeCallback callback) {
        AuthCodeRequest request = createAuthCodeRequest(appConfig.getAppKey(), callback);
        startTryingAuthCodeServices(authType, request, wrapper);
    }

    void startTryingAuthCodeServices(final AuthType authType, final AuthCodeRequest request, final StartActivityWrapper wrapper) {
        addToAuthCodeServicesQueue(authType);
        requestMap.put(request.getRequestCode(), request);
        startActivityWrapper = wrapper;
        tryNextAuthCodeService(request);
    }

    void tryNextAuthCodeService(final AuthCodeRequest request) {
        AuthCodeService authCodeService;
        AuthCodeCallback callback = request.getCallback();
        while ((authCodeService = authCodeManagers.poll()) != null) {
            if (authCodeService.requestAuthCode(request, startActivityWrapper, this)) {
                return;
            }
        }

        // handler??? ????????? ??????????????? authorization code??? ?????? ???????????? error
        if (callback != null) {
            onAuthCodeReceived(request.getRequestCode(), AuthorizationResult.createAuthCodeOAuthErrorResult("Failed to get Authorization Code."));
        }
    }

    @Override
    public void requestAuthCodeWithScopes(AuthType authType, StartActivityWrapper wrapper, List<String> scopes, AuthCodeCallback authCodeCallback) {
        AuthCodeRequest request = createAuthCodeRequest(appConfig.getAppKey(), getRefreshToken(), scopes, authCodeCallback);
        startTryingAuthCodeServices(authType, request, wrapper);
    }

    KakaoAuthCodeManager(final Context context, final AppConfig appConfig, final ISessionConfig sessionConfig, final AuthCodeService kakaoManager, final AuthCodeService storyManager, final AuthCodeService webManager) {
        this.context = context;
        this.appConfig = appConfig;
        requestMap = new SparseArray<AuthCodeRequest>();

        this.sessionConfig = sessionConfig;
        this.kakaoManager = kakaoManager;
        this.storyManager = storyManager;
        this.webManager = webManager;

        authRequestCode = new AtomicInteger();
    }

    private void addToAuthCodeServicesQueue(final AuthType authType) {
        AuthType type = authType == null ? AuthType.KAKAO_TALK : authType;
        switch (type) {
            case KAKAO_TALK:
            case KAKAO_TALK_EXCLUDE_NATIVE_LOGIN:
                authCodeManagers.add(kakaoManager);
                break;
            case KAKAO_STORY:
                authCodeManagers.add(storyManager);
                break;
            case KAKAO_LOGIN_ALL:
                authCodeManagers.add(kakaoManager);
                authCodeManagers.add(storyManager);
                break;
        }
        authCodeManagers.add(webManager);
    }

    public boolean handleActivityResult(final int requestCode, final int resultCode, final Intent data) {
        AuthCodeRequest request = requestMap.get(requestCode);
        if (request == null) {
            return false;
        }
        if (!kakaoManager.handleActivityResult(requestCode, resultCode, data, this)
                && !storyManager.handleActivityResult(requestCode, resultCode, data, this)) {
            tryNextAuthCodeService(request);
        } else {
            // Login either succeeded or failed. Should reset Authentication method queue.
            authCodeManagers.clear();
        }
        return true;
    }

    @Override
    public boolean isTalkLoginAvailable() {
        return kakaoManager.isLoginAvailable();
    }

    @Override
    public boolean isStoryLoginAvailable() {
        return storyManager.isLoginAvailable();
    }

    String getScopesString(final List<String> requiredScopes) {
        String scopeParam = null;
        if (requiredScopes == null) {
            return null;
        }
        StringBuilder builder = null;
        for (String scope : requiredScopes) {
            if (builder != null) {
                builder.append(",");
            } else {
                builder = new StringBuilder("");
            }

            builder.append(scope);
        }

        if (builder != null) {
            scopeParam = builder.toString();
        }

        return scopeParam;
    }

    AuthCodeRequest createAuthCodeRequest(final String appKey, final AuthCodeCallback callback) {
        Integer requestCode = authRequestCode.incrementAndGet();
        AuthCodeRequest request = new AuthCodeRequest(appKey, StringSet.REDIRECT_URL_PREFIX + appKey + StringSet.REDIRECT_URL_POSTFIX, requestCode, callback);
        request.putExtraParam(StringSet.approval_type, sessionConfig.getApprovalType() == null ? ApprovalType.INDIVIDUAL.toString() : sessionConfig.getApprovalType().toString());
        return request;
    }

    AuthCodeRequest createAuthCodeRequest(final String appKey, final String refreshToken, final List<String> scopes, final AuthCodeCallback callback) {
        Integer requestCode = authRequestCode.incrementAndGet();
        AuthCodeRequest request = new AuthCodeRequest(appKey, StringSet.REDIRECT_URL_PREFIX + appKey + StringSet.REDIRECT_URL_POSTFIX, requestCode, callback);
        request.putExtraHeader(StringSet.RT, refreshToken);
        request.putExtraParam(StringSet.scope, getScopesString(scopes));
        request.putExtraParam(StringSet.approval_type, sessionConfig.getApprovalType() == null ? ApprovalType.INDIVIDUAL.toString() : sessionConfig.getApprovalType().toString());
        return request;
    }

    String getRefreshToken() {
        try {
            return Session.getCurrentSession().getTokenInfo().getRefreshToken();
        } catch (IllegalStateException|NullPointerException e) {
            return null;
        }
    }

    @Override
    public final void onAuthCodeReceived(final int requestCode, AuthorizationResult result) {
        AuthCodeRequest request = requestMap.get(requestCode);
        requestMap.delete(requestCode);
        AuthCodeCallback callback = request.getCallback();

        if (callback == null) {
            return;
        }

        AuthorizationCode authCode = null;
        KakaoException exception = null;

        if (result == null) {
            exception = new KakaoException(KakaoException.ErrorType.AUTHORIZATION_FAILED, "the result of authorization code request is null.");
        } else if (result.isCanceled()) {
            exception = new KakaoException(KakaoException.ErrorType.CANCELED_OPERATION, result.getResultMessage());
        } else if (result.isAuthError() || result.isError()) {
            exception = new KakaoException(KakaoException.ErrorType.AUTHORIZATION_FAILED, result.getResultMessage());
        } else {
            final String resultRedirectURL = result.getRedirectURL();
            if (resultRedirectURL != null && resultRedirectURL.startsWith(request.getRedirectURI())) {
                authCode = AuthorizationCode.createFromRedirectedUri(result.getRedirectUri());
                // authorization code??? ???????????? ??????
                if (!authCode.hasAuthorizationCode()) {
                    authCode = null;
                    exception = new KakaoException(KakaoException.ErrorType.AUTHORIZATION_FAILED, "the result of authorization code request does not have authorization code.");
                }
            } else { // ?????? ?????? redirect uri ?????????
                Logger.e(resultRedirectURL);
                exception = new KakaoException(KakaoException.ErrorType.AUTHORIZATION_FAILED, "the result of authorization code request mismatched the registered redirect uri. msg = " + result.getResultMessage());
            }
        }

        if (exception != null) {
            callback.onAuthCodeFailure(new ErrorResult(exception));
            return;
        }
        callback.onAuthCodeReceived(authCode.getAuthorizationCode());
    }
}
