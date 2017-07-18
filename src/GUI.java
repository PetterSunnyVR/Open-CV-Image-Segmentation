

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;

import util.ImageProcessor;
import util.visionUtils;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;


public class GUI {
	
/*	static{
		new LibraryLoader();
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		}*/

	private JLabel imageView;
	private JLabel originalImageLabel; 
	private String windowName;
	private Mat image, originalImage,originalImageAnnotated;

	private final ImageProcessor imageProcessor = new ImageProcessor();

	private int mouseX[] = {0,200,0  ,200};
	private int mouseY[] = {0,50,200,200};



	public GUI(String windowName, Mat newImage) {
		super();
		this.windowName = windowName;
		this.image = newImage;
		this.originalImage = newImage.clone();
		this.originalImageAnnotated = newImage.clone();
	}

	public void init() {
		setSystemLookAndFeel();
		initGUI();
	}

	private void initGUI() {
		JFrame frame = createJFrame(windowName);

		updateView();

		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);

	}




	private JFrame createJFrame(String windowName) {
		JFrame frame = new JFrame(windowName);
		frame.setLayout(new GridBagLayout());

		setupTypeRadio(frame);

		setupImage(frame);


		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		return frame;
	}


	private void setupTypeRadio(JFrame frame) {
			
		GridLayout gridRowLayout = new GridLayout(1,0);
		JPanel radioOperationPanel = new JPanel(gridRowLayout);

		JLabel pointLabel = new JLabel("Point:", JLabel.RIGHT);


		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;

		frame.add(pointLabel,c);
		c.gridx = 1;
		c.gridy = 0;
		frame.add(radioOperationPanel,c);

	}

	private void setupImage(JFrame frame) {
		JPanel imagesPanel = new JPanel();
		imageView = new JLabel();
		imageView.setHorizontalAlignment(SwingConstants.CENTER);

		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 2;

		originalImageLabel = new JLabel();
		Image originalAWTImage = imageProcessor.toBufferedImage(originalImage);
		originalImageLabel.setIcon(new ImageIcon(originalAWTImage));

		imagesPanel.add(originalImageLabel);
		imagesPanel.add(imageView);

		frame.add(imagesPanel,c);
		processOperation();
	}



	protected void processOperation() {
		visionUtils vision = new visionUtils(originalImage);
		vision.processImage();
		image = new Mat();
		image = vision.returnProcessedImage();
		/*Mat gray = new Mat();
		if(originalImage.channels()>1) {
			Imgproc.cvtColor(originalImage, gray, Imgproc.COLOR_BGR2GRAY);
		}else{
			originalImage.copyTo(gray);
		}
		System.out.println(originalImage.toString());
		System.out.println(gray.toString());
		image = new Mat();
		gray.copyTo(image);*/
		updateView();
	}

	private void setSystemLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	private void updateView() {
		Mat newMat = image;
		Image outputImage = imageProcessor.toBufferedImage(newMat);
		imageView.setIcon(new ImageIcon(outputImage));
		
		Mat originalAnnotatedMat = originalImageAnnotated;
		Image originalAnnotated = imageProcessor.toBufferedImage(originalAnnotatedMat);
		originalImageLabel.setIcon(new ImageIcon(originalAnnotated));
		
	}

}
