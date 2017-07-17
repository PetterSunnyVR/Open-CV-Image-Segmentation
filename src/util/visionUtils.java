package util;

import java.awt.font.ImageGraphicAttribute;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import com.sun.javafx.scene.traversal.Hueristic2D;

import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.ImagePattern;

public class visionUtils {
	
	static{
		new LibraryLoader();
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		}
	
	static final ClassLoader loader = visionUtils.class.getClassLoader();
	
	
	private Mat kernel = new Mat(new Size(3,3), 0);
	private Mat originalImg = new Mat();
	//Mat image = Imgcodecs.imread(getClass().getResource(''));
	
	//### Grabbing the image
	public void readImage() {
		
		//OPEN IMAGE
		originalImg = Imgcodecs.imread("D:\\workspace\\OCVImageSegmentation\\res\\sudoku.jpg");
		//COPY AS A GRAYSCALE IMAGE FOR FURHTER PROCESSING
		Mat gray = new Mat();
		if(originalImg.empty()) {
			System.out.println("EMPTY IMAGE. CHECK FILE");
		}else{
			if(originalImg.channels()>1) {
				Imgproc.cvtColor(originalImg, gray, Imgproc.COLOR_BGR2GRAY);
			}else{
				originalImg.copyTo(gray);
			}
			//CONTOURS
			//contoursSegmentation(gray);
			biggestContourSegmentation(gray);
		}
		
		

	}
	
	public void contoursSegmentation(Mat img) {
		Mat edges = new Mat();
		//CANNY REDUCES A LOT OF NOISE SO FINDCONTOURS WILL BE MORE EFFECTIVE 
		Imgproc.Canny(img, edges, 30, 200);
		
		//FINDCONTOURS
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarch = new Mat();
		
		Imgproc.findContours(edges, contours, hierarch, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		
		System.out.println("NUMBER OF CONTOURS: "+contours.size());
		
		Imgproc.drawContours(originalImg, contours, -1, new Scalar(0,255,0),3);
		System.out.println("DONE");
		Imgcodecs.imwrite("E:\\gaussian3.jpg", originalImg);
	}
	
	public void biggestContourSegmentation(Mat img){
		Mat edges = new Mat();
		//CANNY REDUCES A LOT OF NOISE SO FINDCONTOURS WILL BE MORE EFFECTIVE 
		Imgproc.Canny(img, edges, 30, 200);
		unskewImage(edges);
		//FINDCONTOURS
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarch = new Mat();
		
		Imgproc.findContours(edges, contours, hierarch, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		
		int maxArea = 0;
		int maxIndex = 0;
		
		for (int i = 0; i < contours.size(); i++) {
			if(Imgproc.contourArea(contours.get(i))>maxArea){
				maxArea = (int)Imgproc.contourArea(contours.get(i));
				maxIndex = i;
			}
		}
		//Mat linesMatContour = new Mat().zeros(originalImg.size(), CvType.CV_8UC3);
		//Imgproc.drawContours(linesMatContour, contours, maxIndex, new Scalar(0,255,0),5);
		//Imgproc.cvtColor(linesMatContour, linesMatContour, Imgproc.COLOR_BGR2GRAY);
		//Imgproc.threshold(linesMatContour, linesMatContour, 120, 255, Imgproc.THRESH_BINARY);
		//Imgproc.Canny(linesMatContour, linesMatContour, 10, 50);
		//Imgcodecs.imwrite("E:\\gaussian1.jpg", linesMatContour);
		//unskewImage(linesMatContour);
		
		//sortingContours(contours);
		System.out.println("DONE");
		//Imgcodecs.imwrite("E:\\gaussian3.jpg", originalImg);
		Imgcodecs.imwrite("E:\\gaussian3.jpg", cropContour(contours.get(maxIndex), originalImg));
	}
	
	//PERSPECTIVE TRANSFORMATION - finding points
	public void unskewImage(Mat img){
		Mat copyImage = new Mat();
		originalImg.copyTo(copyImage);
		
		//gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
		//edges = cv2.Canny(gray,50,150,apertureSize = 3)
		
		Mat lines = new Mat();
		//lines= cv2.HoughLines(dst, 1, math.pi/180.0, 100, np.array([]), 0, 0)
		Imgproc.HoughLines(img, lines, 1, Math.PI/180.0, 100);
		System.out.println(lines.dump());
		double[] data;
		double rho, theta;
		Point pt1 = new Point();
		Point pt2 = new Point();
		double a, b;
		double x0, y0;
		for (int i = 0; i < lines.cols(); i++)
		{
		    rho = lines.get(i, 0)[0];
		    theta = lines.get(i, 0)[1];
		    a = Math.cos(theta);
		    b = Math.sin(theta);
		    x0 = a*rho;
		    y0 = b*rho;
		    pt1.x = Math.round(x0 + 1000*(-b));
		    pt1.y = Math.round(y0 + 1000*a);
		    pt2.x = Math.round(x0 - 1000*(-b));
		    pt2.y = Math.round(y0 - 1000 *a);
		    Imgproc.line(copyImage, pt1, pt2, new Scalar(0,255,0), 3);
		    
		}
		Imgcodecs.imwrite("E:\\gaussian2.jpg", copyImage);
	}
	
	//sudoku contour - first we would need to unskiew the image
	public void sudokuSecondContour(Mat img){
		Mat edges = new Mat();
		//CANNY REDUCES A LOT OF NOISE SO FINDCONTOURS WILL BE MORE EFFECTIVE 
		Imgproc.Canny(img, edges, 30, 200);
		
		//FINDCONTOURS
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarch = new Mat();
		
		Imgproc.findContours(edges, contours, hierarch, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		
/*		double minArea = 
		
		for (int i = 0; i < contours.size(); i++) {
			if(Imgproc.contourArea(contours.get(i))>maxArea){
				maxArea = (int)Imgproc.contourArea(contours.get(i));
				maxIndex = i;
			}
		}*/
		
		Imgproc.drawContours(originalImg, contours, -1, new Scalar(0,255,0),3);
		System.out.println("DONE");
		Imgcodecs.imwrite("E:\\gaussian3.jpg", originalImg);
	}
	
	//crop contour to a new image
	public Mat cropContour(MatOfPoint contour, Mat img){
		Rect croppedRect = Imgproc.boundingRect(contour);
		return img.submat(croppedRect);
	}
	
	//sorts downwards
	public void sortingContours(List<MatOfPoint> contours){
		List<MatOfPoint> sortedList = new ArrayList<>();
		double maxArea = -1;
		double currentArea = -1;
		int insertToListIndex = -1;
		int count = 0;
		int maxCunt = contours.size();
		while(maxCunt!=count){
			if(count==0){
				for (int i = 0; i < maxCunt; i++) {
					if(Imgproc.contourArea(contours.get(i))>maxArea){
						maxArea=Imgproc.contourArea(contours.get(i));
						insertToListIndex=i;
					}
				}
				
			}else{
				for (int i = 0; i < maxCunt; i++) {
					if(Imgproc.contourArea(contours.get(i))>currentArea && Imgproc.contourArea(contours.get(i))<maxArea){
						currentArea = Imgproc.contourArea(contours.get(i));
						insertToListIndex=i;
					}
				}
				maxArea=currentArea;
				currentArea=-1;
			}
			sortedList.add(contours.get(insertToListIndex));
			count++;
		}
		
		for (int i = 0; i < sortedList.size(); i++) {
			System.out.println(Imgproc.contourArea(sortedList.get(i)));
		}
	}

	
	public void edgeDetection(Mat img) {
		Mat gray = new Mat();
		img.copyTo(gray);
		//Imgproc.cvtColor(img, gray, Imgproc.COLOR_RGB2GRAY);
		Imgproc.GaussianBlur(gray, gray, new Size(5,5), 0);
		Mat edges = new Mat();
		Imgproc.Canny(gray, edges, 75, 200);
		findingContours(edges);
		
		
	}
	
	public void findingContours(Mat img) {
		Mat clone = new Mat();
		img.copyTo(clone);
		List<MatOfPoint> contours = new ArrayList<>();
		Imgproc.findContours(clone, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		if(contours.size()>0) {
			double maxArea = 0;
			int maxIndex = 0;
			for (int i = 0; i < contours.size(); i++) {
				if(Imgproc.contourArea(contours.get(i))>maxArea) {
					maxArea=Imgproc.contourArea(contours.get(i));
					maxIndex=i;
				}
			}
			Rect rect = Imgproc.boundingRect(contours.get(maxIndex));
			
			Imgproc.rectangle(originalImg, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height),new Scalar(0,0,255));
			System.out.println(rect);
		}else {
			System.out.println("ERROR: I havent found any sudoku in the image");
		}
		
	}
	
	
	
	
	//### Preprocessing
	public Mat preprocessImage(Mat img) {
		
		//empty image that will hold outer box of the image
		Mat outerBox = new Mat(img.size(), CvType.CV_8UC1);
		
		//smoothing the noise
		Imgproc.GaussianBlur(img,img, new Size(11,11),0);
		
		//Image Segmentation extract every element that is representing something if there are explicit elements
		Imgproc.adaptiveThreshold(img, img, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 5, 2);
		
		//reverse colors so border is white and background is black
		Core.bitwise_not(img, img);
		
		//connecting points if they lay at position containing "1" from point in the middle of mat representation of 3x3 grid of pixel and pixels around it
		//it makes shapes like numbers and lines more prominent
		
		kernel.put(0, 0, 0);
		kernel.put(0,1,1);
		kernel.put(0, 2, 0);
		kernel.put(1, 0, 1);
		kernel.put(1, 1, 1);
		kernel.put(1, 2, 1);
		kernel.put(2, 0, 0);
		kernel.put(2, 1, 1);
		kernel.put(2, 2, 0);
		//System.out.println(kernel.dump());
		Imgproc.dilate(img, outerBox, kernel);
		//System.out.println(outerBox.row(1).get(0, 20)[0]);
		//System.out.println(outerBox.toString());
		return outerBox;
		
	}
	

}
