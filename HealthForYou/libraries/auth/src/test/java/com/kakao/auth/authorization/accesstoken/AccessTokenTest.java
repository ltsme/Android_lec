package com.kakao.auth.authorization.accesstoken;

import com.kakao.test.common.KakaoTestCase;
import com.kakao.util.helper.SharedPreferencesCache;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.robolectric.RuntimeEnvironment;


import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;

/**
 * This class mocks KakaoSDK, Utility, SharedPreferenceCache, ResponseBody for solely unit-testing AccessToken class.
 * @author kevin.kang
 * Created by kevin.kang on 16. 8. 11..
 */
public class AccessTokenTest extends KakaoTestCase {

    private static final String appKey = "appkey";
    private static final String accessToken = "accessToken";
    private static final String refreshToken = "refreshToken";
    private static final Long expiresIn = 3600 * 12L;
    private static final Long expiresAt = new Date().getTime() + expiresIn;
    private static final String FAKE_KEY_HASH = "lMXltzn4zSwq0EhwLKAo+k0zhqI=";

    private SharedPreferencesCache cache;
    private AccessToken tokenInfo;
    @Before
    public void setup() {
        super.setup();
        cache = Mockito.spy(new SharedPreferencesCache(RuntimeEnvironment.application.getApplicationContext(), appKey));
    }

    @Test
    public void testCreateEmptyToken() {
        tokenInfo = AccessToken.createEmptyToken();
        Assert.assertNotNull(tokenInfo);
        Assert.assertTrue(tokenInfo.getAccessToken().isEmpty());
        Assert.assertTrue(tokenInfo.getRefreshToken().isEmpty());
        Assert.assertFalse(tokenInfo.hasValidAccessToken());
        Assert.assertFalse(tokenInfo.hasRefreshToken());
    }

    @Test
    public void testCreateFromCache() {
        mockCache(cache);
        tokenInfo = AccessToken.createFromCache(RuntimeEnvironment.application.getApplicationContext(), cache, false);
        Assert.assertNotNull(tokenInfo);
        Assert.assertTrue(tokenInfo.hasValidAccessToken());
        Assert.assertTrue(tokenInfo.hasRefreshToken());
    }

    @Test
    public void testSaveAccessTokenToCache() {
        tokenInfo = createAccessToken();
        tokenInfo.saveAccessTokenToCache(RuntimeEnvironment.application.getApplicationContext(), cache, false);

        AccessToken tokenFromCache = AccessToken.createFromCache(RuntimeEnvironment.application.getApplicationContext(), cache, false);
        Assert.assertEquals(tokenInfo.getAccessToken(), tokenFromCache.getAccessToken());
        Assert.assertEquals(tokenInfo.getRefreshToken(), tokenFromCache.getRefreshToken());
    }

    /**
     * This test tests if secure mode is working correctly, but actually bypasses the encryption and decryption mechanism
     * by just using the original value.
     */
    @Test
    public void testSecureMode() {
//        AccessToken token = getStubbedAccessToken();
//        if (token == null)
//            fail("Failed to get");
//        assertEquals(token.getAccessToken(), accessToken);
//        assertEquals(token.getRefreshToken(), refreshToken);
//        assertThat(token.getRemainedExpiresInAccessTokenTime(), Matchers.lessThanOrEqualTo(3600 * 12 * 1000));
//
//        token.saveAccessTokenToCache(RuntimeEnvironment.application.getApplicationContext(), cache, false);
//
//        AccessToken tokenFromCache = AccessToken.createFromCache(RuntimeEnvironment.application.getApplicationContext(), cache, false);
//
//        assertEquals(accessToken, tokenFromCache.getAccessToken());
//        assertEquals(refreshToken, tokenFromCache.getRefreshToken());
//        assertThat(tokenFromCache.getRemainedExpiresInAccessTokenTime(), Matchers.lessThanOrEqualTo(3600 * 12 * 1000));
    }


//    @Test
//    public void testTurnOnSecureMode() {
//        mockSharedPreferenceCache();
//        stubKakaoSDKInit(true);
//        stub(method(AccessToken.class, "getLastSecureMode")).toReturn(false);
//
//        AccessToken token = AccessToken.createFromCache(RuntimeEnvironment.application.getApplicationContext(), cache, false);
//        assertTrue(token.hasValidAccessToken());
//        assertTrue(token.hasRefreshToken());
//    }
//
//    @Test
//    public void testTurnOffSecureMode() {
//        mockSharedPreferenceCache();
//        stubKakaoSDKInit(false);
//        stub(method(AccessToken.class, "getLastSecureMode")).toReturn(true);
//
//        AccessToken token = AccessToken.createFromCache(RuntimeEnvironment.application.getApplicationContext(), cache, false);
//        assertTrue(token.hasValidAccessToken());
//        assertTrue(token.hasRefreshToken());
//    }
//
//    @Test
//    public void testGeneralSecurityException() {
//        mockSharedPreferenceCache();
//        stubKakaoSDKInit(true);
//
//        try {
//            PowerMockito.doThrow(new GeneralSecurityException()).when(AccessToken.class, "encrypt", org.mockito.Matchers.any(Application.class), org.mockito.Matchers.anyString());
//            PowerMockito.doThrow(new GeneralSecurityException()).when(AccessToken.class, "decrypt", org.mockito.Matchers.any(Application.class), org.mockito.Matchers.anyString());
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail(e.toString());
//        }
//
//        AccessToken token = AccessToken.createFromCache(RuntimeEnvironment.application.getApplicationContext(), cache, false);
//
//        assertFalse(token.hasValidAccessToken());
//        assertFalse(token.hasRefreshToken());
//    }
//
//    @Test
//    public void testIOException() {
//        mockSharedPreferenceCache();
//        stubKakaoSDKInit(true);
//
//        try {
//            PowerMockito.doThrow(new IOException()).when(AccessToken.class, "encrypt", org.mockito.Matchers.any(Application.class), org.mockito.Matchers.anyString());
//            PowerMockito.doThrow(new IOException()).when(AccessToken.class, "decrypt", org.mockito.Matchers.any(Application.class), org.mockito.Matchers.anyString());
//        } catch (Exception e) {
//            e.printStackTrace();
//            fail(e.toString());
//        }
//
//        AccessToken token = AccessToken.createFromCache(RuntimeEnvironment.application.getApplicationContext(), cache, false);
//
//        assertFalse(token.hasValidAccessToken());
//        assertFalse(token.hasRefreshToken());
//    }

//    /**
//     * Mocks an access token built from auth API response.
//     * @return
//     */
//    public static AccessToken getStubbedAccessToken() {
//        ResponseBody spy;
//        try {
//            spy = PowerMockito.spy(new ResponseBody(200, new JSONObject()));
//
//            PowerMockito.doReturn(true).when(spy).has(StringSet.access_token);
//            PowerMockito.doReturn(true).when(spy).has(StringSet.refresh_token);
//            PowerMockito.doReturn(accessToken).when(spy).getString(StringSet.access_token);
//            PowerMockito.doReturn(refreshToken).when(spy).getString(StringSet.refresh_token);
//            PowerMockito.doReturn(3600 * 12).when(spy).getInt(StringSet.expires_in);
//            return new AccessToken(spy);
//        } catch (ResponseBody.ResponseBodyException|AuthResponse.AuthResponseStatusError e) {
//            e.printStackTrace();
//            return null;
//        }
//    }

    /**
     * Mocks SharedPreference cache to build an access token on app start.
     */
    private void mockCache(final SharedPreferencesCache cache) {
        if (cache == null)
            return;

        Mockito.doReturn(accessToken).when(cache).getString(AccessToken.CACHE_ACCESS_TOKEN);
        Mockito.doReturn(refreshToken).when(cache).getString(AccessToken.CACHE_REFRESH_TOKEN);
        Mockito.doReturn(expiresAt).when(cache).getLong(AccessToken.CACHE_ACCESS_TOKEN_EXPIRES_AT);
        Mockito.doReturn(expiresAt).when(cache).getLong(AccessToken.CACHE_REFRESH_TOKEN_EXPIRES_AT);
    }


    private void mockEncryptor(final AccessToken tokenInfo) {
        try {
            Mockito.doReturn(accessToken).when(tokenInfo).encrypt(RuntimeEnvironment.application.getApplicationContext(), accessToken);
            Mockito.doReturn(refreshToken).when(tokenInfo).encrypt(RuntimeEnvironment.application.getApplicationContext(), refreshToken);
            Mockito.doReturn(accessToken).when(tokenInfo).decrypt(RuntimeEnvironment.application.getApplicationContext(), accessToken);
            Mockito.doReturn(refreshToken).when(tokenInfo).decrypt(RuntimeEnvironment.application.getApplicationContext(), refreshToken);
        } catch (GeneralSecurityException|IOException e) {
            Assert.fail();
        }
    }

    private AccessToken createAccessToken() {
        long currentTimeInMillis = new Date().getTime();
        Date accessTokenExpireDate = new Date(currentTimeInMillis + 12 * 60 * 60 * 1000);
        Date refreshTokenExpireDate = new Date(currentTimeInMillis + 30 * 24 * 60 * 60 * 1000);
        return new AccessToken(accessToken, refreshToken, accessTokenExpireDate, refreshTokenExpireDate);
    }
}
