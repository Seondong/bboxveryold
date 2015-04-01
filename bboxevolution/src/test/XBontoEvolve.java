package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


import java.util.Iterator;
import java.util.List;

import org.snu.ids.ha.ma.MExpression;
import org.snu.ids.ha.ma.Morpheme;
import org.snu.ids.ha.ma.MorphemeAnalyzer;
import org.snu.ids.ha.ma.Sentence;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;


public class XBontoEvolve {
	
	static String instanceName = "a";
	static int a = 1;
	static BasicDBObject document = null;
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Model model3 = ModelFactory.createDefaultModel();
		
		model3.read("file:xb-release-2015-01-13.ttl","http://xb.saltlux.com/release/","TURTLE");
		model3.setNsPrefix("common", "<http://xb.saltlux.com/schema/common/");
		model3.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
		model3.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		model3.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		model3.setNsPrefix("schema", "http://xb.saltlux.com/schema/");
		model3.setNsPrefix("xbc", "http://xb.saltlux.com/schema/class/");
		model3.setNsPrefix("xbd", "http://xb.saltlux.com/schema/datatype/");
		model3.setNsPrefix("xbp", "http://xb.saltlux.com/schema/property/");
		model3.setNsPrefix("xbv", "http://xb.saltlux.com/schema/vocab/");
		model3.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		
		
		File dir = new File("C:\\Users\\user\\workspace\\exobrain\\xbontoclass_v3.txt");
		BufferedReader br =  new BufferedReader(new InputStreamReader(new FileInputStream(dir), "UTF8"));
		String temp = null;
		for(;;)
		{
			temp = br.readLine();
			if(temp != null)
				ExtractInstances(model3, temp);
			else
				break;
		}
		
		//ExtractInstances(model3, "<http://xb.saltlux.com/schema/class/method_05333823>");
		// http://xb.saltlux.com/schema/class/knowledge_domain_05647070
		//   http://xb.saltlux.com/schema/class/phenomenon_00029881
		model3.close();
		
		
	}
	private static void ExtractInstances( Model model , String nameofclass ) throws Exception
	{
		MongoClient mongoClient = new MongoClient();
		
		DB db = mongoClient.getDB("LinkagePoint_xbonto_GGOGGO");
		DBCollection table = db.getCollection("xbonto_GGOGGO");
		
		table.ensureIndex(new BasicDBObject("Class",1).append("unique", true).append("background", true));
		
		document = new BasicDBObject();
		
		
		
		String sparqlQueryString = "SELECT ?x WHERE {"
			    +"?x <http://www.w3.org/2000/01/rdf-schema#subClassOf>+ " + nameofclass +" ."
			+"}"; // max-depth level class
		
		    Query query = QueryFactory.create(sparqlQueryString);
		    QueryExecution qexec = QueryExecutionFactory.create(query, model);
		    ResultSet results = qexec.execSelect();

		    // Remove Class URI
		    int start = nameofclass.indexOf("http://xb.saltlux.com/schema/class/");
		    
		    String noURIclass = nameofclass.replace("<", "").replace(">", "").substring(start+34);
		    System.out.println("class name " + noURIclass);
		    
			
		    String sparqlQueryString2 = "SELECT ?x WHERE {"
				    +"?x <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>+ " + nameofclass +" ."
				+"}"; // max-depth level class
			Query query2 = QueryFactory.create(sparqlQueryString2);
			QueryExecution qexec2 = QueryExecutionFactory.create(query2, model);
			ResultSet results2 = qexec2.execSelect();
			
			while(results2.hasNext())
			{
				QuerySolution row2 = results2.next();
				Resource r1 = row2.getResource("?x");
				StmtIterator a1 = r1.listProperties();

				while(a1.hasNext())
		          {
		        	  Statement temp = a1.next();
		        	  String subject = temp.getSubject().toString();
		        	  String predicate = temp.getPredicate().toString();
		        	  String object = temp.getObject().toString();
		        	  
		        	  if( !predicate.contains("ns#type"))
		        	  {
		        		  if( predicate.contains("startsOn") || predicate.contains("happenedOn") || predicate.contains("startedOn") || predicate.contains("endedOn") || predicate.contains("bornOn") || predicate.contains("diedOn"))
		        		  {
		        			  String new_obj = normalizeDay(removeURI(predicate), removeURI(object));
		        			  saveInstancesToMongoDB(removeURI(subject), removeURI(predicate), new_obj, noURIclass, table);
		        		  }
		        		  else
		        		  {
		        			  String new_object = analyzeWords(removeURI(object));
		        			  saveInstancesToMongoDB(removeURI(subject), removeURI(predicate), new_object, noURIclass, table);
		        		  }
		        		  

		        		 // System.out.println(removeURI(subject) + "  " + removeURI(predicate) + "  " + removeURI(object));
		        		  
		        		  
		        	  }
		        	  
		          }   
			}
		   

		    
		    if(results.hasNext())
		    {
		    	//System.out.println("Class");
			    while (results.hasNext()) {
			        QuerySolution row = results.next();
    
			        //System.out.println(row.get("?x").toString());
			        Property prop = model.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
			        RDFNode class_name = row.get("?x");
			       
			        ResIterator iter = model.listResourcesWithProperty(prop, class_name);
			        
			        if(iter.hasNext())
			        {
			        	//System.out.println("Instance");
				        while (iter.hasNext()) 
				        {
				          Resource r = iter.nextResource();
				          //System.out.println(r.toString());
				          StmtIterator a = r.listProperties();
				          
				          
				          while(a.hasNext())
				          {
				        	  Statement temp = a.next();
				        	  String subject = temp.getSubject().toString();
				        	  String predicate = temp.getPredicate().toString();
				        	  String object = temp.getObject().toString();
				        	  
				        	  if( !predicate.contains("ns#type"))
				        	  {
				        		  if( predicate.contains("startsOn") || predicate.contains("happenedOn") || predicate.contains("startedOn") || predicate.contains("endedOn") || predicate.contains("bornOn") || predicate.contains("diedOn"))
				        		  {
				        			  String new_obj = normalizeDay(removeURI(predicate), removeURI(object));
				        			  saveInstancesToMongoDB(removeURI(subject), removeURI(predicate), new_obj, noURIclass, table);
				        		  }
				        		  else
				        		  {
				        			  String new_object = analyzeWords(removeURI(object));
				        			  saveInstancesToMongoDB(removeURI(subject), removeURI(predicate), new_object, noURIclass, table);
				        		  }
				        		 // System.out.println(removeURI(subject) + "  " + removeURI(predicate) + "  " + removeURI(object));
				        		  
				        		  
				        	  }
				        	  
				          }   
				        }      
			        } 

			    }
		    }

		    
		   	// ��ü ���̺� �� ���
			//DBCursor cursorDoc = table.find();
			//while (cursorDoc.hasNext()) {
			//	System.out.println(cursorDoc.next());
			//}

			//System.out.println(table.count());  // ���̺��� ��ü instance ���� ���� �κ�
	}
	private static void saveInstancesToMongoDB(String subject, String predicate, String object , String noURIclass, DBCollection table) throws IOException // dirPath�� �ִ� �ν��Ͻ��� �ϳ��� �ҷ��ͼ� property : value ���� ����DB table�� �ִ� �޼ҵ�
	{

		if( !instanceName.equals(subject) ) // ���� ������ true
		{
			if(a!=1)
			{
				table.insert(document);
				document = new BasicDBObject();
			}
			else
			{
				a++;
			}
			
			instanceName = subject;
			document.put("InstanceName", instanceName);
			document.put("Class", noURIclass);
			document.put(predicate, object.toLowerCase());
		}
		else // ���� ���
		{
    		document.put(predicate, object.toLowerCase());
		}

		//table.insert(document);
	}
	
	private static String removeURI( String name )
	{ // for all cases
		
		String uri1 = "http://xb.saltlux.com/resource/kowiki/";
		String uri2 = "http://www.w3.org/2001/XMLSchema";
		String uri3 = "http://xb.saltlux.com/schema/property/";
		String uri4 = "http://www.w3.org/2000/01/rdf-schema#";
		String temp = null;
		if(name.contains("^^"))
		{
			int end = name.indexOf("^^");
			temp = name.substring(0,end);
		}
		else if(name.contains(uri1))
		{
			int start = name.indexOf(uri1);
			temp = name.substring(start+38);
		}
		else if(name.contains(uri2))
		{
			int end = name.indexOf("^^");
			temp = name.substring(0,end);
		}
		else if(name.contains(uri3))
		{
			int start = name.indexOf(uri3);
			temp = name.substring(start+38);
		}
		else if(name.contains(uri4))
		{
			int start = name.indexOf(uri4);
			temp = name.substring(start+37);
		}
		else
		{
			temp = name;
		}
		return temp;
	}
	
	private static String normalizeDay(String property, String day)
	{
		String result = null;
		
		if(property.equals("startsOn") || property.equals("bornOn") || property.equals("diedOn"))
		{
			day = day.replace("-","");
			day = day.replace("년", "");
			result = day.concat("0000");
		}
		else if(property.equals("startedOn") || property.equals("endedOn") || property.equals("happenedOn"))
		{
			result = day.replace("-","");
		}

		
		return result;
	}
	
	private static String analyzeWords(String object)
	{
		String temp = object;
		Morpheme str = null;
		String results = "";
		
		try {
			MorphemeAnalyzer ma = new MorphemeAnalyzer();
	//		ma.createLogger(null);
	//		Timer timer = new Timer();
	//		timer.start();
			List<MExpression> ret = ma.analyze(temp);
	//		timer.stop();
	//		timer.printMsg("Time");

			ret = ma.postProcess(ret);

			ret = ma.leaveJustBest(ret);

			List<Sentence> stl = ma.divideToSentences(ret);
			for( int i = 0; i < stl.size(); i++ ) {
				Sentence st = stl.get(i);
		//		System.out.println("=============================================  " + st.getSentence());
				for( int j = 0; j < st.size(); j++ ) {
		//			System.out.println(st.get(j));
					Iterator<Morpheme> it = st.get(j).iterator();
					for(;it.hasNext();)
					{
						str = it.next(); // getTag�� NNG �̷��� ��ȯ���ִ� �޼ҵ�, getSmplStr()�� '����/NNG'�̷������� ��ȯ����, getString�� '����' �̷������� ��ȯ����
						if(str.getTag()=="NNG" || str.getTag()=="NNP" || str.getTag()=="NNB" || str.getTag()=="NR" || str.getTag()=="NP" || str.getTag()=="VV" || str.getTag()=="SN")	// ü��, ����, ���ڸ� �̾Ƽ� �̾��ִ� �κ�
						{
		//					System.out.println(str.getString());
							results = results.concat(str.getString()+" ");
						}
					}
					//results = results.concat(" ");
				}
			}

	//		ma.closeLogger();
		} catch (Exception e) {
	//		e.printStackTrace();
		}
		
		return results;
	}
}
