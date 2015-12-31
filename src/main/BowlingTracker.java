package main;

import java.io.IOException;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import util.Slider;
import util.VideoReader;

/**
 * @author richykapadia
 * 
 * Capture a lane
 *
 */
public class BowlingTracker {

	/* ***************** VIDEO ***************** */
	static final String viddir = "Video/";
	static final String videoname = "bowling_0001.mp4";
	static final String vidName = videoname.substring(0, videoname.indexOf("."));

	//scale image 
	static final double scaleFactor = 2.50;

	
	static Scalar sMin = new Scalar(88, 88, 88);
	static Scalar sMax = new Scalar(168, 168, 168);
	
	static final int 		HOUGH_TRESHOLD = 25;
	//long line
	static final int 		HOUGH_MIN_LINE_LENGTH = 125;
	//small gaps between pts
	static final int 		HOUGH_MAX_LINE_GAP = 10;		
	

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
		String[] frameNames = { "original", "canny" };
		VideoReader.initalizeVideoPlayer(frameNames);

		// set filesize
		Mat mat = VideoReader.getNextFrame(scaleFactor);

		// UI
		String[] sliderNames = {"cMin", "cMax", "gauss",
								"hthresh", "hminlen", "maxgap"};
		Slider.createAndShowGUI(sliderNames);

		Mat grey = new Mat();
		Mat canny = new Mat();
		double cannyThreshOne = 10;
		double cannyThreshTwo = 50;
		double rho = 50;
		double theta = Math.PI/180;
		int threshold = 50;
		
		//color filtering
		Mat hsv = new Mat();
		Mat thresh = new Mat();
		double hmin;
		double hmax;
		double smin;
		double smax;
		double vmin;
		double vmax;
		double gaussTheta;
		Mat lines = new Mat();

		// infinite loop video
		while (true) {
			while (!mat.empty()) {
				Mat display = new Mat();
				mat.copyTo(display);
				
				int[] data = Slider.getSliderValues();
				cannyThreshOne = (data[0] == 0) ? 1 : data[0];
				cannyThreshTwo = (data[1] == 0) ? 1 : data[1];
				gaussTheta = (data[2] == 0) ? 1 : data[2];
				int hthresh = data[3];
				int hminlen = data[4];
				int maxgap = data[5];
				
				
				Imgproc.cvtColor(mat, hsv, Imgproc.COLOR_RGB2HSV);
				sMin = new Scalar(1, 1, 100);
		        sMax = new Scalar(300, 300, 200);
		        Core.inRange(hsv, sMin, sMax, thresh);
				
				Imgproc.GaussianBlur(thresh, grey, new Size(5,5), gaussTheta);
				Imgproc.Canny(grey, canny, cannyThreshOne, cannyThreshTwo);
				//Imgproc.HoughLinesP(canny, lines, 1, theta, threshold);
				Imgproc.HoughLinesP(canny, lines, 1, Math.PI/180, HOUGH_TRESHOLD, HOUGH_MIN_LINE_LENGTH, HOUGH_MAX_LINE_GAP);


				System.out.println("Num lines: " + lines.rows());
				drawLines(display, lines, new Scalar(100, 250, 150));

				// next frame
				VideoReader.displayImage(display, 0);
				VideoReader.displayImage(canny, 1);
			}
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
