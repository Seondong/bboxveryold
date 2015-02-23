package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;

import com.hp.hpl.jena.util.FileUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;

public class Class_Sim {
	
	static String owlfilePath;
	//static String owlfilePath = "DBpedia_AllIns";
	static JenaOWLModel owlModel;
	static String[] name;
	static double[][] distances;
	static MongoClient mongoClient;
	static DB db;
	static DBCollection table;

	public static void classCluster(String classname, int numofcluster) throws Exception
	{
		File path = new File(".");
		String path2 = path.getCanonicalPath();
		String path3 = path2.replaceAll("\\\\", "/");
		owlfilePath = path3+"/DBpedia_AllIns";
		
		owlModel = loadExistSchema();
    	
		mongoClient = new MongoClient();
		db = mongoClient.getDB("LinkagePoint_KoreanDbpediaEdgesinandout");
		table = db.getCollection("KoreanDbpediaEdgesinandout");
		
		List<String> names = subClass(owlModel, classname); // �ι�_����
    	int numberofclass = names.size();
    	name = new String[numberofclass];
    	for(int i=0;i<numberofclass;i++)
    		name[i] = names.get(i);
    	distances = new double[numberofclass][numberofclass];
    	
    	for(int i=0;i<numberofclass;i++)
    	{
    		for(int j=i;j<numberofclass;j++)
    		{
    			if(i==j)
    			{
    				distances[i][j] = 0;
    				//System.out.println(names.get(i) + "--" + names.get(j) + " = " + distances[i][j]);
    			}
    			else
    			{
    				System.out.println(names.get(i) + " and " + names.get(j) + "now calculating...");
    				distances[i][j] = calculateClass(names.get(i), names.get(j));
    				distances[j][i] = distances[i][j];
    				System.out.println("Finish");
    				//System.out.println(names.get(i) + "--" + names.get(j) + " = " + distances[i][j]);
    			}
    		}
    	}
    	
    	mongoClient.close();
    	
    	RConnection c = new RConnection();
		c.assign("res", distances[0]);
		for (int i = 1; i < names.size(); i++)
		{
			c.assign("tmp", distances[i]);
			c.eval("res <- rbind(res,tmp)");
		}
		  
		c.assign("names", name);
		
		REXP xp = c.eval("try(png(\"" + path3 + "/classcluster1.png\"))");
		c.eval("hc <- hclust(dist(1-res), method=\"ward.D2\")");
		c.eval("clustnumber <- cutree(hc, k=" + numofcluster + ")");
		c.parseAndEval("plot(hc, labels=names)");
		c.voidEval("rect.hclust(hc," + numofcluster + ")");
		c.voidEval("dev.off()");  
		   
		c.eval("tab1clustn <- data.frame(names, clustnumber)");
		c.eval("write.table(tab1clustn,  file=\"" + path3 + "/classclustn1.csv\", row.names=FALSE)");

 	    c.close();
		
		createMiddleclass(owlModel, "classclustn.csv", classname, numofcluster);
	}
	private static void createMiddleclass(JenaOWLModel owlModel, String filename, String superclass, int numberofclass) throws IOException
	{
		OWLNamedClass super_class = owlModel.getOWLNamedClass(superclass);
		ArrayList<OWLNamedClass> class_set = new ArrayList<OWLNamedClass>();
		for(int i = 1; i <= numberofclass; i++)
			class_set.add(owlModel.createOWLNamedSubclass("����"+superclass+i, super_class));
		
		File filedir = new File(filename);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filedir),"UTF-8"));
		in.readLine();
		StringTokenizer st = null;
		String inst = null;
		while((inst = in.readLine()) != null)
		{
			inst = inst.replaceAll("\"", "");
			st = new StringTokenizer(inst, " ");
			
			String classname = st.nextToken();
			String classnumber = st.nextToken();
			OWLNamedClass moveClass = owlModel.getOWLNamedClass(classname);
			moveClass.addSuperclass(class_set.get(Integer.parseInt(classnumber)-1));
			moveClass.removeSuperclass(super_class);
		}
		
		in.close();
		saveSchema(owlModel);
	}
	private static JenaOWLModel loadExistSchema() throws OntologyLoadException {
		String uri = "file:///" + owlfilePath + ".owl";
		JenaOWLModel model = ProtegeOWL.createJenaOWLModelFromURI(uri);
		
		return model;
	}
	private static void saveSchema(JenaOWLModel model) {
    	Collection errors = new ArrayList();
    	model.save(new File(owlfilePath+"_output2.owl").toURI(), FileUtils.langXMLAbbrev, errors);
    	System.out.println("File saved with " + errors.size() + " errors.");
	}
	
	private static List<String> subClass(JenaOWLModel model, String Class){
		JenaOWLModel owlModel = model;
		OWLNamedClass superClass = owlModel.getOWLNamedClass(Class);
		Collection<OWLNamedClass> subClass = superClass.getSubclasses(false);
		Iterator<OWLNamedClass> it = subClass.iterator();
		List<String> stringList = new ArrayList<String>();
		
		while(it.hasNext())
		{
			stringList.add(it.next().getBrowserText());
		}
		
		return stringList;
	}

	private static double calculateClass(String class_name1, String class_name2) throws IOException
	{
		
		BasicDBObject query = new BasicDBObject("Class", class_name1);
		BasicDBObject field = new BasicDBObject();
		field.put("_id", 0);
		field.put("Class", 0);
		field.put("InstanceName", 0);
		
		BasicDBObject query2 = new BasicDBObject("Class", class_name2);
		BasicDBObject field2 = new BasicDBObject();
		field2.put("_id", 0);
		field2.put("Class", 0);
		field2.put("InstanceName", 0);
		
		DBCursor cursor = table.find(query, field);
		cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
		double similarity = 0;
		int numberofins = 0; // The number of instances in class_name1
		while(cursor.hasNext())
		{
			
			String temp = cursor.next().toString();
			ArrayList<ArrayList<String>> ins1 = new ArrayList<ArrayList<String>>();
			
			if(temp.equals("{ }")) // No property
				continue;
			
			JSONObject jsonobj = (JSONObject) JSONValue.parse(temp);
			Iterator iter = jsonobj.keySet().iterator();
			
			while(iter.hasNext()){
				
			   String key = (String) iter.next();
			   Object value = jsonobj.get(key);
			   if(value.equals(""))
				   continue;
			   
			   JSONObject jsonobj2 = (JSONObject) JSONValue.parse(value.toString());
			   Iterator iter2 = jsonobj2.keySet().iterator();
			   ArrayList<String> multi_value = new ArrayList<String>();
			   while(iter2.hasNext())
			   {
				   String key2 = (String) iter2.next();
				   Object value2 = jsonobj2.get(key2);
				   String temp_value = value2.toString();
				   
				   if(temp_value.contains("ko.dbpedia.org")) // remove URI ( relation )
				   {
					   int index = temp_value.indexOf("ko.dbpedia.org/resource/");
					   String real_value = temp_value.substring(index+24); 
					   multi_value.add(real_value);
				   }
				   else
					   multi_value.add(temp_value);
			   }
			   if(multi_value.size() != 0)
			   {
				   ins1.add(multi_value);
			   }
				   
			}
			
			double mostsimilar_ins = 0;	// The most similar ins 2 with ins 1
			DBCursor cursor2 = table.find(query2, field2);
			cursor2.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
			int numberofins2 = 0; // The number of instances in class_name1
			while(cursor2.hasNext())
			{
				String temp2 = cursor2.next().toString();
				ArrayList<ArrayList<String>> ins2 = new ArrayList<ArrayList<String>>();
				
				if(temp2.equals("{ }")) // No property
					continue;

				JSONObject jsonobj2 = (JSONObject) JSONValue.parse(temp2);
				Iterator iter2 = jsonobj2.keySet().iterator();
				
				while(iter2.hasNext()){
					
				   String key = (String) iter2.next();
				   Object value = jsonobj2.get(key);
				   if(value.equals(""))
					   continue;
				   
				   JSONObject jsonobj3 = (JSONObject) JSONValue.parse(value.toString());
				   Iterator iter3 = jsonobj3.keySet().iterator();
				   ArrayList<String> multi_value = new ArrayList<String>();
				   while(iter3.hasNext())
				   {
					   String key2 = (String) iter3.next();
					   Object value2 = jsonobj3.get(key2);
					   String temp_value = value2.toString();
					   
					   if(temp_value.contains("ko.dbpedia.org")) // remove URI ( relation )
					   {
						   int index = temp_value.indexOf("ko.dbpedia.org/resource/");
						   String real_value = temp_value.substring(index+24); 
						   multi_value.add(real_value);
					   }
					   else
						   multi_value.add(temp_value);
				   }
				   if(multi_value.size() != 0)
				   {
					   ins2.add(multi_value);
				   }
				   
				}
				
				////////////////Calculate Between Instances ////////////////
				double temp_result = 0;	// Biggest values for instance 1
				
				for(int i=0;i<ins1.size();i++)
				{
					double Biggest_value = 0;	// Biggest values for attribute i
					for(int j=0;j<ins2.size();j++)
					{
						
						for(int k=0;k<ins1.get(i).size();k++)
						{
							for(int l=0;l<ins2.get(j).size();l++)
							{
								double temp_value = CalculateSimilarity(ins1.get(i).get(k), ins2.get(j).get(l));
								
								if( Biggest_value < temp_value)
									Biggest_value = temp_value;
							}
							
						}	
					}
					temp_result = temp_result + Biggest_value;
				}
				temp_result = temp_result / ins1.size();
				if( mostsimilar_ins < temp_result )
					mostsimilar_ins = temp_result;
				numberofins2++;
			//////////////////////////////////////////////////////////////
			}
			cursor2.close();
			if(numberofins2 == 0)
			{
				cursor.close();
				return 0;
			}

			similarity = similarity + mostsimilar_ins;
			numberofins++;
		}
		
		cursor.close();
		
		if(numberofins == 0)
			return 0;
		else
			return (similarity / numberofins);
	}
	
	private static double CalculateSimilarity(String from_ins1, String from_ins2) // add top K similarity score divided by the number of yago or dbpedia
	{

		//MongeElkan dist = new MongeElkan();
		//JaroWinkler dist2 = new JaroWinkler();
		JaccardSimilarity dist3 = new JaccardSimilarity();
		//CosineSimilarity dist4 = new CosineSimilarity();
		

		//double result = dist.getSimilarity(from_ins1, from_ins2);
		//double result = dist2.getSimilarity(from_ins1, from_ins2);
		double result = dist3.getSimilarity(from_ins1, from_ins2);
		//double result = dist4.getSimilarity(s, s1);
		
		if( isStringInt(from_ins1) && isStringInt(from_ins2))
		{	
			if(from_ins1.equals(from_ins2))
				result = 1;
			else
				result = 0;
		}
		if( isStringDouble(from_ins1) && isStringDouble(from_ins2))
		{	
			if(from_ins1.equals(from_ins2))
				result = 1;
			else
				result = 0;
		}
		return result;
		
	}
	
	private static boolean isStringInt(String s) 
	{
	    try {
	    	Integer.parseInt(s);
	        return true;
	    } 
	    catch (NumberFormatException e) {
	        return false;
	    }
	}
	
	private static boolean isStringDouble(String s) 
	{
	    try {
	    	Double.parseDouble(s);
	        return true;
	    } 
	    catch (NumberFormatException e) {
	        return false;
	    }
	}
	
}
