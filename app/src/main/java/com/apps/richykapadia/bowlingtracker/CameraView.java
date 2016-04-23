package com.apps.richykapadia.bowlingtracker;

import android.content.Context;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;

/**
 * Created by richykapadia on 4/19/16.
 */
public class CameraView implements CameraBridgeViewBase.CvCameraViewListener2 {

    private Context context;

    public CameraView(Context context){
        this.context = context;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //send frame to be analyzed

        //draw shit here





        return inputFrame.rgba();
    }
}
