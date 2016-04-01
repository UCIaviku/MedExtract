package test;

import java.io.File;
import java.io.IOException;
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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

import com.ibm.avatar.algebra.datamodel.TLIter;
import com.ibm.avatar.algebra.datamodel.Tuple;
import com.ibm.avatar.algebra.datamodel.TupleList;

import extractor.*;

public class LuceneTest {
    public static void main(String[] args) throws IOException, ParseException {
    	String dataFileName = "./resources/dataset/100K-abstract-present.txt";
    	
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory index = new RAMDirectory();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter writer = new IndexWriter(index, config);
        
        long startAddDocTime = System.currentTimeMillis();
        
        Scanner dataFile = new Scanner(new File(dataFileName));
		Integer lineNum = 1;
		while (dataFile.hasNextLine()) {
			addDoc(writer, dataFile.nextLine(), lineNum.toString());
			lineNum++;
		}
        dataFile.close();
		writer.close();
		
		double estimatedAddDocTime = ((double) System.currentTimeMillis() - startAddDocTime) / 1000;
		System.out.println("adding docs takes: "+estimatedAddDocTime+" seconds");
		
		long startQueryDictTime = System.currentTimeMillis();
		
		HashSet<Integer> filteredResults = new HashSet<Integer>();
		Scanner dictFile = new Scanner(new File("./resources/dictionaries/WebMD_symptoms.txt"));
		
		while (dictFile.hasNextLine()) {
			String queryStr = dictFile.nextLine().trim();
			if (!queryStr.isEmpty()) {
				QueryParser parser = new QueryParser("data",analyzer);
				parser.setDefaultOperator(QueryParser.Operator.AND);
				parser.setPhraseSlop(0);
				Query query = parser.createPhraseQuery("data",QueryParser.escape(queryStr));
				//Query query = new QueryParser("data", analyzer).parse(QueryParser.escape(queryStr));
				IndexReader reader = DirectoryReader.open(index);
				IndexSearcher searcher = new IndexSearcher(reader);
				int hitsPerPage = 50000;
				TopDocs docs = searcher.search(query,hitsPerPage);
				ScoreDoc[] hits = docs.scoreDocs;
								
				for(int i=0;i<hits.length;++i) {
				    int docId = hits[i].doc;
				    Document d = searcher.doc(docId);
				    filteredResults.add(Integer.parseInt(d.get("id")));
				}
				
				reader.close();
			}
		}
		
		dictFile.close();
		
		
		
		double estimatedQueryDictTime = ((double) System.currentTimeMillis() - startQueryDictTime) / 1000;
		System.out.println("searching takes: "+estimatedQueryDictTime+" seconds");
		
		System.out.println("total of "+filteredResults.size()+" docs after filtering");
		
        Scanner dataFileReopen = new Scanner(new File(dataFileName));
        
		Extractor extractor = new Extractor("dictTest1");
		extractor.compile();
		extractor.load();
		extractor.prepareLogFile("dictTest1");
		
		boolean printResult = false;
		
		try {
			StringBuffer sb = new StringBuffer();	
			Integer counter = 1;

			while (dataFileReopen.hasNextLine()) {
				sb.setLength(0);
				sb.append(dataFileReopen.nextLine());
				if (filteredResults.contains(counter)) {
					extractor.execute(sb.toString(), lineNum.toString());
				}
				counter++;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double executionTime = ((double) extractor.getTotalExecutionTime()) / 1000;
		System.out.println("total execution time is "+executionTime+" seconds");
		System.out.println("total number of results is "+extractor.getTotalResultsNum());
        
	}
		

    private static void addDoc(IndexWriter w, String title, String lineNum) throws IOException {
        Document doc = new Document();
        doc.add(new TextField("data", title, Field.Store.YES));

        // use a string field for isbn because we don't want it tokenized
        doc.add(new StringField("id", lineNum, Field.Store.YES));
        w.addDocument(doc);
    }
}
