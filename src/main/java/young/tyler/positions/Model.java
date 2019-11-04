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
	boolean positionsLoaded = false; 
	boolean pricesLoaded = false; 
	Map<String, String[]> updatedPrices = null;  
	//public static final int TICKER_INDEX = 1;  //make const

	StringBuilder fileText = new StringBuilder();
	StringBuilder lineToWrite = new StringBuilder();

	//set the text to string representing file's contents, notify observers to update text
	@Override
	public void setDisplay(String input) {
		this.display = input;
		notifyViewObservers();
	}

	//return current display text
	@Override
	public String getDisplay() {
		return this.display;
	}

	@Override
	public void setPrices(Map<String, String[]> map) {
		this.updatedPrices = map;
	}


	@Override
	public Map<String, String[]> getMap() {
		return this.updatedPrices;	
	}


	//clear the text
	@Override
	public void clear() {
		this.fileText.setLength(0);
		setDisplay("");
	}


	//save the current loaded/displayed file as txt
	@Override
	public void save(String filePath) {
		try {
			File newTextFile = new File(filePath);
			FileWriter fw = new FileWriter(newTextFile);
			fw.write(this.fileText.toString());
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
				this.fileText.append(line + "\n");
			}
			this.txtFilePath = fileName;
		} catch (FileNotFoundException e1) {
			JOptionPane.showMessageDialog(null, "Invalid positions file type or file does not exist, please select a positions txt file");
			e1.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error reading positions file, please make sure the positions txt file is formatted correctly");
			e.printStackTrace();
		} 
		return getFileText();
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
				mapRow(row); 
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext())
				{
					cell=(HSSFCell) cellIterator.next();
					
					switch(evaluator.evaluateInCell(cell).getCellType())
					{			
					case STRING:		
						this.fileText.append(String.format("%s,", cell.getStringCellValue()));
						break;
					case NUMERIC:
						this.fileText.append(String.format("%s,", Double.toString(cell.getNumericCellValue())));     	
						break;
					case FORMULA:
						break;		
					case _NONE:
						break;
					case BLANK:
						break;
					case BOOLEAN:
						this.fileText.append(String.format("%s", Boolean.toString(cell.getBooleanCellValue())));
						break;
					case ERROR:
						break;
					default:
						break;
					}							
				}	
				this.fileText.append("\n");
			}				
		} 
		catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null, "Invalid prices file type or file does not exist, please select an xls price file");
			e.printStackTrace();
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(null, "Error parsing prices file, please make sure file is xls and formatted correctly");
			e1.printStackTrace();
		}
		return this.fileText.toString().trim();	
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
					this.updatedPrices.put(ticker, lstPrices);				
				}			
			}
		}
	}

	public String getFileText() {
		return this.fileText.toString();	
	}

	public String generatePositionsFile() {
		try (BufferedReader in = new BufferedReader(new FileReader(this.txtFilePath))) {
			String line;
			boolean isFirstLine = true; 	
			while((line = in.readLine()) != null) 
			{		
				initNextLine(line);  
				if(!isFirstLine) {
					writeRecord();	
				} else {
					isFirstLine = !isFirstLine;
					writeHeader();				
				}
			}
		} catch (FileNotFoundException e1) {
			JOptionPane.showMessageDialog(null, "Invalid positions file type or file does not exist, please load a positions txt file");
			e1.printStackTrace();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Error writing positions file, please reload the positions txt file");
			e.printStackTrace();
		}
		return getFileText();
	}

	public void initNextLine(String line) {
		this.lineToWrite.setLength(0);
		this.lineToWrite.append(line);
	}

	public void writeRecord() {
		generateLineToWrite();
		appendTextToFile(this.lineToWrite);
	}

	public void writeHeader() {
		generateDate();
		appendTextToFile(this.lineToWrite);
	}

	public void generateLineToWrite() {
		if(isOption()) {
			String ticker = generatePositionTicker();	
			if(this.updatedPrices.containsKey(ticker)) {
				String newPrice = getNewPositionPrice(ticker);
				updatePositionPrice(newPrice);
			} 	
		}
	}

	public void appendTextToFile(StringBuilder lineToWrite) {
		this.fileText.append(lineToWrite.toString());
		this.fileText.append("\n");	
	}

	public void generateDate() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYYYMMdd");
		this.lineToWrite = this.lineToWrite.replace(8, 17, LocalDate.now().format(formatter));
	}

	//check if line in the file represents an Option security type
	public boolean isOption(){
		return this.lineToWrite.substring(43, 44).equals("O");
	}

	//generate Ticker from positions file
	public String generatePositionTicker() {	
		String putCall = this.lineToWrite.substring(18, 19);
		String tradingSymbol = this.lineToWrite.substring(19, 25).trim();
		String expirationDeliveryYear = this.lineToWrite.substring(27, 29);
		String expirationDeliveryMonth = this.lineToWrite.substring(29, 31);
		String expirationDeliveryDay = this.lineToWrite.substring(31, 33);
		String strikeDollar = StringUtils.stripStart(this.lineToWrite.substring(33, 38), "0");						
		String strikeFraction = StringUtils.stripEnd(this.lineToWrite.substring(38, 42), "0");			
		String ticker = 
				tradingSymbol //CSCO
				+ expirationDeliveryYear //13
				+ expirationDeliveryMonth //01
				+ expirationDeliveryDay //19
				+ putCall //C
				+ strikeDollar; //17

		if(!strikeFraction.isEmpty()) {
			ticker = ticker + "." + strikeFraction;	//.5		
		}
		return ticker;
	}

	public boolean checkUpdate(String ticker) {	
		return this.updatedPrices.containsKey(ticker);	
	}

	public String getNewPositionPrice(String ticker) {
		String[] prices = this.updatedPrices.get(ticker);
		String newPrice = formatPrice(prices[1]);
		return newPrice;
	}

	public void updatePositionPrice(String newPrice) {
		this.lineToWrite = this.lineToWrite.replace(44, 56, newPrice);
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
