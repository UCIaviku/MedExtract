package test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import preprocessor.PreProcessor;
import extractor.*;

public class RegexTests {

	public static void startRegexTests(boolean filterOn, ArrayList<String> aqlModules, ArrayList<String> testFiles) {		
		String tempFolder = "/Users/georgewang/Documents/Project_Medextract/dataset/temp_seperate_50K/";
		String fileFolder = "/Users/georgewang/Documents/Project_Medextract/dataset/";
		String filePrefix = "abstract_";
		String fileSuffix = "K";
		
		
		for (String module : aqlModules) {
			PreProcessor preProcessor = new PreProcessor(module);
			
			for (String dataFileSize : testFiles) {
				System.gc();
				
				Extractor extractor = new Extractor(module);
				extractor.compile();
				extractor.load();
				extractor.prepareLogFile();
				int seperates_50K = 0;
				try {
					int size = Integer.parseInt(dataFileSize);
					if (size < 100)
						seperates_50K = 1;
					else
						seperates_50K = size/50;
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				HashSet<Integer> filteredLines = new HashSet<Integer>();
				
				for (int i = 1; i <= seperates_50K; ++i) {
					preProcessor.setGoFilesFolder(tempFolder+filePrefix+dataFileSize+fileSuffix+"/"+i+"/");
					if (filterOn){
						filteredLines.addAll(preProcessor.filterDocuments(fileFolder+filePrefix+dataFileSize+fileSuffix+".txt"));
						System.out.println(filteredLines.size());
					}
				}

				
				try {
					Scanner inFile = new Scanner(new File(fileFolder+filePrefix+dataFileSize+fileSuffix+".txt"));
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
//				
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
	
	public static void test_DateTimeRegex_FilterOn() {
		ArrayList<String> aqlModules = new ArrayList<>();
		aqlModules.add("dateTimeTest");
		
		ArrayList<String> testFiles = new ArrayList<>();
		testFiles.add("10");
		testFiles.add("20");
		testFiles.add("50");
		testFiles.add("100");
		testFiles.add("200");
		testFiles.add("300");
		testFiles.add("400");
		testFiles.add("500");
		
		System.out.println("test regex datetime with filtering on");
		startRegexTests(true, aqlModules, testFiles);
		System.out.println();
		System.out.println();
	}
	
	public static void test_DateTimeRegex_FilterOff() {
		ArrayList<String> aqlModules = new ArrayList<>();
		aqlModules.add("dateTimeTest");
		
		ArrayList<String> testFiles = new ArrayList<>();
		testFiles.add("10");
		testFiles.add("20");
		testFiles.add("50");
		testFiles.add("100");
		testFiles.add("200");
		testFiles.add("300");
		testFiles.add("400");
		testFiles.add("500");
		
		System.out.println("test regex datetime with filtering off");
		startRegexTests(false, aqlModules, testFiles);
		System.out.println();
		System.out.println();
	}
	
	
}
