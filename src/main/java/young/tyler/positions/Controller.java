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
		this.view = new View(this, model);
		this.view.createView();
	}

	@Override
	public void loadPositions(String filePath) {
		this.model.clear();
		String fileContents = this.model.loadPositions(filePath);
		this.model.setDisplay(fileContents);
	}

	@Override
	public void loadUpdatedPrices(String filePath){
		this.model.clear();
		this.model.setPrices(new HashMap<String, String[]>());  //use private constructor instead
		String fileContents = this.model.loadUpdatedPrices(filePath);
		this.model.setDisplay(fileContents);
	}

	@Override
	public void generatePositionsFile(){ 
		this.model.clear();
		String fileContents = this.model.generatePositionsFile();
		this.model.setDisplay(fileContents);
	}
		
	
	@Override
	public void save(String filePath) {
		this.model.save(filePath);
	}


}
