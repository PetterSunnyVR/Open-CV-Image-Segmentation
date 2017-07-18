

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import util.LibraryLoader;

public class App 
{
	static{
		new LibraryLoader();
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		}

	public static void main(String[] args) throws Exception {
		//String filePath = "src/main/resources/images/building.jpg";
		String filePath =  "D:\\workspace\\OCVImageSegmentation\\res\\sudoku.jpg";
		//String filePath = "E:\\JAVA Projects\\OpenCv\\OCVImageSegmentation\\res\\sudoku.jpg";
		Mat newImage = Imgcodecs.imread(filePath, Imgcodecs.CV_LOAD_IMAGE_ANYCOLOR);

		if(newImage.dataAddr()==0){
			System.out.println("Couldn't open file " + filePath);
		}else{

			GUI gui = new GUI("Warp Example", newImage);
			gui.init();
		}
		return;
	}
}