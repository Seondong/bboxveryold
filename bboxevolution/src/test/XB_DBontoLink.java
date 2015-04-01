package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

public class XB_DBontoLink {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		
		/*
		File filedir = new File("xb_class_noURI.txt");
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filedir),"UTF-8"));
		String dbclass_name = null;
		
		while( (dbclass_name = in.readLine()) != null)
			getNumberofInstance(dbclass_name);
		*/
		
		MongoClient mongoClient = new MongoClient();
		
		// XB ontology
		DB db = mongoClient.getDB("LinkagePoint_xbonto_GGOGGO");
		DBCollection table = db.getCollection("xbonto_GGOGGO");
		
		// Korean DBpedia ontology
		DB db2 = mongoClient.getDB("LinkagePoint_KoreanDbpedia_GGOGGO_Whole");
		DBCollection table2 = db2.getCollection("KoreanDbpedia_GGOGGO_Whole");
		
		FileWriter fw = new FileWriter("XB_DB_result72_GGOGGO_TOP3.txt", false);
		BufferedWriter bw = new BufferedWriter(fw);
		
		File file = new File("xb_class_noURI.txt");
		BufferedReader in2 = new BufferedReader(new InputStreamReader(new FileInputStream(file),"UTF-8"));
		String xbclass_name;
		in2.readLine();
		while( (xbclass_name = in2.readLine()) != null)
		{
			
		File file2 = new File("db_class_noURI.txt");
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file2),"UTF-8"));
		String dbclass_name;
		
		String class_name[] = new String[72];
		float similarity[] = new float[72];
		int class_count = 0;
		
		while( (dbclass_name = in.readLine()) != null)
		{

		class_name[class_count] = dbclass_name; // for Top 3 class name
		
		BasicDBObject query2 = new BasicDBObject("Class", dbclass_name);
		//BasicDBObject query2 = new BasicDBObject("Class", "���_����");
		BasicDBObject field2 = new BasicDBObject();
		field2.put("_id", 0);
		field2.put("InstanceName", 0);
		field2.put("Class", 0);
		
		BasicDBObject query = new BasicDBObject("Class", xbclass_name);
		//BasicDBObject query = new BasicDBObject("Class", "speech_act_06719083");
		BasicDBObject field = new BasicDBObject();
		field.put("_id", 0);
		field.put("InstanceName", 0);
		field.put("Class", 0);

		DBCursor cursor = table.find(query, field);
		
		float average_similarity = 0;
		int num = 0; // the number of xb instances	

		try{

			// XB ontology
			while(cursor.hasNext() && num < 500){	
				
				num++;
				String temp = cursor.next().toString();
				//System.out.println(temp);
				
				ArrayList<String> xb = new ArrayList<String>();
				if(temp.equals("{ }"))	// no property
					continue;
				
				JSONObject jsonobj = (JSONObject) JSONValue.parse(temp);
				Iterator iter = jsonobj.keySet().iterator();
				
				while(iter.hasNext())
				{
				   String key = (String) iter.next();
				   Object value = jsonobj.get(key);
				   if(value.equals(""))
					   continue;
				   String real_value = value.toString();
				   int space = real_value.lastIndexOf(" ");
				   //System.out.println(space);
				   //System.out.println(real_value);
				   if(space < 0)
				   {
					   xb.add(real_value);
					   continue;
				   }
				   if(real_value.substring(space).equals(" "))
				   {
				   		xb.add(real_value.substring(0,space));
				   }
				   else
				   {
					   xb.add(value.toString());
				   }
				}
				

				
				DBCursor cursor2 = table2.find(query2, field2);
				float result = 0; // the largest similarity of two instances
				int num_dbpedia = 0;
				
				// Korean DBpedia
				while(cursor2.hasNext() && num_dbpedia < 500)
				{
					num_dbpedia++;
					ArrayList<String> dbpedia = new ArrayList<String>();
					String temp2 = cursor2.next().toString();
					//System.out.println(temp2);
					if(temp2.equals("{ }"))	// no property
						continue;
					
					JSONObject jsonobj2 = (JSONObject) JSONValue.parse(temp2);
					Iterator iter2 = jsonobj2.keySet().iterator();
					while(iter2.hasNext())
					{
					   String key = (String) iter2.next();
					   Object value = jsonobj2.get(key);
					   if(value.equals(""))
						   continue;
					   String real_value = value.toString();
					   if(real_value.contains("ko.dbpedia.org/resource/"))
					   {
						   int index = real_value.indexOf("ko.dbpedia.org/resource/");	// remove URI
						   real_value = real_value.substring(index+24);  
					   }
					   
					   dbpedia.add(real_value);
					   
					}
					
					// Get the largest similarity btw instances
					float temp_sim = CalculateSimilarity(xb, dbpedia);
					
					if( result < temp_sim)
						result = temp_sim;
						
				}
				
				cursor2.close();
				
				//System.out.println("main result = " + result + " average_ sim = " + average_similarity + " num = " + num);
				// save the largest similarity for calculate average similarity
				average_similarity = average_similarity + result;
				
			}
			
			
		}
		finally{
			
			average_similarity = average_similarity / num;
			similarity[class_count++] = average_similarity;
			
			System.out.println(xbclass_name + " vs " + dbclass_name + " = " + average_similarity);
			//System.out.println("speech" + " vs " + dbclass_name + " = " + average_similarity);
			
			
			//bw.write(xbclass_name + " vs " + dbclass_name + " = " + average_similarity);
			//bw.newLine();
			cursor.close();		
		}
		
		} // while in
		
		// Top K result
		int[] indexes = indexesOfTopElements(similarity, 3);
		//System.out.println(xbclass_name + "  with");
		for(int i = 0; i < indexes.length; i++) {
            int index = indexes[i];
            //System.out.println(class_name[index] + " " + similarity[index]);
            
            
            bw.write(xbclass_name + " " + class_name[index] + " = " + similarity[index]);
            //bw.write("speech" + " " + class_name[index] + " = " + similarity[index]);
            bw.newLine();
        }
		bw.newLine();
		} // while in2
		
		bw.close();
		in2.close();
		mongoClient.close();
		

	}

    static int[] indexesOfTopElements(float[] orig, int nummax) 
    {
    	
        float[] copy = Arrays.copyOf(orig,orig.length);
        Arrays.sort(copy);
        //System.out.println(orig.length);
        float[] honey = Arrays.copyOfRange(copy,copy.length - nummax, copy.length);
        int[] result = new int[nummax];
        int resultPos = 0;
        for(int i = 0; i < orig.length; i++) {
            float onTrial = orig[i];
            int index = Arrays.binarySearch(honey,onTrial);
            if(index < 0) continue;
            result[resultPos++] = i;
            if(resultPos == nummax)
            	break;
            //System.out.println("ResultPost : " + resultPos);
        }
        return result;
    }
    
    private static void checkProp(String ins1) throws IOException
	{
    	
    	MongoClient mongoClient = new MongoClient();
		DB db = mongoClient.getDB("LinkagePoint_KoreanDbpedia_GGOGGO_Whole");
		DBCollection table = db.getCollection("KoreanDbpedia_GGOGGO_Whole");
//		DB db = mongoClient.getDB("LinkagePoint_xbonto_GGOGGO");
//		DBCollection table = db.getCollection("xbonto_GGOGGO");
		
		BasicDBObject query = new BasicDBObject("InstanceName", ins1);
		BasicDBObject field = new BasicDBObject();
		field.put("_id", 0);
		field.put("InstanceName", 0);
		field.put("Class", 0);
		//field.put("label", 0);
		
		DBCursor cursor = table.find(query, field);

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
			   Object value = jsonobj.get(key);
			   if(value.equals(""))
				   continue;
			   String real_value = value.toString();
			   if(real_value.contains("ko.dbpedia.org/resource/"))
			   {
				   int index = real_value.indexOf("ko.dbpedia.org/resource/");	// remove URI
				   real_value = real_value.substring(index+24);  
			   }
			   
			   System.out.println(key + " : " + real_value);

			}
		}
		
		
		
		/* XB test
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
			   Object value = jsonobj.get(key);
			   if(value.equals(""))
				   continue;
			   String real_value = value.toString();
			   int space = real_value.lastIndexOf(" ");
			   System.out.println(space);
			   if(real_value.substring(space).equals(" "))
			   {
			   		System.out.println(key + " : " + real_value.substring(0,space));
			   }
			   else
			   {
				   System.out.println(key + " : " + value);
			   }
			}
		}
		*/

	}
    
    

	private static float CalculateSimilarity(ArrayList<String> xb, ArrayList<String> dbpedia) // add top K similarity score divided by the number of yago or dbpedia
	{
		int num_xb = xb.size();
		int num_dbpedia = dbpedia.size();
		//System.out.println("in");
		//MongeElkan dist = new MongeElkan();
		//JaroWinkler dist2 = new JaroWinkler();
		JaccardSimilarity dist3 = new JaccardSimilarity();
		//CosineSimilarity dist4 = new CosineSimilarity();
		
		float sum = 0;
		
		if(num_xb <= num_dbpedia)
		{
			for(String s : xb)
			{
				if(s == " ")
					continue;
				float result = 0;
				
				
				for(String s1 : dbpedia)
				{
					//System.out.println(s + " " + s1);
					if(s1 == " ")
						continue;
					
					//System.out.println("count");
					//float temp = dist.getSimilarity(s, s1);
					//float temp = dist2.getSimilarity(s, s1);
					float temp = dist3.getSimilarity(s, s1);
					//float temp = dist4.getSimilarity(s, s1);
					//System.out.println("compare");
					if( isStringInt(s) && isStringInt(s1))
					{
						
						if(s.equals(s1))
							temp = 1;
						else
							temp = 0;
						
					}
					if( isStringDouble(s) && isStringDouble(s1))
					{	
						if(s.equals(s1))
							result = 1;
						else
							result = 0;
					}
					//if(temp > 0.9)
					//	System.out.println(s + " " + s1 + " = " + temp);
					if( result < temp )
						result = temp;
				}
				//System.out.println("result  = " + result);
				sum = sum + result;
			}
			sum = sum / num_xb;
		}
		else
		{
			for(String s : dbpedia)
			{
				if(s == " ")
					continue;
				float result = 0;
				for(String s1 : xb)
				{
					if(s1 == " ")
						continue;
					
					//float temp = dist.getSimilarity(s, s1);
					//float temp = dist2.getSimilarity(s, s1);
					float temp = dist3.getSimilarity(s, s1);
					//float temp = dist4.getSimilarity(s, s1);
					
					if( isStringInt(s) && isStringInt(s1))
					{
						if(s.equals(s1))
							temp = 1;
						else
							temp = 0;
						
					}
					if( isStringDouble(s) && isStringDouble(s1))
					{	
						if(s.equals(s1))
							result = 1;
						else
							result = 0;
					}
					
					if( result < temp )
						result = temp;
				}
				
				sum = sum + result;
			}
			sum = sum / num_dbpedia;
		}
		//System.out.println("out");
		if(sum < 0.1)
			return 0;
		else
			return sum;
		
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
	private static void getNumberofInstance(String classname) throws IOException
	{
		MongoClient mongoClient = new MongoClient();
		// Korean DBpedia ontology
		DB db = mongoClient.getDB("LinkagePoint_xbonto_GGOGGO");
		DBCollection table = db.getCollection("xbonto_GGOGGO");
		
		BasicDBObject query = new BasicDBObject("Class", classname);
		DBCursor cursor= table.find(query);
		
		System.out.println(classname + " : " + cursor.count());
		cursor.close();
		mongoClient.close();
	}
	
}


/*
try{

// XB ontology
while(cursor.hasNext() && num < 500){	
	
	num++;
	//System.out.println(num);
	String temp = cursor.next().toString();
	//System.out.println(temp);
	st = new StringTokenizer(temp, ",");
	int count = 0;

	ArrayList<String> xb = new ArrayList<String>();
	
	while(st.hasMoreTokens())
	{
		if(count == 0)
		{
			String a = st.nextToken().replace("{", "").replace("}","").replaceAll("\"", "");
			if(a.equals(" ")) // null entry case
				continue;
			
			int start = a.indexOf(" : ");
			String a1 = a.substring(start+3).replaceAll("_", " ");
			//String a1 = a.substring(start+2);
			//System.out.println("xb : "+ a1 + "sub = " + a1.substring(a1.length()-1) );
			if( a1.length() == 0)
				continue;
			if(  a1.substring(a1.length()-1).equals(" ") )
			{
				a1 = a1.substring(0, a1.length()-1);
				//System.out.println(a1);
				xb.add(a1);
			}
			else
			{
				//System.out.println(a1);
				xb.add(a1);
			}
		}
		else
		{
			String b = st.nextToken().replace("}", "").replaceAll("\"", "");
			if(b.equals(" "))
				continue;
			
			int start2 = b.indexOf(" : ");
			String b1 = b.substring(start2+3).replaceAll("_", " ");
			//String b1 = b.substring(start2+2);
			//System.out.println("xb : " + b1 + "sub = " + b1.substring(b1.length()-1) );
			
			if( b1.equals(""))
				continue;
			if(  b1.substring(b1.length()-1).equals(" ") )
			{
				b1 = b1.substring(0, b1.length()-1);
				//System.out.println(b1);
				xb.add(b1);
			}
			else
			{
				//System.out.println(b1);
				xb.add(b1);
			}

		}
		count++;
	}
	
	
	DBCursor cursor2 = table2.find(query2, field2);
	StringTokenizer st2 = null;
	float result = 0; // the largest similarity of two instances
	
	int num_dbpedia = 0;
	
	// Korean DBpedia
	while(cursor2.hasNext() && num_dbpedia < 500)
	{
		num_dbpedia++;
		//System.out.println(num_dbpedia);
		
		ArrayList<String> dbpedia = new ArrayList<String>();
		
		int count1 = 0;
		
		String temp2 = cursor2.next().toString();
		//System.out.println(temp2);
		st2 = new StringTokenizer(temp2, ",");
		
		while(st2.hasMoreTokens())
		{
			if(count1 == 0)
			{
				String a = st2.nextToken().replace("{", "").replace("}","").replaceAll("\"", "");
				//System.out.println(a);
				if(a.equals(" ")) // { } case
					continue;
				
				int start = a.indexOf(":");
				String a1 = a.substring(start+2).replaceAll("_", " ");
				//String a1 = a.substring(start+2);
				//System.out.println("dbpedia : " + a1);			
				if( a1.length() == 0) // {"����" : ""} case
					continue;
				if(  a1.substring(a1.length()-1).equals(" ") )
				{
					a1 = a1.substring(0, a1.length()-1);
					//System.out.println(a1);
					dbpedia.add(a1);
				}
				else
				{
					//System.out.println(a1);
					dbpedia.add(a1);
				}
				//dbpedia.add(a1);
				
			}
			else
			{
				String b = st2.nextToken().replace("}", "").replaceAll("\"", "");
				if(b.equals(" "))
					continue;
				
				int start2 = b.indexOf(":");
				String b1 = b.substring(start2+2).replaceAll("_", " ");
				//String b1 = b.substring(start2+2);
				//System.out.println("dbpedia : " + b1);
				
				if( b1.equals(""))
					continue;
				if(  b1.substring(b1.length()-1).equals(" ") )
				{
					b1 = b1.substring(0, b1.length()-1);
					//System.out.println(b1);
					dbpedia.add(b1);
				}
				else
				{
					//System.out.println(b1);
					dbpedia.add(b1);
				}
	
				//dbpedia.add(b1);
				
			}
			count1++;
		}
		// Get the largest similarity btw instances
		//System.out.println("calculate");
		float temp_sim = CalculateSimilarity(xb, dbpedia);
		
//		if(temp_sim > 0.5)
//		{
//			//System.out.println(temp + "\n" + temp2);
//			bw.write(temp + "\n" + temp2);
//			bw.newLine();
//		}
		
		if( result < temp_sim)
			result = temp_sim;
			
	}

	cursor2.close();
	
	//System.out.println("main result = " + result + " average_ sim = " + average_similarity + " num = " + num);
	
	// save the largest similarity for calculate average similarity
	average_similarity = average_similarity + result;
	
}


}
finally{

average_similarity = average_similarity / num;
similarity[class_count++] = average_similarity;
System.out.println(xbclass_name + " vs " + dbclass_name + " = " + average_similarity);
//bw.write(xbclass_name + " vs " + dbclass_name + " = " + average_similarity);
//bw.newLine();

//System.out.println("yago vs db = " + average_similarity);
cursor.close();		
}

} // while in

*/

/*
private static float CalculateSimilarity1(ArrayList<String> xb, ArrayList<String> dbpedia) // add top K similarity score divided by the number of yago or dbpedia
{
	int num_xb = xb.size();
	int num_dbpedia = dbpedia.size();
	//System.out.println("in");
	//MongeElkan dist = new MongeElkan();
	//JaroWinkler dist2 = new JaroWinkler();
	JaccardSimilarity dist3 = new JaccardSimilarity();
	//CosineSimilarity dist4 = new CosineSimilarity();
	
	float sum = 0;
	
	if(num_xb <= num_dbpedia)
	{
		for(String s : xb)
		{
			if(s == " ")
				continue;
			float result = 0;
			
			
			for(String s1 : dbpedia)
			{
				//System.out.println(s + " " + s1);
				if(s1 == " ")
					continue;
				
				//System.out.println("count");
				//float temp = dist.getSimilarity(s, s1);
				//float temp = dist2.getSimilarity(s, s1);
				float temp = dist3.getSimilarity(s, s1);
				//float temp = dist4.getSimilarity(s, s1);
				//System.out.println("compare");
				if( isStringInt(s) && isStringInt(s1))
				{
					
					if(s.equals(s1))
						temp = 1;
					else
						temp = 0;
					
				}
				//if(temp > 0.9)
				//	System.out.println(s + " " + s1 + " = " + temp);
				if( result < temp )
					result = temp;
			}
			//System.out.println("result  = " + result);
			sum = sum + result;
		}
		sum = sum / num_xb;
	}
	else
	{
		for(String s : dbpedia)
		{
			if(s == " ")
				continue;
			float result = 0;
			for(String s1 : xb)
			{
				if(s1 == " ")
					continue;
				
				//float temp = dist.getSimilarity(s, s1);
				//float temp = dist2.getSimilarity(s, s1);
				float temp = dist3.getSimilarity(s, s1);
				//float temp = dist4.getSimilarity(s, s1);
				
				if( isStringInt(s) && isStringInt(s1))
				{
					if(s.equals(s1))
						temp = 1;
					else
						temp = 0;
					
				}
				
				if( result < temp )
					result = temp;
			}
			
			sum = sum + result;
		}
		sum = sum / num_dbpedia;
	}
	//System.out.println("out");
	if(sum < 0.1)
		return 0;
	else
		return sum;
	
}
*/