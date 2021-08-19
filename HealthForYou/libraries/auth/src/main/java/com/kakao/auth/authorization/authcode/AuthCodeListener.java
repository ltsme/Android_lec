package com.kakao.auth.authorization.authcode;

import com.kakao.auth.authorization.AuthorizationResult;

/**
 * @author kevin.kang. Created on 2017. 4. 28..
 */

public interface AuthCodeListener {
    void onAuthCodeReceived(final int requestCode, final AuthorizationResult result);
}
