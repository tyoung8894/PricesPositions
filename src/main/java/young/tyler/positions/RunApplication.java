/**
 * RunApplication.java 1.0 October 29, 2019
 *
 * Author: Tyler Young
 */

package young.tyler.positions;

public class RunApplication {

	public static void main(String[] args) {
		IModel model = new Model();
		IController controller = new Controller(model);
	}

}
