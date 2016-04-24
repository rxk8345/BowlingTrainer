package com.apps.richykapadia.bowlingtrackerandroid.Algorithm;

import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by richykapadia on 4/23/16.
 */
public class CornerTracking {

    private DescriptorMatcher matcher;
    private DescriptorExtractor extractor;
    private MatOfKeyPoint prevKP;
    private Mat prevDesc;
    private Scalar RED = new Scalar(255,0,0);
    private Scalar GREEN = new Scalar(0,255,0);
    private Scalar BLUE = new Scalar(0,0,255);


    /**
     * set keypts and descr in the roi
     * @param img
     * @param rio
     */
    public void detect(Mat img, Rect rio){

        if( extractor == null){
            extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        }

        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
        extractor.compute(gray, this.prevKP, this.prevDesc);
//        Imgproc.goodFeaturesToTrack(gray, corners, 25, 0.01, 10);


        List<KeyPoint> good = new ArrayList<KeyPoint>();
        for(KeyPoint kp : this.prevKP.toList()){
            if(rio.contains(kp.pt)){
                good.add(kp);
            }
        }


        this.prevKP.fromList(good);

    }

    public void track(Mat img){
        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
        MatOfKeyPoint kpt = new MatOfKeyPoint();
        Mat desc = new Mat();
        extractor.compute(gray, kpt, desc);

        if( matcher == null){
            matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        }

        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(desc, prevDesc, matches);

        List<KeyPoint> good = new ArrayList<KeyPoint>();
        for( DMatch m : matches.toList() ){
            m.
        }





    }

    public void draw(Mat img){

    }

}
