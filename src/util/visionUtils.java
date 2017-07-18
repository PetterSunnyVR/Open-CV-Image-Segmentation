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

import org.omg.CORBA.portable.BoxedValueHelper;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

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
	private Mat returnImage = new Mat();
	//Mat image = Imgcodecs.imread(getClass().getResource(''));
	
	//CONSTRUCTOR
	public visionUtils() {
		
	}
	
	public visionUtils(Mat img) {
		img.copyTo(originalImg);
	}
	
	//###PROCESS IMAGE
	public void processImage(){
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
			//test(gray);
			List<MatOfPoint> biggestContour = biggestContourSegmentation(gray);
			approximateContours(biggestContour);
			//findLines(biggestContour);
		}
	}
	
	//### Grabbing the image
	public void readImage() {
		
		//OPEN IMAGE
		originalImg = Imgcodecs.imread("D:\\workspace\\OCVImageSegmentation\\res\\sudoku.jpg");
		//originalImg = Imgcodecs.imread("E:\\JAVA Projects\\OpenCv\\OCVImageSegmentation\\res\\sudoku.jpg");
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
			//test(gray);
			List<MatOfPoint> biggestContour = biggestContourSegmentation(gray);
			approximateContours(biggestContour);
			//findLines(biggestContour);
		}
		
		

	}
	
	//FIND ALL CONTOURS
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
	
	//FIND BIGGEST CONTOUR
	public List<MatOfPoint> biggestContourSegmentation(Mat img){
		Mat edges = new Mat();
		//CANNY REDUCES A LOT OF NOISE SO FINDCONTOURS WILL BE MORE EFFECTIVE 
		Imgproc.Canny(img, edges, 30, 200);
		//unskewImage(edges);
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
		
		//Imgcodecs.imwrite("E:\\gaussian3.jpg", cropContour(contours.get(maxIndex), originalImg));
		List<MatOfPoint> bigContour = new ArrayList<>();
		bigContour.add(contours.get(maxIndex));
		
		return bigContour;
	}
	
	//PERSPECTIVE TRANSFORMATION - finding points
	public Mat unskewImage(List<MatOfPoint> contour){
		//calculate the center of mass of our contour image using moments
		Moments moment = Imgproc.moments(contour.get(0));
        int x = (int) (moment.get_m10() / moment.get_m00());
        int y = (int) (moment.get_m01() / moment.get_m00());
        //Imgproc.circle(originalImg, new Point(x, y), 4, new Scalar(255,49,0,255));
        
        
        //SORT POINTS FOR
        Point[] sortedPoints = new Point[4];
        Mat destImage = new Mat();
        double[] data;
        int count = 0;
        for(int i=0; i<contour.get(0).rows(); i++){
        	data = contour.get(0).get(i, 0);
        	double datax = data[0];
        	double datay = data[1];
        	if(datax < x && datay < y){
        		sortedPoints[0]=new Point(datax,datay);
        		count++;
        	}else if(datax > x && datay < y){
        		sortedPoints[1]=new Point(datax,datay);
        		count++;
        	}else if (datax < x && datay > y){
        		sortedPoints[2]=new Point(datax,datay);
        		count++;
        	}else if (datax > x && datay > y){
        		sortedPoints[3]=new Point(datax,datay);
        		count++;
        	}
        }
        
        
				
        
        if (count==4) {
        	MatOfPoint2f src = new MatOfPoint2f(
        			sortedPoints[0],
        			sortedPoints[1],
        			sortedPoints[2],
        			sortedPoints[3]);
        	
        	MatOfPoint2f dst = new MatOfPoint2f(
        			new Point(0, 0),
        			new Point(originalImg.width()-1,0),
        			new Point(0,originalImg.height()-1),
        			new Point(originalImg.width()-1,originalImg.height()-1)		
    				);

        	Mat warpMat = Imgproc.getPerspectiveTransform(src,dst);
        	
        	Imgproc.warpPerspective(originalImg, destImage, warpMat, originalImg.size());
        	
		}else{
			System.out.println("ERROR sorting points");
		}
		
        return destImage;
/*		for(int i =0; i < contour.get(0).rows(); i++){
			
		}
		Imgproc.getPerspectiveTransform(src, dst)*/
		
	}
	
	//approximating contours
	public void approximateContours(List<MatOfPoint> contour){
		MatOfPoint2f  contour2f = new MatOfPoint2f( contour.get(0).toArray() );
		MatOfPoint2f  approxContour = new MatOfPoint2f();
		double accuracy = 0.05 * Imgproc.arcLength(contour2f, true);
		Imgproc.approxPolyDP(contour2f, approxContour, accuracy, true);
		System.out.println(approxContour.dump());
		
		List<MatOfPoint> contoursToDraw = new ArrayList<>();
		
		contoursToDraw.add(new MatOfPoint(approxContour.toArray()));
		
		//Imgproc.drawContours(originalImg, contoursToDraw , -1, new Scalar(0,255,0));
		Mat perspectiveImg = unskewImage(contoursToDraw);
		if(!perspectiveImg.empty()){
			detectNumbers(perspectiveImg);
		}
		//Imgcodecs.imwrite("E:\\gaussian2.jpg", originalImg);
	}
	
	//Detect numbers
	public void detectNumbers(Mat img){
		int boxWidth = img.width()/9;
		int boxHeight = img.height()/9;
		Point pt1, pt2;
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {
				if(i!=8){
					if(j!=8){
						pt1 = new Point(j*boxWidth,i*boxHeight);
						pt2 = new Point(j*boxWidth+boxWidth,i*boxHeight+boxHeight);
						Imgproc.rectangle(img, pt1, pt2, new Scalar(0,255,0));
					}else{
						pt1 = new Point(j*boxWidth,i*boxHeight);
						pt2 = new Point(j*boxWidth+((img.width()-1) - j*boxWidth),i*boxHeight+boxHeight);
						Imgproc.rectangle(img, pt1, pt2, new Scalar(0,255,0));
					}
				}else{
					if(j!=8){
						pt1 = new Point(j*boxWidth,i*boxHeight);
						pt2 = new Point(j*boxWidth+boxWidth,i*boxHeight+((img.height()-1) - i*boxHeight));
						Imgproc.rectangle(img, pt1, pt2, new Scalar(0,255,0));
					}else{
						pt1 = new Point(j*boxWidth,i*boxHeight);
						pt2 = new Point(j*boxWidth+((img.width()-1) - j*boxWidth),i*boxHeight+((img.height()-1) - i*boxHeight));
						Imgproc.rectangle(img, pt1, pt2, new Scalar(0,255,0));
					}
				}

			}
			
		}
		
		returnImage = img;
		//img.copyTo(returnImage);
		//Imgcodecs.imwrite("E:\\gaussian2.jpg", img);
	}
	
	//HOUGH LINES
	public void findLines(List<MatOfPoint> contour) {
		Mat black = new Mat().zeros(originalImg.size(), originalImg.type());
		Imgproc.drawContours(black, contour, -1, new Scalar(0,255,0));
		Imgproc.cvtColor(black, black, Imgproc.COLOR_BGR2GRAY);
		Imgproc.GaussianBlur(black, black, new Size(11,11), 0);
		Imgproc.threshold(black, black, 0, 255, Imgproc.THRESH_BINARY);
		//Imgproc.adaptiveThreshold(black, black, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 5, 2);
		Mat lines =  new Mat();
		
		//detecting lines
		Imgproc.HoughLines(black, lines, 1, Math.PI/180, 310);
		System.out.println(lines.dump());
		
		 double[] data;
         double rho, theta;
         Point pt1 = new Point();
         Point pt2 = new Point();
         double a, b;
         double x0, y0;
         for (int i = 0; i < lines.rows(); i++)
         {
             data = lines.get(i, 0);
             rho = data[0];
             theta = data[1];
             a = Math.cos(theta);
             b = Math.sin(theta);
             x0 = a*rho;
             y0 = b*rho;
             pt1.x = Math.round(x0 + 1000*(-b));
             pt1.y = Math.round(y0 + 1000*a);
             pt2.x = Math.round(x0 - 1000*(-b));
             pt2.y = Math.round(y0 - 1000 *a);
             Imgproc.line(originalImg, pt1, pt2, new Scalar(0,255,0), 3);
         }
		
		Imgcodecs.imwrite("E:\\gaussian2.jpg", originalImg);
	}

	//HOUGH LINES TEST - OK
	public void test(Mat img) {
		Mat edges = new Mat();
		//CANNY REDUCES A LOT OF NOISE SO FINDCONTOURS WILL BE MORE EFFECTIVE 
		Imgproc.Canny(img, edges, 80, 100);

        //Imgproc.cvtColor(edges, edges, Imgproc.COLOR_GRAY2BGRA, 4);
		
        Mat lines = new Mat();
        Imgproc.HoughLines(edges, lines, 1, Math.PI/180, 240);
        
        System.out.println(lines.get(1,0)[1]);
        
        System.out.println(lines.dump());
		 double[] data;
		 double rho, theta;
		 Point pt1 = new Point();
		 Point pt2 = new Point();
		 double a, b;
		 double x0, y0;
		 for (int i = 0; i < lines.rows(); i++)
		 {
		     data = lines.get(i, 0);
		     rho = data[0];
		     theta = data[1];
		     a = Math.cos(theta);
		     b = Math.sin(theta);
		     x0 = a*rho;
		     y0 = b*rho;
		     pt1.x = Math.round(x0 + 1000*(-b));
		     pt1.y = Math.round(y0 + 1000*a);
		     pt2.x = Math.round(x0 - 1000*(-b));
		     pt2.y = Math.round(y0 - 1000 *a);
		     Imgproc.line(originalImg, pt1, pt2, new Scalar(0,255,0), 3);
		 }
		
		 Imgcodecs.imwrite("K:\\gaussian2.jpg", originalImg);
        
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
	public List<MatOfPoint> sortingContours(List<MatOfPoint> contours){
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
		
		return sortedList;
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
	
	public Mat returnProcessedImage(){
		return returnImage;
	}
}
