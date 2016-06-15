package utils;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
//import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class Util {

	// return a BufferedReader for a file in disk file system or inside a Jar
	// file if the first character of filename is '/'.
	// It the first character is not '/', only search in disk file system.

	public static InputStream getInputStreamFromDiskAndJar(String path) {

		InputStream in = null;
		if (path.charAt(0) == '/') {
			// get a inputstream for the file in Jar
			in = (new Util()).getClass().getResourceAsStream(path);
			// if failed to read from jar file, remove the first character of
			// file name and read directly from disk
			if (in != null) {
				return in;
			}
			else
				path = path.substring(1);
		}
		// read file from disk
		Logger.debug("Load data", "read disk file '%s'", path);
		try {
			in = new FileInputStream(path);
		}
		catch (IOException ex) {
			// not able to load the file from disk, check if there is a shortcut for the resource.
			String strRes="resource";
			int pos=path.toLowerCase().indexOf(strRes);
			if(pos>=0) {
				File f=new File("resource.lnk");
				 try {
					if(WindowsShortcut.isPotentialValidLink(f)) 
					{
						WindowsShortcut sc = new WindowsShortcut(f);
						if(sc.isDirectory()) {
							String resPath=sc.getRealFilename();
							String newPath=resPath+"\\"+path.substring(pos+strRes.length()+1);
							in = new FileInputStream(newPath);

						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				} 
				
			}
		}

		return in;
	}

	public static BufferedReader getBufferedReaderFromDiskAndJar(String path) throws IOException {
		InputStream in = getInputStreamFromDiskAndJar(path);
		if (in != null) {
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			return br;
		}
		return null;
	}

	public static byte[] readFile(String fileName) throws IOException {
		// Open file
		RandomAccessFile f = new RandomAccessFile(new File(fileName), "r");
		try {
			// Get and check length
			long longlength = f.length();
			int length = (int) longlength;
			if (length != longlength)
				throw new IOException("File size >= 2 GB");
			// Read file and return data
			byte[] data = new byte[length];
			f.readFully(data);
			return data;
		} finally {
			f.close();
		}
	}

	public static byte[] readFileFromDiskAndJar(String fileName) throws IOException {
		InputStream inStream = getInputStreamFromDiskAndJar(fileName);
		// Get the size of the file
		long streamLength = inStream.available();

		if (streamLength > Integer.MAX_VALUE) {
			// File is too large
			return null;
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int) streamLength];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = inStream.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file ");
		}

		// Close the input stream and return bytes
		inStream.close();
		return bytes;
	}

	public static String list2String(String[] list, String delimiter) {
		String str = "";
		boolean first = true;
		for (String name : list) {
			if (first)
				first = false;
			else
				str += delimiter;
			str += name;
		}
		return str;
	}

	public static String list2String(List<String> list, String delimiter) {
		String str = "";
		boolean first = true;
		for (String name : list) {
			if (first)
				first = false;
			else
				str += delimiter;
			str += name;
		}
		return str;
	}

	// When the name in the list contains the delimiter, the name is quoted
	public static String list2String(List<String> list, String delimiter, String quote) {
		String str = "";
		boolean first = true;
		for (String name : list) {
			if (first)
				first = false;
			else
				str += delimiter;
			if (name.indexOf(delimiter) > 0)
				// the name contains the character of delimiter
				str += quote + name + quote;
			else
				str += name;
		}
		return str;
	}

	static Hashtable<String, Integer> monthnumber = null;

	static int getMonthNumber(String month) throws Exception {
		String[] monthNames = { "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec" };
		if (monthnumber == null) {
			monthnumber = new Hashtable<String, Integer>();
			int cnt = 1;
			for (String amonth : monthNames)
				monthnumber.put(amonth.toLowerCase(), cnt++);
		}
		Integer im = monthnumber.get(month.toLowerCase());
		if (im != null)
			return im;
		else
			throw new Exception("wrong month name");
	}

	// Convert date in format "Nov 14, 2013" to "11/24/2013"
	static String convertDate1(String date) throws Exception {
		String[] md_y = date.split(", ");
		if (md_y.length != 2) {
			throw new Exception("Wrong date format: " + date);
		}
		String[] md = md_y[0].split(" ");

		if (md.length != 2) {
			throw new Exception("Wrong date format: " + date);
		}

		int im = getMonthNumber(md[0].toLowerCase());
		return "" + im + "/" + md[1] + "/" + md_y[1];
	}

	// Convert date in format "20-Oct-55" to "10/20/1955"
	static String convertDate0(String date) throws Exception {
		String[] dmy = date.split("-");
		if (dmy.length != 3) {
			throw new Exception("Wrong date format: " + date);
		}
		int im = getMonthNumber(dmy[1].toLowerCase());
		int iy = Integer.parseInt(dmy[2]);
		if (iy < 30)
			iy = 2000 + iy;
		else
			iy = 1900 + iy;
		return "" + im + "/" + dmy[0] + "/" + iy;
	}

	public static String convertDate(String date) throws Exception {
		try {
			return convertDate0(date);
		} catch (Exception ex) {
			return convertDate1(date);
		}
	}

	/*
	 * When a decimal number has character '0' the end, Excel will remove the
	 * chacracter '0' since it's data type is number To avoid the character '0'
	 * to be removed, we need to insert a non-digital number in the front of
	 * number, so Excel takes it as text, instead of a number. The character we
	 * selected to insert is '~'
	 * 
	 * This function removes character '~' in the front
	 */
	public static String digitalString(String text) {
		if (text.length() > 1 && text.charAt(0) == '~')
			return text.substring(1);
		else
			return text;
	}

	// Get a name form full name,
	// return
	// first name when type=1;
	// last name when type=2;
	// middle name when type=3;

	public static String getAName(String fullName, int type) {
		String[] names = fullName.split(" ");
		if (type == 1)
			return names[0];
		else if (type == 2 && names.length >= 2)
			return names[names.length - 1];
		else if (type == 3 && names.length >= 3) {
			String middleName = "";
			for (int i = 1; i < names.length - 1; i++) {
				if (names[i].length() == 0)
					continue;
				if (middleName.length() == 0)
					middleName = names[i].replace(" ", "");
				else
					middleName += " " + names[i].replace(" ", "");
			}
			return middleName;
		}
		return "";
	}

	public static void copyFile(File source, File dest) throws IOException {
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new FileInputStream(source);
			output = new FileOutputStream(dest);
			byte[] buf = new byte[1024];
			int bytesRead;
			while ((bytesRead = input.read(buf)) > 0) {
				output.write(buf, 0, bytesRead);
			}
		} finally {
			input.close();
			output.close();
		}
	}

	public static int getAge(long idob, long idate) {
		GregorianCalendar dob = new GregorianCalendar();
		GregorianCalendar date = new GregorianCalendar();
		dob.setTime(new Date(idob));
		date.setTime(new Date(idate));

		// Date dob = new Date(idob);
		// Date date = new Date(idate);
		int yDate = date.get(Calendar.YEAR);
		int yDOB = dob.get(Calendar.YEAR);
		int age = yDate - yDOB;
		int dec = 0;
		int mDate = date.get(Calendar.MONTH);
		int mDOB = dob.get(Calendar.MONTH);
		if (mDate < mDOB)
			dec = 1;
		else if (mDate == mDOB) {
			int dDate = date.get(Calendar.DAY_OF_MONTH);
			int dDOB = dob.get(Calendar.DAY_OF_MONTH);
			if (dDate < dDOB)
				dec = 1;
		}
		return age - dec;
	}

	public static boolean getBooleanFromMap(Map<String, Object> map, String name, boolean valueForNull) {
		if (map == null)
			return valueForNull;
		Object obj = map.get(name);
		if (obj == null)
			return valueForNull;
		return (Boolean) obj;
	}

	public static Short getShortFromMap(Map<String, Object> map, String name, Short valueForNull) {
		if (map == null)
			return valueForNull;
		Object obj = map.get(name);
		if (obj == null)
			return valueForNull;
		return (Short) obj;
	}

	public static String getStringFromMap(Map<String, Object> map, String name, String valueForNull) {
		if (map == null)
			return valueForNull;
		Object obj = map.get(name);
		if (obj == null)
			return valueForNull;
		return (String) obj;
	}
	
	public static Long getDateFromMap(Map<String, Object> map, String name) {
		Object obj = map.get(name);
		if (obj == null)
			return null;
		return (Long) obj;
	}

	public static long getTimestampOfDate(String dateText, String format) throws ParseException {
		if (format == null)
			format = "MM/dd/yyyy";
		SimpleDateFormat sdfDate = new SimpleDateFormat(format);
		// sdfDate.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = sdfDate.parse(dateText);
		return date.getTime();
	}

	public static String getDateTextOfTimestamp(long intdate, String format) throws ParseException {
		if (format == null)
			format = "MM/dd/yyyy";
		SimpleDateFormat sdfDate = new SimpleDateFormat(format);
		// sdfDate.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date date = new Date(intdate);
		String dateText = sdfDate.format(date);
		return dateText;
	}

	public static String getDateTextOfTimestamp(long intdate) throws ParseException {
		return getDateTextOfTimestamp(intdate, null);

	}

	public static long getTimestampOfDate(String dateText) throws ParseException {
		return getTimestampOfDate(dateText, null);
	}

	public static String indent(int nofChar) {
		String indent = "";
		for (int i = 0; i < nofChar; i++)
			indent += " ";
		return indent;
	}
	/*
	 * public static void main(String[] args) { try { long idob=
	 * getTimestampOfDate("03/01/2012"); long idate=
	 * getTimestampOfDate("03/02/2012"); int age=getAge(idob,idate);
	 * System.out.printf("Age=%d\n", age); } catch (ParseException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } }
	 */
}