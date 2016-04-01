package extractor;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;



import com.ibm.avatar.algebra.datamodel.FieldSetter;
import com.ibm.avatar.algebra.datamodel.FieldType;
import com.ibm.avatar.algebra.datamodel.TLIter;
import com.ibm.avatar.algebra.datamodel.TextSetter;
import com.ibm.avatar.algebra.datamodel.Tuple;
import com.ibm.avatar.algebra.datamodel.TupleList;
import com.ibm.avatar.algebra.datamodel.TupleSchema;
import com.ibm.avatar.algebra.util.tokenize.TokenizerConfig;
import com.ibm.avatar.api.CompileAQL;
import com.ibm.avatar.api.CompileAQLParams;
import com.ibm.avatar.api.ExternalTypeInfo;
import com.ibm.avatar.api.ExternalTypeInfoFactory;
import com.ibm.avatar.api.OperatorGraph;


public class Extractor {
	
	private OperatorGraph operatorGraph = null;

	// file path related to AQL module
	private String aqlModulePath = "textAnalytics/src/";
	private String aqlModuleName = "";
	private String binaryPath = "textAnalytics/bin/";
	private String dependencyPath = "textAnalytics/lib/";
	private String externalDictName = "";
	private String externalDictURI = "";
	
	// path and PrintWriter for writing result to log file
	private String logFilePath = "./result";
	private PrintWriter logWriter = null;

	// control if print the time and results
	private boolean printTimeOn = false;
	private boolean printExecuteTimeOn = false;
	private boolean printResult = false;
	
	// load() and execute() will exit if not compileSuccessful
	// compile is not necessary if running the same module again (it's already compiled)
	// you can skip compile(), set this variable to true and save a few seconds of compilation time
	private boolean compileSuccessful = false;

	// variables for measuring time
	private int totalResultsNum = 0;
	private long executionTime = 0;
	

	// the AQL module name is specified in the constructor
	public Extractor(String aqlModuleName) {
		this.aqlModuleName = aqlModuleName;
	}
	
	
	// public functions that use the default path to compile, load and execute
	// compile() calls compileModule, load() calls loadModule(), execute() calls exeucteModule() see those functions for details
	
	public void compile() {
		long compileStartTime = System.currentTimeMillis();
		if (printTimeOn) 
			System.err.println("compile starts at: " + formatTime(compileStartTime) + "\n");
		
		this.compileModule(new String[]{ aqlModulePath+aqlModuleName }, binaryPath, dependencyPath);
		
		if (printTimeOn) 
			System.err.println("total compile time is: " + formatTimeDiff(System.currentTimeMillis()-compileStartTime) + "\n");
		
		compileSuccessful = true;
	}
	
	public void load() {
		if (! compileSuccessful) {
			System.err.println("load: compile failed / not yet compiled");
			return;
		}
		
		long loadStartTime = System.currentTimeMillis();
		if (printTimeOn) 
			System.err.println("load starts at: " + formatTime(loadStartTime) + "\n");

		this.operatorGraph = loadModule(new String[]{ aqlModuleName }, new File(binaryPath).toURI().toString(), 
				externalDictName, externalDictURI);
		
		if (printTimeOn) 
			System.err.println("total load time is: " + formatTimeDiff(System.currentTimeMillis() - loadStartTime) + "\n");
	}
	
	
	// execute takes docContent, which is data itself, and docID, an ID of this document used in log file
	public void execute(String docContent, String docID) {
		if (! compileSuccessful) {
			System.err.println("execute: compile failed / not yet compiled");
			return;
		}
		
		// calls executeModule() and records time
		long executeStartTime = System.currentTimeMillis();
		if (printExecuteTimeOn) 
			System.err.println("execution starts at: " + formatTime(executeStartTime) + "\n");
		
		Map<String, TupleList> results = executeModule(this.operatorGraph, docContent);
		
		executionTime += System.currentTimeMillis()-executeStartTime;
		if (printExecuteTimeOn) 
			System.err.println("execution time is: " + formatTimeDiff(System.currentTimeMillis()-executeStartTime) + "\n");
		
	
		// executeModule() returns Map<String, TupleList>
		// each key is a view, each value is the result list of that view
		// more details can be found at section 4 of the following link
		// http://www.ibm.com/support/knowledgecenter/SSPT3X_4.0.0/com.ibm.swg.im.infosphere.biginsights.text.doc/doc/ana_txtan_apis.html
		for(String viewName : results.keySet()) {
			TupleList tups = results.get(viewName);
			TLIter itr = tups.iterator();
			while (itr.hasNext()) { 
				Tuple tup = itr.next();
				this.logWriter.append(tup.toString()+"   |   "+docID+"\n");
				if (printResult) 
					System.out.println(tup+"   |   "+docID);
			}
		}
		
		// update total result number
		if (results != null) {
			for (String viewName : results.keySet()) {
				totalResultsNum += results.get(viewName).size();
			}
		}
	}
	
	
	
	// compileModule: compile the AQL files to binary files
	// loadModule: load the module into memory
	// executeModule: execute the module with data
	
	// code is adapted from the following link, more references can also be found there
	// http://www.ibm.com/support/knowledgecenter/SSPT3X_4.0.0/com.ibm.swg.im.infosphere.biginsights.text.doc/doc/ana_txtan_apis.html
	// (same as the link above)


	/* Compile AQL files into module files (.tam)
	 * 
	 * inputModuleSource - Array to hold URIs pointing to the AQL module source directory
	 * compilationOutputURI - URI to the location where .tam files resulting from compilation of AQL module source should be stored
	 * dependentModulesURI - URI to the location where pre-compiled module files (.tam) required to compile AQL module source files are found
	 */
	private void compileModule(String[] inputModuleSource, String compilationOutputURI, String dependentModulesURI) {
		try {
			// Create an instance of the Standard tokenizer configuration; this configuration must be
			// used when white-space based tokenization suffices in compiling the dictionaries used by
			// the AQL source.
			TokenizerConfig.Multilingual multilinTokenizer = new TokenizerConfig.Multilingual();
	
			// Instantiate the container for compiler parameter
			CompileAQLParams moduleCompilationParams = new CompileAQLParams();
	
			// Populate the compilation parameters into the above instantiated CompileAQLParam object
			moduleCompilationParams.setInputModules(inputModuleSource);
			moduleCompilationParams.setModulePath(dependentModulesURI);
			moduleCompilationParams.setOutputURI(compilationOutputURI);
	
			// Set the tokenizer configuration used to compile dictionaries;defaults to Standard white-space tokenizer
			moduleCompilationParams.setTokenizerConfig(multilinTokenizer);

			// Compile the modules and write the compiled module(.tam) files in the location specified by setOutputURI() methods
			CompileAQL.compile(moduleCompilationParams);
			
			this.compileSuccessful = true;
			
		} catch(Exception exp) {
			exp.printStackTrace();
		}
	}


	/* Load the compiled module files (.tam) into a compiled module files
	 * return an instance of OperatorGraph
	 * 
	 * textAnalyticsModules - Name of the AQL modules to be loaded
	 * compiledModulesPath - URI to the location where the compiled modules should be stored
	 * externalDictName - Qualified name of the external dictionary 'abbreviations' declared in the module 'metricsIndicator_dictionaries' 
	 * 						through the 'create external dictionary...' statement
	 * externalDictURI - URI pointing to the file abbreviations.dict containing entries for 'abbreviations' external dictionary
	 */
	private OperatorGraph loadModule(String[] textAnalyticsModules, String compiledModulesPath, String externalDictName, String externalDictURI) {
		OperatorGraph extractor = null;
		try {
			// Create an instance of Standard tokenizer configuration; this configuration should be used, if white-space based
			// tokenization is sufficient. Step#3 describes how to use the Multilingual tokenizer configurtion
			TokenizerConfig.Multilingual multilinTokenizer = new TokenizerConfig.Multilingual();
	
			// Create an empty instance of the container used to pass in actual content of external dictionaries and
			// external tables to the loader
			ExternalTypeInfo externalTypeInfo = ExternalTypeInfoFactory.createInstance();
			
			// Populate the empty ExternalTypeInfo object with entries for 'abbreviations' dictionary
			if(!externalDictName.isEmpty() && !externalDictURI.isEmpty()) { 
				externalTypeInfo.addDictionary(externalDictName, externalDictURI);
			}
	
			// Instantiate the OperatorGraph object		
			extractor = OperatorGraph.createOG(textAnalyticsModules, compiledModulesPath, externalTypeInfo, multilinTokenizer);
			
		} catch(Exception exp) {
			exp.printStackTrace();
		}
		return extractor;
	}
	

	private Map<String, TupleList> executeModule(OperatorGraph extractor, String docContent) {		
		Map<String, TupleList> results = null;
		
		try {
			// Fetch schema of the document expected by the extractor
			TupleSchema documentSchema = extractor.getDocumentSchema();
						
			// Prepare accessor to set fields in the document tuple
			Map<String, FieldSetter<?>> fieldNameVsAccessor = new HashMap<String, FieldSetter<?>> ();
			String[] fieldNames = documentSchema.getFieldNames();
						
			for (int i=0; i<fieldNames.length; ++i) {
				FieldSetter<?> fieldSetter = null;
				FieldType fieldType = documentSchema.getFieldTypeByName(fieldNames[i]);

				if (fieldType.getIsText()) { fieldSetter = documentSchema.textSetter(fieldNames[i]); }

				fieldNameVsAccessor.put(fieldNames[i], fieldSetter);
			}

			// Prepare the document tuple from the document content. This example assumes, that the document tuple contains a
			// field name 'text' of type Text; and the document content is populated into this 'text' field. For the sake of
			// brevity, this example populates other text fields in the document tuple with the field name and integer fields
			// with the index of the field in the tuple
			Tuple docTuple = documentSchema.createTup();

			for (int i=0; i<fieldNames.length; ++i) {
				FieldSetter<?> fieldSetter = fieldNameVsAccessor.get(fieldNames[i]);
				FieldType fieldType = documentSchema.getFieldTypeByName(fieldNames[i]);

				if (fieldType.getIsText ()) {
					if (fieldNames[i].equals("text"))
						((TextSetter) fieldSetter).setVal(docTuple, docContent);
					else
						((TextSetter) fieldSetter).setVal(docTuple, fieldNames[i]);
				}
			}

			// Execute the operator graph on the current document, generating every single output type that the extractor
			// produces. The second argument is an optional list of what output types to generate; null means "return all types"
			// The third argument is the content of the external views; this argument is optional

			results = extractor.execute(docTuple, null, null);
						
		} catch (Exception e) {
			e.printStackTrace();
		}

		return results;
	}
	
	
	
	// functions for log file, remember to close the file!
	
	public void prepareLogFile() {
		this.prepareLogFile("");
	}
	
	public void prepareLogFile(String logAnnotation) {
		String currentTime = formatTime(System.currentTimeMillis());
		String logFileName = logFilePath + "/log-"+logAnnotation+"-"+currentTime+".txt";
		
		File logFile = new File(logFileName);
		try {
			if(!logFile.exists()) { logFile.createNewFile(); }
			this.logWriter = new PrintWriter(new FileWriter(logFile, true));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void closeLogFile() {
		this.logWriter.close();
	}
	
	

	
	// get execution time in seconds

	public double getExecutionTimeSecs() {
		return executionTime/1000.0;
	}
	
	
	
	// helper functions to format time to string
	
	public String formatTime(long time) {
        Timestamp timestamp = new Timestamp(time);
        Date date = new Date(timestamp.getTime());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        
        return sdf.format(date).toString();
	}
	
	
	public String formatTimeDiff(long timeDiff) {
		StringBuilder result = new StringBuilder();
				
		long diffSeconds = timeDiff / 1000 % 60;
		long diffMinutes = timeDiff / (60 * 1000) % 60;
		long diffHours = timeDiff / (60 * 60 * 1000) % 24;
		long diffDays = timeDiff / (24 * 60 * 60 * 1000);
		
		result.append(diffDays + " Days " + diffHours + " Hours " + diffMinutes + " Minutes " + diffSeconds + " Seconds");
		
		return result.toString();
	}


	
	// auto-generated getters and setters
	
	public void setPrintTimeOn() {
		printTimeOn = true;
	}

	public void setPrintTimeOff() {
		printTimeOn = false;
	}
	
	public void setPrintExecuteTimeOn() {
		printExecuteTimeOn = true;
	}
	
	public void setPrintExecuteTimeOff() {
		printExecuteTimeOn = false;
	}
	
	public String getAqlModulePath() {
		return aqlModulePath;
	}

	public void setAqlModulePath(String aqlModulePath) {
		this.aqlModulePath = aqlModulePath;
	}

	public String getAqlModuleName() {
		return aqlModuleName;
	}

	public void setAqlModuleName(String aqlModuleName) {
		this.aqlModuleName = aqlModuleName;
	}

	public String getBinaryPath() {
		return binaryPath;
	}

	public void setBinaryPath(String binaryPath) {
		this.binaryPath = binaryPath;
	}

	public String getDependencyPath() {
		return dependencyPath;
	}

	public void setDependencyPath(String dependencyPath) {
		this.dependencyPath = dependencyPath;
	}

	public String getExternalDictName() {
		return externalDictName;
	}

	public void setExternalDictName(String externalDictName) {
		this.externalDictName = externalDictName;
	}

	public String getExternalDictURI() {
		return externalDictURI;
	}

	public void setExternalDictURI(String externalDictURI) {
		this.externalDictURI = externalDictURI;
	}

	public int getTotalResultsNum() {
		return totalResultsNum;
	}

	public long getTotalExecutionTime() {
		return executionTime;
	}
	
}