package com.example.nam.healthforyou.view;

import android.content.Context;
import android.content.Intent;

import android.content.SharedPreferences;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
//카카오 로그인
import com.example.nam.healthforyou.component.DBhelper;
import com.example.nam.healthforyou.R;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;
//네이버 로그인
import com.nhn.android.naverlogin.OAuthLogin;
import com.nhn.android.naverlogin.OAuthLoginHandler;
import com.nhn.android.naverlogin.ui.view.OAuthLoginButton;
//페이스북 로그인
import com.facebook.FacebookSdk;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/*
       //*전역변수로 세션쿠키 저장시 로그인이 안되거나 풀려버림(액티비티
        if(cookiesHeader != null) {//쿠키매니저를 통해 쿠키를 보관한다.
            for (String cookie : cookiesHeader) {
                msCookieManager.getCookieStore().add(null,HttpCookie.parse(cookie).get(0));
            }
        }

 */

public class Login extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG =null;
    public static String cookies;
    HttpURLConnection con;
    private EditText input_id;
    private EditText input_pw;
    private String result;
    static final String COOKIES_HEADER = "Set-Cookie";
    public static java.net.CookieManager msCookieManager = new java.net.CookieManager();

    private static String OAUTH_CLIENT_ID = "VjwgVwBRfXh26HTy5mxR";  // AUTH CLIENT ID 에서 받아온 값들을 넣어좁니다
    private static String OAUTH_CLIENT_SECRET = "ZTmUoGPaPC";
    private static String OAUTH_CLIENT_NAME = "네이버 아이디로 로그인";

    private static OAuthLogin mOAuthLoginModule;
    private static Context mContext;

    //카카오톡 로그인
    private SessionCallback callback;
    //페이스북 로그인
    private Button btn_facebook;
    private CallbackManager callbackManager;
    //네이버 로그인
    com.nhn.android.naverlogin.ui.view.OAuthLoginButton btn_naver;
    com.kakao.usermgmt.LoginButton btn_kakao;

    private Button fakenaver;
    private Button fakekakao;
    private Button fakefacebook;
    SharedPreferences session;
    SharedPreferences.Editor session_editor;
    private String session_id;

    SharedPreferences loginemail;
    SharedPreferences.Editor loginemail_editor;
    private String loginemailid;

    //나의정보를 저장할 DB선언
    DBhelper dBhelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getApplicationContext();


        loginemail = getApplicationContext().getSharedPreferences("useremail",MODE_PRIVATE);
        loginemail_editor = loginemail.edit();

        //SQlite
        dBhelper = new DBhelper(mContext, "healthforyou.db", null, 1);//SQLite DBhelper


        FacebookSdk.sdkInitialize(this.getApplicationContext());
        //session id 가 있으면
        //SharedPreference
        session = getApplicationContext().getSharedPreferences("session",MODE_PRIVATE);//SharedPreference에 저장할 준비
        session_editor = session.edit();
        session_id=session.getString("session","false");

        System.out.println(session_id+"세션아이디 체크");

        if(!session_id.equals("false"))
        {
            //Shared에 저장되어 있는 Session id를 불러와 쿠키매니저에 넣어줌
            session = getApplicationContext().getSharedPreferences("session",MODE_PRIVATE);//SharedPreference에 저장할 준비
            session_editor = session.edit();
            session_id=session.getString("session","false");
            session_id=session_id.replace("[","");
            session_id=session_id.replace("]","");
            List<String> cookiesHeader = new ArrayList<>(Arrays.asList(session_id.split(";")));
            System.out.println(cookiesHeader);
            ////*Cookiemanager에 저장해주는 부분
            if (cookiesHeader != null) {//쿠키매니저를 통해 쿠키를 보관한다.
                for (String cookie : cookiesHeader) {
                    msCookieManager.getCookieStore().add(null,HttpCookie.parse(cookie).get(0));
                    //System.out.println(HttpCookie.parse(cookie).get(0)+"Shared Login");
                }
            }
            //Login에서 메인액티비티로 가는부분
            Intent intent = new Intent(Login.this,MainActivity.class);
            startActivity(intent);
            finish();
        }



        setContentView(R.layout.activity_login);

        /////////fake 이미지를 클릭할 수 있도록 해줌
        fakenaver = (Button) findViewById(R.id.fake_naver);
        fakenaver.setOnClickListener(this);

        fakekakao = (Button) findViewById(R.id.fake_kakao);
        fakekakao.setOnClickListener(this);

        fakefacebook = (Button) findViewById(R.id.fake_facebook);
        fakefacebook.setOnClickListener(this);
        //////////////////////////////////////////////

        //카카오 로그인 준비
        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);

        //네이버 로그인 준비
        mOAuthLoginModule = OAuthLogin.getInstance();
        mOAuthLoginModule.init(mContext, OAUTH_CLIENT_ID,OAUTH_CLIENT_SECRET, OAUTH_CLIENT_NAME);
        initSetting();


        input_id=(EditText)findViewById(R.id.id);
        input_pw=(EditText)findViewById(R.id.password);
        Button signup=(Button)findViewById(R.id.signup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(),Signup.class));//회원가입시에는 액티비티를 종료시키지 않음
            }
        });

        Button login=(Button)findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id=input_id.getText().toString();
                String pw=input_pw.getText().toString();
                JSONObject login_info=new JSONObject();
                try {
                    login_info.put("type","self");
                    login_info.put("id",id);
                    login_info.put("pw",pw);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                LoginTask loginTask = new LoginTask();
                loginTask.execute(login_info.toString());
            }
        });

        btn_facebook=(Button)findViewById(R.id.btn_facebook);
        btn_facebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                facebookLoginOnClick(v);
            }
        });

        }//OnCreate end

    private void requestMe() {
        UserManagement.requestMe(new MeResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                String message = "failed to get user info. msg=" + errorResult;
                Logger.d(message);
                redirectLoginActivity();
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                redirectLoginActivity();
            }

            @Override
            public void onSuccess(UserProfile userProfile) {
                Logger.d("UserProfile : " + userProfile);
                String kakaoID = String.valueOf(userProfile.getId()); // userProfile에서 ID값을 가져옴
                String kakaoNickname = userProfile.getNickname();     // Nickname 값을 가져옴
                JSONObject kakao_mem = new JSONObject();
                try {
                    kakao_mem.put("type","kakao");
                    kakao_mem.put("id",kakaoID);
                    kakao_mem.put("name",kakaoNickname);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                LoginTask loginTask = new LoginTask();
                loginTask.execute(kakao_mem.toString());
            }

            @Override
            public void onNotSignedUp() {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }
        callbackManager.onActivityResult(requestCode, resultCode, data);

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
    }

    @Override///////////fake image를 클릭하는 부분
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fake_naver:  //fake_naver 내 버튼을 눌렀을 경우
                btn_naver.performClick(); //performClick 클릭을 실행하게 만들어 자동으로 실행되도록 한다.
                break;

            case R.id.fake_kakao:
                btn_kakao.performClick();
                break;

            case R.id.fake_facebook:
                btn_facebook.performClick();
                break;
        }
    }

    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
            requestMe();
            //redirectMainActivity();
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            if(exception != null) {
                Logger.e(exception);
            }
        }
    }
        private void initSetting() {


        btn_naver = (OAuthLoginButton) findViewById(R.id.buttonOAuthLoginImg);
        btn_naver.setImageResource(R.drawable.naver_login);
        btn_naver.setOAuthLoginHandler(mOAuthLoginHandler);
        btn_naver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOAuthLoginModule.startOauthLoginActivity(Login.this,mOAuthLoginHandler);
            }
        });


        btn_kakao=(com.kakao.usermgmt.LoginButton) findViewById(R.id.com_kakao_login);
        btn_kakao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Session.getCurrentSession().checkAndImplicitOpen();
            }
        });
    }

    private void redirectLoginActivity(){
        Intent intent = new Intent(this,Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        finish();
    }


    private OAuthLoginHandler mOAuthLoginHandler = new OAuthLoginHandler() {
        @Override
        public void run(boolean success) {
            if (success) {
                String accessToken = mOAuthLoginModule.getAccessToken(mContext);
                String refreshToken = mOAuthLoginModule.getRefreshToken(mContext);
                long expiresAt = mOAuthLoginModule.getExpiresAt(mContext);
                String tokenType = mOAuthLoginModule.getTokenType(mContext);
                System.out.println(accessToken);

                new RequestApiTask().execute(accessToken);
                /*mOauthAT.setText(accessToken);
                mOauthRT.setText(refreshToken);
                mOauthExpires.setText(String.valueOf(expiresAt));
                mOauthTokenType.setText(tokenType);
                mOAuthState.setText(mOAuthLoginModule.getState(mContext).toString());*/
            } else {
                String errorCode = mOAuthLoginModule.getLastErrorCode(mContext).getCode();
                String errorDesc = mOAuthLoginModule.getLastErrorDesc(mContext);
                Toast.makeText(mContext, "errorCode:" + errorCode
                        + ", errorDesc:" + errorDesc, Toast.LENGTH_SHORT).show();
            }
        }
    };

    // 네이버 API 예제 - 회원프로필 조회
    private class RequestApiTask extends AsyncTask<String, String, String> {
        StringBuffer response;
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            //String token = "accessToken";// 네이버 로그인 접근 토큰;
            String header = "Bearer " + params[0]; // Bearer 다음에 공백 추가
            try {
                String apiURL = "https://openapi.naver.com/v1/nid/me";
                URL url = new URL(apiURL);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod("GET");
                con.setRequestProperty("Authorization", header);

                int responseCode = con.getResponseCode();
                BufferedReader br;
                if(responseCode==200) { // 정상 호출
                    br = new BufferedReader(new InputStreamReader(con.getInputStream(),"MS949"));
                } else {  // 에러 발생
                    br = new BufferedReader(new InputStreamReader(con.getErrorStream(),"MS949"));
                }
                String inputLine;
                response = new StringBuffer();
                //System.out.println(response);
                while ((inputLine = br.readLine()) != null) {
                    response.append(inputLine);
                }
                br.close();

            } catch (Exception e) {
                System.out.println(e);
            }
            return response.toString();
        }

        protected void onPostExecute(String content) {
            super.onPostExecute(content);
            try {
                JSONObject response = new JSONObject(content);
                JSONObject naver_mem=response.getJSONObject("response");
                naver_mem.put("type","naver");
                System.out.println(naver_mem);
                LoginTask loginTask = new LoginTask();
                loginTask.execute(naver_mem.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    public class LoginTask extends AsyncTask<String,String,String>
    {
        JSONObject loginresult;
        String result;
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //System.out.println(s);
            try {
                loginresult = new JSONObject(s);/////JSON으로 된 데이터 형식에서
                result=loginresult.getString("result");///성공했느냐
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(result.equals("true"))
            {
                String useremail;
                String username;
                ///로그인에 성공하면 로그인한 유저의 이메일과 이름을 받을 수 있음
                try {
                    useremail=loginresult.getString("useremail");
                    username=loginresult.getString("username");
                    System.out.println(useremail);

                    //Shared에 로그인한 user의 이메일을 저장 - 로그아웃시 삭제 필요
                    loginemail_editor.putString("useremail",useremail);
                    loginemail_editor.apply();

                    JSONObject Mejson = new JSONObject();
                    Mejson.put("user_friend",useremail);
                    Mejson.put("user_name",username);
                    dBhelper.friendinsert(Mejson);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Toast.makeText(Login.this, "로그인 되었습니다.", Toast.LENGTH_SHORT).show();
                //로그인이 성공하면 세션쿠키를 얻어옴
                Map<String, List<String>> headerFields = con.getHeaderFields();
                List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);
                System.out.println(cookiesHeader);
                //*전역변수로 세션쿠키 저장시 로그인이 안되거나 풀려버림
                if (cookiesHeader != null) {//쿠키매니저를 통해 쿠키를 보관한다.
                    for (String cookie : cookiesHeader) {
                        msCookieManager.getCookieStore().add(null,HttpCookie.parse(cookie).get(0));
                    }
                    //처음 로그인 시 session id를 받아옴
                    session_id= String.valueOf(msCookieManager.getCookieStore().getCookies());
                    session_editor.putString("session",session_id);
                    session_editor.commit();
                }
                startActivity(new Intent(getApplicationContext(),MainActivity.class));
                finish();
            }else{
                Toast.makeText(Login.this, "ID나 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String strUrl="http://kakapo12.vps.phps.kr/logincheck.php";

            try {
                URL url = new URL(strUrl);
                con = (HttpURLConnection) url.openConnection();//커넥션을 여는 부분
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");// 타입설정(application/json) 형식으로 전송 (Request Body 전달시 application/json로 서버에 전달.)
                con.setDoInput(true);
                con.setDoOutput(true);

                OutputStream os = con.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));

                writer.write(params[0]);

                writer.flush();
                writer.close();
                os.close();

                con.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));

                StringBuilder sb = new StringBuilder();
                String line;
                while((line = br.readLine())!=null)
                {
                    if(sb.length()>0)
                    {
                        sb.append("\n");
                    }
                    sb.append(line);
                }

                result=sb.toString();
                //결과를 보여주는 부분 서버에서 true or false
                //System.out.println(result);

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if(con!=null)
                {
                    con.disconnect();
                }
            }

            return result;
        }
    }

    //페이스북 로그인 준비
    public void facebookLoginOnClick(View v){
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().logInWithReadPermissions(Login.this,
                Arrays.asList("public_profile", "email"));
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(final LoginResult result) {

                GraphRequest request;
                request = GraphRequest.newMeRequest(result.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                    @Override
                    public void onCompleted(JSONObject user, GraphResponse response) {
                        if (response.getError() != null) {

                        } else {
                            Log.i("TAG", "user: " + user.toString());
                            Log.i("TAG", "AccessToken: " + result.getAccessToken().getToken());
                            setResult(RESULT_OK);

                            JSONObject facebook_mem = new JSONObject();

                            try {
                                facebook_mem=user;
                                facebook_mem.put("type","facebook");
                                System.out.println(facebook_mem);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            LoginTask loginTask = new LoginTask();
                            loginTask.execute(facebook_mem.toString());
                        }
                    }
                });
                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("test", "Error: " + error);
                //finish();
            }

            @Override
            public void onCancel() {
                //finish();
            }
        });
    }
}
