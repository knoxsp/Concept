package scatterbox.utils;
import java.io.File;

import org.joda.time.DateTime;

public class BinaryToDecoded {
	/**
	 * The index of file name in a MIT binary file name. 
	 */
	private static final int MIT_FILENAME = 0;
	/**
	 * The index of time in a MIT binary file name.
	 */
	private static final int MIT_TIME = 1;
	/**
	 * read the binary file and convert it to decoded file.
	 * @param a_binaryfile the file name of the binary sensor file.
	 * @param a_decodedDir the directory for the decoded file.
	 * return a_decodedfile the file name of the decoded new file.
	 * 			the name is the same as the binary file but ends with ".decoded".
	 * @throws Exception 
	 * @throws Exception 
	 */
	public static final String binaryToDecoded(final String a_binaryfile, final String a_decodedDir) throws Exception {
		String[] partsOfFileName = a_binaryfile.split("\\.");
		String decodedfile = partsOfFileName[MIT_FILENAME]; 
		if (a_decodedDir != null && a_decodedDir.length() > 0) {
			String[] dirPath = partsOfFileName[MIT_FILENAME].split("/");
			decodedfile = a_decodedDir+"/"+dirPath[dirPath.length-1];	
		} 
		decodedfile = decodedfile +"."+partsOfFileName[MIT_TIME]+".decoded";
		MITesDataAnal mda = new MITesDataAnal(a_binaryfile);
		long timeLimit =  processTime(partsOfFileName[MIT_TIME]).getMillis();
		mda.processFile(decodedfile, timeLimit);
		return decodedfile;
	}
	/**
	 * read all the binary files in a directory and convert them into decoded files under the given directory.
	 * @param a_binaryDir the directory for all the binary files.
	 * @param a_decodedDir the directory for the new decoded files.
	 * @return
	 * @throws Exception
	 */
	public static final void binaryToDecodedAll(final String a_binaryDir, final String a_decodedDir) throws Exception {
		File directory = new File(a_binaryDir);
		String[] allFilesDirs = directory.list();
		for (int i= 0; i<allFilesDirs.length; i++) {
			if (allFilesDirs[i].contains("MITesNonAccelCompositeBytes") 
					&& allFilesDirs[i].endsWith(".b")) {
				binaryToDecoded(a_binaryDir+"/"+allFilesDirs[i], a_decodedDir);
			} else if (allFilesDirs[i].length() <=2) {
				File dir = new File(a_binaryDir+"/"+allFilesDirs[i]);
				if (dir.isDirectory()) {
//					String[] insideFiles = dir.list();
//					for(int j=0; j<insideFiles.length; j++) {
//						if (insideFiles[j].contains("MITesCompositeBytes")
//								&& insideFiles[j].endsWith(".b")) {
//							binaryToDecoded((a_binaryDir+"/"+allFilesDirs[i]+"/"+insideFiles[j]), a_decodedDir);
//							break;
//						}
//					}
				}
			}
		}
	}
	/**
	 * process the time part in the file name to get the starting time.
	 * @param timeExpression in a format "year-month-date-hour-minute-second-X"
	 * @return a starting time of the sense file.
	 */
	public static final DateTime processTime(final String timeExpression) {
		String[] partsOfTime = timeExpression.split("-");
		final int year = Integer.parseInt(partsOfTime[0].trim());
		final int month = Integer.parseInt(partsOfTime[1].trim());
		final int date = Integer.parseInt(partsOfTime[2].trim());
		final int hour = Integer.parseInt(partsOfTime[3].trim());
		final int minute = Integer.parseInt(partsOfTime[4].trim());
		final int second = Integer.parseInt(partsOfTime[5].trim());
		return new DateTime(year, month, date, hour, minute, second, 0);
	}
	
	public static void main(String[] args) throws Exception{
		binaryToDecodedAll("/Users/seamusknox/Documents/datasets/placelab/2006-09-14", "/Users/seamusknox/Documents/datasets/placelab/2006-09-14");
	}
	
}
