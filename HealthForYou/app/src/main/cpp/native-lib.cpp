#include <jni.h>
#include <string>
#include <iostream>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>




using namespace std;
using namespace cv;

extern "C"{
    JNIEXPORT jint JNICALL
    Java_com_example_nam_healthforyou_view_Fragment_1meas_redDetection(JNIEnv *env, jobject instance,jlong matAddrInput,jlong matAddrResult) {

        int sum2=0;
        Mat &matInput = *(Mat *) matAddrInput;
        Mat &matResult = * (Mat *)matAddrResult;
        
        cvtColor(matInput,matResult,CV_RGB2BGR);
        for(int i=0;i<matResult.rows;i++)
        {
            for(int j=0;j<matResult.cols;j++)
            {
                sum2+=matInput.at<cv::Vec3b>(i,j)[2];//Red
                //sum+=matInput.at<cv::Vec3b>(i,j)[1];//Green
                //sum1+=matInput.at<cv::Vec3b>(i,j)[0];//Blue
            }
        }
        // TODO

        //Test 하는 부분 Fatal signal 에러가 발생하는 이유가 Frame에 대해서 메모리를 할당(assign)했다가 해제(release)하지않아서 그런것은 아닐까?
        return sum2;//
    }
}

extern "C"{
JNIEXPORT jint JNICALL
Java_com_example_nam_healthforyou_view_Fragment_1meas_blueDetection(JNIEnv *env, jobject instance,jlong matAddrInput,jlong matAddrResult) {

    int sum1=0;
    Mat &matInput = *(Mat *) matAddrInput;
    Mat &matResult = * (Mat *)matAddrResult;

    cvtColor(matInput,matResult,CV_RGB2BGR);
    for(int i=0;i<matResult.rows;i++)
    {
        for(int j=0;j<matResult.cols;j++)
        {
            //sum1+=matResult.at<cv::Vec3b>(i,j)[2];//Red
            //sum+=matInput.at<cv::Vec3b>(i,j)[1];//Green
            sum1+=matInput.at<cv::Vec3b>(i,j)[0];//Blue
        }
    }
    // TODO
    //Test 하는 부분 Fatal signal 에러가 발생하는 이유가 Frame에 대해서 메모리를 할당(assign)했다가 해제(release)하지않아서 그런것은 아닐까?
    matInput.release();
    matResult.release();
    return sum1;//
}
}

extern "C"{
JNIEXPORT jint JNICALL
Java_com_example_nam_healthforyou_view_Fragment_1meas_greenDetection(JNIEnv *env, jobject instance,jlong matAddrInput,jlong matAddrResult) {

    int sum=0;
    Mat &matInput = *(Mat *) matAddrInput;
    Mat &matResult = * (Mat *)matAddrResult;

    cvtColor(matInput,matResult,CV_RGB2BGR);
    for(int i=0;i<matResult.rows;i++)
    {
        for(int j=0;j<matResult.cols;j++)
        {
            //sum+=matInput.at<cv::Vec3b>(i,j)[2];//Red
            sum+=matInput.at<cv::Vec3b>(i,j)[1];//Green - //TODO GreenComponent가 맞나?
            //sum+=matInput.at<cv::Vec3b>(i,j)[0];//Blue
        }
    }
    // TODO
    //Test 하는 부분 Fatal signal 에러가 발생하는 이유가 Frame에 대해서 메모리를 할당(assign)했다가 해제(release)하지않아서 그런것은 아닐까?

    return sum;// TODO
}
}

extern "C"{
    JNIEXPORT jint JNICALL
    Java_com_example_nam_healthforyou_view_Fragment_1meas_moveDetection(JNIEnv *env, jobject instance, jlong previous, jlong current) {

        Mat &previous_frame = *(Mat *) previous;
        Mat &current_frame = *(Mat *) current;
        Mat differ = previous_frame;
        cvtColor(previous_frame,previous_frame,CV_RGB2GRAY);
        cvtColor(current_frame,current_frame,CV_RGB2GRAY);
        absdiff(previous_frame,current_frame,differ);
        threshold(differ,differ,50,255,THRESH_BINARY);
        int cnt=0;
        for(int i=0;i<differ.rows;i++)
        {
            for(int j=0;j<differ.cols;j++)
            {
                if(differ.at<int>(i,j)>50)
                {
                    cnt++;
                }
            }
        }
        previous_frame.release();
        current_frame.release();
        differ.release();
        return cnt;// TODO
    }
}

extern "C"
{
    JNIEXPORT jint JNICALL
    Java_com_example_nam_healthforyou_OnGetImageListener_redDetection(JNIEnv *env, jobject instance,
                                                                      jlong matAddrInput,
                                                                      jlong matAddrResult) {
        int sum2=0;
        Mat &matInput = *(Mat *) matAddrInput;
        Mat &matResult = * (Mat *)matAddrResult;

        cvtColor(matInput,matResult,CV_RGB2BGR);
        for(int i=0;i<matResult.rows;i++)
        {
            for(int j=0;j<matResult.cols;j++)
            {
                sum2+=matResult.at<cv::Vec3b>(i,j)[2];//Red
                //sum+=matInput.at<cv::Vec3b>(i,j)[1];//Green
                //sum1+=matInput.at<cv::Vec3b>(i,j)[0];//Blue
            }
        }
        // TODO

        //Test 하는 부분 Fatal signal 에러가 발생하는 이유가 Frame에 대해서 메모리를 할당(assign)했다가 해제(release)하지않아서 그런것은 아닐까?

        return sum2;//
        // TODO

    }

    JNIEXPORT jint JNICALL
    Java_com_example_nam_healthforyou_OnGetImageListener_hueDetection(JNIEnv *env, jobject instance,
                                                                   jlong matAddrInput,
                                                                   jlong matAddrResult) {

        int sum1=0;
        Mat &matInput = *(Mat *) matAddrInput;
        Mat &matResult = * (Mat *)matAddrResult;

        cvtColor(matInput,matResult,CV_RGB2HSV);
        for(int i=0;i<matResult.rows;i++)
        {
            for(int j=0;j<matResult.cols;j++)
            {
                //sum1+=matResult.at<cv::Vec3b>(i,j)[2];//Red
                //sum+=matInput.at<cv::Vec3b>(i,j)[1];//Green
                Vec3b hsv=matResult.at<Vec3b>(i,j);
                sum1+=hsv.val[0]; //hue
            }
        }
        // TODO
        //Test 하는 부분 Fatal signal 에러가 발생하는 이유가 Frame에 대해서 메모리를 할당(assign)했다가 해제(release)하지않아서 그런것은 아닐까?
        return sum1;//
        // TODO

    }

    JNIEXPORT jint JNICALL
    Java_com_example_nam_healthforyou_OnGetImageListener_greenDetection(JNIEnv *env, jobject instance,
                                                                      jlong matAddrInput,
                                                                      jlong matAddrResult) {
        int sum=0;
        Mat &matInput = *(Mat *) matAddrInput;
        Mat &matResult = * (Mat *)matAddrResult;

        cvtColor(matInput,matResult,CV_RGB2BGR);
        for(int i=0;i<matResult.rows;i++)
        {
            for(int j=0;j<matResult.cols;j++)
            {
                //sum2+=matInput.at<cv::Vec3b>(i,j)[2];//Red
                sum+=matResult.at<cv::Vec3b>(i,j)[1];//Green
                //sum1+=matInput.at<cv::Vec3b>(i,j)[0];//Blue
            }
        }
        // TODO

        //Test 하는 부분 Fatal signal 에러가 발생하는 이유가 Frame에 대해서 메모리를 할당(assign)했다가 해제(release)하지않아서 그런것은 아닐까?
        matInput.release();
        matResult.release();
        return sum;//
        // TODO

    }

    JNIEXPORT jlong JNICALL
    Java_com_example_nam_healthforyou_OnGetImageListener_rgbtogray(JNIEnv *env, jobject instance,
                                                                        jlong matAddrInputRGB,
                                                                        jlong matAddrResultGRAY) {

        Mat &matInputRGB = *(Mat *) matAddrInputRGB;
        Mat &matResultGRAY = * (Mat *)matAddrResultGRAY;

        cvtColor(matInputRGB,matResultGRAY,CV_RGB2GRAY);


        //Test 하는 부분 Fatal signal 에러가 발생하는 이유가 Frame에 대해서 메모리를 할당(assign)했다가 해제(release)하지않아서 그런것은 아닐까?
        matInputRGB.release();
        // TODO
    }
}