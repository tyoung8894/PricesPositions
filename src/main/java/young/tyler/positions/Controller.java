/**
 * Controller.java 1.0 October 29, 2019
 *
 * Author: Tyler Young
 */

package young.tyler.positions;

import java.io.IOException;
import java.util.HashMap;

public class Controller implements IController {
	IModel model;
	View view;

	public Controller(IModel model) {
		this.model = model;
		view = new View(this, model);
		view.createView();
	}

	public void loadPositions(String filePath) throws IOException {
		String fileString = model.displayPositionsFile(filePath);
		model.setDisplay(fileString);
	}

	public void loadUpdatedPriceFile(String filePath) throws IOException {
		String fileString = model.displayPriceFile(filePath);
		model.setDisplay(fileString);
	}

	//requires a position and xls file to be loaded
	public void generatePositionsFile() throws IOException { 
		HashMap<String, String[]> map = model.parseUpdatedPriceFile();
		String fileString = model.parsePositionsFile(map);
		model.setDisplay(fileString);
	}

	public void save(String filePath) {
		model.save(filePath);
	}

	public void clear() {
		model.clear();
	}


}
