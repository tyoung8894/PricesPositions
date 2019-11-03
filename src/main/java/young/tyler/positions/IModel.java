package young.tyler.positions;

import java.util.Map;
import org.apache.poi.ss.usermodel.Row;

public interface IModel {
	void registerObserver(ViewObserver o);
	void removeObserver(ViewObserver o);
	void notifyViewObservers();
	String getDisplay();
	void setDisplay(String input);
	Map<String, String[]> getMap();
	void setMap(Map<String, String[]> map);
	String loadUpdatedPrices(String filePath);
	String loadPositions(String filePath);
	String generatePositionsFile();
	void clear();
	void save(String filePath);
	void mapRow(Row row);	
}
