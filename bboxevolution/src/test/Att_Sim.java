package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.Rserve.RConnection;
import org.rosuda.REngine.Rserve.RserveException;

import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;
import uk.ac.shef.wit.simmetrics.similaritymetrics.MongeElkan;

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
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.model.RDFSClass;

public class Att_Sim {
	static String owlfilePath;
	//static String owlfilePath = "C:/Users/user/workspace/exobrain/test";
	//static String owlfilePath = "DBpedia_AllIns";
	static JenaOWLModel owlModel;
	static String[] names;
	static double[][] distances;
	static MongoClient mongoClient;
	static DB db;
	static DBCollection table;
	// argument = class name
	public static void instCluster(String classname, int numofcluster) throws Exception
	{
		//hypernym("��");
		File path = new File(".");
		String path2 = path.getCanonicalPath();
		String path3 = path2.replaceAll("\\\\", "/");
		owlfilePath = path3+"/DBpedia_AllIns";
		System.out.println(owlfilePath);
		
		mongoClient = new MongoClient();
		db = mongoClient.getDB("LinkagePoint_KoreanDbpediaEdgesinandout");
		table = db.getCollection("KoreanDbpediaEdgesinandout");
		
		owlModel = loadExistSchema();
		
		int size = calculateClass(classname);
		mongoClient.close();
		
		
		RConnection c = new RConnection();
		c.assign("res", distances[0]);
		for (int i = 1; i < size; i++)
		{
			c.assign("tmp", distances[i]);
			c.eval("res <- rbind(res,tmp)");
		}
		  
		c.assign("names", names);
		
		REXP xp = c.eval("try(png(\"" + path3 + "/mycluster.png\"))");
		c.eval("hc <- hclust(dist(1-res), method=\"ward.D2\")");
		c.eval("clustnumber <- cutree(hc, k=" + numofcluster + ")");
		//c.eval("clustnumber <- cutree(hc, h=0.54)");
		c.parseAndEval("plot(hc, labels=names)");
		c.voidEval("rect.hclust(hc," + numofcluster + ")");
		c.voidEval("dev.off()");  
		   
		c.eval("tab1clustn <- data.frame(names, clustnumber)");
		c.eval("write.table(tab1clustn,  file=\"" + path3 + "/tab1clustn.csv\", row.names=FALSE)");

 	    c.close();
 	    createSubclass(owlModel, "tab1clustn.csv", classname, numofcluster);
	}
	private static void createSubclass(JenaOWLModel owlModel, String filename, String superclass, int numberofclass) throws IOException
	{
		OWLNamedClass super_class = owlModel.getOWLNamedClass(superclass);
		ArrayList<OWLNamedClass> class_set = new ArrayList<OWLNamedClass>();
		for(int i = 1; i <= numberofclass; i++)
			class_set.add(owlModel.createOWLNamedSubclass("세부"+superclass + i, super_class));
		
		File filedir = new File(filename);
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filedir),"UTF-8"));
		in.readLine();
		StringTokenizer st = null;
		String inst = null;
		while((inst = in.readLine()) != null)
		{
			inst = inst.replaceAll("\"", "");
			st = new StringTokenizer(inst, " ");
			
			String instancename = st.nextToken();
			String classnumber = st.nextToken();
			//System.out.println(instancename + " " + classnumber);
			RDFIndividual instance = owlModel.getRDFIndividual(instancename);
			instance.addRDFType(class_set.get(Integer.parseInt(classnumber)-1));
			instance.removeRDFType(super_class);
			
		}
		
		in.close();
		saveSchema(owlModel);
	}
	
	private static void hypernym(String str)
	{
		try{
			Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
		//	System.out.println("����̹� �˻� ����!");
		}catch(ClassNotFoundException e) {
			System.err.println("error = " + e);
		}

		Connection conn = null;   // DB ����
		Statement stmt = null;   // ���� ó���� ���� ����
		ResultSet rs = null;   // ���� ����� �ӽ� ����

		/*
		 * 
		 * SELECT column1, column2
		   INTO new_table_name [IN externaldatabase]
		   FROM old_tablename
		   WHERE Name in('Mark','Luke',etc)
		 */
		
		
		String url = "Jdbc:Odbc:KorLex";
		String id = "wwww";
		String pass = "wwww";
		String query = "SELECT * FROM StdDic";
		String query2 = "SELECT * FROM Synset";
		String query3 = "SELECT * FROM Tran";
	//	System.out.println(query);
	//	String idx = null;
		Integer StdDicidx = null;
		String StdDicsharp1 = null;
		Integer TransWordnetidx = null;
		Integer TransStdDicidx = null;
		Integer Synsetidx = null;
		String SynsetWord = null;
		String SynsetHypernym = null;
		String SynsetOffset = null;
		StringTokenizer st = null;
		String token = null;
				

		try {
			// ����, ���� �˻�, �������
			
	//		System.out.println("dd");
			conn = DriverManager.getConnection(url, id, pass);
	//		System.out.println("dd");
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query);

			// ��� ���
			while(rs.next()) {
				StdDicidx = rs.getInt(1);
				StdDicsharp1 = rs.getString(2);

		//		System.out.print(StdDicidx + "\t");
		//		System.out.println(StdDicsharp1);
				
				if(str.equals(StdDicsharp1))
				{
		//			StdDicidx = idx;
					break;
				}
			}
		//	System.out.println("dd");
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query3);

			
			while(rs.next()) {
				TransWordnetidx = rs.getInt(2);
				TransStdDicidx = rs.getInt(4);

		//		System.out.print(TransWordnetidx + "\t");
		//		System.out.println(TransStdDicidx);
				
				if(StdDicidx.equals(TransStdDicidx))
				{
		//			StdDicidx = idx;
					break;
				}
			}
			
			stmt = conn.createStatement();
			rs = stmt.executeQuery(query2);
			
			while(rs.next()) {
				Synsetidx = rs.getInt(1);
				SynsetWord = rs.getString(3);
				SynsetHypernym = rs.getString(5);

		//		System.out.print(TransWordnetidx + "\t");
		//		System.out.println(TransStdDicidx);
				
				if(TransWordnetidx.equals(Synsetidx))
				{
		//			StdDicidx = idx;
					break;
				}
			}
			
			
	//		System.out.println(Synsetidx);
			System.out.println(SynsetWord);
		//	System.out.println(SynsetHypernym);
			SynsetHypernym = SynsetHypernym.replace("_n_0000", "");
		//	System.out.println(SynsetHypernym);
			
			st = new StringTokenizer(SynsetHypernym, ",");
			
			while(st.hasMoreTokens())
			{
				token = st.nextToken();
		//		System.out.println(token);
		//		System.out.println(st.nextToken());
					
				stmt = conn.createStatement();
				rs = stmt.executeQuery(query2);
	
				while(rs.next()) {
					Synsetidx = rs.getInt(1);
					SynsetOffset = rs.getString(2);
					SynsetWord = rs.getString(3);
					SynsetHypernym = rs.getString(5);
	
					
			//		System.out.print(TransWordnetidx + "\t");
			//		System.out.println(TransStdDicidx);
					
					if(SynsetOffset.equals(token))
					{
			//			StdDicidx = idx;
						break;
					}
				}
				
				
				stmt = conn.createStatement();
				rs = stmt.executeQuery(query3);
	
				
				while(rs.next()) {
					TransWordnetidx = rs.getInt(2);
					TransStdDicidx = rs.getInt(4);
	
			//		System.out.print(TransWordnetidx + "\t");
			//		System.out.println(TransStdDicidx);
					
					if(TransWordnetidx.equals(Synsetidx))
					{
			//			StdDicidx = idx;
						break;
					}
				}
				
				stmt = conn.createStatement();
				rs = stmt.executeQuery(query);
	
				// ��� ���
				while(rs.next()) {
					StdDicidx = rs.getInt(1);
					StdDicsharp1 = rs.getString(2);
	
			//		System.out.print(StdDicidx + "\t");
			//		System.out.println(StdDicsharp1);
					
					if(StdDicidx.equals(TransStdDicidx))
					{
			//			StdDicidx = idx;
						break;
					}
				}
				
				
				//		System.out.println(Synsetidx);
				System.out.println(StdDicsharp1);
			}
			
			

			// ���� ����
			rs.close();     
			stmt.close();
			conn.close();
		}catch(SQLException e) {
			System.err.println("error sql = " + e);
		}
	}
	private static int calculateClass(String class_name) throws IOException
	{
		
		BasicDBObject query = new BasicDBObject("Class", class_name);
		BasicDBObject field = new BasicDBObject();
		field.put("_id", 0);
		field.put("Class", 0);
		
		BasicDBObject query2 = new BasicDBObject("Class", class_name);
		BasicDBObject field2 = new BasicDBObject();
		field2.put("_id", 0);
		field2.put("Class", 0);
		
		DBCursor cursor = table.find(query, field);
		cursor.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
		
		int sizeofarray = cursor.count();
		names = new String[sizeofarray];
		distances = new double[sizeofarray][sizeofarray];
		
		int first = 0;
		while(cursor.hasNext())
		{
			String temp = cursor.next().get("InstanceName").toString();
//			if(temp.contains("������"))
//				continue;
			names[first] = temp;
			
			
			DBCursor cursor2 = table.find(query2, field2);
			cursor2.addOption(com.mongodb.Bytes.QUERYOPTION_NOTIMEOUT);
			int second = 0;
			while(cursor2.hasNext())
			{
				String temp2 = cursor2.next().get("InstanceName").toString();
//				if(temp2.contains("������"))
//					continue;

				if(temp.equals(temp2))
				{
					distances[first][second] = 0.0;
					second++;
					continue;
				}
				Double result = 0.0;

				result = (get_TS(owlModel, temp, temp2) + 2*get_RS(temp, temp2, 0) + 2*get_AS(temp, temp2)) / 5;

				distances[first][second] = result;
				second++;
			}
			System.out.println(names[first] + " is calculated");
			first++;
			cursor2.close();
		}
		cursor.close();
		
		return sizeofarray;
	}
	
	private static JenaOWLModel loadExistSchema() throws OntologyLoadException {
		String uri = "file:///" + owlfilePath + ".owl";
		JenaOWLModel model = ProtegeOWL.createJenaOWLModelFromURI(uri);
		
		return model;
	}
	
	private static void saveSchema(JenaOWLModel model) {
    	Collection errors = new ArrayList();
    	model.save(new File(owlfilePath+"_output.owl").toURI(), FileUtils.langXMLAbbrev, errors);
    	System.out.println("File saved with " + errors.size() + " errors.");
	}
	
	private static double get_TS(JenaOWLModel owlModel, String inst1, String inst2){
		double ts=0;
		double sum = 0;
		double intersection = 0;
		double cm = 0;
		
		String temp=null;
		String temp2=null;
		
		int counter1=0;
		int counter2=0;
		try{
			
			RDFIndividual instance1=owlModel.getRDFIndividual(inst1);
			RDFIndividual instance2=owlModel.getRDFIndividual(inst2);
			
			RDFSClass class1 = instance1.getRDFType();
			RDFSClass class2 = instance2.getRDFType();
	
			HashSet<String> set = new HashSet<String>();
			
			temp = class1.getBrowserText();
			temp2 = class2.getBrowserText();
			
			if(instance1.getBrowserText().equals(instance2.getBrowserText()))
			{
				ts = 1;
				
				return ts;
			}
				
			for(;;)
			{
				//System.out.println(temp);
				set.add(temp);
				counter1++;
				
				if(temp.equals("Thing"))
					break;
				
				class1 = (RDFSClass) class1.getNamedSuperclasses().iterator().next();
				temp = class1.getBrowserText();
			}
			
			for(;;)
			{
				//System.out.println(temp2);
				set.add(temp2);
				counter2++;
				
				if(temp2.equals("Thing"))
					break;
				
				class2 = (RDFSClass) class2.getNamedSuperclasses().iterator().next();
				temp2 = class2.getBrowserText();
			}
			
			/*
			Iterator it = set.iterator();
			while(it.hasNext()){
				Object o1 = it.next();
				System.out.println(o1);
			}
			*/
			
			sum = counter1+counter2;
			intersection = sum-set.size();
			cm = intersection/set.size();
			ts = cm/2;
	    }
		catch (Exception e)
	    {
	       //System.out.println("Instance load failed");
	       return 0;
	    }		
		//System.out.println(ts+" "+counter1+" "+counter2+" "+set.size()+" "+sum+" "+intersection+" "+cm);
		return ts;
	}
	
	private static void checkProp(String ins1) throws IOException
	{
		
		BasicDBObject query = new BasicDBObject("InstanceName", ins1);
		BasicDBObject field = new BasicDBObject();
		field.put("_id", 0);
		field.put("InstanceName", 0);
		field.put("Class", 0);
		
		DBCursor cursor = table.find(query, field);
		
		ArrayList<String> dbpedia_att = new ArrayList<String>();
		ArrayList<ArrayList<String>> dbpedia_value = new ArrayList<ArrayList<String>>();
		
		String temp = cursor.next().toString();
		//System.out.println(temp);
			
		// RS testing
		JSONObject jsonobj = (JSONObject) JSONValue.parse(temp);
		Iterator iter = jsonobj.keySet().iterator();
		while(iter.hasNext()){
		   String key = (String) iter.next();
		   Object value = jsonobj.get(key);
		   if(value.equals(""))
			   continue;
		   
		   dbpedia_att.add(key);
		   
		   JSONObject jsonobj2 = (JSONObject) JSONValue.parse(value.toString());
		   Iterator iter2 = jsonobj2.keySet().iterator();
		   ArrayList<String> multi_value = new ArrayList<String>();

		   int flag = 0;
		   while(iter2.hasNext())
		   {

			   String key2 = (String) iter2.next();
			   Object value2 = jsonobj2.get(key2);
			   String temp_value = value2.toString();
			   boolean income = key.contains("_income");
			   if(temp_value.contains("ko.dbpedia.org/resource/") || income)	// relation check
			   {
				   flag++;
				   if(!income)
				   {
					   int index = temp_value.indexOf("ko.dbpedia.org/resource/");	// remove URI
					   String real_value = temp_value.substring(index+24);
					   multi_value.add(real_value);
				   }
				   else // _income case
					   multi_value.add(temp_value);
				   
//				   if(real_value.equals(ins1)) // self loop escape
//					   continue;

			   }
			   else if(flag != 0)
			   {
				   break;
			   }
			   else
			   {
				   //System.out.println("else  " + key);
				   dbpedia_att.remove(key);
				   break;
			   }
			   
		   }
		   if(multi_value.size() != 0)
		   {
			   System.out.println("size = " + dbpedia_value.size());
			   dbpedia_value.add(multi_value);
		   }
		}
		
		for(int i=0;i<dbpedia_att.size();i++)
		{
			System.out.println(dbpedia_att.get(i) + " size = " + dbpedia_value.get(i).size());
			for(int j=0;j<dbpedia_value.get(i).size();j++)
				System.out.print(dbpedia_value.get(i).get(j) + " ");
			System.out.println();
		}
		
		/*
		// AS testing
		JSONObject jsonobj = (JSONObject) JSONValue.parse(temp);
		Iterator iter = jsonobj.keySet().iterator();
		while(iter.hasNext()){
		   String key = (String) iter.next();
		   Object value = jsonobj.get(key);
		   if(key.contains("_income")) // remove relation
			   continue;
		   if(value.equals(""))
			   continue;
		   
		   dbpedia_att.add(key);
		  
		   JSONObject jsonobj2 = (JSONObject) JSONValue.parse(value.toString());
		   Iterator iter2 = jsonobj2.keySet().iterator();
		   ArrayList<String> multi_value = new ArrayList<String>();
		   while(iter2.hasNext())
		   {
			   String key2 = (String) iter2.next();
			   Object value2 = jsonobj2.get(key2);
			   String temp_value = value2.toString();
			   
			   if(temp_value.contains("ko.dbpedia.org")) // remove relation
			   {
				   dbpedia_att.remove(key);
				   break;
			   }
			   multi_value.add(temp_value);
			   System.out.println(key + " // " + temp_value);
		   }
		   if(multi_value.size() != 0)
			   dbpedia_value.add(multi_value);  
		}
		for(int i=0;i<dbpedia_att.size();i++)
		{
			System.out.println(dbpedia_att.get(i) + " size = " + dbpedia_value.get(i).size());
			for(int j=0;j<dbpedia_value.get(i).size();j++)
				System.out.print(dbpedia_value.get(i).get(j));
			System.out.println();
		}
		*/
		
		/*
		DBCursor cursor = table.find(query, field);

		if(!cursor.hasNext())
		{
			System.out.println("No instance");
			return;
		}
		while(cursor.hasNext())
		{
			String temp = cursor.next().toString();
			System.out.println(temp);
			if(temp.equals("{ }"))
				System.out.println("NULL value");
			//System.out.println(temp);
			
			JSONObject jsonobj = (JSONObject) JSONValue.parse(temp);
			Iterator iter = jsonobj.keySet().iterator();
			while(iter.hasNext())
			{
			   String key = (String) iter.next();
	//		   if(key.contains("_income"))
	//			   continue;
			   Object value = jsonobj.get(key);
			   if(value.equals(""))
				   continue;
			   System.out.println(key);
			   JSONObject jsonobj2 = (JSONObject) JSONValue.parse(value.toString());
			   Iterator iter2 = jsonobj2.keySet().iterator();
			   while(iter2.hasNext())
			   {
				   String key2 = (String) iter2.next();
				   Object value2 = jsonobj2.get(key2);
				   System.out.println(key2 + " " + value2);
			   } 
			}
		}
		*/
	}
	
	private static double get_RS(String ins1, String ins2, int depth) throws IOException
	{
		if(ins1.equals(ins2))
		{
			//System.out.println("They are same");
			return 1;
		}

		BasicDBObject query = new BasicDBObject("InstanceName", ins1);
		BasicDBObject field = new BasicDBObject();
		field.put("_id", 0);
		field.put("InstanceName", 0);
		field.put("Class", 0);
		
		BasicDBObject query2 = new BasicDBObject("InstanceName", ins2);
		BasicDBObject field2 = new BasicDBObject();
		field2.put("_id", 0);
		field2.put("InstanceName", 0);
		field2.put("Class", 0);
		
		DBCursor cursor = table.find(query, field);
		ArrayList<String> dbpedia_att = new ArrayList<String>();
		ArrayList<ArrayList<String>> dbpedia_value = new ArrayList<ArrayList<String>>();
		
		if(!cursor.hasNext())
		{
			cursor.close();
			//System.out.println("No instance 1");
			return 0;
		}
		String temp = cursor.next().toString();
		//System.out.println(temp);
		if(temp.equals("{ }")) // No property
		{
			cursor.close();
			return 0;
		}
		JSONObject jsonobj = (JSONObject) JSONValue.parse(temp);
		Iterator iter = jsonobj.keySet().iterator();
		while(iter.hasNext()){
		   String key = (String) iter.next();
		   Object value = jsonobj.get(key);
		   if(value.equals(""))
			   continue;
		   
		   dbpedia_att.add(key);
		   
		   JSONObject jsonobj2 = (JSONObject) JSONValue.parse(value.toString());
		   Iterator iter2 = jsonobj2.keySet().iterator();
		   ArrayList<String> multi_value = new ArrayList<String>();
		   int flag = 0;
		   while(iter2.hasNext())
		   {
			   String key2 = (String) iter2.next();
			   Object value2 = jsonobj2.get(key2);
			   String temp_value = value2.toString();
			   boolean income = key.contains("_income");
			   if(temp_value.contains("ko.dbpedia.org/resource/") || income)	// relation check
			   {
				   flag++;
				   if(!income)
				   {
					   int index = temp_value.indexOf("ko.dbpedia.org/resource/");	// remove URI
					   String real_value = temp_value.substring(index+24);
					   //if(real_value.equals(ins1)) // self loop escape
					   //   continue;	   
					   real_value = real_value.replaceAll(" ", " ");
					   multi_value.add(real_value);
					   
				   }
				   else // _income case
				   {
					   temp_value = temp_value.replaceAll(" ", " ");
					   multi_value.add(temp_value);
				   }
			  
			   }
			   else if(flag != 0)
			   {
				   break;
			   }
			   else
			   {
				   dbpedia_att.remove(key);
				   break;
			   }

		   }
		   if(multi_value.size() != 0)
			   dbpedia_value.add(multi_value);  
		   
		}
		
		DBCursor cursor2 = table.find(query2, field2);
		ArrayList<String> dbpedia2_att = new ArrayList<String>();
		ArrayList<ArrayList<String>> dbpedia2_value = new ArrayList<ArrayList<String>>();
		
		if(!cursor2.hasNext())
		{
			cursor.close();
			cursor2.close();
			//System.out.println("No instance 2");
			return 0;
		}
		String temp2 = cursor2.next().toString();
		//System.out.println(temp2);
		if(temp2.equals("{ }")) // No property
		{
			cursor.close();
			cursor2.close();
			return 0;
		}
		
		JSONObject jsonobj2 = (JSONObject) JSONValue.parse(temp2);
		Iterator iter2 = jsonobj2.keySet().iterator();
		while(iter2.hasNext()){
		   String key = (String) iter2.next();
		   Object value = jsonobj2.get(key);
		   if(value.equals(""))
			   continue;
		   
		   dbpedia2_att.add(key);
		   
		   JSONObject jsonobj3 = (JSONObject) JSONValue.parse(value.toString());
		   Iterator iter3 = jsonobj3.keySet().iterator();
		   ArrayList<String> multi_value = new ArrayList<String>();
		   int flag = 0;
		   
		   while(iter3.hasNext())
		   {
			   String key2 = (String) iter3.next();
			   Object value2 = jsonobj3.get(key2);
			   String temp_value = value2.toString();
			   boolean income = key.contains("_income");
			   if(temp_value.contains("ko.dbpedia.org/resource/") || income)	// relation check
			   {
				   flag++;
				   if(!income)
				   {
					   int index = temp_value.indexOf("ko.dbpedia.org/resource/");	// remove URI
					   String real_value = temp_value.substring(index+24);
					   //if(real_value.equals(ins1)) // self loop escape
					   //   continue;  
					   real_value = real_value.replaceAll(" ", " ");
					   multi_value.add(real_value);
				   }
				   else
				   {
					   temp_value = temp_value.replaceAll(" ", " ");
					   multi_value.add(temp_value);
				   }
			   }
			   else if(flag != 0)
			   {
				   break;
			   }
			   else
			   {
				   dbpedia2_att.remove(key);
				   break;
			   }

		   }
		   if(multi_value.size() != 0)
			   dbpedia2_value.add(multi_value);  
		   
		}
		
		cursor.close();
		cursor2.close();
		
		if( (dbpedia_value.size() == 0) || (dbpedia2_value.size() == 0))	// no relation
		{
			//System.out.println("No relation at all  --" + ins1 + " " + ins2);
			return 0;
		}

		double temp_result = 0;
		int count = 0;
		HashSet<String> set = new HashSet<String>();
		for(int i=0;i<dbpedia_att.size();i++)
		{
			for(int j=0;j<dbpedia2_att.size();j++)
			{
				if(dbpedia_att.get(i).equals(dbpedia2_att.get(j))) // common relation case
				{
					//System.out.println(dbpedia_att.get(i));
					
					for(int k=0;k<dbpedia_value.get(i).size();k++)
					{
						set.add(dbpedia_value.get(i).get(k));
					}
					
					for(int l=0;l<dbpedia2_value.get(j).size();l++)
					{			
						set.add(dbpedia2_value.get(j).get(l));						
					}
					
					int counter = dbpedia_value.get(i).size() + dbpedia2_value.get(j).size();
					int intersection = counter - set.size();
					temp_result = temp_result + (intersection / set.size());
					//System.out.println(dbpedia_att.get(i) + " " + (intersection / set.size()));
					count++;
					
					set.clear();
					break;
				}
			}
		}
		
		if(count == 0) // no common relation
		{
			cursor.close();
			cursor2.close();
			//System.out.println("No common relation");
			return 0;
		}
		cursor.close();
		cursor2.close();
		return (temp_result / count);
	}
	
	private static double get_AS(String ins1, String ins2) throws IOException
	{
		
		BasicDBObject query = new BasicDBObject("InstanceName", ins1);
		BasicDBObject field = new BasicDBObject();
		field.put("_id", 0);
		field.put("InstanceName", 0);
		field.put("Class", 0);
		
		BasicDBObject query2 = new BasicDBObject("InstanceName", ins2);
		BasicDBObject field2 = new BasicDBObject();
		field2.put("_id", 0);
		field2.put("InstanceName", 0);
		field2.put("Class", 0);
		
		DBCursor cursor = table.find(query, field);
		ArrayList<String> dbpedia_att = new ArrayList<String>();
		ArrayList<ArrayList<String>> dbpedia_value = new ArrayList<ArrayList<String>>();
		
		String temp = cursor.next().toString();
		//System.out.println(temp);
		if(temp.equals("{ }")) // No property
		{
			cursor.close();
			return 0;
		}
			
		JSONObject jsonobj = (JSONObject) JSONValue.parse(temp);
		Iterator iter = jsonobj.keySet().iterator();
		while(iter.hasNext()){
		   String key = (String) iter.next();
		   Object value = jsonobj.get(key);
		   if(key.contains("_income")) // remove relation
			   continue;
		   if(value.equals(""))
			   continue;
		   
		   dbpedia_att.add(key);
		   
		   JSONObject jsonobj2 = (JSONObject) JSONValue.parse(value.toString());
		   Iterator iter2 = jsonobj2.keySet().iterator();
		   ArrayList<String> multi_value = new ArrayList<String>();
		   while(iter2.hasNext())
		   {
			   String key2 = (String) iter2.next();
			   Object value2 = jsonobj2.get(key2);
			   String temp_value = value2.toString();
			   
			   if(temp_value.contains("ko.dbpedia.org")) // remove relation
			   {
				   dbpedia_att.remove(key);
				   break;
			   }
			   temp_value = temp_value.replaceAll(" ", " ");
			   multi_value.add(temp_value);
			   //System.out.println(value2);
		   }
		   if(multi_value.size() != 0)
			   dbpedia_value.add(multi_value);  
		}
		
		
		DBCursor cursor2 = table.find(query2, field2);
		ArrayList<String> dbpedia2_att = new ArrayList<String>();
		ArrayList<ArrayList<String>> dbpedia2_value = new ArrayList<ArrayList<String>>();
		
		String temp2 = cursor2.next().toString();
		//System.out.println(temp2);
		if(temp2.equals("{ }")) // No property
		{
			cursor.close();
			cursor2.close();
			return 0;
		}
		JSONObject jsonobj3 = (JSONObject) JSONValue.parse(temp2);
		Iterator iter3 = jsonobj3.keySet().iterator();
		while(iter3.hasNext()){
		   String key = (String) iter3.next();
		   if(key.contains("_income"))
			   continue;
		   Object value = jsonobj3.get(key);
		   if(value.equals(""))
			   continue;
		   
		   dbpedia2_att.add(key);
		   
		   JSONObject jsonobj4 = (JSONObject) JSONValue.parse(value.toString());
		   Iterator iter4 = jsonobj4.keySet().iterator();
		   ArrayList<String> multi_value = new ArrayList<String>();
		   while(iter4.hasNext())
		   {
			   String key2 = (String) iter4.next();
			   Object value2 = jsonobj4.get(key2);
			   String temp_value = value2.toString();
			   
			   if(temp_value.contains("ko.dbpedia.org")) // remove relation
			   {
				   dbpedia2_att.remove(key);
				   break;
			   }
			   temp_value = temp_value.replaceAll(" ", " ");
			   multi_value.add(temp_value);
			   //System.out.println(value2);
		   }
		   if(multi_value.size() != 0)
			   dbpedia2_value.add(multi_value);
		   //System.out.println(key + " : " + value); 
		   
		}
		
		cursor.close();
		cursor2.close();
		
		double temp_result = 0;
		int count = 0;
		for(int i=0;i<dbpedia_att.size();i++)
		{
			for(int j=0;j<dbpedia2_att.size();j++)
			{
				//System.out.println(dbpedia_att.get(i) + " " + dbpedia2_att.get(j));
				if(dbpedia_att.get(i).equals(dbpedia2_att.get(j)))
				{
					double Biggest_value = 0;
					for(int k=0;k<dbpedia_value.get(i).size();k++)
					{
						for(int l=0;l<dbpedia2_value.get(j).size();l++)
						{
							double temp_value = CalculateSimilarity(dbpedia_value.get(i).get(k), dbpedia2_value.get(j).get(l));
							//System.out.println(dbpedia_value.get(i).get(k) + " with " + dbpedia2_value.get(j).get(l) + " = " + temp_value);
							if( Biggest_value < temp_value)
								Biggest_value = temp_value;
						}
						
					}
					//System.out.println(dbpedia_att.get(i) + " : " + Biggest_value);
					temp_result = temp_result + Biggest_value;
					count++;
					break;
				}
			}
		}
		if(count == 0) // No common attribute
		{
			cursor.close();
			cursor2.close();
			//System.out.println("NO common attribute");
			return 0;
		}
		cursor.close();
		cursor2.close();
		double result = temp_result / count;
		//System.out.println(result + " count = " + count);
		return result;
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


/*
//hypernym("��");
File path = new File(".");
String path2 = path.getCanonicalPath();
String path3 = path2.replaceAll("\\\\", "/");
owlfilePath = path3+"/DBpedia_AllIns";
System.out.println(owlfilePath);

mongoClient = new MongoClient();
db = mongoClient.getDB("LinkagePoint_KoreanDbpediaEdgesinandout");
table = db.getCollection("KoreanDbpediaEdgesinandout");

owlModel = loadExistSchema();

int size = calculateClass(args[0]);
mongoClient.close();


RConnection c = new RConnection();
c.assign("res", distances[0]);
for (int i = 1; i < size; i++)
{
	c.assign("tmp", distances[i]);
	c.eval("res <- rbind(res,tmp)");
}
  
c.assign("names", names);

REXP xp = c.eval("try(png(\"" + path3 + "/mycluster.png\"))");
c.eval("hc <- hclust(dist(1-res), method=\"ward.D2\")");
c.eval("clustnumber <- cutree(hc, k=" + Integer.parseInt(args[1]) + ")");
//c.eval("clustnumber <- cutree(hc, h=0.54)");
c.parseAndEval("plot(hc, labels=names)");
c.voidEval("rect.hclust(hc," + Integer.parseInt(args[1]) + ")");
c.voidEval("dev.off()");  
   

c.eval("tab1clustn <- data.frame(names, clustnumber)");
c.eval("write.table(tab1clustn,  file=\"" + path3 + "/tab1clustn.csv\", row.names=FALSE)");

 c.close();
 */
 //createSubclass(owlModel, "tab1clustn.csv", args[0], Integer.parseInt(args[1]));
 
//owlModel = loadExistSchema();
//System.out.println(get_RS("����õ","����õ",0));
//System.out.println(get_AS("������_��","������_��"));
//checkProp("������");
/*
// Initiate cluster
HierarchicalClusterer clusterer = new HierarchicalClusterer();
clusterer.setOptions(new String[] {"-L", "Single"});
clusterer.setDebug(true);
clusterer.setNumClusters(1);
clusterer.setDistanceFunction(new EuclideanDistance());
clusterer.setDistanceIsBranchLength(true);

// Build dataset
List<String> test = null;
attributes = new ArrayList<Attribute>();
attributes.add(new Attribute("A"));
attributes.add(new Attribute("B",test));
//attributes.add(new Attribute("C"));
data = new Instances("Weka test", attributes, 0);


// Add data
//data.add(new DenseInstance(1.0, new double[] { 1.0, 0.0, 0.1 }));
//data.add(new DenseInstance(inst));
//data.add(new DenseInstance(1.0, new double[] { 0.5, 0.0, 0.2 }));
//data.add(new DenseInstance(1.0, new double[] { 0.0, 1.0, 0.3 }));
//data.add(new DenseInstance(1.0, new double[] { 0.0, 1.0, 0.4 }));
// 0.1 0.3 0.7
// Cluster network
clusterer.buildClusterer(data);

// Print normal
clusterer.setPrintNewick(false);
System.out.println(clusterer.graph());
// Print Newick
clusterer.setPrintNewick(true);
System.out.println(clusterer.graph());



// Let's try to show this clustered data!
JFrame mainFrame = new JFrame("Weka Test");
mainFrame.setSize(600, 400);
mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
Container content = mainFrame.getContentPane();
content.setLayout(new GridLayout(1, 1));

HierarchyVisualizer visualizer = new HierarchyVisualizer(clusterer.graph());
content.add(visualizer);

mainFrame.setVisible(true);
 */