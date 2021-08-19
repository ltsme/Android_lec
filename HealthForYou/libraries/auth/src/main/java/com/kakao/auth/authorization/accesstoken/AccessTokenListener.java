package com.kakao.auth.authorization.accesstoken;

import com.kakao.network.ErrorResult;

/**
 * @author kevin.kang. Created on 2017. 5. 8..
 */

public interface AccessTokenListener {
    void onAccessTokenReceived(final AccessToken accessToken);
    void onAccessTokenFailure(final ErrorResult errorResult);
}
