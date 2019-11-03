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
import java.util.Iterator;
import java.util.Map;

import javax.swing.JOptionPane;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.codehaus.plexus.util.StringUtils;

public class Model implements IModel {
	ArrayList<ViewObserver> viewObservers = new ArrayList<ViewObserver>();
	String display = "";
	String txtFilePath = "";
	StringBuilder sb = new StringBuilder();
	boolean positionsLoaded = false;
	boolean pricesLoaded = false;
	Map<String, String[]> updatedPricesMap = null;

	//set the text to string representing file's contents, notify observers to update text
	@Override
	public void setDisplay(String input) {
		display = input;
		notifyViewObservers();
	}

	//return current display text
	@Override
	public String getDisplay() {
		return display;
	}

	@Override
	public void setMap(Map<String, String[]> map) {
		updatedPricesMap = map;
	}


	@Override
	public Map<String, String[]> getMap() {
		return updatedPricesMap;	
	}


	//clear the text
	@Override
	public void clear() {
		sb.setLength(0);
		setDisplay("");
	}


	//save the current loaded/displayed file as txt
	@Override
	public void save(String filePath) {
		try {
			File newTextFile = new File(filePath);
			FileWriter fw = new FileWriter(newTextFile);
			fw.write(sb.toString());
			fw.close();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error saving file, please try again");
			e.printStackTrace();
		}

	}


	//creates positions .txt file string to display, check if txt 
	@Override
	public String loadPositions(String fileName) {
		try (BufferedReader in = new BufferedReader(new FileReader(fileName))) {
			String line;
			while((line = in.readLine()) != null)
			{
				sb.append(line + "\n");
			}
			txtFilePath = fileName;
		} catch (FileNotFoundException e1) {
			JOptionPane.showMessageDialog(null, "Invalid positions file type or file does not exist, please select a positions txt file");
			e1.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error reading positions file, please make sure the positions txt file is formatted correctly");
			e.printStackTrace();
		} 
		return sb.toString();
	}


	//creates updated price xls file string to display
	@Override
	public String loadUpdatedPrices(String fileName) {
		try(InputStream excelPriceFile = new FileInputStream(fileName); HSSFWorkbook wb = new HSSFWorkbook(excelPriceFile) ) {
			HSSFSheet sheet = wb.getSheetAt(0);			
			FormulaEvaluator evaluator = wb.getCreationHelper().createFormulaEvaluator();
			Iterator<Row> rowIterator = sheet.iterator();
			HSSFRow row; 
			HSSFCell cell;

			while (rowIterator.hasNext())
			{
				row=(HSSFRow) rowIterator.next();
				mapRow(row); //map the row 
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext())
				{
					cell=(HSSFCell) cellIterator.next();

					switch(evaluator.evaluateInCell(cell).getCellType())
					{			
					case STRING:		
						sb.append(String.format("%s,", cell.getStringCellValue()));
						break;
					case NUMERIC:
						sb.append(String.format("%s,", Double.toString(cell.getNumericCellValue())));     							
						break;
					case FORMULA:
						break;		
					case _NONE:
						break;
					case BLANK:
						break;
					case BOOLEAN:
						sb.append(String.format("%s", Boolean.toString(cell.getBooleanCellValue())));
						break;
					case ERROR:
						break;
					default:
						break;
					}							
				}	
				sb.append("\n");
			}				
		} 
		catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Invalid prices file type or file does not exist, please select an xls price file");
			e.printStackTrace();
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "Error parsing prices file, please make sure file is xls and formatted correctly");
			e1.printStackTrace();
		}
		return sb.toString().trim();	
	}


	//loads each option into a map with the old/updated prices
	@Override
	public void mapRow(Row row) {
		if(row.getCell(8) != null) {
			if(row.getCell(8).getCellType() == CellType.STRING) {
				if(row.getCell(8).getStringCellValue().equals("Option")) { //secType cell, only care about options
					String[] lstPrices = new String[2];
					String ticker = "";

					if(row.getCell(1) != null) { //if ticker cell is populated 		
						HSSFCell tickerCell =(HSSFCell)row.getCell(1);
						if(tickerCell.getCellType() == CellType.STRING) {		
							ticker = tickerCell.getStringCellValue();					
						}
					} 

					if(row.getCell(5) != null) { //oldPrice
						HSSFCell oldPriceCell = (HSSFCell)row.getCell(5);			
						if(oldPriceCell.getCellType() == CellType.NUMERIC) {
							lstPrices[0] = Double.toString(oldPriceCell.getNumericCellValue()); 
						} 
					}

					if(row.getCell(9) != null) {  //newPrice			
						HSSFCell newPriceCell = (HSSFCell)row.getCell(9);
						if(newPriceCell.getCellType() == CellType.NUMERIC) {	
							lstPrices[1] = Double.toString(newPriceCell.getNumericCellValue()); 
						}
					} else {
						lstPrices[1] = lstPrices[0]; 								
					}
					updatedPricesMap.put(ticker, lstPrices);				
				}			
			}
		}
	}

	//creates a new position file string to display
	@Override
	public String generatePositionsFile() {		
		StringBuilder result = new StringBuilder();

		try (BufferedReader in = new BufferedReader(new FileReader(txtFilePath))) {
			String line;
			String secType;
			String putCall;
			String tradingSymbol;
			String year;
			String month;
			String day;
			String strikeDollar;
			String strikeFraction;
			StringBuilder sbLine = new StringBuilder();

			if((line = in.readLine()) != null) { //header record
				sbLine.setLength(0);
				sbLine.append(line);
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYYMMdd");

				sbLine = sbLine.replace(8, 17, LocalDate.now().format(formatter));
				result.append(sbLine.toString());
				result.append("\n");
			}

			while((line = in.readLine()) != null) //detail records
			{		
				sbLine.setLength(0);
				sbLine.append(line);
				secType = sbLine.substring(43,44);

				if(secType.equals("O")) {								
					putCall = sbLine.substring(18, 19);
					tradingSymbol = sbLine.substring(19, 25).trim();
					year = sbLine.substring(27, 29);
					month = sbLine.substring(29, 31);
					day = sbLine.substring(31, 33);
					strikeDollar = StringUtils.stripStart(sbLine.substring(33, 38), "0");						
					strikeFraction = StringUtils.stripEnd(sbLine.substring(38, 42), "0");			
					String keyValue = tradingSymbol + year + month + day + putCall + strikeDollar;

					if(!strikeFraction.equals("")) {
						keyValue = keyValue + "." + strikeFraction;			
					}

					if(updatedPricesMap.containsKey(keyValue)) {
						String[] prices = updatedPricesMap.get(keyValue);
						String newPrice = formatPrice(prices[1]);
						sbLine = sbLine.replace(44, 56, newPrice); //replace with updated price
						sbLine.append("\n");
						result.append(sbLine.toString());
					} 
				} else {
					result.append(sbLine.toString());
					result.append("\n");
				}
			}
		} catch (FileNotFoundException e1) {
			JOptionPane.showMessageDialog(null, "Invalid positions file type or file does not exist, please load a positions txt file");
			e1.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error writing positions file, please reload the positions txt file");
			e.printStackTrace();
		}
		sb = result;
		return result.toString();
	}


	//format the prices according to the Risk Based Haircut System
	public String formatPrice(String price) {	
		BigDecimal bd = new BigDecimal(price);
		bd = bd.setScale(6, RoundingMode.HALF_UP);	
		price = StringUtils.leftPad(bd.toString(),13,"0").replace(".", "");
		return price;		
	}

	@Override
	public void registerObserver(ViewObserver o) {
		viewObservers.add(o);
	}

	@Override
	public void removeObserver(ViewObserver o) {
		int i = viewObservers.indexOf(o);
		if (i >= 0) {
			viewObservers.remove(i);
		}
	}

	@Override
	public void notifyViewObservers() {
		for(int i = 0; i < viewObservers.size(); i++) {
			ViewObserver observer = (ViewObserver)viewObservers.get(i);
			observer.updateText();
		}
	}


}
