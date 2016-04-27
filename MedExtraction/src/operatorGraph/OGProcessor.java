package operatorGraph;

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
import com.ibm.avatar.api.exceptions.TextAnalyticsException;
import com.ibm.avatar.api.tam.ModuleMetadata;
import com.ibm.avatar.api.ExplainModule;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;


public class OGProcessor {

	private OperatorGraph operatorGraph;
	
	public OGProcessor(OperatorGraph operatorGraph) {
		this.operatorGraph = operatorGraph;
	}
	
	
	public void operatorGraphTest() {
		try {
//			TupleSchema tupleSchema = this.operatorGraph.getDocumentSchema();
//			for (int i = 0; i < tupleSchema.size(); ++i) {
//				String tupleName = tupleSchema.getFieldNameByIx(i);
//				String tupleType = tupleSchema.getFieldTypeByIx(i).toString();
//				System.out.printf("tuple name: %s | type: %s\n", tupleName, tupleType);
//			}
//			
//			Map<String,TupleSchema> test = this.operatorGraph.getOutputTypeNamesAndSchema();
//			for (String s : test.keySet()) {
//				System.out.println(s+" | "+test.get(s).toString());
//			}
			
			Writer writer = new StringWriter();
			ExplainModule.explain(new File("textAnalytics/bin/RegexTest.tam"), writer);
			System.out.println(writer.toString());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
