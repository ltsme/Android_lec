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

    //????????????
    HttpURLConnection con;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);///???????????? ??????
        mContext = getApplicationContext();
        getSupportActionBar().setTitle("??????");//Action Bar?????? ??????
        stream = new ByteArrayOutputStream();
        SharedPreferences useremail = getApplicationContext().getSharedPreferences("useremail",MODE_PRIVATE);
        String loginemailid=useremail.getString("useremail","false");
        //?????? ?????? ????????? SQlite?????? ?????????
        dBhelper = new DBhelper(getApplicationContext(),"healthforyou.db", null, 1);
        JSONObject mejson = dBhelper.getFriend(loginemailid);
        myId=mejson.optString("user_friend");
        //?????? ?????? ?????? ??????
        TextView tv_settingName = (TextView)findViewById(R.id.tv_settingName);
        tv_settingName.setText(mejson.optString("user_name"));
        TextView tv_settingEmail = (TextView)findViewById(R.id.tv_settingEmail);
        tv_settingEmail.setText(mejson.optString("user_friend"));
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            checkPermissions();//?????? ??????
        }

        Button btn_logout = (Button)findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //AsyncTask??? ?????? ???????????? ??? DB ?????????
                LogoutTask logoutTask = new LogoutTask();
                logoutTask.execute();
            }
        });
        //Bitmap bitmap = new InternalImageManger(mContext).setFileName(myId).setDirectoryName("PFImage").load();
        String completePath = mContext.getFilesDir().getParent()+"/"+"app_PFImage"+"/"+myId;
        System.out.println(completePath+"?????????");
        ///data/user/0/com.example.nam.healthforyou/app_PFImage/
        //File file = new File(completePath);
        Uri imageUri=null;
        File file = new InternalImageManger(mContext).setFileName(myId).setDirectoryName("PFImage").loadFile();
        if(file!=null)
        {
            imageUri = Uri.fromFile(file);
        }

        //Uri imageUri = new InternalImageManger(mContext).setFileName(myId).setDirectoryName("PFImage").load();
        //?????? ????????? ?????????
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

        // ????????? ????????? ????????? ????????? ??????
        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));

        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        // ?????????????????? ????????? ??????????????? ????????? ?????? ????????? ???????????? ?????????.
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PICK_FROM_CAMERA_2);
    }

    /**
     * ???????????? ????????? ????????????
     */
    private void doTakeAlbumAction()
    {
        // ?????? ??????
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
            Toast.makeText(Setting.this, "????????? ?????? ??????! ?????? ??????????????????.", Toast.LENGTH_SHORT).show();
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
                con = (HttpURLConnection) url.openConnection();//???????????? ?????? ??????
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");// ????????????(application/json) ???????????? ?????? (Request Body ????????? application/json??? ????????? ??????.)
                con.setDoInput(true);
                con.setDoOutput(true);
                //?????????????????? ?????????????????? ?????? ????????? ???????????? ??????

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

                //????????? ???????????? ?????? ???????????? true or false
                result = sb.toString();
                System.out.println(result+"??????");

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



    @Override//Setting????????? ??????????????? ????????? MainActivity??? finish ????????? MainActivity??? ?????? ???????????????
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

            //SQLITE DB??? ?????? ?????? ??????
            dBhelper.delete("delete from User_health;");//???????????? ??????
            dBhelper.delete("delete from User_friend;");//???????????? ??????
            dBhelper.delete("delete from ChatMessage;");//?????? ????????? ??????
            dBhelper.delete("delete from GroupChat;");//??????????????? ?????? ??????

            //SharedPreference ?????? - ?????? ????????? ?????????
            session = getApplicationContext().getSharedPreferences("session",MODE_PRIVATE);//SharedPreference??? ????????? ??????
            session_editor = session.edit();
            session_editor.remove("session");
            session_editor.apply();

            loginemail = getApplicationContext().getSharedPreferences("useremail",MODE_PRIVATE);
            loginemail_editor = loginemail.edit();
            loginemail_editor.remove("useremail");
            loginemail_editor.apply();

            //????????? ??????????????? ??????
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
                con = (HttpURLConnection) url.openConnection();//???????????? ?????? ??????
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");// ????????????(application/json) ???????????? ?????? (Request Body ????????? application/json??? ????????? ??????.)
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

                //????????? ???????????? ?????? ???????????? true or false
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
            Toast.makeText(this, "?????? ???????????????.", Toast.LENGTH_SHORT).show();
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
                // ???????????? ????????????
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
                    bitmap = Bitmap.createBitmap(resizeimage.cols(), resizeimage.rows(), Bitmap.Config.RGB_565);//???????????? ??? ???????????? bitmap??? ?????? ?????????
                    Utils.matToBitmap(resizeimage,bitmap);//??????????????? ?????? ??????

                } catch (IOException e) {
                    e.printStackTrace();
                }

                int bitmapWidth=bitmap.getWidth();
                int bitmapHeight=bitmap.getHeight();

                Glide.with(this).
                      load(photoUri).
                      asBitmap().
                      diskCacheStrategy(DiskCacheStrategy.NONE).//???????????? ??????????????? ????????? DiskCacheStrage??? ??????????????? ?????? ?????????
                      skipMemoryCache(true).//???????????? ????????????????????? Glide??? ???????????? ????????? ??????
                      override(bitmapWidth/4,bitmapHeight/4).
                      transform(new RoundedCornersTransformation(mContext,10,10)).
                      into(iv_myprofileImage);//Glide??? ?????? ??????????????? ??????

                /////?????? ???????????? ?????? ???????????? InternalStorage??? ??????

                new InternalImageManger(mContext).setFileName(myId).setDirectoryName("PFImage").save(bitmap);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);//???????????? stream?????? ??????
                byte[] byteArrayForupload = stream.toByteArray();//???????????? ?????? bytearray??? ?????????
                String base64Image = Base64.encodeToString(byteArrayForupload,Base64.DEFAULT);//Base64??? Encode

                ///////byteArray??? ????????? ????????? Bitmap??? ?????? ???????????? ????????? ???????????? Array??? ????????? ????????? ??? ??????
                //////file??? ?????? ??? file??? ?????? ??? ????????? ?????? ?????? ??????

                //???????????? ??? ????????? ?????? upload ??????
                JSONObject uploadprofile = new JSONObject();

                long now = System.currentTimeMillis();
                // ??????????????? date ????????? ????????????.
                Date date = new Date(now);
                // ????????? ????????? ????????? ????????? ( yyyy/MM/dd ?????? ????????? ?????? ?????? )
                SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                // nowDate ????????? ?????? ????????????.
                final String formatDate = sdfNow.format(date);

                profileSaveTask profileSaveTask = new profileSaveTask();///???????????? ?????? AsyncTask ??? ?????? ??? ?????????????????? ?????????

                try {
                    uploadprofile.put("profile",base64Image);
                    uploadprofile.put("update",formatDate);
                    uploadprofile.put("myId",myId);
                    System.out.println(uploadprofile);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                /////?????? ???????????? ????????? ????????? - MariaDB - HTTP -PHP -MariaDB
                profileSaveTask.execute(uploadprofile.toString());

                /////?????? ???????????? ?????? ??????, ???????????? ????????? DB??? ?????? - SQlite
                dBhelper.updateProfile(myId+"_Image",formatDate,myId);
                break;
            }

            case CROP_FROM_CAMERA_2:
            {
                // ????????? ??? ????????? ???????????? ?????? ????????????.
                // ??????????????? ???????????? ?????????????????? ???????????? ?????? ?????????
                // ?????? ????????? ???????????????.
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
                        photo = Bitmap.createBitmap(resizeimage.cols(), resizeimage.rows(), Bitmap.Config.RGB_565);//???????????? ??? ???????????? bitmap??? ?????? ?????????
                        Utils.matToBitmap(resizeimage,photo);//??????????????? ?????? ??????
                        photo.compress(Bitmap.CompressFormat.JPEG, 90, stream);//???????????? JPEG ???????????? ??????
                    }catch(NullPointerException e)
                    {
                        Toast.makeText(mContext,"????????? ??????????????? ??????",Toast.LENGTH_SHORT).show();
                    }
                    int bitmapWidth=photo.getWidth();
                    int bitmapHeight=photo.getHeight();

                    byte[] byteArray = stream.toByteArray();//???????????? ?????? bytearray??? ?????????

                    /////?????? ???????????? ?????? ???????????? InternalStorage??? ??????
                    new InternalImageManger(mContext).setFileName(myId).setDirectoryName("PFImage").save(photo);

                    String completePath = mContext.getFilesDir().getParent()+"/"+"app_PFImage"+"/"+myId;
                    System.out.println(completePath+"?????????");
                    //"/data/user/0/com.example.nam.healthforyou/app_PFImage/"
                    File file = new File(completePath);
                    Uri imageUri = Uri.fromFile(file);

                    Glide.with(this).
                            load(imageUri).
                            override(bitmapWidth/4,bitmapHeight/4).
                            diskCacheStrategy(DiskCacheStrategy.NONE).
                            skipMemoryCache(true).
                            into(iv_myprofileImage);//Glide??? ?????? ??????????????? ??????

                    String base64Image = Base64.encodeToString(byteArray,Base64.DEFAULT);

                    ///////byteArray??? ????????? ????????? Bitmap??? ?????? ???????????? ????????? ???????????? Array??? ????????? ????????? ??? ??????
                    //////file??? ?????? ??? file??? ?????? ??? ????????? ?????? ?????? ??????

                    //???????????? ??? ????????? ?????? upload ??????
                    JSONObject uploadprofile = new JSONObject();

                    long now = System.currentTimeMillis();
                    // ??????????????? date ????????? ????????????.
                    Date date = new Date(now);
                    // ????????? ????????? ????????? ????????? ( yyyy/MM/dd ?????? ????????? ?????? ?????? )
                    SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    // nowDate ????????? ?????? ????????????.
                    final String formatDate = sdfNow.format(date);

                    profileSaveTask profileSaveTask = new profileSaveTask();///???????????? ?????? AsyncTask ??? ?????? ??? ?????????????????? ?????????

                    try {
                        uploadprofile.put("profile",base64Image);
                        uploadprofile.put("update",formatDate);
                        uploadprofile.put("myId",myId);
                        System.out.println(uploadprofile);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    /////?????? ???????????? ????????? ????????? - MariaDB - HTTP -PHP -MariaDB
                    profileSaveTask.execute(uploadprofile.toString());

                    /////?????? ???????????? ?????? ??????, ???????????? ????????? DB??? ?????? - SQlite
                    dBhelper.updateProfile(myId+"_Image",formatDate,myId);
                }

                // ?????? ?????? ??????
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
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)//Android ????????? ?????? ????????? ??????
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
                .setTitle("???????????? ????????? ??????")
                .setNeutralButton("?????????", cameraListener)
                .setNegativeButton("??????", albumListener)
                .setPositiveButton("??????", cancelListener)
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
        Toast.makeText(this, "?????? ????????? ?????? ???????????? ?????? ???????????????. ???????????? ?????? ?????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, "?????? ???????????????.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Toast.makeText(this, "????????? ??? ????????? ?????? ????????? ?????? ?????? ??? ????????????.", Toast.LENGTH_SHORT).show();

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
