package com.apps.richykapadia.bowlingtrackerandroid.Algorithm;

import org.opencv.core.Mat;
import org.opencv.core.Point;

import java.util.ArrayList;

/**
 * Created by richykapadia on 4/30/16.
 */
public class BallDetection {

    private Point foul_left;
    private Point foul_right;

    private Point top_left;
    private Point top_right;


    private ArrayList<Point> ballPath;

    public BallDetection( Point[] corners ){
        this.foul_left = corners[0];
        this.top_left = corners[1];
        this.top_right = corners[2];
        this.foul_right = corners[3];
    }

    public void detect(Mat gray){
        
    }


    public void draw(){

    }




}
