package main;

import templates.SpatialStatsToTerminal;

public class SpatialStatsToTerminalTest {
	
	private static boolean FIRST_ANALYSIS = true;
	
	private static SpatialStatsToTerminal spatialStatsToTerminal = new SpatialStatsToTerminal();
	
	public static void main(String[] args) {
	
	try {

		if(FIRST_ANALYSIS) {
			
			spatialStatsToTerminal.setAnalysisType(SpatialStatsToTerminal.FIRST_ANALYSIS);
			
			spatialStatsToTerminal.setTreePath("/home/filip/Dropbox/SPREAD_dev/CustomTimeSlicing/Cent_ITS_broad.tree");
			
			spatialStatsToTerminal.setNumberOfIntervals(10);
			
		} else {
			
			spatialStatsToTerminal.setAnalysisType(SpatialStatsToTerminal.SECOND_ANALYSIS);
			
			spatialStatsToTerminal.setCustomSliceHeightsPath("/home/filip/Dropbox/SPREAD_dev/CustomTimeSlicing/treeslice_small.txt");
			
		}	
		
		spatialStatsToTerminal.setTreesPath("/home/filip/Dropbox/SPREAD_dev/CustomTimeSlicing/Cent_ITS_small.trees");

		spatialStatsToTerminal.setBurnIn(0);
		
		spatialStatsToTerminal.setLocationAttributeName("coords");
		
		spatialStatsToTerminal.setRateAttributeName("rate");

		spatialStatsToTerminal.setPrecisionAttName("precision");

		spatialStatsToTerminal.setUseTrueNoise(false);
		
		spatialStatsToTerminal.calculate();

	} catch (Exception e) {
		e.printStackTrace();
	}//END: try-catch block
	
	}//END: main
	
}//END: class
