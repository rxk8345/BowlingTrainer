package com.apps.richykapadia.bowlingtrackerandroid.Algorithm;

import android.util.Log;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.security.Key;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by richykapadia on 4/23/16.
 */
public class CornerTracking {

    private static final String TAG = "Corner-tracking";

    private DescriptorMatcher bf_matcher;
    private FeatureDetector featureDetector;
    private DescriptorExtractor descriptorExtractor;
    private MatOfKeyPoint trained_kp;
    private List<KeyPoint> trained_kp_list;
    private Mat trainedDescriptor;
    private Mat firstFrameRio;
    private Scalar RED = new Scalar(255,0,0);
    private Scalar GREEN = new Scalar(0,255,0);
    private Scalar BLUE = new Scalar(0,0,255);

    //    vector<Point2f> object_bb;
    Mat object_bb;


    private static final double MATCH_RATIO = 0.75;
    private static final double RANSAC_THRESH = 4;

    public CornerTracking(){
        this.featureDetector = FeatureDetector.create(FeatureDetector.ORB);
        this.descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        this.bf_matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
    }

    /**
     * set keypts and descr in the roi
     * @param img
     * @param rio
     */
    public void detect(Mat img, Rect rio){

        //get keypts
        //check rio within image
        if(0 > rio.tl().x || 0 > rio.br().x ||
            rio.tl().x > img.width() || rio.br().x > img.width() ||
            0 > rio.tl().y || 0 > rio.br().y ||
            rio.tl().y > img.height() || rio.br().y > img.height() || rio.area() < 50){
            return;
        }
        firstFrameRio = new Mat(img, rio);
        Mat gray = new Mat();
        Imgproc.cvtColor(firstFrameRio, gray, Imgproc.COLOR_RGBA2GRAY);

        //find keypoints in rio
        trained_kp = new MatOfKeyPoint();
        featureDetector.detect(gray, trained_kp);
        trained_kp_list = trained_kp.toList();

        //compute descr
        this.trainedDescriptor = new Mat();
        descriptorExtractor.compute(gray, trained_kp, this.trainedDescriptor);

        //set bounding box for perspective transform
        this.object_bb = new Mat(4, 1, CvType.CV_32FC2);
        this.object_bb.put(0, 0,
                rio.tl().x, rio.tl().y,
                rio.br().x, rio.tl().y,
                rio.br().x, rio.br().y,
                rio.tl().x, rio.br().y);


    }

    public void track(Mat img){

        if( trained_kp == null || trained_kp.toList().isEmpty()){
            return;
        }

        //get current frame keypts
        Mat gray = new Mat();
        Imgproc.cvtColor(img, gray, Imgproc.COLOR_BGR2GRAY);
        MatOfKeyPoint kpts = new MatOfKeyPoint();
        this.featureDetector.detect(gray, kpts);
        List<KeyPoint> kpts_list = kpts.toList();

        //get keypts descriptor
        Mat descr = new Mat();
        this.descriptorExtractor.compute(gray, kpts, descr);

        // match keypoints
        List<MatOfDMatch> matches = new ArrayList<>();
        bf_matcher.knnMatch(descr, this.trainedDescriptor, matches, 2);

        //matched key points
        List<KeyPoint> trained = new ArrayList<>();
        List<KeyPoint> queried = new ArrayList<>();

        for( MatOfDMatch curr : matches ){
            List<DMatch> list = curr.toList();
            //should be the size of k-neighbors
            if (list.size() == 2) {
                DMatch m = list.get(0);
                DMatch n = list.get(1);
                // knn ratio test
                if (m.distance < MATCH_RATIO * n.distance) {
                    trained.add(this.trained_kp_list.get(m.trainIdx));
                    queried.add(kpts_list.get(m.queryIdx));
                }
            }
        }

        //homography for perspective transform
        if( trained.size() >= 4){
            //java is stupid
            Point[] one_pt_array = new Point[queried.size()];
            int i = 0;
            for(KeyPoint kp : trained){
                one_pt_array[i] = kp.pt;
                i++;
            }
            Point[] two_pt_array = new Point[trained.size()];
            i = 0;
            for(KeyPoint kp : trained){
                two_pt_array[i] = kp.pt;
                i++;
            }

            MatOfPoint2f one = new MatOfPoint2f(one_pt_array);
            MatOfPoint2f two = new MatOfPoint2f(two_pt_array);
            Mat homography = Calib3d.findHomography(one, two, Calib3d.RANSAC, RANSAC_THRESH);

            if( !homography.empty() ) {
                Mat new_bb = new Mat(4, 1, CvType.CV_32FC2);
                Core.perspectiveTransform(this.object_bb, new_bb, homography);

                //draw bounding box

                Point tl = new Point(new_bb.get(0, 0));
                Point tr = new Point(new_bb.get(1, 0));
                Point br = new Point(new_bb.get(2, 0));
                Point bl = new Point(new_bb.get(3, 0));


                Imgproc.line(img, tl, tr, GREEN);
                Imgproc.line(img, tr, br, GREEN);
                Imgproc.line(img, br, bl, GREEN);
                Imgproc.line(img, bl, tl, GREEN);

            }

        }

        /*

        vector<Point2f> new_bb;
        perspectiveTransform(object_bb, new_bb, homography);
        Mat frame_with_bb = frame.clone();
        if(stats.inliers >= bb_min_inliers) {
            drawBoundingBox(frame_with_bb, new_bb);
        }
         */




//        draw_keypoints(img, queried);

    }

    public void draw_keypoints(Mat img, List<KeyPoint> keyPoints){

        for( KeyPoint kp : keyPoints){
            Imgproc.circle(img, kp.pt, 2, GREEN);
        }

    }

}
