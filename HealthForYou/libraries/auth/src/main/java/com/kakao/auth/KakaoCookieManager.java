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
package com.kakao.auth;

import android.os.Build;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

/**
 * This is a wrapper class for managing webview cookies. Either CookieManager or CookieSyncManager
 * is used depending on Android API level.
 *
 * @author kevin.kang. Created on 2017. 5. 22..
 */

public class KakaoCookieManager {
    private CookieSyncManager cookieSyncManager;
    private CookieManager cookieManager;

    private static KakaoCookieManager instance;

    /**
     * Returns a singleton instance for managing webview cookies for the application.
     *
     * @return a singleton instance for managing webview cookies for the application
     */
    public static KakaoCookieManager getInstance() {
        if (instance == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                instance = new KakaoCookieManager(CookieManager.getInstance());
            } else {
                CookieSyncManager.createInstance(KakaoSDK.getAdapter().getApplicationConfig().getApplicationContext());
                instance = new KakaoCookieManager(CookieSyncManager.getInstance());
            }
        }
        return instance;
    }

    /**
     * Construct KakaoCookieManager with Android's CookieSyncManager.
     * @param manager CookieSyncManager instance
     */
    public KakaoCookieManager(final CookieSyncManager manager) {
        cookieSyncManager = manager;
    }

    /**
     * Construct KakaoCookieManager with Android's CookieManager.
     * @param manager CookieManager instance
     */
    public KakaoCookieManager(final CookieManager manager) {
        cookieManager = manager;
    }

    /**
     * Flushes cashes to persistent storage.
     */
    public void flush() {
        if (cookieManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.flush();
        } else if (cookieSyncManager != null) {
            cookieSyncManager.sync();
        }
    }
}
