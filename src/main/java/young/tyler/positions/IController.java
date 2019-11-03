package young.tyler.positions;

public interface IController {
	void loadPositions(String filePath);
	void loadUpdatedPrices(String filePath);
	void generatePositionsFile();
	void save(String filePath);
}
