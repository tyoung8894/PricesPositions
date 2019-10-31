/**
 * Model.java 1.0 October 29, 2019
 *
 * Author: Tyler Young
 */

package young.tyler.positions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellType;
import org.codehaus.plexus.util.StringUtils;

public class Model implements IModel {
	ArrayList<ViewObserver> viewObservers = new ArrayList<ViewObserver>();
	String display = "";
	String val1String = "";
	String val2String = "";
	String xlsFilePath = "";
	String txtFilePath = "";
	StringBuilder sb = new StringBuilder();

	//set the text to string representing file's contents, notify observers to update text
	public void setDisplay(String input) {
		display = input;
		notifyViewObservers();
	}

	//return current display text
	public String getDisplay() {
		return display;
	}


	//clear the text
	public void clear() {
		sb.setLength(0);
		setDisplay("");
	}

	//save the current loaded/displayed file as txt
	public void save(String filePath) {
		try {
			File newTextFile = new File(filePath);
			FileWriter fw = new FileWriter(newTextFile);
			fw.write(sb.toString());
			fw.close();
		} catch (IOException iox) {
			iox.printStackTrace();
		}

	}


	//creates positions .txt file string to display
	public String displayPositionsFile(String fileName) {
		txtFilePath = fileName;
		BufferedReader in;
		String line;
		try {
			in = new BufferedReader(new FileReader(txtFilePath));
			while((line = in.readLine()) != null)
			{
				sb.append(line + "\n");
				System.out.println(line);
			}
			in.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}


	//creates updated price xls file string to display
	public String displayPriceFile(String fileName) throws IOException {
		xlsFilePath = fileName;
		InputStream excelPriceFile;
		excelPriceFile = new FileInputStream(xlsFilePath);
		HSSFWorkbook wb = new HSSFWorkbook(excelPriceFile);	
		HSSFSheet sheet = wb.getSheetAt(0);
		HSSFRow row; 
		HSSFCell cell;
		Iterator rows = sheet.rowIterator();

		while (rows.hasNext())
		{
			row=(HSSFRow) rows.next();
			Iterator cells = row.cellIterator();
			while (cells.hasNext())
			{
				cell=(HSSFCell) cells.next();

				if (cell.getCellType() == CellType.STRING)
				{
					sb.append(cell.getStringCellValue()+" ");
				}
				else if(cell.getCellType() == CellType.NUMERIC)
				{
					sb.append(cell.getNumericCellValue()+" ");
				}
				else if(cell.getCellType() == CellType.FORMULA)
				{
					sb.append(cell.getCellFormula());
				}
			}
			sb.append("\n");
		}	

		excelPriceFile.close();
		wb.close();
		return sb.toString().trim();
	}


	//creates a map of the Options in the input xls file containing updated prices
	public HashMap<String, String[]> parseUpdatedPriceFile() throws IOException {
		InputStream excelPriceFile;
		excelPriceFile = new FileInputStream(xlsFilePath);
		HSSFWorkbook wb = new HSSFWorkbook(excelPriceFile);	
		HSSFSheet sheet = wb.getSheetAt(0);

		HashMap<String, String[]> map = new HashMap<String, String[]>();
		String[] lstPrices;

		HSSFRow row; 
		HSSFCell tickerCell;
		HSSFCell oldPriceCell;
		HSSFCell newPriceCell;

		Iterator rows = sheet.rowIterator();

		while (rows.hasNext())
		{
			row=(HSSFRow) rows.next();

			if(row.getCell(8) != null) {
				if(row.getCell(8).getCellType() == CellType.STRING) {
					if(row.getCell(8).getStringCellValue().equals("Option")) { //secType cell, only care about options
						if(row.getCell(1) != null) { //if ticker cell is populated 		
							tickerCell =(HSSFCell)row.getCell(1);
							if(tickerCell.getCellType() == CellType.STRING) {							
								lstPrices = new String[2];

								if(row.getCell(5) != null) { //oldPrice
									oldPriceCell = (HSSFCell)row.getCell(5);
									if(oldPriceCell.getCellType() == CellType.NUMERIC) {
										lstPrices[0] = Double.toString(oldPriceCell.getNumericCellValue()); 
									} 
								}

								if(row.getCell(9) != null) {  //newPrice			
									newPriceCell = (HSSFCell)row.getCell(9);
									if(newPriceCell.getCellType() == CellType.NUMERIC) {	
										lstPrices[1] = Double.toString(newPriceCell.getNumericCellValue()); 
									}
								} else {
									lstPrices[1] = lstPrices[0];						
								}
								map.put(tickerCell.getStringCellValue(), lstPrices);
							}
						} 

					}				
				}
			}
		}
		return map;
	}


	//creates a new position file string to display
	public String parsePositionsFile(HashMap<String, String[]> map) {	
		BufferedReader in;
		String line;
		String secType;
		String putCall;
		String tradingSymbol;
		String year;
		String month;
		String day;
		String strikeDollar;
		String strikeFraction;
		StringBuilder sb2 = new StringBuilder();
		StringBuilder result = new StringBuilder();

		try {
			in = new BufferedReader(new FileReader(txtFilePath));

			//handle header record
			if((line = in.readLine()) != null) {
				sb2.setLength(0);
				sb2.append(line);
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYYMMdd");
				sb2 = sb2.replace(8, 17, LocalDate.now().format(formatter));
				result.append(sb2.toString());
				result.append("\n");
			}

			//handle detail records
			while((line = in.readLine()) != null)
			{	
				sb2.setLength(0);
				sb2.append(line);
				secType = sb2.substring(43,44);
				if(secType.equals("O")) {									
					putCall = sb2.substring(18, 19);
					tradingSymbol = sb2.substring(19, 25).trim();
					year = sb2.substring(27, 29);
					month = sb2.substring(29, 31);
					day = sb2.substring(31, 33);
					strikeDollar = StringUtils.stripStart(sb2.substring(33, 38), "0");						
					strikeFraction = StringUtils.stripEnd(sb2.substring(38, 42), "0");			

					String keyValue = tradingSymbol + year + month + day + putCall + strikeDollar;

					if(!strikeFraction.equals("")) {
						keyValue = keyValue + "." + strikeFraction;			
					}

					if(map.containsKey(keyValue)) {
						String[] values = map.get(keyValue);

						String newPrice = values[1]; 
						BigDecimal bd = new BigDecimal(newPrice);
						bd = bd.setScale(6, RoundingMode.HALF_UP);

						newPrice = bd.toString();		
						newPrice = StringUtils.leftPad(newPrice,13,"0").replace(".", "");

						sb2 = sb2.replace(44, 56, newPrice); //replace with updated price
						sb2.append("\n");

						result.append(sb2.toString());
					} 
				} else {
					result.append(sb2.toString());
					result.append("\n");
				}

			}
			in.close();

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		sb = result;
		return result.toString();
	}


	public void registerObserver(ViewObserver o) {
		viewObservers.add(o);
	}

	public void removeObserver(ViewObserver o) {
		int i = viewObservers.indexOf(o);
		if (i >= 0) {
			viewObservers.remove(i);
		}
	}

	public void notifyViewObservers() {
		for(int i = 0; i < viewObservers.size(); i++) {
			ViewObserver observer = (ViewObserver)viewObservers.get(i);
			observer.updateText();
		}
	}





}
