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

public class Calibration {

	/* ***************** VIDEO ***************** */
	static final String viddir = "Video/";
	static final String videoname = "bowling_0001.mp4";
	static final String vidName = videoname.substring(0, videoname.indexOf("."));

	//scale image 
	static final double scaleFactor = 2.50;

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
		String[] frameNames = { "original", "difference" };
		VideoReader.initalizeVideoPlayer(frameNames);

		//setup sliders
		String[] sliderNames = {"hmin", "hmax", "smin", "smax", "vmin", "vmax"};
		Slider.createAndShowGUI(sliderNames);
		
		// set filesize
		Mat calibrated = VideoReader.getNextFrame(scaleFactor);
		Mat curr = VideoReader.getNextFrame(scaleFactor);
		
		while(true){
			while (!curr.empty()) {
				Mat display = new Mat();
				curr.copyTo(display);
				
				//skip a random amount
				int skip = (int) ((Math.random() * 100) % 30);
				for( int i = 0; i < skip; i++ ){
					display = VideoReader.getNextFrame(scaleFactor);
					VideoReader.displayImage(display, 0);
				}
				
				Core.subtract(curr, display, calibrated);
				Imgproc.cvtColor(calibrated, calibrated, Imgproc.COLOR_RGB2GRAY);
				VideoReader.displayImage(calibrated, 1);
				if(isBlank(calibrated)){
					System.out.println("Calibrated");
				}
				
				VideoReader.displayImage(display, 0);

				curr = VideoReader.getNextFrame(scaleFactor);
			}
			curr = VideoReader.getNextFrame(scaleFactor);
		}
	}

	private static boolean isBlank(Mat calibrated) {
		for( int i = 0; i < calibrated.rows(); i++){
			for( int j = 0; j < calibrated.cols(); j++){
				double[] data = calibrated.get(i, j);
				for( int k = 0; k < data.length; k++){
					if( data[k] != 0 ){
						return false;
					}
				}
			}
		}
		return true;
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
}
