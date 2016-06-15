package utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*; 

public class DataTable {

	public List <List <String>> table;
	public int rowSize;
	public int columnSize;
	public boolean numberEncoded=false; // if true, put character '~' in the front of a number cell when output to a file
	public static int ERR_FileNotFound = 1;
	public static int ERR_FileReadError = 2;
	public static int ERR_NoFileDefined = 3;
	
	public DataTable()
	{
		table= new ArrayList<List <String>>();
		List <String> row= new ArrayList <String>();
		row.add("");
		table.add(row);
		rowSize=1;
		columnSize=1;
	}
	
	public void clear()
	{
		table.clear();
		rowSize=0;
		columnSize=0;
	}
	private void extendRow(int n) {
		for(int i=0;i<n;i++) {
			List <String> row= new ArrayList <String>();
			for(int j=0;j<columnSize;j++) {
				row.add("");
			}
			table.add(row);
		}
		rowSize+=n;
	}
	
	private void extendColumn(int n) {
		for(int i=0;i<rowSize;i++) {
			List <String> row= table.get(i);
			for(int j=0;j<n;j++) {
				row.add("");
			}
		}
		columnSize+=n;
	}
	
	public List <String> GetRow(int rowIndex)
	{
		if (rowIndex<rowSize)
			return table.get(rowIndex);
		else
			return null;
	}

	public String GetItem(int rowIndex,int columnIndex)
	{
		String result="";
		if (rowIndex<rowSize) {
			List <String> row =table.get(rowIndex); 
			if ( columnIndex<row.size()) {
				result=row.get(columnIndex);
			}
		}
		return result;
	}

	public void SetItem(int rowIndex,int columnIndex,double value)
	{
		SetItem(rowIndex,  columnIndex,null, value);
	}
	
	public void SetItem(int rowIndex,int columnIndex,String format,double value)
	{
		if(format==null)
			format="";
		SetItem(rowIndex,  columnIndex,String.format("%"+format+"f", value));
	}
	
	public void SetItem(int rowIndex,int columnIndex,String value)
	{
		if(columnIndex>=columnSize)
			extendColumn(1+columnIndex-columnSize);
		
		if(rowIndex>=rowSize)
			extendRow(1+rowIndex-rowSize);
			
		if (rowIndex<rowSize) {
			List <String> row =table.get(rowIndex); 
			if ( columnIndex<row.size()) {
				row.set(columnIndex,value);
			}
		}
	}
	
	public boolean isNumber(String text)
	{
		int nofDigit=0;
		int nofOfDot=0;
		for(int i=0;i<text.length();i++) {
			char ch=text.charAt(i);
			if(i==0 && ch=='~')
				continue;
			if(ch>='0' && ch<='9') {
				nofDigit++;
				continue;
			}
			else if (ch=='.') { 
				if(nofOfDot>0)
					return false;
				nofOfDot++;
				continue;
			}
			else if (ch=='-') { 
				if(nofDigit>0)
					return false;
				nofDigit++;
				continue;
			}
			else
				return false;
		}
		return true;
	}
	
	// remove character '~' from the number
	public String NumberDecode(String number) {
		if(number!=null && number.length()>0) {
			if (number.charAt(0)=='~') {
				if (isNumber(number))
					return number.substring(1);
			}
		}
		return number;
	}
	
	// add character '~' to the number
	public String NumberEncode(String number) {
		if(number!=null && number.length()>0) {
			if (number.charAt(0)=='~')
				return number;
			else {
				if (isNumber(number)) {
					if(numberEncoded)
						return "~"+number;
					else 
						return number;
				}
			}
		}
		return number;
	}
	
	public List <String> getCSVRow(String line,char delimiter)
	{
		//Logger.debug("Load data","Line: %s",line);
		List <String> row= new ArrayList<String>();
		int len=line.length();
		int p=0;
		
		// A row is presented in a line, each item is separated character ',',
		// when the text of an item contains character ',', the item is presented by putting a character '"' at then begin and end of the item
		// for example, if the item text contains only one comma, this item is presented as ","
		// if the item text contains character ", it is presented as 2 characters of ",
		// Here are more examples of items presented
		// ,   --> ","
		// "   --> """"
		// ""  --> """"""
		// "," --> ""","""
		// 1"  --> "1"""
		// parsing CSV line:
		// scan the for value, when meet character '"', the inText state is set as true. when inText state is true and meets '"', 
		// if there is another '"' after it, character '"' is decoded, otherwise inText state turns back to false.
		// when inText is false, character ',' or end of line will terminate the search for item text

		boolean inText=false; 
		String item="";
		while (p<len) {
			char c=line.charAt(p);
			if (inText) {
				if (c=='"') {
					if (p+1<len && line.charAt(p+1)=='"') {
						item+='"';
						p++;
					}
					else
						inText=false;
				}
				else 
					item+=c;
			}
			else {
				// state: inText is false
				if(c=='"') 
					inText=true;
				else if(c==delimiter) {
					row.add(NumberDecode(item));
					item="";
				}
				else
					item+=c;
			}
			p++;
		}
		row.add(NumberDecode(item));
		return row;
	}

	String readAFullLine(BufferedReader br) throws Exception {
		String result="";
		int cnt=0; // count of charact "
		while(true) {
			String line = br.readLine();
			if(line==null)
				return null;
			result+=line;
			for(int i=0;i<line.length();i++)
				if(line.charAt(i)=='"')
					cnt++;
			if(cnt%2==0)
				break;
			result+="\n";
		}
		return result;
	}
	
	public int LoadFromCSV(String fileName) {
		//Logger.debug("Load data","Load table from CSV file:"+fileName);
		BufferedReader br = null;
		String line = "";
		int err=0;
		
		clear();
		
		try {
			//br = new BufferedReader(new FileReader(fileName));
			br=Util.getBufferedReaderFromDiskAndJar(fileName);
			if(br==null) {
				return ERR_FileReadError;
			}
//			while ((line = br.readLine()) != null) {
			while ((line = readAFullLine(br)) != null) {
				// Logger.debug("print","line= %s",line);
				// check if this line end with " pair
				List <String> row = getCSVRow(line,',');
				if ( row.size()>columnSize)
					columnSize=row.size();
				table.add(row);
				rowSize++;
			}
	 
		} catch (Exception e) {
			e.printStackTrace();
			err=ERR_FileNotFound;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return err;
	}

	
	private String csvEncode(String text,String delimiter)
	{
		if(text==null)
			return "null";
		String encoded="";
		boolean hasSpecialChar=false;
		int length=text.length();
		if (length>0) {
			for(int i=0;i<length;i++) {
				char ch=text.charAt(i);
				if(ch==delimiter.charAt(0) || ch=='"' || ch=='\n') {
					hasSpecialChar=true;
					continue;
				}
			}
			
			if(hasSpecialChar) {
				encoded="\"";
				for(int i=0;i<length;i++) {
					char ch=text.charAt(i);
					if( ch=='"') 
						encoded+="\"";
					encoded+=ch;
				}
				encoded+="\"";
			}
			else 
				return text;
				
		}
		return encoded;
	}

//	static boolean ChangeNumberCoding=false;
	public int SaveToFile(String fileName,String delimiter) {
		return SaveToFile(fileName,delimiter,true);
	}
	
	public int SaveToFile(String fileName,String delimiter,boolean changeNumberCoding) {
		int err=0;
		FileWriter file=null;
		try {
			file= new FileWriter(fileName);
			// find the width of each column;
			for(int row=0;row<=rowSize;row++) {
				boolean firstColumn=true;
				for(int col=0;col<=columnSize;col++) {
					String value=GetItem(row,col);
					// encode the value for special characters
					String encoded="";
					
					if(firstColumn)
						firstColumn=false;
					else
						encoded=delimiter;
					
					
					if(changeNumberCoding)
						encoded+=csvEncode(NumberEncode(value),delimiter);
					else
						encoded+=csvEncode(value,delimiter);
					file.write(encoded);	
				}
				file.write("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
			err=1;
		}
		finally {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return err;
	}

	public int SaveToCSV(String fileName) {
		return SaveToFile(fileName,",");
	}

	public int SaveToCSV(String fileName,boolean changeNumberCoding) {
		return SaveToFile(fileName,",",changeNumberCoding);
	}
	
	public int SaveToTSV(String fileName) {
		return SaveToFile(fileName,"\t");
	}
	
	public int SaveToTSV(String fileName,boolean changeNumberCoding) {
		return SaveToFile(fileName,"\t",changeNumberCoding);
	}
	
/*		
	public List <String> getTSVRow(String line)
	{
		//Logger.debug("Get Data","Line: %s",line);
		List <String> row= new ArrayList<String>();
		String[] values = line.split("\\t");
		for (String value : values) {
			row.add(value);
		}
		return row;
	}
*/
	public int LoadFromTSV(BufferedReader br) throws IOException{
		return LoadFromBuffered(br,'\t');
	}

	public int LoadFromCSV(BufferedReader br) throws IOException{
		return LoadFromBuffered(br,',');
	}
	
	public int LoadFromBuffered(BufferedReader br,char delimiter) throws IOException{
		String line = "";
		int err=0;
		
		clear();
		
		try {
			while ((line = readAFullLine(br)) != null) {
			// Logger.debug("Get Data","line= %s",line);
				List <String> row = getCSVRow(line,delimiter);
			if ( row.size()>columnSize)
				columnSize=row.size();
			table.add(row);
			rowSize++;
		}
		} catch (Exception e) {
			e.printStackTrace();
			err=1;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return err;
	}

	public int LoadFromTSV(byte[] data) {
		InputStream is = new ByteArrayInputStream(data);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		int err=0;
		try {
			LoadFromTSV(br);
		}catch (IOException e) {
			e.printStackTrace();
			err=1;
		}
		return err;
	}
	
	public int LoadFromCSV(byte[] data) {
		InputStream is = new ByteArrayInputStream(data);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		int err=0;
		try {
			LoadFromCSV(br);
		}catch (IOException e) {
			e.printStackTrace();
			err=1;
		}
		return err;
	}
	

	
	// Load table from Tab Delimited Value file
	public int LoadFromTSV(String fileName) {
		//Logger.debug("Load data","Load table from Tab Delimited Value file:"+fileName);
		BufferedReader br = null;
		int err=0;
		try {
			br=Util.getBufferedReaderFromDiskAndJar(fileName);
			LoadFromTSV(br);
		}
		catch (IOException e) {
			e.printStackTrace();
			err=1;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return err;
	}
	
	// Load table from Tab Delimited Value file
	public int LoadFromTSV_1(String fileName) {
		//Logger.debug("Load data","Load table from Tab Delimited Value file:"+fileName);
		BufferedReader br = null;
		String line = "";
		int err=0;
		try {
			br = new BufferedReader(new FileReader(fileName));
			while ((line = br.readLine()) != null) {
				// Logger.debug("Get Data","line= %s",line);
				List <String> row = getCSVRow(line,'\t');
				if ( row.size()>columnSize)
					columnSize=row.size();
				table.add(row);
				rowSize++;
			}
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			err=1;
		} catch (IOException e) {
			e.printStackTrace();
			err=2;
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return err;
	}
	
	
	public void print()
	{
		Logger.debug("print","Row=%d, Column=%d",rowSize,columnSize);
	}

	public void printData(int row1,int col1,int row2,int col2) 
	{
		if(row2<row1 || row2>=rowSize)
			row2=rowSize-1;
		
		if(col2<col1 || col2>=columnSize)
			col2=columnSize-1;

		Integer[] colWidth = new Integer[columnSize];

		for(int col=col1;col<=col2;col++) 
			colWidth[col]=0;
		// find the width of each column;
		for(int row=row1;row<=row2;row++) {
			for(int col=col1;col<=col2;col++) {
				int length=GetItem(row,col).length();
				if (length>colWidth[col])
					colWidth[col]=length;
			}
		}
		
		String text=String.format("Data in (%d, %d)-(%d,%d)\n",row1, col1, row2, col2);
		// print the column name
		text+="     ";
		boolean first=true;
		for(int col=col1;col<=col2;col++) {
			if (first)
				first=false;
			else
				text+=" |";
			
			//String format=String.format("\\%%ds", colWidth[col]+1);
			String format="%-"+(colWidth[col]+1)+"s";
			text+=String.format(format, ExcelColumnName(col));
		}
		text+="\n\n";
		
		// print the contain of data
		for(int row=row1;row<=row2;row++) {
			text+=String.format("%4d ", row+1);
			first=true;
			for(int col=col1;col<=col2;col++) {
				if (first)
					first=false;
				else
					text+=" |";
				String format="%-"+(colWidth[col]+1)+"s";
				String value=GetItem(row,col);
				text+=String.format(format, value.replace("\n", "\\n"));
			}
			text+="\n";
		}
		Logger.debug("print", text);
	}

	public void printData() 
	{
		printData(0,0,rowSize-1,columnSize-1);
	}
	
	
	static public String ExcelColumnName(int index)
	{
		String name="##";
		if (index<26)
			name= Character.toString ((char)('A'+ index));
		else if (index<26*26)
			name= Character.toString ((char)('A'+ (index/26-1)))+ Character.toString ((char)('A'+ (index%26)));
		return name;
	}
	

	static public int LetterToInt(char c)
	{
		if(c>='A' && c<='Z') 
			return c-'A';
		if(c>='a' && c<='z') 
			return c-'a';
		return -1;
	}
	
	static public int ExcelNumber(String columnLabel)
	{
		int value=0;
		if(columnLabel.length()==1) {
			return LetterToInt(columnLabel.charAt(0));
		}
		else if(columnLabel.length()==2) {
			return 26+26*LetterToInt(columnLabel.charAt(0))+LetterToInt(columnLabel.charAt(1));
		}
		return value;
	}
	
	
	//======================= Unit test
	
	
	
	public static void UnitTest() {
		Logger.debug("Unit Test","DataTest started");
		DataTable data= new DataTable();
		List <String> items = data.getCSVRow("\",\",\"\"\"\"",',');
		for( String item:items) {
			Logger.debug("Unit Test","item:%s",item);
		}
		
		data.LoadFromCSV("data\\preferred_drug_list_test1.csv");
		
		// list raw table
		if(true) {
			for(List <String> row: data.table) {
				for (String item: row) {
					Logger.debug("Unit Test","%s; ",item);
				}
			}
		}
		
		// list table by raw index and column index
		if(false) {
			Logger.debug("Unit Test","Title:");
			int row=0;
			
			for(int column=0;column<data.columnSize;column++) {
				String item=data.GetItem(row, column);
				Logger.debug("Unit Test","column %d: %s",column,item);
			}

			int column=1;
			for(row=1;row<data.rowSize;row++) {
				String item=data.GetItem(row, column);
				Logger.debug("Unit Test","row %d: %s",row,item);
			}
		}
		Logger.debug("Unit Test","===== Data Table =====");
		//data.printData(0,0,2,2);
		data.printData();
	}
	
	public static void UnitTest_setData() {
		System.out.printf("UnitTest_setData begin");
		DataTable data= new DataTable();
		data.SetItem(10, 8, "Item(10,8)");
		for(int i=0;i<=10;i++)
			data.SetItem(i, i, "V"+i);
		data.SetItem(0, 0, ",+,");
		data.SetItem(0, 1, ",");
		data.SetItem(0, 2, ",,");
		data.SetItem(0, 3, "\"");
		data.SetItem(0, 4, "\"\"");
		data.SetItem(0, 5, "\"+\"");
		data.SetItem(1 ,0, "line1\nline2\nline3");
		
//		data.SetItem(0, 0, "Item(10,8)");
		data.printData();
		data.SaveToCSV("c:\\pgxdata\\test_out.csv");
		data.SaveToTSV("c:\\pgxdata\\test_out.txt");
		System.out.printf("UnitTest_setData END");
	}

	public static void UnitTest_Number() {
		System.out.printf("UnitTest_Number begin");
		DataTable data= new DataTable();
		data.SetItem(0, 0, "ABC");
		data.SetItem(0, 1, "000001234");
		data.SetItem(1, 0, "Big number");
		data.SetItem(1, 1, "123400000000000");
		
		data.SaveToCSV("c:\\pgxdata\\test_number.csv");

		DataTable data1= new DataTable();
		data1.LoadFromCSV("c:\\pgxdata\\test_number.csv");
		
		for(int i=0;i<2;i++)
			for(int j=0;j<2;j++) {
				System.out.printf("Item(%d,%d)=%s\n",i,j,data1.GetItem(i, j));
			}
				
		
		System.out.printf("UnitTest_number END");
	}

	// Add character '~' to the number in the file
	public static void UnitTest_NumberFormat(String fileName) {
		System.out.printf("UnitTest_Number begin");
		DataTable data= new DataTable();
		data.LoadFromCSV(fileName);
		data.SaveToCSV(fileName);
		System.out.printf("UnitTest_number END");
	}
	
	public static void main(String[] args) {
		//UnitTest_setData();
		//UnitTest_Number();
		UnitTest_NumberFormat("C:\\code\\billingPlugin\\code\\Plugin_Debug\\res\\Data_Request.csv");
	}
}
