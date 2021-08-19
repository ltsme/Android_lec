package com.kakao.auth;

import android.app.Activity;
import android.content.Context;

import com.kakao.auth.authorization.accesstoken.AccessTokenManager;
import com.kakao.auth.authorization.authcode.AuthCodeManager;
import com.kakao.auth.mocks.TestAccessTokenManager;
import com.kakao.auth.mocks.TestAuthCodeManager;
import com.kakao.network.ErrorResult;
import com.kakao.test.common.KakaoTestCase;
import com.kakao.util.exception.KakaoException;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author kevin.kang. Created on 2017. 4. 26..
 */

public class SessionTest extends KakaoTestCase {
    private Context context;
    private String appKey = "app_key_for_session_test";
    private String authCode = "auth_code";
    private Activity activity;

    private AuthCodeManager authCodeManager;
    private AccessTokenManager accessTokenManager;
    private Session currentSession;

    private List<String> events = new ArrayList<String>();
    private KakaoException exception;

    @Before
    public void setup() {
        super.setup();
        context = RuntimeEnvironment.application.getApplicationContext();
        activity = Robolectric.setupActivity(Activity.class);

        ISessionConfig sessionConfig = new ISessionConfig() {
            @Override
            public AuthType[] getAuthTypes() {
                return new AuthType[] { AuthType.KAKAO_TALK };
            }

            @Override
            public boolean isUsingWebviewTimer() {
                return false;
            }

            @Override
            public boolean isSecureMode() {
                return false;
            }

            @Override
            public ApprovalType getApprovalType() {
                return ApprovalType.INDIVIDUAL;
            }

            @Override
            public boolean isSaveFormData() {
                return false;
            }
        };

        authCodeManager = Mockito.spy(new TestAuthCodeManager());
        accessTokenManager = Mockito.spy(new TestAccessTokenManager());
        currentSession = new Session(context, appKey, sessionConfig, authCodeManager, accessTokenManager);

        Assert.assertEquals(true, currentSession.isClosed());
        Assert.assertEquals("", currentSession.getTokenInfo().getAccessToken());
        Assert.assertEquals("", currentSession.getTokenInfo().getRefreshToken());

        currentSession.addCallback(new ISessionCallback() {
            @Override
            public void onSessionOpened() {
                events.add("success");
            }

            @Override
            public void onSessionOpenFailed(KakaoException e) {
                exception = e;
                events.add("failure");
            }
        });
    }

    @After
    public void cleanup() {
        events.clear();
        exception = null;
    }

    @Test
    public void implicitOpen() {
        Assert.assertFalse(currentSession.implicitOpen());
        Assert.assertFalse(currentSession.isOpened());
        Assert.assertFalse(currentSession.isOpenable());
        Assert.assertTrue(currentSession.isClosed());
    }

    @Test
    public void checkAndImplicitOpen() {
        Assert.assertFalse(currentSession.checkAndImplicitOpen());
        Assert.assertFalse(currentSession.isOpened());
        Assert.assertFalse(currentSession.isOpenable());
        Assert.assertTrue(currentSession.isClosed());
    }

    @Test
    public void testOpenWithActivity() {
        Assert.assertTrue(events.isEmpty());
        currentSession.open(AuthType.KAKAO_LOGIN_ALL, activity);
        Assert.assertTrue(events.contains("success"));
        Assert.assertTrue(currentSession.isOpened());
    }

    @Test
    public void testOpenWithAuthCode() {
        Assert.assertTrue(events.isEmpty());
        currentSession.openWithAuthCode(authCode);
        Assert.assertTrue(events.contains("success"));
        Assert.assertTrue(currentSession.isOpened());
    }

    @Test
    public void testOpenWithAuthorizationFailed() {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                AuthCodeCallback callback = invocation.getArgument(2);
                callback.onAuthCodeFailure(new ErrorResult(new KakaoException(KakaoException.ErrorType.AUTHORIZATION_FAILED, "Authorization failed mock.")));
                return null;
            }
        }).when(authCodeManager).requestAuthCode(AuthType.KAKAO_LOGIN_ALL, activity, currentSession.getAuthCodeCallback());

        currentSession.open(AuthType.KAKAO_LOGIN_ALL, activity);
        Assert.assertTrue(events.contains("failure"));
        Assert.assertTrue(currentSession.isClosed());
        Assert.assertEquals(KakaoException.ErrorType.AUTHORIZATION_FAILED, exception.getErrorType());
    }

    @Test
    public void testOpenWithCanceledOperation() {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                AuthCodeCallback callback = invocation.getArgument(2);
                callback.onAuthCodeFailure(new ErrorResult(new KakaoException(KakaoException.ErrorType.CANCELED_OPERATION, "Canceled operation mock.")));
                return null;
            }
        }).when(authCodeManager).requestAuthCode(AuthType.KAKAO_LOGIN_ALL, activity, currentSession.getAuthCodeCallback());

        currentSession.open(AuthType.KAKAO_LOGIN_ALL, activity);
        Assert.assertTrue(events.contains("failure"));
        Assert.assertTrue(currentSession.isClosed());
        Assert.assertEquals(KakaoException.ErrorType.CANCELED_OPERATION, exception.getErrorType());
    }

    @Test
    public void testOpenWithNetworkError() {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                AuthCodeCallback callback = invocationOnMock.getArgument(2);
                callback.onAuthCodeFailure(new ErrorResult(new IllegalArgumentException("Error message")));
                return null;
            }
        }).when(authCodeManager).requestAuthCode(AuthType.KAKAO_LOGIN_ALL, activity, currentSession.getAuthCodeCallback());
        currentSession.open(AuthType.KAKAO_LOGIN_ALL, activity);
        Assert.assertTrue(events.contains("failure"));
        Assert.assertTrue(exception != null);
    }

    @Test
    public void testOpenWithAuthCodeWithError() {
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                AccessTokenCallback callback = invocationOnMock.getArgument(1);
                callback.onAccessTokenFailure(new ErrorResult(new IllegalArgumentException("error message")));
                return null;
            }
        }).when(accessTokenManager).requestAccessTokenByAuthCode("auth_code", currentSession.getAccessTokenCallback());
        currentSession.openWithAuthCode("auth_code");

        Assert.assertTrue(events.contains("failure"));
    }

    @Test
    public void testAddAndRemoveCallback() {
        ISessionCallback callback1 = new ISessionCallback() {
            @Override
            public void onSessionOpened() {

            }

            @Override
            public void onSessionOpenFailed(KakaoException exception) {

            }
        };

        ISessionCallback callback2 = new ISessionCallback() {
            @Override
            public void onSessionOpened() {

            }

            @Override
            public void onSessionOpenFailed(KakaoException exception) {

            }
        };

        currentSession.clearCallbacks();
        Assert.assertTrue(currentSession.getCallbacks().isEmpty());
        currentSession.addCallback(callback1);
        Assert.assertEquals(1, currentSession.getCallbacks().size());
        currentSession.addCallback(callback1);
        Assert.assertEquals(1, currentSession.getCallbacks().size());
        currentSession.removeCallback(callback2);
        Assert.assertEquals(1, currentSession.getCallbacks().size());
        currentSession.addCallback(callback2);
        Assert.assertEquals(2, currentSession.getCallbacks().size());
        currentSession.removeCallback(callback1);
        Assert.assertEquals(1, currentSession.getCallbacks().size());
        currentSession.removeCallback(callback2);
        Assert.assertTrue(currentSession.getCallbacks().isEmpty());
    }

    @Test
    public void testInternalClose() {
        currentSession.internalClose(null, false);
        currentSession.internalClose(null, true);
    }
//    @Test
//    public void testOpenWithWrongClientSecret() {
//        Mockito.doAnswer(new Answer() {
//            @Override
//            public Object answer(InvocationOnMock invocation) throws Throwable {
//                Logger.e("mocked answer...");
//                AccessTokenCallback callback = invocation.getArgumentAt(1, AccessTokenCallback.class);
//                callback.onAccessTokenFailure();
//                return null;
//            }
//        }).when(accessTokenManager).requestAccessTokenByAuthCode(Matchers.anyString(), Matchers.any(AccessTokenCallback.class));
//    }
}
