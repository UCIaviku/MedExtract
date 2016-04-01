package test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import preprocessor.PreProcessor;
import extractor.*;

public class DictTests {

	public static void startDictTests(boolean filterOn, ArrayList<String> aqlModules, ArrayList<String> testFiles) {
		String fileFolder = "./resources/dataset/";
		String filePrefix = "abstract_";
		
		for (String module : aqlModules) {
			PreProcessor preProcessor = new PreProcessor(module);
			for (String dataFileSize : testFiles) {
				System.gc();
				
				Extractor extractor = new Extractor(module);
				extractor.compile();
				extractor.load();
				extractor.prepareLogFile();
				HashSet<Integer> filteredLines = new HashSet<Integer>();
				if (filterOn) {
					filteredLines = preProcessor.filterDocuments(fileFolder+filePrefix+dataFileSize+".txt");
				}
				
				try {
					Scanner inFile = new Scanner(new File(fileFolder+filePrefix+dataFileSize+".txt"));
					Integer lineNum = 1;
					while (inFile.hasNextLine()) {
						String nextLine = inFile.nextLine();
						if (filterOn) {
							if (filteredLines.contains(lineNum))
								extractor.execute(nextLine, lineNum.toString());
						} else {
							extractor.execute(nextLine, lineNum.toString());
						}
						
						lineNum++;
					}
					inFile.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				extractor.closeLogFile();
				
//				System.out.printf("%.2f seconds | index\n", extractor.getIndexTimeSecs());
//				System.out.printf("%.2f seconds | search\n", extractor.getSearchTimeSecs());
//				System.out.printf("%.2f seconds | execution\n", extractor.getExecutionTimeSecs());
//				System.out.printf("%d results in total\n", extractor.getTotalResultsNum());
				if (filterOn) {
					System.out.printf("%s, %.2f, %.2f, %.2f", 
							dataFileSize, preProcessor.getIndexTimeSecs(), preProcessor.getSearchTimeSecs(), extractor.getExecutionTimeSecs());
				} else {
					System.out.printf("%s, %.2f", 
							dataFileSize, extractor.getExecutionTimeSecs());
				}
				
				System.out.println();
			}	
		}
	}
	
	
	public static void testSmallOn() {
		ArrayList<String> aqlModules = new ArrayList<>();
		aqlModules.add("dictTest1");
		//aqlModules.add("dictTest2");
		
		ArrayList<String> testFiles = new ArrayList<>();
		
		testFiles.add("100K");
		testFiles.add("250K");
		testFiles.add("400K");
		testFiles.add("550K");
		testFiles.add("700K");
		testFiles.add("850K");
		testFiles.add("1M");
		
		System.out.println("test small dict with filtering on");
		DictTests.startDictTests(true, aqlModules, testFiles);
		System.out.println();
		System.out.println();			
	}
	
	public static void testSmallOff() {
		ArrayList<String> aqlModules = new ArrayList<>();
		aqlModules.add("dictTest1");
		//aqlModules.add("dictTest2");
		
		ArrayList<String> testFiles = new ArrayList<>();
		
		testFiles.add("100K");
		testFiles.add("250K");
		testFiles.add("400K");
//		testFiles.add("550K");
//		testFiles.add("700K");
//		testFiles.add("850K");
//		testFiles.add("1M");
		
		System.out.println("test small dict with filtering on");
		DictTests.startDictTests(false, aqlModules, testFiles);
		System.out.println();
		System.out.println();		
	}
	
	public static void testLargeOn() {
		ArrayList<String> aqlModules = new ArrayList<>();
		//aqlModules.add("dictTest1");
		aqlModules.add("dictTest2");
		
		ArrayList<String> testFiles = new ArrayList<>();
		
		testFiles.add("100K");
		testFiles.add("250K");
		testFiles.add("400K");
		testFiles.add("550K");
//		testFiles.add("700K");
//		testFiles.add("850K");
//		testFiles.add("1M");

		System.out.println("test large dict with filtering on");
		DictTests.startDictTests(true, aqlModules, testFiles);
		System.out.println();
		System.out.println();		
	}
	
	public static void testLargeOff() {
		ArrayList<String> aqlModules = new ArrayList<>();
		//aqlModules.add("dictTest1");
		aqlModules.add("dictTest2");
		
		ArrayList<String> testFiles = new ArrayList<>();
		
		testFiles.add("100K");
		testFiles.add("250K");
		testFiles.add("400K");
//		testFiles.add("550K");
//		testFiles.add("700K");
//		testFiles.add("850K");
//		testFiles.add("1M");
		
		System.out.println("test large dict with filtering off");
		DictTests.startDictTests(false, aqlModules, testFiles);
		System.out.println();
		System.out.println();	
	}
}
