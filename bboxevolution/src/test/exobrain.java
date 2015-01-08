package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Map.Entry;
import java.net.URLEncoder;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.util.FileUtils;

import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protege.model.DefaultCls;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLDatatypeProperty;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;
import edu.stanford.smi.protegex.owl.model.OWLNamedClass;

public class exobrain {
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

	public static void main(String[] args) throws OntologyLoadException,
			FileNotFoundException, UnsupportedEncodingException, IOException {
		String inputowlfilePath = args[0];
		String instanceCSVfilePath = args[1];
		String outputowlfilePath = args[2];

		// String[] koreanClass = koreanMapping();
		// String[] englishClass = englishMapping();

		JenaOWLModel owlModel = loadExistSchema(inputowlfilePath);
		addTriple(owlModel, instanceCSVfilePath, 0);
		executeLogic(owlModel);
		saveEvolvedSchema(owlModel, outputowlfilePath);

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

	private static JenaOWLModel executeLogic(JenaOWLModel model) {
		OWLNamedClass Thingcls = model.getOWLNamedClass("owl:Thing");
		DefaultCls Thingclss = (DefaultCls) Thingcls;

		// System.out.println("------Now we are finding class depth---------");
		HashMap nodeDepths = new HashMap(); // Hashmap nodeDepths saves Class
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
					System.out.println(cls.getBrowserText() + "   , " + i);
					// Get information of each subclass, and pull up relative
					// property

					// propertyDeletion(cls, i, model);
					// propertyGeneralization(cls, i, model);
				} catch (ClassCastException e) {

				}
			}
		}

		for (int i = j; i >= 2; i--) { // Extract relevant class for instances.
			// for(int i = 1; i<=j; i++){
			Iterator it = getKeysByValue(nodeDepthsNew, i).iterator();
			while (it.hasNext()) {
				String clsName = it.next().toString();
				try {
					OWLNamedClass cls = model.getOWLNamedClass(clsName);
					// System.out.println(cls.getBrowserText() + "   , " + i);

					List<Double> n = secondaryClass(cls, model);
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
	private static JenaOWLModel addTriple(JenaOWLModel model,
			String CSVfilePath, int i) throws FileNotFoundException,
			IOException {
		String path = CSVfilePath;
		File files = new File(path);
		File[] filelist = files.listFiles();
		System.out.println(path);
		System.out.println("Filelist : " + filelist);

		for (int a = 0; a < filelist.length; a++) {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(path + filelist[a].getName()), "UTF8"));
			System.out.println("Filename : " + filelist[a].getName());
			String str;
			Integer numtriple = 0;
			while ((str = br.readLine()) != null) {
				numtriple = numtriple + 1;
				StringTokenizer st = new StringTokenizer(str, "\t");
				String subject;
				String predicate;
				String object;
				try {
					for (int b = 1; b <= st.countTokens(); b++) {
						String s = st.nextToken();
						s = s.replaceAll(" ", "_");
						String p = st.nextToken();
						p = p.replaceAll(" ", "_");
						String o = st.nextToken();
						o = o.replaceAll(" ", "_");
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
						// System.out.println(s + ", " + p + ", " + o);
						OWLNamedClass cls = model.getOWLNamedClass(type);

						if (cls == null) {
							cls = model.createOWLNamedClass(type);
							// cls.addLabel(type+"_generated" , "New" );
						}

						OWLIndividual individual;
						OWLDatatypeProperty property;
						try {
							individual = model.getOWLIndividual(s);
						} catch (ClassCastException e) {
							System.out.println(e.getMessage());
							individual = model.getOWLIndividual(s
									+ "_individual");
						}

						property = model.getOWLDatatypeProperty(p);

						if (individual == null) {
							try {
								individual = cls.createOWLIndividual(s);
							} catch (IllegalArgumentException e) {
								System.out.println(e.getMessage());
								individual = cls.createOWLIndividual(s
										+ "_individual");
							}
						}
						try {
							if (property == null) {
								property = model.createOWLDatatypeProperty(p);
							}
							individual.addPropertyValue(property, o);

						} catch (IllegalArgumentException e) {
							System.out.println(e.getMessage());
						}

						relatedIndividualList.add(individual);
						relatedPropertyList.add(property);
						if(!relatedIndividualMap.containsValue(individual)){
							relatedIndividualMap.put(cls, individual);
						}
					}
				} catch (Exception e) {

				}
			}
		}
		System.out.println("Number of new individual : "
				+ relatedIndividualNameList.size());
		System.out.println("Number of new property : "
				+ relatedPropertyNameList.size());
		return model;
	}

	private static void FindnodeDepth(JenaOWLModel owlModel,
			HashMap nodeDepths, List<String> sequence, DefaultCls rootClass,
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

		//Collection a = cls.getDirectInstances();
		
		Collection<OWLIndividual> a = relatedIndividualMap.get(cls);

		if (a.size() > 0) {
			Iterator it2 = a.iterator();

			try {
				while (it2.hasNext()) {
					Object o = it2.next();
					try {
						OWLIndividual ind = (OWLIndividual) o;
						HashMap<String, Integer> h = new HashMap<String, Integer>();

//						System.out.println("Class : " + cls.getBrowserText()  + ",     Instance : " + ind.getBrowserText());
						Collection instanceprop = ind.getRDFProperties();
						Iterator it3 = instanceprop.iterator();
//						System.out.println("inst-prop size : " + instanceprop.size());

						for (Object oal : instanceprop) {
							String s = oal.toString();
//							System.out.println(s);
							int wordIdx = s.lastIndexOf("#");
							String propname = s.substring(wordIdx + 1,
									s.length() - 1);
							if(propname.contains("/")){
								int wordIdx2 = s.lastIndexOf("/");
								propname = s.substring(wordIdx2 + 1,
										s.length() - 1);
							}
//							System.out.println(propname);
							
							try {
								if (!propname.equals("wikiPageUsesTemplate")
										&& !propname.equals("type")) {
									OWLDatatypeProperty prop = owlModel.getOWLDatatypeProperty(propname);
									Collection c = prop.getDirectDomain();
									Iterator it = c.iterator();
									try {
//										System.out.println(c.size() +", " + prop);
										while (it.hasNext()) {
											Object ooo = it.next();
											String ss = ooo.toString();
											OWLNamedClass ooocls = (OWLNamedClass) ooo;
											Collection oooclssub = ooocls
													.getSubclasses();

											// Property's Domain and its
											// frequency - ������� �������
											// Domain�� �ι�_����
											if (!h.containsKey(ss)) {
												h.put(ss, 1);
											} else {
												h.put(ss, h.get(ss) + 1);
											}

											// Property's All subDomain and its
											// frequency �ι�_������ subdomain�鵵
											// ��� �߰�
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
										e.printStackTrace();
									}

								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						int hival = 0;
						String key = "";

						List<String> indName = new ArrayList<String>();
						List<String> beforeClsName = new ArrayList<String>();
						List<String> newClsName = new ArrayList<String>();
						List<String> gtName = new ArrayList<String>();

						for (Entry<String, Integer> e : h.entrySet()) {

							System.out.println(ind.getBrowserText() + "," + e.getValue() + "," + e.getKey());
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
						System.out.println("Current class : "
								+ cls.getBrowserText() + ", Instance name : "
								+ ind.getBrowserText() + ", Domain Name : "
								+ key + ", Frequency : " + hival);

						if (key.equals(cls.getBrowserText())) {
							count3 = count3 + 1.0;
						}

						OWLNamedClass clss = owlModel.getOWLNamedClass(key);
						if (clss.equals(null)) {
							clss = owlModel.getOWLNamedClass("Thing");
						}
						Collection comments = ind.getComments();

						if (!comments.contains("Seed")) { // seed instance�� �ƴ�
															// �͸� �ٲٱ�
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
						// s=s.replace("Ʋ:", "");
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

}
