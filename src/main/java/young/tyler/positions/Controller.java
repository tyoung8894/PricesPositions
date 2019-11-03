/**
 * Controller.java 1.0 October 29, 2019
 *
 * Author: Tyler Young
 */

package young.tyler.positions;


import java.util.HashMap;

public class Controller implements IController {
	IModel model;
	View view;

	public Controller(IModel model) {
		this.model = model;
		view = new View(this, model);
		view.createView();
	}

	@Override
	public void loadPositions(String filePath) {
		model.clear();
		String fileContents = model.loadPositions(filePath);
		model.setDisplay(fileContents);
	}

	@Override
	public void loadUpdatedPrices(String filePath){
		model.clear();
		model.setMap(new HashMap<String, String[]>());  
		String fileContents = model.loadUpdatedPrices(filePath);
		model.setDisplay(fileContents);
	}

	//requires a position and xls file to be loaded
	@Override
	public void generatePositionsFile(){ 
		String fileContents = model.generatePositionsFile();
		model.setDisplay(fileContents);
	}
		
	
	@Override
	public void save(String filePath) {
		model.save(filePath);
	}


}
