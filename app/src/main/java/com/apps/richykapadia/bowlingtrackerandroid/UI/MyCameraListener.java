package com.apps.richykapadia.bowlingtrackerandroid.UI;


import com.apps.richykapadia.bowlingtrackerandroid.Algorithm.CornerTracking;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.Point;
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
    private CornerTracking tracking = new CornerTracking();

    public MyCameraListener(CameraBridgeViewBase view){
        this.view = view;
        this.selector = new RegionSelector();
        this.view.setOnTouchListener(this.selector);
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat display = inputFrame.rgba();
        if(imgSize == null && display != null){
            imgSize = new Size();
            imgSize.width = display.cols();
            imgSize.height = display.width();
        }

        Mat m = new Mat();

        // Track here
        tracking.detect(display, roi);


        if( this.selector.getCurr() == RegionSelector.MODE.INIT ||
            this.selector.getCurr() == RegionSelector.MODE.DRAGGING){
            Point[] pts = translateCoord();
            Imgproc.rectangle(display, pts[0], pts[1], BLUE);
        }else if( this.selector.getCurr() == RegionSelector.MODE.RELEASE){

            /*
            Point[] pts = translateCoord();
            Rect rect = new Rect(pts[0], pts[1]);
            ArrayList<Point> trackThese = new ArrayList<>();
            for(Point p : corners.toList()){
                if(rect.contains(p)) {
                    Imgproc.circle(display, p, 1, GREEN);
                    trackThese.add(p);
                }
            }
            */

            this.selector.reset();

        }

        return display;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    //TODO fix this shit! Mat size != screen view size
    private Point[] translateCoord(){

        Point one = this.selector.getOne();
        Point two = this.selector.getTwo();

        int cols = (int) imgSize.width;
        int rows = (int) imgSize.height;

        int xOffset = (this.view.getWidth() - cols) / 2;
        int yOffset = (this.view.getHeight() - rows) / 2;

        int x1 = (int) one.x - xOffset;
        int y1 = (int) one.y - yOffset;

        int x2 = (int) two.x - xOffset;
        int y2 = (int) two.y - yOffset;

        Point scaled_one = new Point(x1, y1);
        Point scaled_two = new Point(x2, y2);

        Point[] result = new Point[2];
        result[0] = scaled_one;
        result[1] = scaled_two;

        return result;

    }


}