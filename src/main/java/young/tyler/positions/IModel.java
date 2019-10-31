package young.tyler.positions;

import java.io.IOException;
import java.util.HashMap;

public interface IModel {
	void registerObserver(ViewObserver o);
	void removeObserver(ViewObserver o);
	void setDisplay(String input);
	String displayPriceFile(String filePath) throws IOException;
	String displayPositionsFile(String filePath);
	HashMap<String, String[]> parseUpdatedPriceFile() throws IOException;
	String parsePositionsFile(HashMap<String, String[]> map);
	void clear();
	void save(String filePath);
	String getDisplay();
}
