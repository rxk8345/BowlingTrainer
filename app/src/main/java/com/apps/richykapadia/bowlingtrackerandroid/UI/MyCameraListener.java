package com.apps.richykapadia.bowlingtrackerandroid.UI;


import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.apps.richykapadia.bowlingtrackerandroid.Algorithm.BallDetection;
import com.apps.richykapadia.bowlingtrackerandroid.Algorithm.CornerTracking;
import com.apps.richykapadia.bowlingtrackerandroid.Algorithm.KeypointTracking;
import com.apps.richykapadia.bowlingtrackerandroid.Algorithm.LaneEdges;
import com.apps.richykapadia.bowlingtrackerandroid.Algorithm.PerspectiveTransform;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.util.List;


/**
 * Created by richykapadia on 4/23/16.
 */
public class MyCameraListener implements CameraBridgeViewBase.CvCameraViewListener2 {

    private final CameraBridgeViewBase view;
    private final ImageView lane_overlay;
    private final Activity activity;

    //algorithms
    private KeypointTracking tracking;
    private LaneEdges laneEdges;
    private BallDetection ballDetection;
    private PerspectiveTransform perspectiveTransform;

    //State machine
    private enum STATE {LANE_EDGE, PERSPECTIVE, DETECT_BALL, TRACKING}
    private STATE curr_state = STATE.LANE_EDGE;

    public MyCameraListener(CameraBridgeViewBase view, ImageView lane_overlay, Activity activity){
        this.view = view;
        this.lane_overlay = lane_overlay;
        this.view.enableFpsMeter();
        this.activity = activity;
    }

    public void initialize(){
        laneEdges = new LaneEdges();
        tracking = new KeypointTracking();
        perspectiveTransform = new PerspectiveTransform();
        this.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                curr_state = STATE.PERSPECTIVE;
            }
        });


    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat display = inputFrame.rgba();

        switch ( curr_state ){
            case LANE_EDGE:
                laneEdges.detect(inputFrame.gray());
                laneEdges.draw(display);
                break;
            case PERSPECTIVE:
                Point[] corners = laneEdges.getCorners();
                if(corners != null) {
                    Mat lane = perspectiveTransform.transform(inputFrame.rgba(), corners);
                    Bitmap bmp = Bitmap.createBitmap(lane.width(), lane.height(), Bitmap.Config.ARGB_8888);
                    Utils.matToBitmap(lane, bmp);
                    this.activity.runOnUiThread( new DisplayOverlay(bmp, lane_overlay));
                    this.ballDetection = new BallDetection(corners);
                    this.curr_state = STATE.DETECT_BALL;
                }else{
                    this.curr_state = STATE.LANE_EDGE;
                }

                break;
            case DETECT_BALL:

                break;
            case TRACKING:
                tracking.draw(display);
                break;
            default:
                break;
        }


//        Mat roi = laneEdges.getRoi(inputFrame.rgba());
//        if(roi != null){
//            //start tracking key points
//            return roi;
//        }




        return display;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    private class DisplayOverlay implements Runnable{
        private Bitmap bitmap;
        private ImageView imageView;

        public DisplayOverlay(Bitmap bmp, ImageView imageView){
            this.bitmap = bmp;
            this.imageView = imageView;
        }

        @Override
        public void run(){
            this.imageView.setImageBitmap(this.bitmap);
            this.imageView.setVisibility(View.VISIBLE);

        }

    }

}