package com.apps.richykapadia.bowlingtrackerandroid.Algorithm;

import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 * Created by richykapadia on 4/27/16.
 */
public class HoughLines {

    public static final String TAG = "Hough-lines";

    private Scalar GREEN = new Scalar(0,255,0);
    private final Size blur_size = new Size(3,3);
    private final double sigma_x = 1.5;

    private final Mat gray = new Mat();
    private final Mat edge = new Mat();
    private final Mat gutterEdge = new Mat();
    private final Mat foulEdge = new Mat();

    private double[] lastKnownLeft;
    private double[] lastKnownRight;
    private double[] lastKnownFoul;

    public Mat detect(Mat img){

        Imgproc.cvtColor(img, gray, Imgproc.COLOR_RGBA2GRAY);
        Imgproc.GaussianBlur(gray, gray, blur_size, sigma_x);
        Imgproc.Canny(img, edge, 70, 110);
        // accumulator 2 deg, no scaling
        Imgproc.HoughLines(edge, gutterEdge, 1, Math.PI/90, 125);

        boolean[] set = {false, false, false};
        double[] left = {0, 0};
        double[] right = {img.size().width, 0};
        double[] foul = {0, 0};

        double center_x = img.size().width / 2;

        for (int i = 0; i < gutterEdge.rows(); i++) {
            double[] data = gutterEdge.get(i, 0);
            double rho = data[0];
            double theta = data[1];

            //left line is the largest rho less than center_x
            if( left[0] < rho && rho < center_x && -(Math.PI/4) < theta && theta < Math.PI/4 ){
                left[0] = rho;
                left[1] = theta;
                set[0] = true;
            }

            //right is smallest rho greater than center_x
            else if( center_x < rho && rho < right[0] && -(Math.PI/4) < theta && theta < Math.PI/4 ){
                right[0] = rho;
                right[1] = theta;
                set[1] = true;
            }

            //foul line is largest rho?
            else if( foul[0] < rho && Math.PI/3 < theta && theta < 2*Math.PI/3){
                foul[0] = rho;
                foul[1] = theta;
                set[2] = true;

            }
        }

        //draw left
        if( set[0] ) drawLine(img, left); lastKnownLeft = left;
        //draw right
        if( set[1] ) drawLine(img, right); lastKnownRight = right;
        //foul line
        if( set[2] ) drawLine(img, foul); lastKnownFoul = foul;

        return img;
    }

    private void drawLine(Mat img, double[] data){
        double rho = data[0];
        double theta = data[1];
        double a = Math.cos(theta);
        double b = Math.sin(theta);
        double x0 = a*rho;
        double y0 = b*rho;
        Point pt1 = new Point();
        Point pt2 = new Point();
        pt1.x = Math.round(x0 + 1000*(-b));
        pt1.y = Math.round(y0 + 1000*a);
        pt2.x = Math.round(x0 - 1000*(-b));
        pt2.y = Math.round(y0 - 1000 *a);
        Log.d(TAG, "Lines: " + rho + ", " + theta);

        Imgproc.line(img, pt1, pt2, GREEN, 3);
    }

}
