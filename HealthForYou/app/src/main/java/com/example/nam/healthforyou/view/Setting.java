package com.example.nam.healthforyou.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.nam.healthforyou.component.DBhelper;
import com.example.nam.healthforyou.util.InternalImageManger;
import com.example.nam.healthforyou.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


import android.Manifest;
import android.content.ComponentName;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import android.media.MediaScannerConnection;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;

import java.io.IOException;
import java.util.ArrayList;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class Setting extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG = "OPENCV";
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java3");
    }
    Context mContext;
    //SQLite
    DBhelper dBhelper;
    //Shared
    SharedPreferences session;
    SharedPreferences.Editor session_editor;

    SharedPreferences loginemail;
    SharedPreferences.Editor loginemail_editor;

    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_ALBUM = 2;
    private static final int CROP_FROM_CAMERA = 3;

    private Uri photoUri;
    private String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};
    private static final int MULTIPLE_PERMISSIONS = 101;

    private String mCurrentPhotoPath;
    ImageView iv_myprofileImage;
    ByteArrayOutputStream stream;
    String myId;

    private Uri mImageCaptureUri;
    private static final int PICK_FROM_CAMERA_2 = 4;
    private static final int PICK_FROM_ALBUM_2 = 5;
    private static final int CROP_FROM_CAMERA_2 = 6;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    //통신부분
    HttpURLConnection con;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);///뒤로가기 버튼
        mContext = getApplicationContext();
        getSupportActionBar().setTitle("설정");//Action Bar이름 지정
        stream = new ByteArrayOutputStream();
        SharedPreferences useremail = getApplicationContext().getSharedPreferences("useremail",MODE_PRIVATE);
        String loginemailid=useremail.getString("useremail","false");
        //나에 대한 정보를 SQlite에서 갖고옴
        dBhelper = new DBhelper(getApplicationContext(),"healthforyou.db", null, 1);
        JSONObject mejson = dBhelper.getFriend(loginemailid);
        myId=mejson.optString("user_friend");
        //나에 대한 정보 세팅
        TextView tv_settingName = (TextView)findViewById(R.id.tv_settingName);
        tv_settingName.setText(mejson.optString("user_name"));
        TextView tv_settingEmail = (TextView)findViewById(R.id.tv_settingEmail);
        tv_settingEmail.setText(mejson.optString("user_friend"));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            checkPermissions();//권한 체크
        }

        Button btn_logout = (Button)findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //AsyncTask를 통한 로그아웃 및 DB 초기화
                LogoutTask logoutTask = new LogoutTask();
                logoutTask.execute();
            }
        });
        //Bitmap bitmap = new InternalImageManger(mContext).setFileName(myId).setDirectoryName("PFImage").load();
        String completePath = mContext.getFilesDir().getParent()+"/"+"app_PFImage"+"/"+myId;
        System.out.println(completePath+"저장소");
        ///data/user/0/com.example.nam.healthforyou/app_PFImage/
        //File file = new File(completePath);
        Uri imageUri=null;
        File file = new InternalImageManger(mContext).setFileName(myId).setDirectoryName("PFImage").loadFile();
        if(file!=null)
        {
            imageUri = Uri.fromFile(file);
        }

        //Uri imageUri = new InternalImageManger(mContext).setFileName(myId).setDirectoryName("PFImage").load();
        //나의 프로필 이미지
        iv_myprofileImage = (ImageView)findViewById(R.id.iv_settingProfile);
        //Glide.clear(iv_myprofileImage);
        if(imageUri!=null)
        {
            Glide.with(this).
                    load(imageUri).
                    override(100,100).
                    diskCacheStrategy(DiskCacheStrategy.NONE).
                    skipMemoryCache(true).
                    into(iv_myprofileImage);
        }else{
            Glide.with(this).
                    load(R.drawable.no_profile).
                    override(100,100).
                    diskCacheStrategy(DiskCacheStrategy.NONE).
                    skipMemoryCache(true).
                    into(iv_myprofileImage);
        }


        //iv_myprofileImage.setImageBitmap(bitmap);
        iv_myprofileImage.setOnClickListener(this);
    }
    ///API 19
    private void doTakePhotoAction()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // 임시로 사용할 파일의 경로를 생성
        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));

        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        // 특정기기에서 사진을 저장못하는 문제가 있어 다음을 주석처리 합니다.
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PICK_FROM_CAMERA_2);
    }

    /**
     * 앨범에서 이미지 가져오기
     */
    private void doTakeAlbumAction()
    {
        // 앨범 호출
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_FROM_ALBUM_2);
    }


    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    private void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photoFile = null;
        try {
            photoFile = createImageFile();
        } catch (IOException e) {
            Toast.makeText(Setting.this, "이미지 처리 오류! 다시 시도해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            e.printStackTrace();
        }
        if (photoFile != null) {
            photoUri = FileProvider.getUriForFile(Setting.this,"com.example.nam.healthforyou.provider", photoFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            startActivityForResult(intent, PICK_FROM_CAMERA);
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("HHmmss").format(new Date());
        String imageFileName = "nostest_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/NOSTest/");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void goToAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class profileSaveTask extends AsyncTask<String,String,String>
    {
        String result;
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            System.out.println(s);
        }

        @Override
        protected String doInBackground(String... params) {
            String strUrl2="http://kakapo12.vps.phps.kr/uploadProfile.php";

            try {
                URL url = new URL(strUrl2);
                con = (HttpURLConnection) url.openConnection();//커넥션을 여는 부분
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");// 타입설정(application/json) 형식으로 전송 (Request Body 전달시 application/json로 서버에 전달.)
                con.setDoInput(true);
                con.setDoOutput(true);
                //쿠키매니저에 저장되어있는 세션 쿠키를 사용하여 통신

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

                //결과를 보여주는 부분 서버에서 true or false
                result = sb.toString();
                System.out.println(result+"결과");

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



    @Override//Setting에서는 로그아웃을 생각해 MainActivity를 finish 하므로 MainActivity를 다시 띄워줘야함
    public void onBackPressed() {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        super.onBackPressed();
    }

    public class LogoutTask extends AsyncTask<Void,Void,Void>
    {
        HttpURLConnection con;
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            //SQLITE DB에 있는 자료 삭제
            dBhelper.delete("delete from User_health;");//건강정보 삭제
            dBhelper.delete("delete from User_friend;");//친구목록 삭제
            dBhelper.delete("delete from ChatMessage;");//채팅 메세지 삭제
            dBhelper.delete("delete from GroupChat;");//그룹채팅방 정보 삭제

            //SharedPreference 삭제 - 모든 파일이 삭제됨
            session = getApplicationContext().getSharedPreferences("session",MODE_PRIVATE);//SharedPreference에 파일에 접근
            session_editor = session.edit();
            session_editor.remove("session");
            session_editor.apply();

            loginemail = getApplicationContext().getSharedPreferences("useremail",MODE_PRIVATE);
            loginemail_editor = loginemail.edit();
            loginemail_editor.remove("useremail");
            loginemail_editor.apply();

            //로그인 액티비티로 이동
            Intent intent = new Intent(Setting.this,Login.class);
            intent.putExtra("is_login",0);
            startActivity(intent);
            finish();
        }

        @Override
        protected Void doInBackground(Void... params) {
            String strUrl="http://kakapo12.vps.phps.kr/logoutcheck.php";

            try {
                URL url = new URL(strUrl);
                con = (HttpURLConnection) url.openConnection();//커넥션을 여는 부분
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");// 타입설정(application/json) 형식으로 전송 (Request Body 전달시 application/json로 서버에 전달.)
                con.setDoInput(true);
                con.setDoOutput(true);

                if (Login.msCookieManager.getCookieStore().getCookies().size() > 0) {
                    // While joining the Cookies, use ',' or ';' as needed. Most of the servers are using ';'
                    con.setRequestProperty("Cookie", TextUtils.join(",", Login.msCookieManager.getCookieStore().getCookies()));
                    System.out.println(Login.msCookieManager.getCookieStore().getCookies()+"Request");
                }

                OutputStream os = con.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));

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
            return null;
        }
    }//Async End

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode != RESULT_OK) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        switch(requestCode)
        {
            case PICK_FROM_ALBUM:
            {
                if (data == null) {
                    return;
                }
                photoUri = data.getData();
                cropImage();

                break;
            }

            case PICK_FROM_CAMERA:
            {
                cropImage();
                // 갤러리에 나타나게
                MediaScannerConnection.scanFile(Setting.this,
                        new String[]{photoUri.getPath()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                            }
                        });
            }

            case CROP_FROM_CAMERA:
            {
                iv_myprofileImage.setImageURI(null);
                Bitmap bitmap=null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    Mat sourceImage = new Mat();
                    Utils.bitmapToMat(bitmap, sourceImage);

                    Size sz = new Size(960,720);
                    Mat resizeimage = new Mat(sz,CvType.CV_8U);
                    Imgproc.resize(sourceImage,resizeimage,sz);
                    bitmap = Bitmap.createBitmap(resizeimage.cols(), resizeimage.rows(), Bitmap.Config.RGB_565);//리사이즈 한 이미지를 bitmap에 덮어 씌워줌
                    Utils.matToBitmap(resizeimage,bitmap);//비트맵으로 전환 완료

                } catch (IOException e) {
                    e.printStackTrace();
                }

                int bitmapWidth=bitmap.getWidth();
                int bitmapHeight=bitmap.getHeight();

                Glide.with(this).
                      load(photoUri).
                      asBitmap().
                      diskCacheStrategy(DiskCacheStrategy.NONE).//이부분을 추가해주지 않으면 DiskCacheStrage에 저장해놔서 계속 그대로
                      skipMemoryCache(true).//이미지를 유지하기때문에 Glide에 이미지가 바뀌지 않음
                      override(bitmapWidth/4,bitmapHeight/4).
                      transform(new RoundedCornersTransformation(mContext,10,10)).
                      into(iv_myprofileImage);//Glide를 통해 이미지뷰에 올림

                /////나의 프로필에 대한 이미지를 InternalStorage에 저장

                new InternalImageManger(mContext).setFileName(myId).setDirectoryName("PFImage").save(bitmap);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);//이미지를 stream으로 옮김
                byte[] byteArrayForupload = stream.toByteArray();//스트림을 통해 bytearray로 만들고
                String base64Image = Base64.encodeToString(byteArrayForupload,Base64.DEFAULT);//Base64로 Encode

                ///////byteArray로 보내는 이유는 Bitmap은 이미 메모리에 올라가 있으므로 Array로 바꿀시 접근이 더 용이
                //////file로 보낼 시 file로 생성 후 보내야 되는 시간 필요

                //업데이트 된 내용에 대한 upload 요청
                JSONObject uploadprofile = new JSONObject();

                long now = System.currentTimeMillis();
                // 현재시간을 date 변수에 저장한다.
                Date date = new Date(now);
                // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                // nowDate 변수에 값을 저장한다.
                final String formatDate = sdfNow.format(date);

                profileSaveTask profileSaveTask = new profileSaveTask();///네트워크 부분 AsyncTask 로 기록 후 측정내역으로 넘어감

                try {
                    uploadprofile.put("profile",base64Image);
                    uploadprofile.put("update",formatDate);
                    uploadprofile.put("myId",myId);
                    System.out.println(uploadprofile);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                /////나의 프로필을 서버에 업로드 - MariaDB - HTTP -PHP -MariaDB
                profileSaveTask.execute(uploadprofile.toString());

                /////나의 프로필이 바뀐 날짜, 이미지의 경로를 DB에 저장 - SQlite
                dBhelper.updateProfile(myId+"_Image",formatDate,myId);
                break;
            }

            case CROP_FROM_CAMERA_2:
            {
                // 크롭이 된 이후의 이미지를 넘겨 받습니다.
                // 이미지뷰에 이미지를 보여준다거나 부가적인 작업 이후에
                // 임시 파일을 삭제합니다.
                final Bundle extras = data.getExtras();

                if(extras != null)
                {
                    Bitmap photo = extras.getParcelable("data");

                    try{
                        Mat sourceImage = new Mat();
                        Utils.bitmapToMat(photo, sourceImage);
                        Mat resizeimage = new Mat();
                        Size sz = new Size(960,720);
                        Imgproc.resize( sourceImage, resizeimage, sz );
                        photo = Bitmap.createBitmap(resizeimage.cols(), resizeimage.rows(), Bitmap.Config.RGB_565);//리사이즈 한 이미지를 bitmap에 덮어 씌워줌
                        Utils.matToBitmap(resizeimage,photo);//비트맵으로 전환 완료
                        photo.compress(Bitmap.CompressFormat.JPEG, 90, stream);//이미지를 JPEG 형식으로 압축
                    }catch(NullPointerException e)
                    {
                        Toast.makeText(mContext,"프로필 사진업로드 실패",Toast.LENGTH_SHORT).show();
                    }
                    int bitmapWidth=photo.getWidth();
                    int bitmapHeight=photo.getHeight();

                    byte[] byteArray = stream.toByteArray();//스트림을 통해 bytearray로 만들고

                    /////나의 프로필에 대한 이미지를 InternalStorage에 저장
                    new InternalImageManger(mContext).setFileName(myId).setDirectoryName("PFImage").save(photo);

                    String completePath = mContext.getFilesDir().getParent()+"/"+"app_PFImage"+"/"+myId;
                    System.out.println(completePath+"저장소");
                    //"/data/user/0/com.example.nam.healthforyou/app_PFImage/"
                    File file = new File(completePath);
                    Uri imageUri = Uri.fromFile(file);

                    Glide.with(this).
                            load(imageUri).
                            override(bitmapWidth/4,bitmapHeight/4).
                            diskCacheStrategy(DiskCacheStrategy.NONE).
                            skipMemoryCache(true).
                            into(iv_myprofileImage);//Glide를 통해 이미지뷰에 올림

                    String base64Image = Base64.encodeToString(byteArray,Base64.DEFAULT);

                    ///////byteArray로 보내는 이유는 Bitmap은 이미 메모리에 올라가 있으므로 Array로 바꿀시 접근이 더 용이
                    //////file로 보낼 시 file로 생성 후 보내야 되는 시간 필요

                    //업데이트 된 내용에 대한 upload 요청
                    JSONObject uploadprofile = new JSONObject();

                    long now = System.currentTimeMillis();
                    // 현재시간을 date 변수에 저장한다.
                    Date date = new Date(now);
                    // 시간을 나타냇 포맷을 정한다 ( yyyy/MM/dd 같은 형태로 변형 가능 )
                    SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    // nowDate 변수에 값을 저장한다.
                    final String formatDate = sdfNow.format(date);

                    profileSaveTask profileSaveTask = new profileSaveTask();///네트워크 부분 AsyncTask 로 기록 후 측정내역으로 넘어감

                    try {
                        uploadprofile.put("profile",base64Image);
                        uploadprofile.put("update",formatDate);
                        uploadprofile.put("myId",myId);
                        System.out.println(uploadprofile);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    /////나의 프로필을 서버에 업로드 - MariaDB - HTTP -PHP -MariaDB
                    profileSaveTask.execute(uploadprofile.toString());

                    /////나의 프로필이 바뀐 날짜, 이미지의 경로를 DB에 저장 - SQlite
                    dBhelper.updateProfile(myId+"_Image",formatDate,myId);
                }

                // 임시 파일 삭제
                if(mImageCaptureUri.getPath()!=null)
                {
                    File f = new File(mImageCaptureUri.getPath());
                    if(f.exists())
                    {
                        f.delete();
                    }
                }


                break;
            }

            case PICK_FROM_ALBUM_2:
            {
                mImageCaptureUri = data.getData();
            }

            case PICK_FROM_CAMERA_2:
            {
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mImageCaptureUri, "image");
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, CROP_FROM_CAMERA_2);

                break;
            }
        }
    }

    @Override
    public void onClick(View v)
    {
        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)//Android 버전에 따라 다르게 구현
                {
                    takePhoto();
                }else{
                    doTakePhotoAction();
                }

            }
        };

        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                {
                    goToAlbum();
                }else{
                    doTakeAlbumAction();
                }
            }
        };

        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        };

        new AlertDialog.Builder(this)
                .setTitle("업로드할 이미지 선택")
                .setNeutralButton("카메라", cameraListener)
                .setNegativeButton("앨범", albumListener)
                .setPositiveButton("취소", cancelListener)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[0])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();
                            }
                        } else if (permissions[i].equals(this.permissions[1])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();

                            }
                        } else if (permissions[i].equals(this.permissions[2])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showNoPermissionToastAndFinish();

                            }
                        }
                    }
                } else {
                    showNoPermissionToastAndFinish();
                }
            }
        }
    }

    private void showNoPermissionToastAndFinish() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

    //Android N crop image
    public void cropImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.grantUriPermission("com.android.camera", photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoUri, "image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            grantUriPermission(list.get(0).activityInfo.packageName, photoUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Toast.makeText(this, "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            intent.putExtra("crop", "true");
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", true);
            File croppedFileName = null;
            try {
                croppedFileName = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            File folder = new File(Environment.getExternalStorageDirectory() + "/NOSTest/");
            File tempFile = new File(folder.toString(), croppedFileName.getName());

            photoUri = FileProvider.getUriForFile(Setting.this,
                    "com.example.nam.healthforyou.provider", tempFile);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }

            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

            Intent i = new Intent(intent);
            ResolveInfo res = list.get(0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                grantUriPermission(res.activityInfo.packageName, photoUri,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(i, CROP_FROM_CAMERA);
        }
    }

    public Bitmap resizeBitmap(String photoPath, int targetW, int targetH) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true; //Deprecated API 21

        return BitmapFactory.decodeFile(photoPath, bmOptions);
    }

    public static void loadImage(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.no_profile)
                .centerCrop()
                .crossFade()
                .into(imageView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "onResume :: Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0,this, mLoaderCallback);
        } else {
            Log.d(TAG, "onResume :: OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
