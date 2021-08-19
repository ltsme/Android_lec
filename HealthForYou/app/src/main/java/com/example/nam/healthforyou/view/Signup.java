package com.example.nam.healthforyou.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nam.healthforyou.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Signup extends AppCompatActivity {

    private Context mContext;

    private EditText input_id;
    private EditText input_pw;
    private EditText input_confirmpw;
    private EditText input_name;

    private Spinner input_sex;
    private Spinner input_age;
    private ImageView check;

    private String id;
    private String confirmid;
    private String pw;
    private String name;
    private String sex;
    private String age;
    private String result;
    private Boolean is_overlap=null;
    JSONObject signup_mem;
    HttpURLConnection con;



    //동의를 구하는 부분에서 사용할 다이얼로그
    AlertDialog.Builder builder;
    AlertDialog alertDialog;
    Boolean is_accepted;
    TextView acceptrule;
    /**
     * 이메일 포맷 체크
     * @param email
     * @return
     */
    public static boolean checkEmail(String email){
        //아이디는 4자리에서 15자리;;세부도메인은 기준없음-;;일반 최상위 도메인은 2부터 13자리
        //최상위 도메인 선정 기준 2017.7.23 기준 최소 2자리 최대 24자리 - 평균 13자리
        String regex = "^[_a-zA-Z0-9-\\.]+@[\\.a-zA-Z0-9-]+\\.[a-zA-Z]{2,13}+$";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(email);
        boolean isNormal = m.matches();
        return isNormal;
    }

    public static boolean checkPassword(String password)
    {   //12자리에서 20자리까지 검사 특수문자,영어,숫자
        String regex = "^(?=.*[a-zA-Z])((?=.*\\d))((?=.*\\W)).{12,20}$";
        //문자, 숫자, 특수문자의 조합인지 확인
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(password);
        boolean isOk= m.matches();
        return isOk;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mContext = this;
        //회원정보의 입력을 받는 위젯 객체
        input_id=(EditText)findViewById(R.id.input_id);
        input_pw=(EditText)findViewById(R.id.input_pw);
        input_confirmpw=(EditText)findViewById(R.id.input_confirmpw);
        input_name=(EditText)findViewById(R.id.input_name);

        input_sex=(Spinner)findViewById(R.id.input_sex);
        input_age=(Spinner)findViewById(R.id.input_age);

        is_accepted = null;
        acceptrule =(TextView)findViewById(R.id.isacceptrule);///동의시 사라지는 메시지
        //동의부분 다이얼로그 정의
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.custom_dialog,null);

        Button accept_btn = (Button)layout.findViewById(R.id.accept);
        accept_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_accepted=true;
                alertDialog.dismiss();
                acceptrule.setText("모든 정보를 입력하셨다면 확인버튼을 눌러주세요");
            }
        });
        Button notaccept_btn = (Button)layout.findViewById(R.id.notaccept);
        notaccept_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_accepted=false;
                alertDialog.dismiss();
                acceptrule.setText("이용 약관을 동의를 해야 회원가입을 완료할 수 있습니다");
            }
        });
        TextView ruleforuser1 = (TextView)layout.findViewById(R.id.ruleforuse1);
        TextView ruleforuser2 = (TextView)layout.findViewById(R.id.ruleforuse2);
        TextView ruleforuser3 = (TextView)layout.findViewById(R.id.ruleforuse3);

        builder = new AlertDialog.Builder(mContext);
        builder.setTitle("이용 약관");
        builder.setView(layout);
        alertDialog = builder.create();

        //비밀번호 체크 이미지
        check=(ImageView)findViewById(R.id.check);
        check.setVisibility(View.INVISIBLE);
        final String false_message="*12-20자의 영문,숫자,특수문자의 조합이여야 합니다";
        final TextView tv_pwmessage = (TextView)findViewById(R.id.password_message);
        //비밀번호 정규식 검사
        input_pw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                tv_pwmessage.setText("*영문,숫자,특수문자의 조합이여야 합니다");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String pw = input_pw.getText().toString();
                if(checkPassword(pw))//정규식 조건에 일치
                {
                    tv_pwmessage.setText("*적절한 비밀번호 입니다.");
                }else{
                    tv_pwmessage.setText(false_message);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        //비밀번호 확인에 대한 검사
        input_confirmpw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String pw=input_pw.getText().toString();
                    String confirmpw =input_confirmpw.getText().toString();
                    if(pw.equals(confirmpw)&&checkPassword(pw))//비밀번호와 비밀번호 확인 같은지 비교 + 비밀번호 정규식 체크
                    {
                        check.setVisibility(View.VISIBLE);//확인 그림을 보여줌
                        check.setImageResource(R.drawable.check2);
                    }else{
                        check.setVisibility(View.VISIBLE);//엑스 그림을 보여줌
                        check.setImageResource(R.drawable.cancel2);
                    }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        Button ruleforuse = (Button)findViewById(R.id.ruleforuse);
        ruleforuse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.show();
            }
        });

        Button id_confirm =(Button)findViewById(R.id.id_confirm);
        id_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//중복확인 버튼을 눌렀을 시 AsyncTask를 통해 중복을 DB로 부터 확인함
                if(!checkEmail(input_id.getText().toString()))///중복확인 시에도 형식체크-이메일 형식이 아니면
                {
                    Toast.makeText(Signup.this,"이메일 형식에 맞게 입력해주세요",Toast.LENGTH_SHORT).show();
                    input_id.requestFocus();
                    return;//return으로 다음 단계로 못넘어가게 함
                }
                idconfirmTask idconfirmtask = new idconfirmTask();
                idconfirmtask.execute(input_id.getText().toString());

            }
        });
        Button cancel_signup=(Button)findViewById(R.id.cancel_signup);
        cancel_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Button complete_signup=(Button)findViewById(R.id.complete_signup);
        complete_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //입력들 비어 있는지 확인작업
                if(input_id.getText().toString().length()==0)
                {
                    Toast.makeText(Signup.this,"ID를 입력해주세요!",Toast.LENGTH_SHORT).show();
                    input_id.requestFocus();
                    return;//return으로 다음 단계로 못넘어가게 함
                }

                //아이디 중복확인 했는지 체크
                if(is_overlap!=null)
                {
                    if(is_overlap)//중복이 되었으면
                    {
                        Toast.makeText(Signup.this,"중복된 ID를 입력하셨습니다",Toast.LENGTH_SHORT).show();
                        input_id.requestFocus();
                        is_overlap=null;
                        return;
                    }else{//중복된 아이디가 아니면

                    }
                }else{//중복확인을 안했을때
                    Toast.makeText(Signup.this,"ID 중복확인이 필요합니다!",Toast.LENGTH_SHORT).show();
                    input_id.requestFocus();
                    return;
                }

                //확인된 아이디로 가입하려고 하는지 검사
                if(!input_id.getText().toString().equals(confirmid))///중복확인 된 아이디가 아니면
                {
                    Toast.makeText(Signup.this,"ID 중복확인이 필요합니다!",Toast.LENGTH_SHORT).show();
                    input_id.requestFocus();
                    return;
                }

                if(input_pw.getText().toString().length()==0)
                {
                    Toast.makeText(Signup.this,"비밀번호를 입력해주세요!",Toast.LENGTH_SHORT).show();
                    input_pw.requestFocus();
                    return;//return으로 다음 단계로 못넘어가게 함
                }

                if(input_confirmpw.getText().toString().length()==0)
                {
                    Toast.makeText(Signup.this,"비밀번호 확인을 입력해주세요!",Toast.LENGTH_SHORT).show();
                    input_confirmpw.requestFocus();
                    return;//return으로 다음 단계로 못넘어가게 함
                }

                if(input_name.getText().toString().length()==0)
                {
                    Toast.makeText(Signup.this,"이름을 입력해주세요!",Toast.LENGTH_SHORT).show();
                    input_name.requestFocus();
                    return;//return으로 다음 단계로 못넘어가게 함
                }

                //이메일 주소 유효성 검사
                String email_check=input_id.getText().toString();
                boolean isEmail=checkEmail(email_check);
                if(isEmail)
                {

                }else{
                    Toast.makeText(Signup.this,"올바른 ID 형식이 아닙니다.",Toast.LENGTH_SHORT).show();
                    input_id.requestFocus();
                    return;
                }

                //비밀번호 유효성 검사
                String password = input_pw.getText().toString();
                boolean isSafe=checkPassword(password);
                //유효성 검사 한번더함 저장할때
                if(password.length()>=12)
                {
                    if(isSafe)
                    {

                    }else{
                        Toast.makeText(Signup.this,"비밀번호는 영문,숫자,특수문자로 이루어져있어야 합니다",Toast.LENGTH_SHORT).show();
                        input_pw.requestFocus();
                        return;
                    }
                }else{
                    Toast.makeText(Signup.this,"비밀번호는 12자리 이상 입력하셔야됩니다",Toast.LENGTH_SHORT).show();
                    input_pw.requestFocus();
                    return;
                }


                if( !input_pw.getText().toString().equals(input_confirmpw.getText().toString()) ) {
                    Toast.makeText(Signup.this, "비밀번호가 일치하지 않습니다!", Toast.LENGTH_SHORT).show();
                    check.setVisibility(View.INVISIBLE);//엑스 그림을 보여줌
                    input_pw.setText("");
                    input_confirmpw.setText("");
                    input_pw.requestFocus();
                    return;
                }
                //이용 약관에 동의했는지 판단
                if(is_accepted==null || !is_accepted)///null이거나 false일 경우 알림
                {
                    Toast.makeText(Signup.this, "이용 약관을 읽고 동의 버튼을 눌러주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                //EditText에 들어 있는 값을 받아옴
                id=input_id.getText().toString();
                name=input_name.getText().toString();
                pw=input_pw.getText().toString();

                //Spinner의 값을 받아옴
                //*성별
                if(input_sex.getSelectedItem().toString().equals("남성"))
                {
                    sex="male";
                }else{
                    sex="female";
                }
                //**나이
                age=input_age.getSelectedItem().toString();

                signup_mem=new JSONObject();
                try {
                    signup_mem.put("id",id);
                    signup_mem.put("pw",pw);
                    signup_mem.put("name",name);
                    signup_mem.put("age",age);
                    signup_mem.put("sex",sex);
                    System.out.println(signup_mem);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                signupTask signtask =new signupTask();
                signtask.execute();
            }
        });
        //*주의사항 입력폼 체크 해야됨
    }





    public class signupTask extends AsyncTask<String,String,String>
    {
        @Override
        protected void onPostExecute(String s) {//회원정보를 DB에 저장하고 나면
            super.onPostExecute(s);

            if(s.equals("true"))//DB에 저장이 되었을 때
            {
                System.out.println(s);
                Toast.makeText(Signup.this, "회원가입 완료", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(),Login.class));
                finish();
            }else{//DB에 저장이 안되었을 시(서버가 꺼져 있거나,와이파이가 꺼져있을 때)
                Toast.makeText(Signup.this, "재시도 요망", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String strUrl="http://kakapo12.vps.phps.kr/signup.php";

            try {
                URL url = new URL(strUrl);
                con = (HttpURLConnection) url.openConnection();//커넥션을 여는 부분
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");// 타입설정(application/json) 형식으로 전송 (Request Body 전달시 application/json로 서버에 전달.)
                con.setDoInput(true);
                con.setDoOutput(true);

                OutputStream os = con.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));

                writer.write(signup_mem.toString());

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
                System.out.println(result);
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

    public class idconfirmTask extends AsyncTask<String,String,String>
    {
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            if(s.equals("1"))//아이디 중복체크 시 1이면 중복
            {
                Toast.makeText(Signup.this, "중복된 ID 입니다", Toast.LENGTH_SHORT).show();
                input_id.requestFocus();
                is_overlap=true;//중복이면 overlap이 1로 set

            }else{//아이디 중복체크 시 0이면 중복이 아님
                Toast.makeText(Signup.this, "사용할 수 있는 ID입니다", Toast.LENGTH_SHORT).show();
                confirmid=input_id.getText().toString();///중복체크된 아이디
                is_overlap=false;//중복이 아니면 0으로 set
            }//원래 is_overlap은 null값
        }

        @Override
        protected String doInBackground(String... params) {
            String strUrl="http://kakapo12.vps.phps.kr/idconfirm.php";

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
                System.out.println(result);
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


}
