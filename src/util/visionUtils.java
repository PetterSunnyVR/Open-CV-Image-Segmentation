package util;

import java.awt.font.ImageGraphicAttribute;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.ImagePattern;

public class visionUtils {
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME);}
	
	static final ClassLoader loader = visionUtils.class.getClassLoader();
	
	
	private Mat kernel = new Mat(new Size(3,3), 0);
	private Mat originalImg = new Mat();
	//Mat image = Imgcodecs.imread(getClass().getResource(''));
	
	//### Grabbing the image
	public void readImage() {
		
		//OPEN IMAGE
		originalImg = Imgcodecs.imread("E:\\JAVA Projects\\OpenCv\\OCVImageSegmentation\\res\\shapes.jpg");
		
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
			ContoursSegmentation(gray);
		}
		
		

	}
	
	public void ContoursSegmentation(Mat img) {
		Mat edges = new Mat();
		//CANNY REDUCES A LOT OF NOISE SO FINDCONTOURS WILL BE MORE EFFECTIVE 
		Imgproc.Canny(img, edges, 30, 200);
		
		//FINDCONTOURS
		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarch = new Mat();
		
		Imgproc.findContours(edges, contours, hierarch, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
		
		System.out.println("NUMBER OF CONTOURS: "+contours.size());
		
		Imgproc.drawContours(originalImg, contours, -1, new Scalar(0,255,0),3);
		Imgcodecs.imwrite("K:\\gaussian3.jpg", originalImg);
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
			Imgcodecs.imwrite("K:\\gaussian3.jpg", originalImg);
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
