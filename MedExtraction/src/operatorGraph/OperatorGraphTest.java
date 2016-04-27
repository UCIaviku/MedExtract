package operatorGraph;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;

import com.ibm.avatar.api.ExplainModule;

import extractor.Extractor;


public class OperatorGraphTest {

	public static void main(String[] args) {
		
		String aqlModule = "RelationTest2";
		System.out.printf("module %s\n\n", aqlModule);
		System.out.println();
		Extractor extractor = new Extractor(aqlModule);
		
		boolean compile = true;
		if (compile) {
			extractor.compile();
		}
		
		try {
			Writer writer = new StringWriter();
			ExplainModule.explain(new File("textAnalytics/bin/"+aqlModule+".tam"), writer);
			System.out.println(writer.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
