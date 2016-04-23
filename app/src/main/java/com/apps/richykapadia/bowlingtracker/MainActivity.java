package com.apps.richykapadia.bowlingtracker;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraGLSurfaceView;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

public class MainActivity extends AppCompatActivity{

    private BaseLoaderCallback mLoaderCallback;
    private JavaCameraView javaCameraView;
    private CameraView cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI Element
        javaCameraView = (JavaCameraView) findViewById(R.id.CameraView);
//        mCameraView.setMaxFrameSize(352, 288);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);

        //Set listener and callback
        cameraView = new CameraView(this);
        javaCameraView.setCvCameraViewListener(cameraView);
        mLoaderCallback = new LoaderCallback(this, javaCameraView);



    }


    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
