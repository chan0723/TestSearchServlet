/**
 * 
 */
package Utils;

import java.io.*;
//import java.io.StringWriter;
//import java.net.Socket;
import java.nio.file.Files;
import java.util.Scanner;


/**
 * @author dichenli
 * static methods to facilitates IO classes
 */
public class IOUtils {
	
	/**
	 * count number of lines of a file using its scanner to scan line by line
	 * return number of lines. Return null if input file is illegal. No exception thrown.
	 * @param file
	 * @return
	 */
	public static Long countLines(File file) {
		if(!fileExists(file)) {
			return null;
		}
		Scanner sc = getScanner(file);
		long count = 0;
		while(sc.hasNextLine()) {
			sc.nextLine();
			count++;
		}
		sc.close();
		return count;
	}

	public static BufferedReader getReader(
			InputStream is) throws IOException {
		if(is == null) {
			throw new IOException();
		}
		InputStreamReader isr = null;
		BufferedReader in = null;
		isr = new InputStreamReader(is);
		in = new BufferedReader(isr);
		return in;
	}
	
	/**
	 * open an scanner to a file, no exception thrown, return null for abnormals
	 * @param file
	 * @return
	 */
	public static Scanner getScanner(File file) {
		if(file == null || !file.exists() || file.isDirectory()) {
			return null;
		}
		try {
			Scanner sc = new Scanner(file);
			return sc;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * suitable for writing characters
	 * @param socket
	 * @return
	 * @throws IOException 
	 */
	public static PrintWriter getWriter(
			OutputStream os) throws IOException {
		if(os == null) {
			throw new IOException();
		}
		
		PrintWriter writer = null;
		writer = new PrintWriter(os);
		return writer;
	}
	
	/**
	 * get the printwriter of a file, no null return, but throws IOException
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public static PrintWriter getWriter(File file) throws IOException {
		OutputStream out = new FileOutputStream(file);
		return getWriter(out);
	}
	
	/**
	 * is a valid file, exists, and not direcotry, not null
	 * @param file
	 * @return
	 */
	public static boolean isValidFile(File file) {
		if(file == null || !file.exists() || !file.isFile()) {
			return false;
		}
		return true;
	}

	/**
	 * is directory and directory exists
	 * @param relativePath, null is not allowed
	 * @return
	 */
	public static boolean dirExists(File file) {
		return file.exists() && file.isDirectory();
	}

	public static boolean dirExists(String fileName) {
		return dirExists(new File(fileName));
	}
	
	public static String fullPath(String rootDir, String relativePath) {
		while (relativePath.charAt(0) == '/') {
			relativePath = relativePath.substring(1);
		}
		return rootDir + relativePath;
	}

	public static File getFile(String rootDir, String relativePath) {
		String path = fullPath(rootDir, relativePath);
		return new File(path);
	}

	
	/**
	 * return true if the file exists and is a file (not directory)
	 * @param file
	 * @return
	 */
	public static boolean fileExists(File file) {
		return file != null && file.isFile() && file.exists();
	}

	/**
	 * return true if the given file is a directory, exists, and has files in it 
	 * @param dir
	 * @return
	 */
	public static boolean containsFile(File dir) {
		if(!dirExists(dir)) {
			return false;
		}
		return dir.listFiles().length > 0;
	}

	/**
	 * Check if a folder exists and is a directory, create the folder if
	 * it doesn't exist. 
	 * It doesn't delete any existing 
	 * files in the folders. But it will delete the file with the same 
	 * name as the folder we need to create
	 * 
	 * @param folder
	 * @param root
	 * @param relPath
	 * @return true if folder exists or was successfully created
	 */
	public static boolean createFolder(File folder) {
		if(folder == null) {
			return false;
		}
		System.out.println("IOUtils.createFolder: " + folder.getAbsolutePath());
		if(folder.exists() && !folder.isDirectory()) { //a file of the same name exists
			System.out.println("Delete folder: " + folder.getAbsolutePath());
			boolean success = folder.delete();
			if(!success) { //can't delete it
				System.err.println("IOUtils.createFolder: can't delete folder");
				return false;
			}
		}
		System.err.println("IOUtils.createFolder: folder exists");
		if(folder.exists() && folder.isDirectory()) {
			return true; //the folder already exists, no need to create new
		}
		System.err.println("IOUtils.createFolder: make folder");
		return folder.mkdirs();
	}

	/**
	 * delete all files in a folder, but not the folder itself
	 * @param folder
	 * @return false if argument folder is invalid or some file can't be
	 * deleted.
	 */
	public static boolean clearFolder(File folder) {
		if(folder == null || !folder.exists() || !folder.isDirectory()) {
			return false;
		}
		
		File[] files = folder.listFiles();
		if(files == null) {
			return false; //if not folder, it will be null. For a folder, it will return empty array rather than null
		}
		
		boolean success = true;
		for(File f : files) { //delete each file
			System.out.println("Delete file... " + f.getAbsolutePath());
			success &= f.delete();
		}
		return success;
	}
	
	/**
	 * create a file to write to. It first check if the original file exists, if do, it will
	 * first try to delete the file. So it may delete what you had!
	 * if file is null, false is returned
	 * @param file
	 * @return false for any abnormal
	 */
	public static boolean createFile(File file) {
		if(file == null) {
			return false;
		}
		if(file.exists()) {
			System.out.println("Delete file... " + file.getAbsolutePath());
			file.delete();
		}
		try {
			System.out.println("Create file... " + file.getAbsolutePath());
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
}
