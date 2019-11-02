/**
 * View.java 1.0 October 29, 2019
 *
 * Author: Tyler Young
 */

package young.tyler.positions;



import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BorderLayout;
import javax.swing.JTextArea;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;



public class View extends JPanel implements ActionListener, ViewObserver {
	JFrame frame;
	JButton btnLoadPositions; 
	JButton btnLoadUpdatedPrices;  
	JButton btnGeneratePositionsFile;
	JButton btnSave; 
	JTextArea textArea;
	JFileChooser fileChooser;

	IModel model;
	IController controller;
	String filePath;
	File file;
	boolean positionsLoaded = false;
	boolean pricesLoaded = false;

	/**
	 * Create the application.
	 */
	public View(IController controller, IModel model) {
		this.controller = controller;
		this.model = model;
		model.registerObserver((ViewObserver)this);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	public void createView() {
		frame = new JFrame("Tyler Young Prices/Positions");
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		textArea = new JTextArea(5, 20);
		textArea.setMargin(new Insets(5,5,5,5));
		JScrollPane textAreaScrollPane = new JScrollPane(textArea);

		fileChooser = new JFileChooser();

		JPanel btnPanel = new JPanel();

		btnLoadPositions = new JButton("Load Positions File");
		btnLoadPositions.addActionListener(this);
		btnPanel.add(btnLoadPositions);	

		btnLoadUpdatedPrices = new JButton("Load Price File");
		btnLoadUpdatedPrices.addActionListener(this);
		btnPanel.add(btnLoadUpdatedPrices);

		btnGeneratePositionsFile = new JButton("Generate New Positions File");
		btnGeneratePositionsFile.addActionListener(this);
		btnPanel.add(btnGeneratePositionsFile);
		btnGeneratePositionsFile.setEnabled(false);

		btnSave = new JButton("Save As");  //save text file to local
		btnSave.addActionListener(this);
		btnPanel.add(btnSave);
		btnSave.setEnabled(false);

		frame.getContentPane().add(btnPanel, BorderLayout.PAGE_START);
		frame.getContentPane().add(textAreaScrollPane, BorderLayout.CENTER);

		frame.pack();
		frame.setVisible(true);	
	}


	public void actionPerformed(ActionEvent e) {
		
		
		//load positions file clicked
		if(e.getSource() == btnLoadPositions) {
			int returnVal = fileChooser.showOpenDialog(View.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {		
					controller.loadPositions(fileChooser.getSelectedFile().getAbsolutePath());
					btnSave.setEnabled(true);
					positionsLoaded = true;
					if(pricesLoaded) {
						btnGeneratePositionsFile.setEnabled(true);				
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}	
			}	

		}

		//load updated price file clicked
		if(e.getSource() == btnLoadUpdatedPrices) {
			int returnVal = fileChooser.showOpenDialog(View.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				try {
					controller.loadUpdatedPrices(fileChooser.getSelectedFile().getAbsolutePath());	
					btnSave.setEnabled(true);
					pricesLoaded = true;
					if(positionsLoaded) {
						btnGeneratePositionsFile.setEnabled(true);			
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}		

		}

		//generate new positions file clicked, xls and txt file must be loaded first 
		if(e.getSource() == btnGeneratePositionsFile) {
			try {
				controller.generatePositionsFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		//save clicked, xls or txt file must be loaded first, saves to txt file
		if(e.getSource() == btnSave) {
			int returnVal = fileChooser.showSaveDialog(View.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {		
				controller.save(fileChooser.getSelectedFile().getAbsolutePath());			
			} 

		}

	}


	public void updateText() {
		String display = model.getDisplay();
		textArea.setText(display);	
	}
	
	

}