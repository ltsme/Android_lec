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

import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.kakao.auth.ISessionConfig;
import com.kakao.auth.KakaoCookieManager;
import com.kakao.auth.helper.StartActivityWrapper;
import com.kakao.util.AppConfig;
import com.kakao.util.protocol.KakaoProtocolService;

/**
 * This is an interface that abstracts various methods of getting authorization code from oauth server.
 * @author kevin.kang. Created on 2017. 5. 30..
 */

public interface AuthCodeService {

    /**
     *
     * @param request
     * @param wrapper
     * @param listener
     * @return
     */
    boolean requestAuthCode(final AuthCodeRequest request, final StartActivityWrapper wrapper, AuthCodeListener listener);

    /**
     *
     * @param requestCode
     * @param resultCode
     * @param data
     * @param listener
     * @return true if intent received by onActivityResult() is handled with this AuthCodeService, false otherwise.
     */
    boolean handleActivityResult(int requestCode, int resultCode, Intent data, AuthCodeListener listener);

    /**
     * Checks whether authorization code can be retrieved from this service.
     *
     * @return true if this service is available on this device, false otherwise
     */
    boolean isLoginAvailable();

    /**
     * This is a factory class for various AuthCodeSrvice types.
     */
    class Factory {
        static AuthCodeService createTalkService(final Context context, final AppConfig appConfig, final ISessionConfig sessionConfig, final KakaoProtocolService protocolService) {
            return new TalkAuthCodeService(context, appConfig, sessionConfig, protocolService);
        }

        static AuthCodeService createStoryService(final Context context, final AppConfig appConfig, final ISessionConfig sessionConfig, final KakaoProtocolService protocolService) {
            return new StoryAuthCodeService(context, appConfig, sessionConfig, protocolService);
        }

        static AuthCodeService createWebService(final Handler handler, final KakaoCookieManager cookieManager, final ISessionConfig sessionConfig) {
            return new WebAuthCodeService(handler, cookieManager, sessionConfig);
        }
    }
}
