import java.io.File;
import java.util.Scanner;

public class MyExtraction {
	
	public static void main(String[] args) {
		String dataFileName = "/Users/georgewang/Documents/Project_Medextract/dataset/abstract_10K.txt";
		
		String aqlModule = "relationTest2";
		Extractor extractor = new Extractor(aqlModule);
		extractor.compile();
		extractor.load();
		extractor.prepareLogFile();

		
		try {
			Scanner inFile = new Scanner(new File(dataFileName));
			Integer lineNum = 1;
			while (inFile.hasNextLine()) {
				String nextLine = inFile.nextLine();
				extractor.execute(nextLine, lineNum.toString());
				lineNum++;
			}
			inFile.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		extractor.closeLogFile();
		
		System.out.printf("total execution time is %.3f seconds\n", extractor.getExecutionTimeSecs());
		System.out.printf("total number of results is %d seconds\n", extractor.getTotalResultsNum());
	}

}
