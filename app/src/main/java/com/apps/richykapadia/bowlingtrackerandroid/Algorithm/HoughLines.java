package com.apps.richykapadia.bowlingtrackerandroid.Algorithm;

import android.provider.Settings;
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by richykapadia on 4/27/16.
 */
public class HoughLines {

    public static final String TAG = "Hough-lines";

    private Scalar GREEN = new Scalar(0,255,0);
    private Scalar BLUE = new Scalar(255,0,0);
    private Scalar RED = new Scalar(0,0,255);

    private final Size blur_size = new Size(3,3);
    private final double sigma_x = 1.5;

    private final Mat gray = new Mat();
    private final Mat edge = new Mat();
    private final Mat gutterEdge = new Mat();
    private final Mat foulEdge = new Mat();

    // Polar
    private double[] lastKnownLeft;
    private double[] lastKnownRight;
    private double[] lastKnownFoul;

    //timestamps
    private long detectedTime;

    private static final long THREE_SECONDS = 3000l;


    public Mat testLine(Mat gray){
        Imgproc.GaussianBlur(gray, gray, blur_size, sigma_x);
        Imgproc.Canny(gray, edge, 70, 110);
        // accumulator 2 deg, no scaling, TODO make hough thesh dynamic based on #prev lines found
        Imgproc.HoughLines(edge, gutterEdge, 1, Math.PI/90, 100, 0, 0, -(Math.PI/4), 2*Math.PI/3);

        boolean[] set = {false, false, false};
        double[] left = {0, 0};
        double[] right = {gray.size().width, 0};
        double[] foul = {gray.size().height/2, 0};

        double center_x = gray.size().width / 2;

        for (int i = 0; i < gutterEdge.rows(); i++) {
            double[] data = gutterEdge.get(i, 0);
            double rho = data[0];
            double theta = data[1];

            Log.d(TAG, "rho: " + rho +  ", " + "theta: " + theta );

            double a = Math.cos(theta) * rho;
            double b = Math.sin(theta) * rho;
            Log.d(TAG, "xcom: " + a +  ", " + "ycom: " + b );
            drawLine(gray, data);

        }
        return gray;
    }


    public void detect(Mat gray){

        Imgproc.GaussianBlur(gray, gray, blur_size, sigma_x);
        Imgproc.Canny(gray, edge, 70, 110);
        // accumulator 2 deg, no scaling, TODO make hough thresh dynamic based on #prev lines found
        Imgproc.HoughLines(edge, gutterEdge, 1, Math.PI/90, 75, 0, 0, -(Math.PI/4), 2*Math.PI/3);

        boolean[] set = {false, false, false};
        double[] left = {0, 0};
        double[] right = {gray.size().width, 0};
        double[] foul = {gray.size().height/2, 0};

        double center_x = gray.size().width / 2;

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

            //foul line is largest rho
            else if( foul[0] < rho && Math.PI/3 < theta && theta < 2*Math.PI/3){
                foul[0] = rho;
                foul[1] = theta;
                set[2] = true;
            }
        }

        if( set[0] && set[1] && set[2] ) {
            lastKnownLeft = left;
            lastKnownRight = right;
            lastKnownFoul = foul;
            detectedTime = System.currentTimeMillis();
        }

    }

    public void draw(Mat img){
        if( lastKnownLeft != null && lastKnownRight != null && lastKnownFoul != null && detectedTime > System.currentTimeMillis() - THREE_SECONDS){
            drawLine(img, lastKnownLeft);
            drawLine(img, lastKnownRight);
            drawLine(img, lastKnownFoul);
        }
    }

    private void drawLine(Mat img, double[] polar){
        double[] cart = polar2cart(polar);

        Point pt1 = new Point(cart[0], cart[1]);
        Point pt2 = new Point(cart[2], cart[3]);

        Imgproc.line(img, pt1, pt2, GREEN, 2);
    }

    private double[] polar2cart(double[] polar){
        double rho = polar[0];
        double theta = polar[1];
        double[] cart = new double[4];
        double a = Math.cos(theta);
        double b = Math.sin(theta);
        double x0 = a*rho;
        double y0 = b*rho;
        cart[0] = Math.round(x0 + 1000 *( -b));
        cart[1] = Math.round(y0 + 1000 * a);
        cart[2] = Math.round(x0 - 1000 * (-b));
        cart[3] = Math.round(y0 - 1000 * a);
        return cart;
    }

    public Mat getRoi(Mat img){
        if(lastKnownLeft != null && lastKnownRight != null && lastKnownFoul != null && detectedTime > System.currentTimeMillis() - THREE_SECONDS){

            //top of the screen horizontal
            double[] top = {0, Math.PI/2};

            Point lf = calcIntersection(lastKnownLeft, lastKnownFoul);
            Point rf = calcIntersection(lastKnownRight, lastKnownFoul);
            Point lt = calcIntersection(lastKnownLeft, top);
            Point rt = calcIntersection(lastKnownRight, top);

            Imgproc.circle(img, lf, 10, RED);
            Imgproc.circle(img, rf, 10, RED);
            Imgproc.circle(img, lt, 14, BLUE);
            Imgproc.circle(img, rt, 14, BLUE);

            drawLine(img, lastKnownLeft);
            drawLine(img, lastKnownRight);
            drawLine(img, lastKnownFoul);
            drawLine(img, top);

//            List<MatOfPoint> rioPoints = new ArrayList<>();
//            rioPoints.add(new MatOfPoint(lf));
//            rioPoints.add(new MatOfPoint(rf));
//            rioPoints.add(new MatOfPoint(lt));
//            rioPoints.add(new MatOfPoint(rt));
//
//            Mat mask = new Mat(img.rows(), img.cols(), img.type());
//            Imgproc.fillPoly(mask, rioPoints, new Scalar(1));
            return img;

        }
        return null;
    }

    private Point calcIntersection(double[] one, double[] two) {
        double r1 = one[0];
        double t1 = one[1];
        double r2 = two[0];
        double t2 = two[1];
        double ct1 = Math.cos(t1);     //matrix element a
        double st1 = Math.sin(t1);     //b
        double ct2 = Math.cos(t2);     //c
        double st2 = Math.sin(t2);     //d
        double d=ct1*st2-st1*ct2;        //determinative (rearranged matrix for inverse)
        if(d!=0.0f) {
            int x = (int)((st2*r1-st1*r2)/d);
            int y = (int)((-ct2*r1+ct1*r2)/d);
            return new Point(x,y);
        } else { //lines are parallel and will NEVER intersect!
            return null;
        }
    }

}
