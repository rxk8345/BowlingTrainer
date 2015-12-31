package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;


import util.Slider;
import util.VideoReader;

public class TraceBall {

	/* ***************** VIDEO ***************** */
	static final String viddir = "Video/";
	static final String videoname = "bowling_0005.mp4";
	static final String vidName = videoname.substring(0, videoname.indexOf("."));

	//scale image 
	static final double scaleFactor = 2.50;
	
	//ball trace points
	static ArrayList<Point> tracePts = new ArrayList<Point>();
	

	public static void main(String[] args) {
		// load opencv
		System.loadLibrary(org.opencv.core.Core.NATIVE_LIBRARY_NAME);

		try {
			VideoReader.initalizeVideoReader(viddir + videoname);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		// setup frames
		String[] frameNames = { "original", "init_hsv" };
		VideoReader.initalizeVideoPlayer(frameNames);

		//setup sliders
//		String[] sliderNames = {"min", "max"};
//		Slider.createAndShowGUI(sliderNames);
		
		// set filesize
		Mat init = VideoReader.getNextFrame(scaleFactor);
		Mat curr = VideoReader.getNextFrame(scaleFactor);
		Mat display = new Mat();
		Mat prev = curr;

		Mat diff = new Mat();
		Mat hsv_diff = new Mat();
		Mat hsv_curr = new Mat();
		Mat circle = new Mat();

		int delayCount = 5;
		
		int iCannyUpperThreshold = 300;
		int iAccumulator = 100;
		int iMinRadius = 1;
		int iMaxRadius = 100;

		// infinite loop video
		while (true) {
			while (!curr.empty() && !prev.empty()) {
				
				curr.copyTo(display);
				Core.subtract(init, curr, diff);
				Mat grey = new Mat();
				Imgproc.cvtColor(diff, grey, Imgproc.COLOR_RGB2GRAY);
				
				
//				Mat inRange = new Mat();
//				int[] values = Slider.getSliderValues();
//				Core.inRange(diff, new Scalar(values[0]), new Scalar(values[1]), inRange);
//				
				Imgproc.HoughCircles(grey, circle, Imgproc.CV_HOUGH_GRADIENT, 2.0, diff.rows()/4,
						iCannyUpperThreshold, iAccumulator, iMinRadius, iMaxRadius);
				
				drawCircles(display, circle);
				traceBall(display, circle);
			
				// next frame
				VideoReader.displayImage(display, 0);
				VideoReader.displayImage(diff, 1);
//				VideoReader.displayImage(inRange, 2);
			
				if( VideoReader.getFrameCount() % delayCount == 0){
					prev = curr;
				}
				curr = VideoReader.getNextFrame(scaleFactor);

				
			}
			//reset trace
			tracePts.clear();
			curr = VideoReader.getNextFrame(scaleFactor);

		}
	}

	public static void drawBox(Mat mat, List<MatOfPoint> contours, Scalar color) {
		// draw boxs
		for (int i = 0; i < contours.size(); i++) {
			if (Imgproc.contourArea(contours.get(i)) > 50) {
				Rect rect = Imgproc.boundingRect(contours.get(i));
				Imgproc.rectangle(mat, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
						color);
			}
		}
	}

	public static void drawCircles(Mat display, Mat circle){
		//circles are x,y,radius 
		if( circle.cols() > 0){
			for( int i = 0; i < circle.cols(); i++){
				double[] data = circle.get(0,i);
				if( data == null){
					break;
				}
				
				Point center = new Point(data[0], data[1]);
				double r = data[2];
				//draw circles
				Imgproc.circle(display, center, (int) r, new Scalar(0,255,0));
			}
		}
	}
	
	public static void traceBall(Mat display, Mat circle){
		//circles are x,y,radius 
		if( circle.cols() > 0){
			for( int i = 0; i < circle.cols(); i++){
				double[] data = circle.get(0,i);
				if( data == null){
					break;
				}

				double r = data[2];
				Point contact = new Point(data[0], data[1] + r);
				tracePts.add(contact);

			}
		}
		
		Point prev = null;
		for( Point curr : tracePts){
			Imgproc.rectangle(display, curr, curr, new Scalar(0,0,255), 5);
			if(prev != null){
				Imgproc.line(display, curr, prev, new Scalar(255,0,0), 1);
			}
			prev = curr;
		}
		
	}

	public static void drawLines(Mat mat, Mat lines, Scalar color) {
		double[] data;
		double rho, theta;
		Point pt1 = new Point();
		Point pt2 = new Point();
		double a, b;
		double x0, y0;
		for( int c = 0; c < lines.cols(); c++){
			for(int r = 0; r < lines.rows(); r++){
				data = lines.get(r, c);
				if( data.length == 4){
					//x1,y1,x2,y2
					pt1 = new Point(data[0], data[1]);
					pt2 = new Point(data[2], data[3]);
					Imgproc.line(mat, pt1, pt2, color);
				}else if( data.length == 2){
					// r theta 
					rho = data[0];
					theta = data[1];
					a = Math.cos(theta);
					b = Math.sin(theta);
					x0 = a * rho;
					y0 = b * rho;
					pt1.x = Math.round(x0 + 1000 * (-b));
					pt1.y = Math.round(y0 + 1000 * a);
					pt2.x = Math.round(x0 - 1000 * (-b));
					pt2.y = Math.round(y0 - 1000 * a);
					Imgproc.line(mat, pt1, pt2, color, 1);
				}
			}
		}
	}
}
