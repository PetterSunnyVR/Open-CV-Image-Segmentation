package util;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.util.Date;

public class LibraryLoader {
	
private static String path = "";

static {
	
	try {
		path = getPath();
		//System.out.println(path+"\\opencv_java310.dll");
		System.load(path+"\\opencv_java310.dll");
		System.load(path+"\\opencv_ffmpeg310_64.dll");
	} catch (UnsatisfiedLinkError e) {
		try {
			loadLib(path);
			//System.out.println(new File(LibraryLoader.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
			//System.out.println(path+"\\opencv_java310.dll");
			System.load(path+"\\opencv_java310.dll");
			System.load(path+"\\opencv_ffmpeg310_64.dll");
		}catch(UnsatisfiedLinkError ex) {
			ex.printStackTrace();
		}
		
	} 
}

public static String getPath() {
	File f = new File(System.getProperty("java.class.path"));
	File dir = f.getAbsoluteFile().getParentFile();
	String newString = dir.toString();
	if(newString.contains(";")) {
		newString=newString.substring(newString.indexOf(';')+1);
		System.out.println(newString);
	}
	return newString;
}

private static void loadLib(String path) {
	// load opencv_java310.dll
	InputStream in = LibraryLoader.class.getClassLoader().getResourceAsStream("opencv_java310.dll");
	File fileout = new File(path+"\\opencv_java310.dll");
	try(FileOutputStream fos = new FileOutputStream(fileout);){
		byte[] buf = new byte[2048];
		int r;
		while(-1!=(r = in.read(buf))) {
			fos.write(buf, 0, r);
		}
	}catch(Exception ex){
		
	}
	
	//load opencv_ffmpeg310_64
	in = LibraryLoader.class.getClassLoader().getResourceAsStream("opencv_ffmpeg310_64.dll");
	fileout = new File(path+"\\opencv_ffmpeg310_64.dll");
	try(FileOutputStream fos = new FileOutputStream(fileout);){
		byte[] buf = new byte[2048];
		int r;
		while(-1!=(r = in.read(buf))) {
			fos.write(buf, 0, r);
		}
	}catch(Exception ex){
		
	}
}



}
