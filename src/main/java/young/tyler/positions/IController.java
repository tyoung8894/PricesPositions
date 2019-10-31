package young.tyler.positions;

import java.io.IOException;

public interface IController {
	void loadPositions(String filePath) throws IOException;
	void loadUpdatedPriceFile(String filePath) throws IOException;
	void generatePositionsFile() throws IOException;
	void save(String filePath);
	void clear();
}
