package com.apps.richykapadia.bowlingtrackerandroid.Algorithm;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

/**
 * Created by richykapadia on 4/30/16.
 */
public class PerspectiveTransform {

    private Mat inputQuad;
    private Mat outputQuad;
    private Mat perspective;
    private Mat output;

    private Size laneSize;

    public PerspectiveTransform(){
        inputQuad = new Mat(4, 1, CvType.CV_32FC2);
        outputQuad = new Mat(4, 1, CvType.CV_32FC2);
        laneSize = new Size(39 * 5, 60 * 10);
    }


    public Mat transform(Mat img, Point[] corners){
        Point lf = corners[0];
        Point lt = corners[1];
        Point rt = corners[2];
        Point rf = corners[3];

        inputQuad.put(0,0,
                lf.x, lf.y,
                lt.x, lt.y,
                rt.x, rt.y,
                rf.x, rf.y);

        outputQuad.put(0,0,
                0.0, laneSize.height,
                0.0, 0.0,
                laneSize.width, 0.0,
                laneSize.width, laneSize.height);

        perspective = Imgproc.getPerspectiveTransform(inputQuad, outputQuad);

        output = new Mat();
        Imgproc.warpPerspective(img, output, perspective, laneSize);
        return output;

    }

}
