package com.apps.richykapadia.bowlingtrackerandroid.Algorithm;

import android.provider.Settings;
import android.util.Log;

import com.apps.richykapadia.bowlingtrackerandroid.UI.Constants;

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
public class LaneEdges {
    public static final String TAG = "Lane-Edges";

    private final Size BLUR_SIZE = new Size(3,3);
    private final Size STRUCT_SIZE = new Size(8,8);
    private final Mat CIRCLE_STRUCT = Imgproc.getStructuringElement(Imgproc.CV_SHAPE_ELLIPSE, STRUCT_SIZE);

    private final double SIGMA_X = 1.5;

    private final Mat edge = new Mat();
    private final Mat gutterEdge = new Mat();

    // Polar
    private double[] lastKnownLeft;
    private double[] lastKnownRight;
    private double[] lastKnownFoul;

    //dynamically changes depending on whats found
    private int houghThresh = 100;

    //timestamps
    private long detectedTime;

    private static final long THREE_SECONDS = 3000l;

    public void detect(Mat gray){

        Imgproc.GaussianBlur(gray, gray, BLUR_SIZE, SIGMA_X);
        Imgproc.Canny(gray, edge, 70, 110);
        // accumulator 2 deg, no scaling, TODO make hough thesh dynamic based on #prev lines found
        Imgproc.HoughLines(edge, gutterEdge, 1, Math.PI/90, houghThresh, 0, 0, -(Math.PI/3), 2*Math.PI/3);

        ArrayList<double[]> leftCandidate = new ArrayList<double[]>();
        ArrayList<double[]> rightCandidate = new ArrayList<double[]>();
        ArrayList<double[]> foulCandidate = new ArrayList<double[]>();

        for (int i = 0; i < gutterEdge.rows(); i++) {

            double[] data = gutterEdge.get(i, 0);
            double rho = data[0];
            double theta = data[1];
            // a - x ratio
            // b - y ratio
            double a = (Math.cos(theta) * rho) / gray.size().width;
            double b = (Math.sin(theta) * rho) / gray.size().height;

            if( -Math.PI/3 < theta && theta < Math.PI/3 && a < 0.4){
                leftCandidate.add( new double[] {rho, theta} );
            }else if(-Math.PI/3 < theta && theta < Math.PI/3 && a > 0.4){
                rightCandidate.add( new double[] {rho, theta} );

            }else if(Math.PI/3 < theta && theta < 2 * Math.PI/3 && b > 0.75){
                foulCandidate.add( new double[] {rho, theta} );
            }

        }

        // modify hough thresh
        if( rightCandidate.size() > 10 && leftCandidate.size() > 10 && !foulCandidate.isEmpty()){
            this.houghThresh += 1;
            double[][] result = this.chooseBestLines(leftCandidate, rightCandidate, foulCandidate);
        	this.detectedTime = System.currentTimeMillis();
            this.lastKnownLeft = result[0];
            this.lastKnownRight = result[1];
            this.lastKnownFoul = result[2];
        }else if(this.houghThresh > 65){
            this.houghThresh -= 1;
        }
    }


    /**
     * @param leftCandidate - each element is (rho, theta)
     * @param rightCandidate - each element is (rho, theta)
     * @param foulCandidate - each element is (rho, theta)
     * @return - {left, right, foul}
     */
    private double[][] chooseBestLines(ArrayList<double[]> leftCandidate, ArrayList<double[]> rightCandidate, ArrayList<double[]> foulCandidate){
        //init chosen left, right, foul
        double[] c_left = new double[] {0,0};
        double[] c_right = new double[] {0,0};
        double[] c_foul = new double[] {0,0};
        // pick the best by minimize the dist between
        // the intersection pts with the foul line
        double minDist = Double.MAX_VALUE;
        for(double[] f : foulCandidate){
            for( double[] l : leftCandidate){
                Point lf = this.calcIntersection(l, f);
                for( double[] r: rightCandidate){
                    // make sure left and right don't intersect on the screen
                    Point lr = this.calcIntersection(l, r);
                    if( lr != null && lr.y > 0 ){
                        continue;
                    }
//        			Imgproc.circle(rgb, lr, 5, GREEN);

                    Point rf = this.calcIntersection(r, f);
                    double dx = lf.x - rf.x;
                    double dy = lf.y - rf.y;
                    double currDist = Math.sqrt(dx * dx + dy * dy);
                    if( currDist < minDist){
                        minDist = currDist;
                        c_left = l;
                        c_right = r;
                        c_foul = f;
                    }
                }
            }
        }

        double[][] result = { c_left, c_right, c_foul };
        return result;

    }

    public void draw(Mat img){
        if( lastKnownLeft != null && lastKnownRight != null && lastKnownFoul != null && detectedTime > System.currentTimeMillis() - THREE_SECONDS){
            drawLine(img, lastKnownLeft, Constants.PURPLE);
            drawLine(img, lastKnownRight, Constants.CYAN);
            drawLine(img, lastKnownFoul, Constants.RED);
        }
    }

    /**
     *
     * @return - if available, returns a list of points describing the corners of the lane,
     *          [LeftFoulLine, LeftTop, RightTop, RightFoulLine]
     */
    public Point[] getCorners(){
        if(lastKnownLeft != null && lastKnownRight != null && lastKnownFoul != null  && detectedTime > System.currentTimeMillis() - THREE_SECONDS) {
            double[] top = {0, Math.PI/2};

            Point lf = calcIntersection(lastKnownLeft, lastKnownFoul);
            Point lt = calcIntersection(lastKnownLeft, top);
            Point rt = calcIntersection(lastKnownRight, top);
            Point rf = calcIntersection(lastKnownRight, lastKnownFoul);

            Point[] result =  {lf, lt, rt, rf};
            return result;

        }

        return null;
    }

    /**
     * if lane edges are found, this function will create a mask of the area
     * @param img
     * @return
     */
    public Mat getRoi(Mat img){
        if(lastKnownLeft != null && lastKnownRight != null && lastKnownFoul != null  && detectedTime > System.currentTimeMillis() - THREE_SECONDS){
            //top of the screen horizontal
            double[] top = {0, Math.PI/2};
            this.drawLine(img, top, Constants.GREEN);

            Point lf = calcIntersection(lastKnownLeft, lastKnownFoul);
            Point rf = calcIntersection(lastKnownRight, lastKnownFoul);
            Point lt = calcIntersection(lastKnownLeft, top);
            Point rt = calcIntersection(lastKnownRight, top);

            List<MatOfPoint> rioPoints = new ArrayList<>();
            rioPoints.add(new MatOfPoint(lf, rf, rt, lt));

            Mat mask = Mat.zeros(img.size(), img.type());
            //inflate the mask a bit
            Imgproc.fillPoly(mask, rioPoints, new Scalar(255));
            Imgproc.dilate(mask, mask, CIRCLE_STRUCT);

            return mask;

        }
        return null;
    }

    private void drawLine(Mat img, double[] polar, Scalar color){
        double[] cart = polar2cart(polar);

        Point pt1 = new Point(cart[0], cart[1]);
        Point pt2 = new Point(cart[2], cart[3]);

        Imgproc.line(img, pt1, pt2, color, 2);
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

    private Point calcIntersection(double[] polar_one, double[] polar_two) {
        double r1 = polar_one[0];
        double t1 = polar_one[1];
        double r2 = polar_two[0];
        double t2 = polar_two[1];
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
