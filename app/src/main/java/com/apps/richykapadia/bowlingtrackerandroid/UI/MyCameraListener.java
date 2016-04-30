package com.apps.richykapadia.bowlingtrackerandroid.UI;


import com.apps.richykapadia.bowlingtrackerandroid.Algorithm.CornerTracking;
import com.apps.richykapadia.bowlingtrackerandroid.Algorithm.HoughLines;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;

import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

/**
 * Created by richykapadia on 4/23/16.
 */
public class MyCameraListener implements CameraBridgeViewBase.CvCameraViewListener2 {

    private CameraBridgeViewBase view;
    private RegionSelector selector;
    private Scalar RED = new Scalar(255,0,0);
    private Scalar GREEN = new Scalar(0,255,0);
    private Scalar BLUE = new Scalar(0,0,255);
    private Size imgSize;
    private CornerTracking tracking;
    private HoughLines lines;

    public MyCameraListener(CameraBridgeViewBase view){
        this.view = view;
        this.selector = new RegionSelector();
        this.view.setOnTouchListener(this.selector);
        this.view.enableFpsMeter();
    }

    public void initialize(){
        tracking = new CornerTracking();
        lines = new HoughLines();

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat display = inputFrame.rgba();
        this.selector.setScreenSize( view.getWidth(), view.getHeight() );
        this.selector.setImgSize(display.size());

//        tracking.track(display);


        if( this.selector.getCurr() == RegionSelector.MODE.INIT ||
            this.selector.getCurr() == RegionSelector.MODE.DRAGGING){
            Point one = this.selector.getOne();
            Point two = this.selector.getTwo();
            Imgproc.rectangle(display, one, two, BLUE);
        }else if( this.selector.getCurr() == RegionSelector.MODE.RELEASE){
            // Track here
            Point one = this.selector.getOne();
            Point two = this.selector.getTwo();
            Rect roi = new Rect(one, two);
//            tracking.detect(display, roi);
            this.selector.reset();
        }


//        lines.detect(inputFrame.gray());
//        lines.draw(display);
//        Mat mask = lines.getRoi(inputFrame.rgba());
//        if(mask != null){
////            Core.bitwise_and(display, mask, display);
//            mask.copyTo(display);
//        }

        display = lines.testLine(inputFrame.gray());



        return display;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }


}