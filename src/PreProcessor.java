import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;


public class PreProcessor {
	
	// see filterRegex() for further explanation
	private String goFilesFolder = null;
	
	private String aqlModulePath = "textAnalytics/src/";
	private String aqlModuleName = "";
	
	private boolean printRegexDict = false;
	
	private long indexTime;
	private long searchTime;
	
	public PreProcessor(String aqlModuleName) {
		this.aqlModuleName = aqlModuleName;
	}
	
	
	// pre-process the input data file
	// @param document is the path to the document file
	
	public HashSet<Integer> filterDocuments(String document) {
		HashSet<Integer> filteredLines = new HashSet<Integer>();
		
		ArrayList<String> regexList = new ArrayList<String>();
		ArrayList<String> dictList = new ArrayList<String>();
		ArrayList<String> aqlFiles = new ArrayList<String>();
		
		File folder = new File(aqlModulePath+aqlModuleName);
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				String fileName = listOfFiles[i].getName();
				if (fileName.endsWith("aql")) {
					aqlFiles.add(aqlModulePath+aqlModuleName+"/"+fileName);
					if (printRegexDict)
						System.out.println("AQL file: " + aqlModulePath+aqlModuleName+"/"+fileName);
				}  
			}
		}
		
		for (String aqlFileName : aqlFiles) {
			Parser parser = new Parser(aqlFileName);
			parser.parse();
			regexList.addAll(parser.getRegexList());
			dictList.addAll(parser.getDictList());
		}
		
		if (printRegexDict) {
			System.out.println("regex list: "+regexList.toString());
			System.out.println("dict list: "+dictList.toString());	
		}
		
		if (! regexList.isEmpty())
			filteredLines.addAll(filterRegex(regexList, document));
		if (! dictList.isEmpty())
			filteredLines.addAll(filterDictionary(dictList, document));
		if (printRegexDict) {
			System.out.println("total of "+filteredLines.size()+" docs after filtering");
		}
		
		return filteredLines;
	}
	
	private void createTempFiles(String document) {
		if (goFilesFolder != null) 
			return;
		
		// if the folder for temp files is not specified, then create one
		
		long startTempFileTime = System.currentTimeMillis();
		System.out.println("no temp foler provided, start splitting files");
		String tempFileFolder = "/Users/georgewang/Documents/Project_Medextract/dataset/temp/";	
		goFilesFolder = tempFileFolder;
		// create the temp folder if it doesn't exist, else clean the folder
		File tempFolder = new File(tempFileFolder);
		if (!tempFolder.exists() || !tempFolder.isDirectory()) {
			System.out.println("creating directory: " + tempFileFolder);
			try{
				tempFolder.mkdir();
			} 
			catch(SecurityException se){
				se.printStackTrace();
			}
		} else {
			File[] listOfFiles = tempFolder.listFiles();
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					listOfFiles[i].delete();
				}
			}
		}
		
		// create temp files based on the document
		try {
			Scanner doc = new Scanner(new File(document));
			Integer lineNum = 1;
			StringBuffer sb = new StringBuffer();
			while (doc.hasNextLine()) {
				sb.setLength(0);
				sb.append(doc.nextLine());
				File tempFile = new File(tempFileFolder+lineNum.toString());
				try {
					if(!tempFile.exists()) { tempFile.createNewFile(); }
					PrintWriter tempFileWriter = new PrintWriter(new FileWriter(tempFile, true));
					Integer cropLength = 1500;
					if (sb.length() < cropLength) {
						tempFileWriter.println(sb.toString());
					} else {
						Integer currentPos = 0;
						while (currentPos+cropLength < sb.length()) {
							tempFileWriter.println(sb.substring(currentPos, currentPos+cropLength));
							currentPos += cropLength;
						}
						tempFileWriter.println(sb.substring(currentPos));
					}
					tempFileWriter.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				lineNum++;
			}
			doc.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		double estimatedTmpeFileTime = ((double) System.currentTimeMillis() - startTempFileTime) / 1000;
		System.out.println("spliting files takes: "+estimatedTmpeFileTime+" seconds");
	}
	

	private HashSet<Integer> filterRegex(ArrayList<String> regexList, String document) {
		HashSet<Integer> filteredLines = new HashSet<Integer>();
		String execIndex = "/Users/georgewang/documents/project_medextract/go/bin/cindex";
		String execSearch = "/Users/georgewang/documents/project_medextract/go/bin/csearch";
		
		createTempFiles(document);
		
		if (goFilesFolder == null) {
			System.err.println("temp file folder for regex not set up!");
		}
		
		if (! goFilesFolder.endsWith("/")) {
			goFilesFolder = goFilesFolder+"/";
		}
		System.out.println(goFilesFolder);
		
		// run google code search
		Runtime runtime = Runtime.getRuntime();
		try {
			// clear index
			Process resetIndex = runtime.exec(new String[]{execIndex, "-reset"});
			BufferedReader resetInput = new BufferedReader(new InputStreamReader(resetIndex.getInputStream()));
			while (resetInput.readLine() != null) {}
			resetIndex.waitFor();
			
			// build index
			long startIndexTime = System.currentTimeMillis();
			
			ProcessBuilder pbIndex = new ProcessBuilder(execIndex, goFilesFolder);
			pbIndex.redirectError();
			Process buildIndex = pbIndex.start();
			BufferedReader indexInput = new BufferedReader(new InputStreamReader(buildIndex.getInputStream()));
			while (indexInput.readLine() != null) {}
			buildIndex.waitFor();
			
			long endIndexTime = System.currentTimeMillis();
			indexTime += (endIndexTime - startIndexTime);
			
			// search
			for (String regex : regexList) {
				long startSearchTime = System.currentTimeMillis();
				
				ProcessBuilder pbSearch = new ProcessBuilder(execSearch, "-l", "-f", goFilesFolder, regex);
				pbSearch.redirectError();
				Process searchRegex = pbSearch.start();
				BufferedReader input = new BufferedReader(new InputStreamReader(searchRegex.getInputStream()));
				String inputLine = null; 
				while ((inputLine = input.readLine()) != null) {
					try {
						File inputLineFile = new File(inputLine);
						String inputLineAbsolutePath = inputLineFile.getCanonicalPath();
						Integer lineNum = Integer.parseInt(inputLineAbsolutePath.substring(goFilesFolder.length()));
						filteredLines.add(lineNum);
						//System.out.println(lineNum);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
				searchRegex.waitFor();
				
				long endSearchTime = System.currentTimeMillis();
				searchTime += (endSearchTime - startSearchTime);
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return filteredLines;
	}
	
	private HashSet<Integer> filterDictionary(ArrayList<String> dictList, String document) {
		HashSet<Integer> filteredLines = new HashSet<Integer>();
		
		ArrayList<String> absoluteDictPaths = new ArrayList<String>();
		File aqlModuleFolder = new File(aqlModulePath+aqlModuleName);
		for (String relativePath : dictList) {
			try {
				File dictFile = new File(aqlModuleFolder, relativePath);
				String absolute = dictFile.getCanonicalPath();
				if (printRegexDict) {
					System.out.println("dict absolute path: "+absolute);
				}
				absoluteDictPaths.add(absolute);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory index = new RAMDirectory();
        long startAddDocTime = System.currentTimeMillis();
		try {
	        IndexWriterConfig config = new IndexWriterConfig(analyzer);
	        IndexWriter writer = new IndexWriter(index, config);
	        Scanner dataFile = new Scanner(new File(document));
			Integer lineNum = 1;
			while (dataFile.hasNextLine()) {
		        Document doc = new Document();
		        doc.add(new TextField("data", dataFile.nextLine(), Field.Store.YES));
		        doc.add(new StringField("id", lineNum.toString(), Field.Store.YES));
		        writer.addDocument(doc);
				lineNum++;
			}
	        dataFile.close();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		double endAddDocTime = System.currentTimeMillis();
		indexTime += (endAddDocTime - startAddDocTime);
		
		
		long startQueryDictTime = System.currentTimeMillis();
		for (String dictPath : absoluteDictPaths) {
			try {
				Scanner dictFile = new Scanner(new File(dictPath));
				while (dictFile.hasNextLine()) {
					String queryStr = dictFile.nextLine().trim();
					if (!queryStr.isEmpty()) {
						QueryParser parser = new QueryParser("data",analyzer);
						parser.setDefaultOperator(QueryParser.Operator.AND);
						parser.setPhraseSlop(0);
						Query query = parser.createPhraseQuery("data",QueryParser.escape(queryStr));
						IndexReader reader = DirectoryReader.open(index);
						IndexSearcher searcher = new IndexSearcher(reader);
						//TODO: too many docs?
						TopDocs docs = searcher.search(query,reader.numDocs());
						ScoreDoc[] hits = docs.scoreDocs;
										
						for(int i=0;i<hits.length;++i) {
						    int docId = hits[i].doc;
						    Document d = searcher.doc(docId);
						    filteredLines.add(Integer.parseInt(d.get("id")));
						}
						
						reader.close();
					}
				}
				dictFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		
		long endQueryDictTime = System.currentTimeMillis();
		searchTime += (endQueryDictTime - startQueryDictTime);
		
		return filteredLines;
	}
	


	
	// set the goFilesFolder, it's converted to absolute path
	
	public void setGoFilesFolder(String s) {
		try {
			File goFilesFolderFile = new File(s);
			String goFilesFolderAbsolutePath = goFilesFolderFile.getCanonicalPath();
			this.goFilesFolder = goFilesFolderAbsolutePath;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public double getIndexTimeSecs() {
		return indexTime/1000.0;
	}
	
	public double getSearchTimeSecs() {
		return searchTime/1000.0;
	}
	
	
	
}
