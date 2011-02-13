package templates;

import java.io.FileReader;
import java.io.IOException;

import jebl.evolution.graphs.Node;
import jebl.evolution.io.ImportException;
import jebl.evolution.io.NexusImporter;
import jebl.evolution.io.TreeImporter;
import jebl.evolution.trees.RootedTree;
import processing.core.PApplet;
import processing.core.PFont;
import utils.ReadLocations;
import utils.Utils;

@SuppressWarnings("serial")
public class DiscreteTreeToProcessing extends PApplet {

	private TreeImporter importer;
	private static RootedTree tree;
	private static ReadLocations data;
	private static String stateAttName;

	private ReadLocations mapdata;
	// min/max longitude
	private static float minX, maxX;
	// min/max latitude
	private static float minY, maxY;
	// Border of where the map should be drawn on screen
	private static float mapX1, mapX2;
	private static float mapY1, mapY2;

	public DiscreteTreeToProcessing() {

	}// END: DiscreteTreeToProcessing

	public void setStateAttName(String name) {
		stateAttName = name;
	}

	public void setTreePath(String path) throws IOException, ImportException {
		importer = new NexusImporter(new FileReader(path));
		tree = (RootedTree) importer.importNextTree();
	}

	public void setLocationFilePath(String path) {
		data = new ReadLocations(path);
	}

	public void setup() {

		size(800, 500);
		noLoop();
		smooth();

		mapX1 = 30;
		mapX2 = width - mapX1;
		mapY1 = 20;
		mapY2 = height - mapY1;

		// will improve font rendering speed with default renderer
		hint(ENABLE_NATIVE_FONTS);
		PFont plotFont = createFont("Arial", 12);
		textFont(plotFont);

		// load the map data
		mapdata = new ReadLocations(this.getClass()
				.getResource("world_map.txt").getPath());

		// calculate min/max longitude
		minX = mapdata.getLongMin();
		maxX = mapdata.getLongMax();
		// calculate min/max latitude
		minY = Utils.getMercatorLatitude(mapdata.getLatMin());
		maxY = Utils.getMercatorLatitude(mapdata.getLatMax());

	}// END: setup

	public void draw() {

		drawPlotArea();
		drawHorGrid();
		drawVertGrid();
		drawMapPolygons();

		DrawPlaces();
		DrawBranches();
		DrawPlacesLabels();

		drawOutline();

	}// END:draw

	void drawPlotArea() {

		// White background
		background(255, 255, 255);
		// Show the plot area as a blue box.
		fill(100, 149, 237);
		noStroke();
		rectMode(CORNERS);
		rect(mapX1, mapY1, mapX2, mapY2);

	}// END: drawPlotArea

	void drawMapPolygons() {

		// Dark grey polygon boundaries
		stroke(105, 105, 105);
		strokeWeight(1);
		strokeJoin(ROUND);
		// Sand brown polygon filling
		fill(244, 164, 96);

		int rowCount = mapdata.nrow;
		String region;
		String nextRegion;

		for (int row = 0; row < rowCount - 1; row++) {

			region = mapdata.locations[row];
			nextRegion = mapdata.locations[row + 1];

			if (nextRegion.toLowerCase().equals(region.toLowerCase())) {

				beginShape();

				float X = map(mapdata.getFloat(row, 0), minX, maxX, mapX1,
						mapX2);
				float Y = map(Utils.getMercatorLatitude(mapdata
						.getFloat(row, 1)), minY, maxY, mapY2, mapY1);

				float XEND = map(mapdata.getFloat(row + 1, 0), minX, maxX,
						mapX1, mapX2);
				float YEND = map(Utils.getMercatorLatitude(mapdata.getFloat(
						row + 1, 1)), minY, maxY, mapY2, mapY1);

				vertex(X, Y);
				vertex(XEND, YEND);

			}

			endShape(CLOSE);

		}// END: row loop

	}// END: drawMapPolygons

	void drawVertGrid() {

		int Interval = 100;
		stroke(255, 255, 255);

		for (float v = mapX1; v <= mapX2; v += Interval) {

			strokeWeight(1);
			line(v, mapY1, v, mapY2);

		}// END: longitude loop
	}// END: drawVertGrid

	void drawHorGrid() {

		int Interval = 50;
		stroke(255, 255, 255);

		for (float v = mapY1; v <= mapY2; v += Interval) {

			strokeWeight(1);
			line(mapX1, v, mapX2, v);

		} // END: latitude loop
	}// End: drawHorGrid

	void drawOutline() {

		noFill();
		stroke(0, 0, 0);
		rectMode(CORNERS);
		rect(mapX1, mapY1, mapX2, mapY2);

	}// END: drawOutline

	// //////////////
	// ---PLACES---//
	// //////////////
	private void DrawPlaces() {

		float radius = 7;
		// White
		fill(255, 255, 255);
		noStroke();

		for (int row = 0; row < data.nrow; row++) {

			float X = map(data.getFloat(row, 1), minX, maxX, mapX1, mapX2);
			float Y = map(Utils.getMercatorLatitude(data.getFloat(row, 0)),
					minY, maxY, mapY2, mapY1);

			ellipse(X, Y, radius, radius);

		}
	}// END DrawPlaces

	private void DrawPlacesLabels() {

		textSize(7);
		// Black labels
		fill(0, 0, 0);

		for (int row = 0; row < data.nrow; row++) {

			String name = data.locations[row];
			float X = map(data.getFloat(row, 1), minX, maxX, mapX1, mapX2);
			float Y = map(Utils.getMercatorLatitude(data.getFloat(row, 0)),
					minY, maxY, mapY2, mapY1);

			text(name, X, Y);
		}
	}// END: DrawPlacesLabels

	// ////////////////
	// ---BRANCHES---//
	// ////////////////
	private void DrawBranches() {

		strokeWeight(2);

		double treeHeightMax = Utils.getTreeHeightMax(tree);

		for (Node node : tree.getNodes()) {
			if (!tree.isRoot(node)) {

				String state = (String) node.getAttribute(stateAttName);

				Node parentNode = tree.getParent(node);
				String parentState = (String) parentNode
						.getAttribute(stateAttName);

				if (!state.toLowerCase().equals(parentState.toLowerCase())) {

					float longitude = Utils
							.MatchStateCoordinate(data, state, 1);
					float latitude = Utils.MatchStateCoordinate(data, state, 0);

					float parentLongitude = Utils.MatchStateCoordinate(data,
							parentState, 1);
					float parentLatitude = Utils.MatchStateCoordinate(data,
							parentState, 0);

					float x0 = map(parentLongitude, minX, maxX, mapX1, mapX2);
					float y0 = map(Utils.getMercatorLatitude(parentLatitude),
							minY, maxY, mapY2, mapY1);

					float x1 = map(longitude, minX, maxX, mapX1, mapX2);
					float y1 = map(Utils.getMercatorLatitude(latitude), minY,
							maxY, mapY2, mapY1);

					/**
					 * Color mapping
					 * */
					double nodeHeight = tree.getHeight(node);

					int red = 255;
					int green = 0;
					int blue = (int) Utils.map(nodeHeight, 0, treeHeightMax,
							255, 0);
					int alpha = (int) Utils.map(nodeHeight, 0, treeHeightMax,
							100, 255);

					stroke(red, green, blue, alpha);
					line(x0, y0, x1, y1);
				}
			}
		}// END: nodes loop

	}// END: DrawBranches

}// END: PlotOnMap class
