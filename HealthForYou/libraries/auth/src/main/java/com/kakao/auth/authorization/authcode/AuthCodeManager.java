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
import android.os.Handler;
import android.os.Looper;

import com.kakao.auth.AuthCodeCallback;
import com.kakao.auth.AuthType;
import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoCookieManager;
import com.kakao.auth.helper.StartActivityWrapper;
import com.kakao.util.AppConfig;
import com.kakao.util.protocol.KakaoProtocolService;

import java.util.List;

/**
 * Interface responsible for getting authorization code from Kakao OAuth server.
 *
 * @author kevin.kang. Created on 2017. 5. 25..
 */

public interface AuthCodeManager {

    /**
     *
     * @param authType AuthTyp
     * @param activity
     * @param authCodeCallback
     */
    void requestAuthCode(final AuthType authType, final Activity activity, AuthCodeCallback authCodeCallback);

    /**
     *
     * @param authType
     * @param fragment
     * @param authCodeCallback
     */
    void requestAuthCode(final AuthType authType, final Fragment fragment, AuthCodeCallback authCodeCallback);

    /**
     *
     * @param authType
     * @param fragment
     * @param authCodeCallback
     */
    void requestAuthCode(final AuthType authType, final android.support.v4.app.Fragment fragment, AuthCodeCallback authCodeCallback);

    /**
     *
     * @param authType
     * @param wrapper
     * @param authCodeCallback
     */
    void requestAuthCode(final AuthType authType, final StartActivityWrapper wrapper, AuthCodeCallback authCodeCallback);

    /**
     *
     * @param authType
     * @param wrapper
     * @param scopes
     * @param authCodeCallback
     */
    void requestAuthCodeWithScopes(final AuthType authType, final StartActivityWrapper wrapper, final List<String> scopes, AuthCodeCallback authCodeCallback);

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     * @return
     */
    boolean handleActivityResult(int requestCode, int resultCode, Intent data);

    /**
     *
     * @return
     */
    boolean isTalkLoginAvailable();

    /**
     *
     * @return
     */
    boolean isStoryLoginAvailable();

    class Factory {
        private static AuthCodeManager authCodeManager;

        public static AuthCodeManager initialize(final Context context, final AppConfig appConfig, final ISessionConfig sessionConfig) {
            if (authCodeManager == null) {
                AuthCodeService talkManager = AuthCodeService.Factory.createTalkService(context, appConfig, sessionConfig, KakaoProtocolService.Factory.getInstance());
                AuthCodeService storyManager = AuthCodeService.Factory.createStoryService(context, appConfig, sessionConfig, KakaoProtocolService.Factory.getInstance());
                AuthCodeService webManager = AuthCodeService.Factory.createWebService(new Handler(Looper.getMainLooper()), KakaoCookieManager.getInstance(), sessionConfig);
                authCodeManager = new KakaoAuthCodeManager(context, appConfig, sessionConfig, talkManager, storyManager, webManager);
            }
            return authCodeManager;
        }

        public static AuthCodeManager getInstance() {
            return authCodeManager;
        }
    }
}
