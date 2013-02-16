package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;

import readers.LogFileReader;
import templates.MapBackground;
import templates.RateIndicatorBFToKML;
import templates.RateIndicatorBFToProcessing;
import utils.Utils;
import app.SpreadApp;
import colorpicker.swing.ColorPicker;

@SuppressWarnings("serial")
public class RateIndicatorBFTab extends JPanel {

	// Sizing constants
	private final int leftPanelWidth = 260;
	private final int leftPanelHeight = 1000;
	private final int spinningPanelHeight = 20;
	private final int mapImageWidth = MapBackground.MAP_IMAGE_WIDTH;
	private final int mapImageHeight = MapBackground.MAP_IMAGE_HEIGHT;
	private final Dimension minimumDimension = new Dimension(0, 0);

	// Colors
	private Color backgroundColor;
	private Color branchesMaxColor;
	private Color branchesMinColor;

	// Locations & coordinates table
	private InteractiveTableModel table = null;

	// Strings for paths
	private String logFilename = null;
	private File workingDirectory = null;

	// Text fields
	private JTextField numberOfIntervalsParser;
	private JTextField maxAltMappingParser;
	private JTextField bfCutoffParser;
	private JTextField kmlPathParser;
	
	// Buttons
	private JButton openLog;
	private JButton openLocations;
	private JButton generateKml;
	private JButton generateProcessing;
	private JButton saveProcessingPlot;
	private JButton branchesMaxColorChooser;
	private JButton branchesMinColorChooser;

	// Sliders
	private JSlider burnInParser;
	private JSlider branchesWidthParser;

	// Combo boxes
	private JComboBox meanPoissonPriorParser;
	private JComboBox poissonPriorOffsetParser;
	private JComboBox indicatorNameBox;
	  
	// Left tools pane
	private JPanel leftPanel;
	private JPanel tmpPanel;
	private SpinningPanel sp;
	private JPanel tmpPanelsHolder;

	// Processing pane
	private RateIndicatorBFToProcessing rateIndicatorBFToProcessing;

	// Progress bar
	private JProgressBar progressBar;

	public RateIndicatorBFTab() {

		// Setup miscallenous
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		backgroundColor = new Color(231, 237, 246);
		branchesMaxColor = new Color(255, 5, 50, 255);
		branchesMinColor = new Color(0, 0, 0, 255);
		GridBagConstraints c = new GridBagConstraints();

		// Setup text fields
		numberOfIntervalsParser = new JTextField("100", 10);
		maxAltMappingParser = new JTextField("500000", 10);
		bfCutoffParser = new JTextField("3.0", 5);
		kmlPathParser = new JTextField("output.kml", 10);
		
		// Setup buttons
		openLog = new JButton("Open", SpreadApp.logIcon);
		openLocations = new JButton("Open", SpreadApp.locationsIcon);
		generateKml = new JButton("Generate", SpreadApp.nuclearIcon);
		generateProcessing = new JButton("Plot", SpreadApp.processingIcon);
		saveProcessingPlot = new JButton("Save", SpreadApp.saveIcon);
		branchesMaxColorChooser = new JButton("Setup max");
		branchesMinColorChooser = new JButton("Setup min");

		// Setup sliders
		burnInParser = new JSlider(JSlider.HORIZONTAL, 0, 100, 10);
		burnInParser.setMajorTickSpacing(20);
		burnInParser.setMinorTickSpacing(10);
		burnInParser.setPaintTicks(true);
		burnInParser.setPaintLabels(true);
		branchesWidthParser = new JSlider(JSlider.HORIZONTAL, 2, 10, 4);
		branchesWidthParser.setMajorTickSpacing(2);
		branchesWidthParser.setMinorTickSpacing(1);
		branchesWidthParser.setPaintTicks(true);
		branchesWidthParser.setPaintLabels(true);

		// Setup Combo boxes
		indicatorNameBox = new JComboBox(new String[] {"indicator"});
		 
		// Setup progress bar
		progressBar = new JProgressBar();

		/**
		 * left tools pane
		 * */
		leftPanel = new JPanel();
		leftPanel.setBackground(backgroundColor);
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
		// leftPanel.setSize(new Dimension(leftPanelWidth, leftPanelHeight));

		// Listeners
		openLog.addActionListener(new ListenOpenLog());
		generateKml.addActionListener(new ListenGenerateKml());
		openLocations
				.addActionListener(new ListenOpenLocationCoordinatesEditor());
		generateProcessing.addActionListener(new ListenGenerateProcessing());
		saveProcessingPlot.addActionListener(new ListenSaveProcessingPlot());
		branchesMaxColorChooser
				.addActionListener(new ListenBranchesMaxColorChooser());
		branchesMinColorChooser
				.addActionListener(new ListenBranchesMinColorChooser());

		// /////////////
		// ---INPUT---//
		// /////////////

		tmpPanelsHolder = new JPanel();
		tmpPanelsHolder.setLayout(new BoxLayout(tmpPanelsHolder,
				BoxLayout.Y_AXIS));

		tmpPanel = new JPanel();
		tmpPanel.setMaximumSize(new Dimension(leftPanelWidth, 100));
		tmpPanel.setBackground(backgroundColor);
		tmpPanel.setBorder(new TitledBorder("Load log file:"));
		tmpPanel.add(openLog);
		tmpPanelsHolder.add(tmpPanel);

		tmpPanel = new JPanel();
		tmpPanel.setMaximumSize(new Dimension(leftPanelWidth, 100));
		tmpPanel.setBackground(backgroundColor);
		tmpPanel.setBorder(new TitledBorder("Setup location coordinates:"));
		tmpPanel.add(openLocations);
		tmpPanelsHolder.add(tmpPanel);

		tmpPanel = new JPanel();
		tmpPanel.setMaximumSize(new Dimension(leftPanelWidth, 100));
		tmpPanel.setBackground(backgroundColor);
		tmpPanel.setBorder(new TitledBorder("Indicator attribute name:"));
		tmpPanel.add(indicatorNameBox);
		tmpPanelsHolder.add(tmpPanel);
		
		sp = new SpinningPanel(tmpPanelsHolder, "   Input", new Dimension(
				leftPanelWidth, spinningPanelHeight));
		sp.showBottom(true);
		leftPanel.add(sp);

		// ////////////////////
		// ---COMPUTATIONS---//
		// ////////////////////

		tmpPanelsHolder = new JPanel();
		tmpPanelsHolder.setLayout(new BoxLayout(tmpPanelsHolder,
				BoxLayout.Y_AXIS));

		tmpPanel = new JPanel();
		tmpPanel.setMaximumSize(new Dimension(leftPanelWidth, 100));
		tmpPanel.setBackground(backgroundColor);
		tmpPanel.setBorder(new TitledBorder("Specify burn-in %:"));
		tmpPanel.add(burnInParser);
		tmpPanelsHolder.add(tmpPanel);

		tmpPanel = new JPanel();
		tmpPanel.setMaximumSize(new Dimension(leftPanelWidth, 100));
		tmpPanel.setLayout(new GridBagLayout());
		tmpPanel.setBackground(backgroundColor);
		tmpPanel.setBorder(new TitledBorder("Poisson prior mean / offset:"));
		String[] meanPoissonPrior = { "log(2)", " " };
		meanPoissonPriorParser = new JComboBox(meanPoissonPrior);
		meanPoissonPriorParser.setEditable(true);
		meanPoissonPriorParser.setPreferredSize(new Dimension(70, 25));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		tmpPanel.add(meanPoissonPriorParser, c);
		String[] poissonPriorOffset = { "n-1", " " };
		poissonPriorOffsetParser = new JComboBox(poissonPriorOffset);
		poissonPriorOffsetParser.setEditable(true);
		poissonPriorOffsetParser.setPreferredSize(new Dimension(70, 25));
		c.gridx = 2;
		c.gridy = 0;
		tmpPanel.add(poissonPriorOffsetParser, c);
		tmpPanelsHolder.add(tmpPanel);

		tmpPanel = new JPanel();
		tmpPanel.setMaximumSize(new Dimension(leftPanelWidth, 100));
		tmpPanel.setBackground(backgroundColor);
		tmpPanel.setBorder(new TitledBorder("Bayes Factor cut-off:"));
		tmpPanel.add(bfCutoffParser);
		tmpPanelsHolder.add(tmpPanel);

		sp = new SpinningPanel(tmpPanelsHolder, "   Computations",
				new Dimension(leftPanelWidth, spinningPanelHeight));
		sp.showBottom(false);
		leftPanel.add(sp);

		// ////////////////////////
		// ---BRANCHES MAPPING---//
		// ////////////////////////

		tmpPanelsHolder = new JPanel();
		tmpPanelsHolder.setLayout(new BoxLayout(tmpPanelsHolder,
				BoxLayout.Y_AXIS));

		// Rates color mapping:
		tmpPanel = new JPanel();
		tmpPanel.setMaximumSize(new Dimension(leftPanelWidth, 100));
		tmpPanel.setLayout(new GridBagLayout());
		tmpPanel.setBackground(backgroundColor);
		tmpPanel.setBorder(new TitledBorder("Rates color mapping:"));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		tmpPanel.add(branchesMinColorChooser, c);
		c.gridx = 2;
		c.gridy = 0;
		tmpPanel.add(branchesMaxColorChooser, c);
		tmpPanelsHolder.add(tmpPanel);

		// Branches width:
		tmpPanel = new JPanel();
		tmpPanel.setMaximumSize(new Dimension(leftPanelWidth, 100));
		tmpPanel.setBackground(backgroundColor);
		tmpPanel.setBorder(new TitledBorder("Rates width:"));
		tmpPanel.add(branchesWidthParser);
		tmpPanelsHolder.add(tmpPanel);

		tmpPanel = new JPanel();
		tmpPanel.setMaximumSize(new Dimension(leftPanelWidth, 100));
		tmpPanel.setBackground(backgroundColor);
		tmpPanel.setBorder(new TitledBorder("Number of intervals:"));
		tmpPanel.add(numberOfIntervalsParser);
		tmpPanelsHolder.add(tmpPanel);

		tmpPanel = new JPanel();
		tmpPanel.setMaximumSize(new Dimension(leftPanelWidth, 100));
		tmpPanel.setBackground(backgroundColor);
		tmpPanel.setBorder(new TitledBorder("Maximal altitude:"));
		tmpPanel.add(maxAltMappingParser);
		tmpPanelsHolder.add(tmpPanel);

		sp = new SpinningPanel(tmpPanelsHolder, "   Branches mapping",
				new Dimension(leftPanelWidth, spinningPanelHeight));
		sp.showBottom(false);
		leftPanel.add(sp);

		// //////////////
		// ---OUTPUT---//
		// //////////////

		tmpPanelsHolder = new JPanel();
		tmpPanelsHolder.setLayout(new BoxLayout(tmpPanelsHolder,
				BoxLayout.Y_AXIS));

		tmpPanel = new JPanel();
		tmpPanel.setMaximumSize(new Dimension(leftPanelWidth, 100));
		tmpPanel.setBackground(backgroundColor);
		tmpPanel.setBorder(new TitledBorder("KML name:"));
		tmpPanel.add(kmlPathParser);
		tmpPanelsHolder.add(tmpPanel);

		tmpPanel = new JPanel();
		tmpPanel.setMaximumSize(new Dimension(leftPanelWidth, 100));
		tmpPanel.setLayout(new GridBagLayout());
		tmpPanel.setBackground(backgroundColor);
		tmpPanel.setBorder(new TitledBorder("Generate KML / Plot map:"));
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 0;
		tmpPanel.add(generateKml, c);
		c.gridx = 2;
		c.gridy = 0;
		tmpPanel.add(generateProcessing, c);
		c.ipady = 7;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 1;
		tmpPanel.add(progressBar, c);
		tmpPanelsHolder.add(tmpPanel);

		tmpPanel = new JPanel();
		tmpPanel.setMaximumSize(new Dimension(leftPanelWidth, 100));
		tmpPanel.setBackground(backgroundColor);
		tmpPanel.setBorder(new TitledBorder("Save plot:"));
		tmpPanel.add(saveProcessingPlot);
		tmpPanelsHolder.add(tmpPanel);

		sp = new SpinningPanel(tmpPanelsHolder, "   Output", new Dimension(
				leftPanelWidth, spinningPanelHeight));
		sp.showBottom(false);
		leftPanel.add(sp);

		// ////////////////////////
		// ---LEFT SCROLL PANE---//
		// ////////////////////////

		JScrollPane leftScrollPane = new JScrollPane(leftPanel,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		leftScrollPane.setMinimumSize(minimumDimension);
		leftScrollPane.setMaximumSize(new Dimension(leftPanelWidth,
				leftPanelHeight));

		/**
		 * Processing pane
		 * */
		rateIndicatorBFToProcessing = new RateIndicatorBFToProcessing();
		rateIndicatorBFToProcessing.setPreferredSize(new Dimension(
				mapImageWidth, mapImageHeight));

//		if (System.getProperty("java.runtime.name").toLowerCase().startsWith(
//				"openjdk")) {
//
//			JScrollPane rightScrollPane = new JScrollPane(
//					rateIndicatorBFToProcessing,
//					JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
//					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
//			rightScrollPane.setMinimumSize(minimumDimension);
//
//			SplitPane splitPane = new SplitPane(JSplitPane.HORIZONTAL_SPLIT,
//					leftScrollPane, rightScrollPane);
//			splitPane.setDividerLocation(leftPanelWidth);
//
//			this.add(splitPane);
//
//		} else {

			ScrollPane rightScrollPane = new ScrollPane(
					ScrollPane.SCROLLBARS_ALWAYS);
			rightScrollPane.add(rateIndicatorBFToProcessing);
			rightScrollPane.setMinimumSize(minimumDimension);

			SplitPane splitPane = new SplitPane(JSplitPane.HORIZONTAL_SPLIT,
					leftScrollPane, rightScrollPane);
			splitPane.setDividerLocation(leftPanelWidth);

			this.add(splitPane);

//		}

	}// END: RateIndicatorBFTab

	private class ListenOpenLog implements ActionListener {
		public void actionPerformed(ActionEvent ev) {

			try {

				String[] logFiles = new String[] { "log" };

				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Opening log file...");
				chooser.setMultiSelectionEnabled(false);
				chooser.addChoosableFileFilter(new SimpleFileFilter(logFiles,
						"Log files (*.log)"));
				chooser.setCurrentDirectory(workingDirectory);

				chooser.showOpenDialog(Utils.getActiveFrame());
				File file = chooser.getSelectedFile();
				logFilename = file.getAbsolutePath();
				System.out.println("Opened " + logFilename + "\n");

				File tmpDir = chooser.getCurrentDirectory();

				if (tmpDir != null) {
					workingDirectory = tmpDir;
				}
				populateInidcatorCombobox();
			} catch (Exception e) {
				System.err.println("Could not Open! \n");
			}
		}

		private void populateInidcatorCombobox() {
			LogFileReader parser = new LogFileReader();
			String [] colNames = parser.getColNames(logFilename);

			// remove numbers from end of column name, and count their occurrances 
			Map<String, Integer> map = new HashMap<String, Integer>();
			for (String colName : colNames) {
				colName = colName.replaceAll("[0-9]*$", "");
				if (map.containsKey(colName)) {
					map.put(colName, map.get(colName) + 1);
				} else {
					map.put(colName, 1);
				}
			}
			
			// make those that occur more than once a candidate
			List<String> indicatorNames = new ArrayList<String>(); 
			for (String colName : map.keySet()) {
				if (map.get(colName) > 1) {
					indicatorNames.add(colName);
				}
			}
			
			// re-initialise combobox
			ComboBoxModel model = new DefaultComboBoxModel(indicatorNames.toArray(new String[0]));
			indicatorNameBox.setModel(model);
		}
	}

	private class ListenOpenLocationCoordinatesEditor implements ActionListener {
		public void actionPerformed(ActionEvent ev) {

			try {

				LocationCoordinatesEditor locationCoordinatesEditor = new LocationCoordinatesEditor();
				locationCoordinatesEditor.launch(workingDirectory);

				table = locationCoordinatesEditor.getTable();

			} catch (Exception e) {

				Utils.handleException(e, null);

			}
		}
	}// END: ListenOpenLocations

	private class ListenBranchesMinColorChooser implements ActionListener {
		public void actionPerformed(ActionEvent ev) {

			Color c = ColorPicker.showDialog(Utils.getActiveFrame(),
					"Choose minimum branches color...", branchesMinColor, true);

			if (c != null)
				branchesMinColor = c;
		}
	}

	private class ListenBranchesMaxColorChooser implements ActionListener {
		public void actionPerformed(ActionEvent ev) {

			Color c = ColorPicker.showDialog(Utils.getActiveFrame(),
					"Choose maximum branches color...", branchesMaxColor, true);

			if (c != null)
				branchesMaxColor = c;
		}
	}

	private class ListenGenerateKml implements ActionListener {
		public void actionPerformed(ActionEvent ev) {

			if (logFilename == null) {

				new ListenOpenLog().actionPerformed(ev);

			} else if (table == null) {

				new ListenOpenLocationCoordinatesEditor().actionPerformed(ev);

			} else {

				generateKml.setEnabled(false);
				progressBar.setIndeterminate(true);

				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

					// Executed in background thread
					public Void doInBackground() {

						try {

							RateIndicatorBFToKML rateIndicatorBFToKML = new RateIndicatorBFToKML();

							rateIndicatorBFToKML.setTable(table);

							rateIndicatorBFToKML.setLogFileParser(logFilename,
									burnInParser.getValue() / 100.0, indicatorNameBox.getSelectedItem().toString());

							rateIndicatorBFToKML.setBfCutoff(Double
									.valueOf(bfCutoffParser.getText()));

							rateIndicatorBFToKML.setMaxAltitudeMapping(Double
									.valueOf(maxAltMappingParser.getText()));

							rateIndicatorBFToKML
									.setNumberOfIntervals(Integer
											.valueOf(numberOfIntervalsParser
													.getText()));

							rateIndicatorBFToKML
									.setKmlWriterPath(workingDirectory
											.toString().concat("/").concat(
													kmlPathParser.getText()));

							rateIndicatorBFToKML
									.setMinBranchRedMapping(branchesMinColor
											.getRed());

							rateIndicatorBFToKML
									.setMinBranchGreenMapping(branchesMinColor
											.getGreen());

							rateIndicatorBFToKML
									.setMinBranchBlueMapping(branchesMinColor
											.getBlue());

							rateIndicatorBFToKML
									.setMinBranchOpacityMapping(branchesMinColor
											.getAlpha());

							rateIndicatorBFToKML
									.setMaxBranchRedMapping(branchesMaxColor
											.getRed());

							rateIndicatorBFToKML
									.setMaxBranchGreenMapping(branchesMaxColor
											.getGreen());

							rateIndicatorBFToKML
									.setMaxBranchBlueMapping(branchesMaxColor
											.getBlue());

							rateIndicatorBFToKML
									.setMaxBranchOpacityMapping(branchesMaxColor
											.getAlpha());

							rateIndicatorBFToKML
									.setBranchWidth(branchesWidthParser
											.getValue());

							if (meanPoissonPriorParser.getSelectedIndex() == 0) {
								rateIndicatorBFToKML
										.setDefaultMeanPoissonPrior();

							} else {
								rateIndicatorBFToKML
										.setUserMeanPoissonPrior(Double
												.valueOf(meanPoissonPriorParser
														.getSelectedItem()
														.toString()));
							}

							if (poissonPriorOffsetParser.getSelectedIndex() == 0) {
								rateIndicatorBFToKML
										.setDefaultPoissonPriorOffset();

							} else {
								rateIndicatorBFToKML
										.setUserPoissonPriorOffset(Double
												.valueOf(poissonPriorOffsetParser
														.getSelectedItem()
														.toString()));
							}

							rateIndicatorBFToKML.GenerateKML();

							System.out.println("Finished in: "
									+ RateIndicatorBFToKML.time + " msec \n");

						} catch (final OutOfMemoryError e) {

							
							Utils.handleException(e, "Increase Java Heap Space");
							
//							SwingUtilities.invokeLater(new Runnable() {
//
//								public void run() {
//
//									e.printStackTrace();
//
//									String msg = String.format(
//											"Unexpected problem: %s", e
//													.toString());
//
//									JOptionPane.showMessageDialog(Utils
//											.getActiveFrame(), msg
//											+ "\nIncrease Java Heap Space",
//											"Error", JOptionPane.ERROR_MESSAGE,
//											SpreadApp.errorIcon);
//
//								}
//							});

						} catch (Exception e) {

							Utils.handleException(e, null);

						}

						return null;
					}// END: doInBackground()

					// Executed in event dispatch thread
					public void done() {
						generateKml.setEnabled(true);
						progressBar.setIndeterminate(false);

						System.out.println("Generated "
								+ workingDirectory.toString().concat("/")
										.concat(kmlPathParser.getText()));
					}
				};

				worker.execute();

			}// END: if not loaded

		}// END: actionPerformed
	}// END: ListenGenerateKml

	private class ListenGenerateProcessing implements ActionListener {
		public void actionPerformed(ActionEvent ev) {

			if (logFilename == null) {

				new ListenOpenLog().actionPerformed(ev);

			} else if (table == null) {

				new ListenOpenLocationCoordinatesEditor().actionPerformed(ev);

			} else {

				generateProcessing.setEnabled(false);
				progressBar.setIndeterminate(true);

				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

					// Executed in background thread
					public Void doInBackground() {

						try {

							rateIndicatorBFToProcessing.setTable(table);

							rateIndicatorBFToProcessing.setLogFilePath(
									logFilename,
									burnInParser.getValue() / 100.0, indicatorNameBox.getSelectedItem().toString());

							rateIndicatorBFToProcessing.setBfCutoff(Double
									.valueOf(bfCutoffParser.getText()));

							// rateIndicatorBFToProcessing
							// .setNumberOfIntervals(Integer
							// .valueOf(numberOfIntervalsParser
							// .getText()));

							rateIndicatorBFToProcessing
									.setMinBranchRedMapping(branchesMinColor
											.getRed());

							rateIndicatorBFToProcessing
									.setMinBranchGreenMapping(branchesMinColor
											.getGreen());

							rateIndicatorBFToProcessing
									.setMinBranchBlueMapping(branchesMinColor
											.getBlue());

							rateIndicatorBFToProcessing
									.setMinBranchOpacityMapping(branchesMinColor
											.getAlpha());

							rateIndicatorBFToProcessing
									.setMaxBranchRedMapping(branchesMaxColor
											.getRed());

							rateIndicatorBFToProcessing
									.setMaxBranchGreenMapping(branchesMaxColor
											.getGreen());

							rateIndicatorBFToProcessing
									.setMaxBranchBlueMapping(branchesMaxColor
											.getBlue());

							rateIndicatorBFToProcessing
									.setMaxBranchOpacityMapping(branchesMaxColor
											.getAlpha());

							rateIndicatorBFToProcessing
									.setBranchWidth(branchesWidthParser
											.getValue() / 2);

							if (meanPoissonPriorParser.getSelectedIndex() == 0) {
								rateIndicatorBFToProcessing
										.setDefaultMeanPoissonPrior();

							} else {
								rateIndicatorBFToProcessing
										.setUserMeanPoissonPrior(Double
												.valueOf(meanPoissonPriorParser
														.getSelectedItem()
														.toString()));
							}

							if (poissonPriorOffsetParser.getSelectedIndex() == 0) {
								rateIndicatorBFToProcessing
										.setDefaultPoissonPriorOffset();

							} else {
								rateIndicatorBFToProcessing
										.setUserPoissonPriorOffset(Double
												.valueOf(poissonPriorOffsetParser
														.getSelectedItem()
														.toString()));
							}

							rateIndicatorBFToProcessing.init();

							System.out.println("Finished. \n");

						} catch (final Exception e) {

							Utils.handleException(e, null);

						}

						return null;
					}// END: doInBackground()

					// Executed in event dispatch thread
					public void done() {

						generateProcessing.setEnabled(true);
						progressBar.setIndeterminate(false);

					}
				};

				worker.execute();

			}// END: if not loaded

		}// END: actionPerformed
	}// END: ListenGenerateProcessing

	private class ListenSaveProcessingPlot implements ActionListener {
		public void actionPerformed(ActionEvent ev) {

			try {

				JFileChooser chooser = new JFileChooser();
				chooser.setDialogTitle("Saving as png file...");

				chooser.showSaveDialog(Utils.getActiveFrame());
				File file = chooser.getSelectedFile();
				String plotToSaveFilename = file.getAbsolutePath();

				rateIndicatorBFToProcessing.save(plotToSaveFilename);
				System.out.println("Saved " + plotToSaveFilename + "\n");

			} catch (Exception e) {
				System.err.println("Could not save! \n");
			}

		}// END: actionPerformed
	}// END: class

}// END: class
