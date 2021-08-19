package com.example.nam.healthforyou.util;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

/**
 * Created by asamajda on 07-Feb-16.
 */
public class javaViewCameraControl extends JavaCameraView {
    //Copied this constructor from OpenCV tutorial 3
    public javaViewCameraControl(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void turnFlashOn(){
        Camera.Parameters params = mCamera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(params);
    }

    public void turnFlashOff(){
        Camera.Parameters params = mCamera.getParameters();
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(params);
    }

    public void setFrameRate(int min,int max){
        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewFpsRange(min*1000, max*1000);
        mCamera.setParameters(params);
    }
}
