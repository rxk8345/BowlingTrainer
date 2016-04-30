package com.apps.richykapadia.bowlingtrackerandroid.Algorithm;

import com.apps.richykapadia.bowlingtrackerandroid.UI.Constants;

import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

/**
 * Created by richykapadia on 4/30/16.
 */
public class KeypointTracking {

    private DescriptorExtractor descriptorExtractor;
    private FeatureDetector featureDetector;
    private DescriptorMatcher descriptorMatcher;
    private Mat trained_descriptor;
    private MatOfKeyPoint detected_points;

    public KeypointTracking(){
        featureDetector = FeatureDetector.create(FeatureDetector.ORB);
        descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        descriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);
    }


    public boolean initializeKeypoints(Mat gray, Mat roi){
        if(roi != null){
            detected_points = new MatOfKeyPoint();
            featureDetector.detect(gray, detected_points, roi);
            trained_descriptor = new Mat();
            descriptorExtractor.compute(gray, detected_points, trained_descriptor);
            return true;
        }
        return false;

    }

    public void draw(Mat mat){
        for(KeyPoint p : detected_points.toList()){
            Imgproc.circle(mat, p.pt, 2, Constants.GREEN);
        }
    }
}
