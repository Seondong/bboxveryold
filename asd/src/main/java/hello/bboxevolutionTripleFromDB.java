package hello;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Ints;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.util.FileUtils;

import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protege.model.DefaultCls;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;
import edu.stanford.smi.protegex.owl.model.RDFIndividual;
import edu.stanford.smi.protegex.owl.model.RDFSClass;

/* B-Box Evolution Algorithms working on Pseudo_K_Box DB (Not working yet)
 * by Sundong Kim (sundong.kim@kaist.ac.kr)
 * Input : Knowledge base, Triples
 * Output : Evolved Knowledge base
 * 
 * argument example : C:/Users/user/git/bbox/bboxevolution/evolvedold.owl C:/Users/user/git/bbox/bboxevolution/triples/
C:/Users/user/git/bbox/bboxevolution/evolvedoldenriched.owl
 */

public class bboxevolutionTripleFromDB {
	// static String owlfilePath = "output_kor_output";
	// static String instanceCSVfilePath = "triple";
	public static List<String> relatedIndividualNameList = new ArrayList<String>();
	public static List<String> relatedPropertyNameList = new ArrayList<String>();
	public static List<OWLIndividual> relatedIndividualList = new ArrayList<OWLIndividual>();
	public static List<OWLDatatypeProperty> relatedPropertyList = new ArrayList<OWLDatatypeProperty>();
	public static Multimap<OWLNamedClass, OWLIndividual> relatedIndividualMap = ArrayListMultimap
			.create();
	public static Multimap<OWLNamedClass, OWLIndividual> relatedPropertyMap = ArrayListMultimap
			.create();

	static HashSet<String> set = new HashSet<String>();
	
	public static int[] doDemo(String[] args) throws Exception {
		//String inputowlfilePath = args[0];
		//String instanceCSVfilePath = args[1];
		//String outputowlfilePath = args[2];

		
		// String[] koreanClass = koreanMapping();
		// String[] englishClass = englishMapping();

//		JenaOWLModel owlModel = loadExistSchema(inputowlfilePath);
		
		ArrayList<Integer> T_id = new ArrayList<Integer>();
		
		T_id.add(864336);
		T_id.add(864337);
		T_id.add(864338);
		T_id.add(864339);
		T_id.add(864340);
		T_id.add(864341);
		T_id.add(864342);
		T_id.add(864343);
		T_id.add(864344);
		
		// 현재 OWL 경로에 맞게 수정 필요
//		String uri = "file:///C:/Users/user/git/bbox/bboxevolution/DBpedia_AllIns.owl";
//		String uri = "file:///D:/data1/DBpedia_AllIns.owl";
		
//		JenaOWLModel owlModel = ProtegeOWL.createJenaOWLModelFromURI(uri);
//		addTripleFromDB(owlModel, 0, T_id);
//		executeLogic(owlModel);
		
//		writeTripleToDB(owlModel);
		// 원하는 OWL 경로에 맞게 수정 필요
//		saveEvolvedSchema(owlModel, "C:/Users/user/git/bbox/bboxevolution/evolvedoldenriched.owl");
//		saveEvolvedSchema(owlModel, "D:/data1/evolvedoldenriched.owl");
		
//		새로운 증강 모듈 1	예제
//		Att_Sim test = new Att_Sim();
//		test.instCluster("정당_정보", 6);

//		새로운 증강 모듈 2 예제
//		Class_Sim test2 = new Class_Sim();
//		test2.classCluster("ArchitecturalStructure", 2);
		
		
        ArrayList<Integer> resultTID = bboxRun(T_id); 
		if(resultTID != null) {
		    for(int tid : resultTID)
		        System.out.println(tid);
		    return Ints.toArray(resultTID);
		} else {
			int[] retval = new int[0];
			return retval;
		}
		
		//saveEvolvedSchema2(owlModel, outputowlfilePath);
		// addTriple(owlModel, 0, koreanClass, englishClass);
		// addInstance(owlModel, 23, koreanClass, englishClass);

		// executeLogic(owlModel);
		// saveEvolvedSchema(owlModel, 22);

		// int evolutionstep = 32;
		//
		//
		// for(int i=24; i<evolutionstep; i++){
		// JenaOWLModel owlModel = loadExistSchema(i);
		// // addTriple(owlModel, i, koreanClass, englishClass);
		// // addInstance(owlModel, i, koreanClass, englishClass);
		// executeLogic(owlModel);
		// saveEvolvedSchema(owlModel, i);
		// }
		//

		// int correctionstep = 10;
		// for(int i2=1; i2<correctionstep; i2++){
		// owlModel = loadExistSchema(i2);
		// executeLogic(owlModel);
		// saveEvolvedSchema(owlModel, i2);
		// }

	}
/*
	public static void main(String[] args) throws Exception {
		doDemo(null);
	}
*/
	public static ArrayList<Integer> bboxRun(ArrayList<Integer> tripleIDs) {

        try {
            // 현재 OWL 경로에 맞게 수정 필요
          String uri = "file:///C:/Users/user/git/bboxlocal/asd/DBpedia_AllIns.owl";
    //        String uri = "file:///"+System.getProperty("user.home")+"/OPENKB/HOME/data/bbox/DBpedia_AllIns.owl";
            
            JenaOWLModel owlModel = ProtegeOWL.createJenaOWLModelFromURI(uri);
            addTripleFromDB(owlModel, 0, tripleIDs);
            executeLogic(owlModel);
            
            writeTripleToDB(owlModel);
            // 원하는 OWL 경로에 맞게 수정 필요
          saveEvolvedSchema(owlModel, "C:/Users/user/git/bboxlocal/asd/evolvedoldenriched.owl");
    //      saveEvolvedSchema(owlModel, "file:///"+System.getProperty("user.home")+"/evolvedoldenriched.owl");
    //        saveEvolvedSchema(owlModel, "evolvedoldenriched.owl");
            
            ArrayList<Integer> resultTID = getTID(); 
            
            return resultTID;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
        return null;
	}
	
	private static ArrayList<Integer> getTID()
	{
	    //      Psuedo K-Box에 접근
	    String jdbcUrl = "jdbc:mysql://211.109.9.71:3306/pseudo_kbox_db?useUnicode=true&characterEncoding=utf8";
	    String userId = "openkbuser";
	    String userPass = "openkbpass";
	    try{
	        Class.forName("com.mysql.jdbc.Driver");
	    }catch(ClassNotFoundException e){

	    }
	    
	    ArrayList<Integer> tid = new ArrayList<Integer>();
	    Connection conn = null;
	    Statement stmt = null;
	    ResultSet rs = null;

	    try{

	        conn = DriverManager.getConnection(jdbcUrl, userId, userPass);
	        stmt = conn.createStatement();
	        Iterator iter = set.iterator();
	        while(iter.hasNext())
	        {
	            String s = iter.next().toString();
	            s = s.replaceAll("\\'", "\\\\'");
	            System.out.println(s);
	            // http://www.w3.org/1999/02/22-rdf-syntax-ns#type
	            String query = "select cand_id from bboxDemo where (subject_uri = '" + s + "') AND (predicate_uri = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type')";
	            rs = stmt.executeQuery(query);

	            while(rs.next())
	            {
	                tid.add(rs.getInt(1));
	            }
	        }

	    }catch(Exception ex){
	        ex.printStackTrace();
	    }finally{
	        try{

	            rs.close();
	            stmt.close();
	            conn.close();
	        }catch(SQLException e){
	        }
	    }
	    return tid;
	}
	
	private static JenaOWLModel loadExistSchema(String filePath)
			throws OntologyLoadException {
		// String uri = "file:///"+owlfilePath;
		String uri = "file:///" + filePath;
		JenaOWLModel model = ProtegeOWL.createJenaOWLModelFromURI(uri);

		return model;
	}

	private static void saveEvolvedSchema(JenaOWLModel model, String filePath) {
		Collection errors = new ArrayList();
		try {
			model.save(new File(filePath).toURI(), FileUtils.langXMLAbbrev,
					errors);
		} catch (Exception e) {

		}
		System.out.println("File saved with " + errors.size() + " errors.");
	}

	private static void saveEvolvedSchema2(JenaOWLModel model, String filePath) {
		OntModel ontmodel = model.getOntModel();
		FileWriter out = null;
		try {
			out = new FileWriter("evolvedKB22.owl");
			ontmodel.write(out, "RDF/XML-ABBREV");
		} catch (Exception e) {

		}
	}
	
	private static ArrayList<String> getAllHierarchy(JenaOWLModel owlModel, String inst1)
	{
		ArrayList<String> hierarchy = new ArrayList<String>();
		String temp=null;

		try{
			
			RDFIndividual instance1=owlModel.getRDFIndividual(inst1);			
			RDFSClass class1 = instance1.getRDFType();
			temp = class1.getBrowserText();
			
			for(;;)
			{
				char c = temp.charAt(1);
				if(temp.equals("Thing"))
				{
					hierarchy.add("http://www.w3.org/2002/07/owl#Thing");
					//"http://www.w3.org/2002/07/owl#Thing"
				}
				else if(( 0x61 <= c && c <= 0x7A ) || ( 0x41 <= c && c <= 0x5A )) // 영어
				{
					hierarchy.add("http://dbpedia.org/ontology/" + temp);
					//"http://dbpedia.org/ontology/" + temp
				}
				else // 한글
				{
					hierarchy.add("http://ko.dbpedia.org/page/틀:" + temp);
					//"http://ko.dbpedia.org/page/틀:" + temp
				}
				
				if(temp.equals("Thing"))
				{
					break;
				}
					
				class1 = (RDFSClass) class1.getNamedSuperclasses().iterator().next();
				temp = class1.getBrowserText();
			}
			
	    }
		catch (Exception e)
	    {
	       //System.out.println("Instance load failed");
	       return null;
	    }		
		return hierarchy;
	}

	private static void writeTripleToDB(JenaOWLModel model)
	{
//		Psuedo K-Box에 접근
		String jdbcUrl = "jdbc:mysql://211.109.9.71:3306/pseudo_kbox_db?useUnicode=true&characterEncoding=utf8";
		String userId = "openkbuser";
		String userPass = "openkbpass";
		try{
			Class.forName("com.mysql.jdbc.Driver");
		}catch(ClassNotFoundException e){
			
		}
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		
		try{
			conn = DriverManager.getConnection(jdbcUrl, userId, userPass);
			String sql = "insert into bboxDemo (subject_uri,predicate_uri,object_uri) values(?,?,?)";        
			pstmt = conn.prepareStatement(sql);  
			Iterator iter = set.iterator();
			while(iter.hasNext())
			{
				String subject = iter.next().toString();
				String s = subject.replaceAll(" ", "_");
				s = s.replaceAll("http://ko.dbpedia.org/resource/", "");
				s = s.replaceAll(":", "_");
				ArrayList<String> candidate_object = getAllHierarchy(model, s);
				if(candidate_object == null) continue;
				
			    String predicate = "http://www.w3.org/1999/02/22-rdf-syntax-ns#type";
				
				for(int i = 0; i < candidate_object.size(); i++)
				{
					String object = null;
					object = candidate_object.get(i);
					pstmt.setString(1, subject);
					pstmt.setString(2, predicate);
					pstmt.setString(3, object);
					pstmt.addBatch();
				}

			}
			
			pstmt.executeBatch();
			
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			if(pstmt != null) // PreparedStatement 객체 해제
				try{pstmt.close();
				}catch(SQLException sqle)
				{}            
			if(conn != null) // Connection 해제
				try{conn.close();
				}catch(SQLException sqle)
				{}            
		}
	}
	
	private static JenaOWLModel executeLogic(JenaOWLModel model) {
		OWLNamedClass Thingcls = model.getOWLNamedClass("owl:Thing");
		DefaultCls Thingclss = (DefaultCls) Thingcls;

		// System.out.println("------Now we are finding class depth---------");
		HashMap<String, Integer> nodeDepths = new HashMap<String, Integer>(); // Hashmap nodeDepths saves Class
											// name(key) & tree depth (value)
		List<String> nodeSequence = new ArrayList<String>(); // Since Hashmap
																// doesn't save
																// the sequence,
																// we make list
																// for key
																// sequence.
		int nodeDepth = 1;
		DefaultCls rootcls = Thingclss;

		FindnodeDepth(model, nodeDepths, nodeSequence, rootcls, nodeDepth);

		// for(int i=0; i<nodeSequence.size(); i++){ //Print hashmap by key
		// sequence
		// System.out.println(i + "   " + nodeSequence.get(i) + " : " +
		// nodeDepths.get(nodeSequence.get(i)));
		// }

		for (int i = 1; i <= 68; i++) { // Remove all structure-related class
										// (not useful for property
										// generalization)
			nodeSequence.remove(1);
		}
		System.out.println("------Finish finding class depth---------");

		int j = 0; // Counting Deepest Node
		ArrayList<String> classname = new ArrayList<String>(); // Two ArrayList
																// for
																// backtracking
		ArrayList<Integer> depth = new ArrayList<Integer>();
		HashMap nodeDepthsNew = new HashMap();

		for (int i = 0; i < nodeSequence.size(); i++) { // Print eligible class
														// lists
	        int k = (int) nodeDepths.get(nodeSequence.get(i));
			// System.out.println(i + "   " + nodeSequence.get(i) + " : " +
			// nodeDepths.get(nodeSequence.get(i)));
			nodeDepthsNew.put(nodeSequence.get(i), k);

			classname.add(nodeSequence.get(i));
			depth.add(k);
			if (j < k) {
				j = k;
			}
		}

		double compareGTwithAlgo = 0.0;
		List<Double> evalsSum = new ArrayList<Double>();
		evalsSum.add(0.0);
		evalsSum.add(0.0);
		evalsSum.add(0.0);
		evalsSum.add(0.0);

		for (int i = j; i >= 2; i--) { // Property Generalization from the
										// lowest node.
			// for(int i = 1; i<=j; i++){
			Iterator it = getKeysByValue(nodeDepthsNew, i).iterator();
			while (it.hasNext()) {
				String clsName = it.next().toString();
				// System.out.println(clsName);
				try {
					OWLNamedClass cls = model.getOWLNamedClass(clsName);
//					System.out.println(cls.getBrowserText() + "   , " + i);
					// Get information of each subclass, and pull up relative
					// property

					// propertyDeletion(cls, i, model);
					// propertyGeneralization(cls, i, model);
				} catch (ClassCastException e) {

				}
			}
		}
		
		System.out.println("----------Finding Instance Type-----------");
		for (int i = j; i >= 2; i--) { // Extract relevant class for instances.
			// for(int i = 1; i<=j; i++){
			Iterator it = getKeysByValue(nodeDepthsNew, i).iterator();
			while (it.hasNext()) {
				String clsName = it.next().toString();
				try {
					OWLNamedClass cls = model.getOWLNamedClass(clsName);
				    System.out.println(cls.getBrowserText() + "   , " + i);

					List<Double> n = secondaryClassTFIDF(cls, model);
					if (!cls.getBrowserText().equals("Thing")) {
						evalsSum.set(0, evalsSum.get(0) + n.get(0));
						evalsSum.set(1, evalsSum.get(1) + n.get(1));
						evalsSum.set(2, evalsSum.get(2) + n.get(2));
						evalsSum.set(3, evalsSum.get(3) + n.get(3));
					}
					// System.out.println("TotalInstanceCount : " +
					// evalsSum.get(0) + "ExceptThing : " +evalsSum.get(1) +
					// "TotalMatch : " + evalsSum.get(2) + "Algodbp : " +
					// evalsSum.get(3));

					evalsSum.set(0, 0.0);
					evalsSum.set(1, 0.0);
					evalsSum.set(2, 0.0);
					evalsSum.set(3, 0.0);

				} catch (ClassCastException e) {

				}
			}
		}

		return model;
	}

	// private static JenaOWLModel addTriple(JenaOWLModel model, int i, String[]
	// koreanClass, String[] englishClass) throws FileNotFoundException,
	// IOException {
	
	private static JenaOWLModel addTripleFromDB(JenaOWLModel model, int i, ArrayList<Integer> Tid) throws FileNotFoundException,
			IOException {
//		Psuedo K-Box에 접근
		String jdbcUrl = "jdbc:mysql://211.109.9.71:3306/pseudo_kbox_db?useUnicode=true&characterEncoding=utf8";
		String userId = "openkbuser";
		String userPass = "openkbpass";
		try{
			Class.forName("com.mysql.jdbc.Driver");
		}catch(ClassNotFoundException e){
			
		}
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		try{
			conn = DriverManager.getConnection(jdbcUrl, userId, userPass);
			stmt = conn.createStatement();
			
			System.out.println("----------Adding Triples-----------");

			Integer numtriple = 0;
			
			int tid = 0;
			for(tid = 0; tid < Tid.size(); tid++)	
			{
				String query = "select subject_uri, predicate_uri, object_uri from bboxDemo where cand_id = " + Tid.get(tid);
				rs = stmt.executeQuery(query);
				
				String s = null;
				String p = null;
				String o = null;
				
				if(rs.next()){
					s = rs.getString(1);
					p = rs.getString(2);
					o = rs.getString(3);
					
					set.add(s);
				}
				
				numtriple = numtriple + 1;
				
				try {
						
					//String s = st.nextToken();
					s = s.replaceAll(" ", "_");
					s = s.replaceAll("http://ko.dbpedia.org/resource/", "");
					s = s.replaceAll(":", "_");
					//String p = st.nextToken();
					p = p.replaceAll(" ", "_");
					p = p.replaceAll("http://ko.dbpedia.org/property/", "");
					p = p.replaceAll("http://dbpedia.org/ontology/", "");
					//String o = st.nextToken();
					o = o.replaceAll(" ", "_");
					o = o.replaceAll("http://ko.dbpedia.org/resource/", "");      //Do not think of individual here as a object
					
					
					if (!relatedIndividualNameList.contains(s)) {
						relatedIndividualNameList.add(s);
					}
					if (!relatedPropertyNameList.contains(p)) {
						relatedPropertyNameList.add(p);
					}
					if ((numtriple % 1000) == 0) {
						System.out.println("Triple count = " + numtriple
								+ ", " + s + ", " + p + ", " + o);
					}

					String type = "Thing";
//						System.out.println(s + ", " + p + ", " + o);
					OWLNamedClass cls = model.getOWLNamedClass(type);

					if (cls == null) {
						cls = model.createOWLNamedClass(type);
						// cls.addLabel(type+"_generated" , "New" );
					}

					OWLIndividual individual;
					OWLDatatypeProperty property;
					try {
						individual = model.getOWLIndividual(s);
					} catch (Exception e) {
						//System.out.println(e.getMessage());
						e.printStackTrace();
						individual = model.getOWLIndividual(s
								+ "_individual");
					}

					property = model.getOWLDatatypeProperty(p);

					if (individual == null) {
						try {
							individual = cls.createOWLIndividual(s);
						} catch (Exception e) {
							e.printStackTrace();
//								System.out.println(e.getMessage());
							individual = cls.createOWLIndividual(s
									+ "_individual");
						}
					}
					try {
						if (property == null) {
							property = model.createOWLDatatypeProperty(p);
						}
						if(!individual.hasPropertyValue(property, o)){
							individual.addPropertyValue(property, o);
						}
					} catch (IllegalArgumentException e) {
						System.out.println(e.getMessage());
					}

//					System.out.println(individual.getBrowserText() + ", " + property.getBrowserText() + ", " + o);
					
					relatedIndividualList.add(individual);
					relatedPropertyList.add(property);
					
					if (!relatedIndividualMap.containsValue(individual)) {
						relatedIndividualMap.put(cls, individual);
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}finally{
			try{	
				rs.close();
				stmt.close();
				conn.close();
			}catch(SQLException e){
			}
		}
		
		System.out.println("Number of new individual : "
				+ relatedIndividualNameList.size());
		System.out.println("Number of new property : "
				+ relatedPropertyNameList.size());
		return model;
	}

	private static void FindnodeDepth(JenaOWLModel owlModel,
			HashMap<String, Integer> nodeDepths, List<String> sequence, DefaultCls rootClass,
			int nodeDepth) { // Find tree depth of each class in dbpedia
								// Ontology
		DefaultCls rootcls = rootClass;
		// System.out.println(rootcls.getBrowserText());
		// System.out.println(nodeDepths.size());
		if (!nodeDepths.containsKey(rootcls.getBrowserText())) {
			nodeDepths.put(rootcls.getBrowserText(), nodeDepth);
			sequence.add(rootcls.getBrowserText());
		}

		Iterator it = rootcls.getSubclasses().iterator();
		while (it.hasNext()) { // && nodeDepth<10
			try {
				DefaultCls subclass = (DefaultCls) it.next();
				if (!subclass.getBrowserText().equals(rootcls.getBrowserText())
						&& !nodeDepths.containsKey(subclass.getBrowserText())) {
					nodeDepths.put(subclass.getBrowserText(), nodeDepth + 1);
					sequence.add(subclass.getBrowserText());
					FindnodeDepth(owlModel, nodeDepths, sequence, subclass,
							nodeDepth + 1);
				}
			} catch (ClassCastException e) {

			}

		}
	}

	public static <String, Integer> Set<String> getKeysByValue(
			Map<String, Integer> map, Integer value) {
		Set<String> keys = new HashSet<String>();
		for (Entry<String, Integer> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				keys.add(entry.getKey());
			}
		}
		return keys;
	}

	public static List<Double> secondaryClass(OWLNamedClass cls,
			JenaOWLModel owlModel) {
		
		List<Double> evals = new ArrayList<Double>();
		Double count1 = 0.0;
		Double count2 = 0.0;
		Double count3 = 0.0;
		Double count4 = 0.0;

		// Collection a = cls.getDirectInstances();

		Collection<OWLIndividual> a = relatedIndividualMap.get(cls);
		
		if (a.size() > 0) {
			Iterator it2 = a.iterator();

			try {
				while (it2.hasNext()) {
					Object o = it2.next();
					try {
						OWLIndividual ind = (OWLIndividual) o;
						HashMap<String, Integer> h = new HashMap<String, Integer>();

						// System.out.println("Class : " + cls.getBrowserText()
						// + ",     Instance : " + ind.getBrowserText());
						Collection instanceprop = ind.getRDFProperties();
						Iterator it3 = instanceprop.iterator();
						// System.out.println("inst-prop size : " +
						// instanceprop.size());

						for (Object oal : instanceprop) {
							String s = oal.toString();
							// System.out.println(s);
							int wordIdx = s.lastIndexOf("#");
							String propname = s.substring(wordIdx + 1,
									s.length() - 1);
							if (propname.contains("/")) {
								int wordIdx2 = s.lastIndexOf("/");
								propname = s.substring(wordIdx2 + 1,
										s.length() - 1);
							}
							// System.out.println(propname);

							try {
								if (!propname.equals("wikiPageUsesTemplate")
										&& !propname.equals("type")) {
									OWLDatatypeProperty prop = owlModel
											.getOWLDatatypeProperty(propname);
									Collection c = prop.getDirectDomain();
									Iterator it = c.iterator();
									try {
										// System.out.println(c.size() +", " +
										// prop);
										while (it.hasNext()) {
											Object ooo = it.next();
											String ss = ooo.toString();
											OWLNamedClass ooocls = (OWLNamedClass) ooo;
											Collection oooclssub = ooocls
													.getSubclasses();

											// Property's Domain and its
											// frequency - 占쏙옙占쏙옙占쏙옙占� 占쏙옙占쏙옙占쏙옙占�
											// Domain占쏙옙 占싸뱄옙_占쏙옙占쏙옙
											if (!h.containsKey(ss)) {
												h.put(ss, 1);
											} else {
												h.put(ss, h.get(ss) + 1);
											}

											// Property's All subDomain and its
											// frequency 占싸뱄옙_占쏙옙占쏙옙占쏙옙 subdomain占썽도
											// 占쏙옙占� 占쌩곤옙
											// for(Object sub : oooclssub){
											// String sss = sub.toString();
											// if(!h.containsKey(sss)){
											// h.put(sss,1);
											// }else{
											// h.put(sss, h.get(sss) + 1);
											// }
											// }
										}
									} catch (Exception e) {
										// e.printStackTrace();
									}

								}
							} catch (Exception e) {
								// e.printStackTrace();
							}
						}

						int hival = 0;
						String key = "";

						List<String> indName = new ArrayList<String>();
						List<String> beforeClsName = new ArrayList<String>();
						List<String> newClsName = new ArrayList<String>();
						List<String> gtName = new ArrayList<String>();

						for (Entry<String, Integer> e : h.entrySet()) {

							System.out.println(ind.getBrowserText() + ","
									+ e.getValue() + "," + e.getKey());
							if (e.getValue() > hival
									&& !e.getKey()
											.equals("DefaultOWLNamedClass(http://www.w3.org/2002/07/owl#Thing)")) {
								hival = e.getValue();
								key = e.getKey();
								int wordIdxx = key.lastIndexOf("#");
								key = key.substring(wordIdxx + 1,
										key.length() - 1);
							}
						}
						//
//						System.out.println("Current class : "
//								+ cls.getBrowserText() + ", Instance name : "
//								+ ind.getBrowserText() + ", Domain Name : "
//								+ key + ", Frequency : " + hival);

						if (key.equals(cls.getBrowserText())) {
							count3 = count3 + 1.0;
						}

						OWLNamedClass clss = owlModel.getOWLNamedClass(key);
						if (clss.equals(null)) {
							clss = owlModel.getOWLNamedClass("Thing");
						}
						Collection comments = ind.getComments();

						if (!comments.contains("Seed")) { // seed instance占쏙옙 占싣댐옙
															// 占싶몌옙 占쌕꾸깍옙
							if (!key.equals(ind.getDirectType()
									.getBrowserText())) {
								ind.addRDFType(clss);
								ind.removeRDFType(cls); // Move instance from
														// cls to clss
								// System.out.println(cls.getBrowserText() +
								// ": " + cls.getDirectInstanceCount() + ", " +
								// ind.getRDFType().getBrowserText() + ": " +
								// ind.getRDFType().getDirectInstanceCount());
								// ind.addLabel(cls.getBrowserText(),
								// "PreviousType");
							}
						}

						// Collection c = ind.getComments();
						//
						// for(Object oc : c){
						// String s = oc.toString();
						// s=s.replace("틀:", "");
						// // if(!cls.getBrowserText().equals(key)){
						// // System.out.println(ind.getBrowserText() +
						// ",  in DBpedia : " + cls.getBrowserText() +
						// " ,  GT : " + s + ", Algo Val : " + key);
						// // }
						//
						// if(s.equals(key)){
						// algoEqualGT=algoEqualGT+1.0;
						// }
						// if(s.equals(cls.getBrowserText())){
						// dbpEqualGT=dbpEqualGT+1.0;
						// }
						// if(key.equals(cls.getBrowserText())){
						// algoEqualdbp = algoEqualdbp+1.0;
						// }
						// total = total + 1.0;
						// }

						// Write File
						// String WRITE_FILE_PATH = methodoutputPath;
						// BufferedWriter bw = null;
						// try{
						// File writeFile = new File(WRITE_FILE_PATH);
						// bw = new BufferedWriter(new FileWriter(writeFile,
						// true));
						// bw.write(cls.getBrowserText() + "," +
						// ind.getBrowserText() + "," + hival + "," + key +
						// "\n");
						// bw.close();
						// }catch(Exception e){
						// e.printStackTrace();
						// }

						// System.out.println("Current class : " +
						// cls.getBrowserText() + ",     Class : " +
						// ind.getRDFType() + ",    Instance name : " +
						// ind.getBrowserText() + ",      Biggest value : " +
						// hival + ",        Biggest Key : " + key);
						//
						// End
					} catch (Exception e) {
						// e.printStackTrace();
					}
				}
			} catch (Exception e) {
				// e.printStackTrace();
			}
		}
		count1 = (double) cls.getDirectInstanceCount();
		count2 = (double) cls.getDirectSubclassCount();

		evals.add(count1);
		evals.add(count2);
		evals.add(count3);
		evals.add(count4);

		return evals;
	}

	public static List<Double> secondaryClassTFIDF(OWLNamedClass cls,
			JenaOWLModel owlModel) {

		List<Double> evals = new ArrayList<Double>();
		Double count1 = 0.0;
		Double count2 = 0.0;
		Double count3 = 0.0;
		Double count4 = 0.0;

		// Collection a = cls.getDirectInstances();

		Collection<OWLIndividual> a = relatedIndividualMap.get(cls);
//		for(OWLIndividual b : a){
//			System.out.println(b);
//		}
		
		if (a.size() > 0) {
			Iterator it2 = a.iterator();

			try {
				while (it2.hasNext()) {
					Object o = it2.next();
					try {
						OWLIndividual ind = (OWLIndividual) o;
						HashMap<String, Double> h = new HashMap<String, Double>();

//						 System.out.println("Class : " + cls.getBrowserText()
//						 + ",     Instance : " + ind.getBrowserText());
						 
						Collection instanceprop = ind.getRDFProperties();
						Iterator it3 = instanceprop.iterator();
//						 System.out.println("inst-prop size : " +
//						 instanceprop.size());

						 
						 
						for (Object oal : instanceprop) {
							String s = oal.toString();
							// System.out.println(s);
							int wordIdx = s.lastIndexOf("#");
							String propname = s.substring(wordIdx + 1,
									s.length() - 1);
							if (propname.contains("/")) {
								int wordIdx2 = s.lastIndexOf("/");
								propname = s.substring(wordIdx2 + 1,
										s.length() - 1);
							}
//							 System.out.println(propname);

							try {
								if (!propname.equals("wikiPageUsesTemplate")
										&& !propname.equals("type")) {
									OWLDatatypeProperty prop = owlModel
											.getOWLDatatypeProperty(propname);
									Collection c = prop.getDirectDomain();
									Iterator it = c.iterator();
									try {
//										 System.out.println(c.size() +", " +
//										 prop);
										while (it.hasNext()) {
											Object ooo = it.next();
											String ss = ooo.toString();
											OWLNamedClass ooocls = (OWLNamedClass) ooo;
											Collection oooclssub = ooocls
													.getSubclasses();

											// Property's Domain and its
											// frequency - 占쏙옙占쏙옙占쏙옙占� 占쏙옙占쏙옙占쏙옙占�
											// Domain占쏙옙 占싸뱄옙_占쏙옙占쏙옙
											if (!h.containsKey(ss)) {
												h.put(ss,
														idfVal(owlModel, prop,
																ooocls));
											} else {
												h.put(ss,
														h.get(ss)
																+ idfVal(
																		owlModel,
																		prop,
																		ooocls));
											}

											// Property's All subDomain and its
											// frequency 占싸뱄옙_占쏙옙占쏙옙占쏙옙 subdomain占썽도
											// 占쏙옙占� 占쌩곤옙
											// for(Object sub : oooclssub){
											// String sss = sub.toString();
											// if(!h.containsKey(sss)){
											// h.put(sss,1);
											// }else{
											// h.put(sss, h.get(sss) + 1);
											// }
											// }
										}
									} catch (Exception e) {
										// e.printStackTrace();
									}

								}
							} catch (Exception e) {
								// e.printStackTrace();
							}
						}

						double hival = 0;
						String key = "";

						List<String> indName = new ArrayList<String>();
						List<String> beforeClsName = new ArrayList<String>();
						List<String> newClsName = new ArrayList<String>();
						List<String> gtName = new ArrayList<String>();
						
						
						List<Entry<String, Double>> relatedClassList = entriesSortedByValues(h);
						List<Entry<String, Double>> thingsToBeRemoved = new ArrayList<Entry<String, Double>>();
//						System.out.println(relatedClassList);
						Iterator<Entry<String, Double>> entryit = relatedClassList.iterator();
						while(entryit.hasNext()){
							Entry<String, Double> e = entryit.next();
							if(e.getKey().equals("DefaultOWLNamedClass(http://www.w3.org/2002/07/owl#Thing)")){
								thingsToBeRemoved.add(e);
							}
							if(e.getValue()<0){
								thingsToBeRemoved.add(e);
							}
						}
						relatedClassList.removeAll(thingsToBeRemoved);
						
// Similar way with followed code.										
//						for (Entry<String, Double> e : relatedClassList){              
//							if(e.getKey().equals("DefaultOWLNamedClass(http://www.w3.org/2002/07/owl#Thing)")){
//								relatedClassList.remove(e);
//							}
//							if(e.getValue()<0){
//								relatedClassList.remove(e);
//							}
//						}
	
						
						
//						System.out.println("Instance : " + ind.getBrowserText());
//						for(int i=0; i<relatedClassList.size(); i++){
//							System.out.println(relatedClassList.get(i));
//						}
						
						String bestType = relatedClassList.get(0).getKey();
//						System.out.println(bestType);
						int Idx = bestType.lastIndexOf("#");
						bestType = bestType.substring(Idx + 1, bestType.length() - 1);
						OWLNamedClass clss = owlModel.getOWLNamedClass(bestType);
						ind.addRDFType(clss);
						ind.removeRDFType(cls);
//						System.out.println("Instance : " + ind.getBrowserText() + ind.getRDFTypes());
						
						
						
						// for (Entry<String, Double> e : h.entrySet()) {
						//
						// System.out.println(ind.getBrowserText() + ","
						// + e.getValue() + "," + e.getKey());
						// if (e.getValue() > hival
						// && !e.getKey()
						// .equals("DefaultOWLNamedClass(http://www.w3.org/2002/07/owl#Thing)"))
						// {
						// hival = e.getValue();
						// key = e.getKey();
						// int wordIdxx = key.lastIndexOf("#");
						// key = key.substring(wordIdxx + 1,
						// key.length() - 1);
						// }
						// }
						//
						// System.out.println("Current class : "
						// + cls.getBrowserText() + ", Instance name : "
						// + ind.getBrowserText() + ", Domain Name : "
						// + key + ", Frequency : " + hival);

						
//						if (key.equals(cls.getBrowserText())) {
//							count3 = count3 + 1.0;
//						}
//
//						OWLNamedClass clss = owlModel.getOWLNamedClass(key);
//						if (clss.equals(null)) {
//							clss = owlModel.getOWLNamedClass("Thing");
//						}
//						Collection comments = ind.getComments();
//
//						if (!comments.contains("Seed")) { // seed instance占쏙옙 占싣댐옙
//															// 占싶몌옙 占쌕꾸깍옙
//							if (!key.equals(ind.getDirectType()
//									.getBrowserText())) {
//								ind.addRDFType(clss);
//								ind.removeRDFType(cls); // Move instance from
//														// cls to clss
//								// System.out.println(cls.getBrowserText() +
//								// ": " + cls.getDirectInstanceCount() + ", " +
//								// ind.getRDFType().getBrowserText() + ": " +
//								// ind.getRDFType().getDirectInstanceCount());
//								// ind.addLabel(cls.getBrowserText(),
//								// "PreviousType");
//							}
//						}

						
						
						// Collection c = ind.getComments();
						//
						// for(Object oc : c){
						// String s = oc.toString();
						// s=s.replace("틀:", "");
						// // if(!cls.getBrowserText().equals(key)){
						// // System.out.println(ind.getBrowserText() +
						// ",  in DBpedia : " + cls.getBrowserText() +
						// " ,  GT : " + s + ", Algo Val : " + key);
						// // }
						//
						// if(s.equals(key)){
						// algoEqualGT=algoEqualGT+1.0;
						// }
						// if(s.equals(cls.getBrowserText())){
						// dbpEqualGT=dbpEqualGT+1.0;
						// }
						// if(key.equals(cls.getBrowserText())){
						// algoEqualdbp = algoEqualdbp+1.0;
						// }
						// total = total + 1.0;
						// }

						// Write File
						// String WRITE_FILE_PATH = methodoutputPath;
						// BufferedWriter bw = null;
						// try{
						// File writeFile = new File(WRITE_FILE_PATH);
						// bw = new BufferedWriter(new FileWriter(writeFile,
						// true));
						// bw.write(cls.getBrowserText() + "," +
						// ind.getBrowserText() + "," + hival + "," + key +
						// "\n");
						// bw.close();
						// }catch(Exception e){
						// e.printStackTrace();
						// }

						// System.out.println("Current class : " +
						// cls.getBrowserText() + ",     Class : " +
						// ind.getRDFType() + ",    Instance name : " +
						// ind.getBrowserText() + ",      Biggest value : " +
						// hival + ",        Biggest Key : " + key);
						//
						// End
					} catch (Exception e) {
						 e.printStackTrace();
					}
				}
			} catch (Exception e) {
				 e.printStackTrace();
			}
		}
		count1 = (double) cls.getDirectInstanceCount();
		count2 = (double) cls.getDirectSubclassCount();

		evals.add(count1);
		evals.add(count2);
		evals.add(count3);
		evals.add(count4);

		return evals;
	}

	public static double idfVal(JenaOWLModel owlmodel,
			OWLDatatypeProperty property1, OWLNamedClass class1) {
		double idfvalue = 1;
		double numTotalClass = 1;
		double numPropExistingClass = 1;
		numTotalClass = owlmodel.getDirectSubclassCount(owlmodel
				.getOWLNamedClass("Thing"));
		numPropExistingClass = property1.getDirectDomain().size();
		idfvalue = Math.log10(numTotalClass / numPropExistingClass);

		return idfvalue;
	}

	public static <K, V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(
			Map<K, V> map) {

		List<Entry<K, V>> sortedEntries = new ArrayList<Entry<K, V>>(
				map.entrySet());

		Collections.sort(sortedEntries, new Comparator<Entry<K, V>>() {
			@Override
			public int compare(Entry<K, V> e1, Entry<K, V> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		});

		return sortedEntries;
	}

}
