import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;


public class GoogleCodeSearchTest {
	
	private static boolean printResult = false;
	
	public static void main(String[] args) {
		String execIndex = "/Users/georgewang/documents/project_medextract/go/bin/cindex";
		String execSearch = "/Users/georgewang/documents/project_medextract/go/bin/csearch";
		String fileFolder = "/Users/georgewang/documents/project_medextract/workspace/MedExtraction/resources/dataset/abstract_oneK/";
		//String regex = "^\\w+@[a-zA-Z_]+?\\.[a-zA-Z]{2,3}$";
		String regex = "water";
		
		
		Runtime runtime = Runtime.getRuntime();
		try {
			// clear index
			Process resetIndex = runtime.exec(new String[]{execIndex, "-reset"});
			BufferedReader resetInput = new BufferedReader(new InputStreamReader(resetIndex.getInputStream()));
			while (resetInput.readLine() != null) {}
			resetIndex.waitFor();
			
			// build index
			long startIndexTime = System.currentTimeMillis();
			System.out.println(execIndex+" "+fileFolder);
			ProcessBuilder pbIndex = new ProcessBuilder(execIndex, fileFolder);
			pbIndex.redirectError();
			Process buildIndex = pbIndex.start();
			//Process buildIndex = runtime.exec(new String[]{execIndex, fileFolder});
			BufferedReader indexInput = new BufferedReader(new InputStreamReader(buildIndex.getInputStream()));
			//BufferedReader indexError = new BufferedReader(new InputStreamReader(buildIndex.getErrorStream()));
			while (indexInput.readLine() != null) {}

			
			// search
			buildIndex.waitFor();
			double estimatedIndexTime = ((double) System.currentTimeMillis() - startIndexTime) / 1000;
			System.out.println("build index takes: "+estimatedIndexTime+" seconds");
			
			long startSearchTime = System.currentTimeMillis();
			System.out.println(execSearch+" -l"+" "+fileFolder+" "+regex);
			ProcessBuilder pbSearch = new ProcessBuilder(execSearch, "-l", "-f", fileFolder, regex);
			pbSearch.redirectError();
			Process searchRegex = pbSearch.start();
			//Process searchRegex = runtime.exec(new String[]{execSearch, "-l", fileFolder, regex});
			
			ArrayList<String> filteredDocs = new ArrayList<String>();
			
			BufferedReader input = new BufferedReader(new InputStreamReader(searchRegex.getInputStream()));
					
			String line = null; 
			int counter = 0;
			while ((line = input.readLine()) != null) {
				filteredDocs.add(line);
				counter++;
			}
			
			searchRegex.waitFor();	
			double estimatedSearchTime = ((double) System.currentTimeMillis() - startSearchTime) / 1000;
			System.out.println("search takes: "+estimatedSearchTime+" seconds");
			System.out.println(counter+" filtered docs in total");
			
			
			
			Extractor extractor = new Extractor("regexTest1");
			extractor.compile();
			extractor.load();
			extractor.prepareLogFile("regexTest1");
			
			try {
				StringBuffer sb = new StringBuffer();
				
				Integer count = 1;
				while (count < filteredDocs.size()) {
					String doc = filteredDocs.get(count-1);
					Integer docNum = Integer.parseInt(doc.replaceAll("[\\D]", ""));
					Scanner inFile = new Scanner(new File(doc));
					sb.setLength(0);
					while (inFile.hasNextLine()) {
						sb.append(inFile.nextLine());
					}	
					inFile.close();
					extractor.execute(sb.toString(), docNum.toString());
					count++;	
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			double executionTime = ((double) extractor.getTotalExecutionTime()) / 1000;
			System.out.println("total execution time is "+executionTime+" seconds");
			System.out.println("total number of results is "+extractor.getTotalResultsNum());

				
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
