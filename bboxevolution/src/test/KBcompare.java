package test;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import edu.stanford.smi.protege.exception.OntologyLoadException;
import edu.stanford.smi.protegex.owl.ProtegeOWL;
import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;
import edu.stanford.smi.protegex.owl.model.OWLIndividual;

public class KBcompare {
/*
	This class is for comparing type results of sampled instances.
	Input : Instance subject list, Knowledge bases
	Output : Type of those instances depending on different knowledge base versions
	by Sundong Kim (sundong.kim@kaist.ac.kr)
*/	
	public static void main(String[] args) throws OntologyLoadException,
			FileNotFoundException, UnsupportedEncodingException, IOException {
	
		String instanceNamePath = "C:/Users/user/git/bbox/bboxevolution/instancenameenrichedAll.txt";
		String outputCSVPath = "C:/Users/user/git/bbox/bboxevolution/outputresultenrichedAll.tsv";
		
		BufferedReader br = null;
		try{
			String a;
			br = new BufferedReader(new FileReader(instanceNamePath));
			while((a = br.readLine()) != null){
				System.out.println(a);
			}
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try{
				if(br!=null)br.close();
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}
	
		String inputowlfilePath1 = "C:/Users/user/git/bbox/bboxevolution/output_kor_output.owl";
		String inputowlfilePath2 = "C:/Users/user/git/bbox/bboxevolution/evolvedoldenriched.owl";
		String inputowlfilePath3 = "C:/Users/user/git/bbox/bboxevolution/evolvednewenriched.owl";
	
		JenaOWLModel kb1 = loadExistSchema(inputowlfilePath1);  //original KB
		JenaOWLModel kb2 = loadExistSchema(inputowlfilePath2);  //evolved KB - 1st year algorithm
		JenaOWLModel kb3 = loadExistSchema(inputowlfilePath3);  //evolved KB - 2nd year algorithm
		
		compareKBs(kb1, kb2, kb3, instanceNamePath, outputCSVPath);
		
//		addTriple(owlModel, instanceCSVfilePath, 0);
//		executeLogic(owlModel);
//		saveEvolvedSchema(owlModel, outputowlfilePath);
	}

  
	private static JenaOWLModel loadExistSchema(String filePath)
			throws OntologyLoadException {
		// String uri = "file:///"+owlfilePath;
		String uri = "file:///" + filePath;
		JenaOWLModel model = ProtegeOWL.createJenaOWLModelFromURI(uri);

		return model;
	}
	
	
	public static void compareKBs(JenaOWLModel kb1, JenaOWLModel kb2, JenaOWLModel kb3, String Path, String OutputPath){
		
		try{
			FileWriter writer = new FileWriter(OutputPath);
			
			writer.append("Instance Name"+'\t'+"Type Name(Before)"+'\t'+"Type Name(1st)"+'\t'+"Type Name(2nd)"+'\n');
			
			
			BufferedReader br = null;
			try{
				String a;
				br = new BufferedReader(new FileReader(Path));
				while((a = br.readLine()) != null){
					System.out.println(a);
					writer.append(a + "\t");
					String a1, a2, a3;
					try{
						OWLIndividual ainst1 = kb1.getOWLIndividual(a);
						System.out.println(ainst1);
						System.out.println(a1 = takeName(ainst1.getRDFTypes().toString()));
						writer.append(a1 + "\t");
					}catch(Exception e){
						e.printStackTrace();
						writer.append("null" + "\t");
					}try{
						OWLIndividual ainst2 = kb2.getOWLIndividual(a);
						System.out.println(ainst2);
						System.out.println(a2 = takeName(ainst2.getRDFTypes().toString()));
						writer.append(a2 + "\t");
					}catch(Exception e){
						e.printStackTrace();
						writer.append("null" + "\t");
					}try{
						OWLIndividual ainst3 = kb3.getOWLIndividual(a);
						System.out.println(ainst3);
						System.out.println(a3 = takeName(ainst3.getRDFTypes().toString()));
						writer.append(a3);
					}catch(Exception e){
						e.printStackTrace();
						writer.append("null");
					}
					
				  writer.append('\n');
					
				}
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				try{
					if(br!=null)br.close();
				}catch(IOException ex){
					ex.printStackTrace();
				}
			}
			
			
			writer.flush();
			writer.close();
			
		}catch(IOException e){
			e.printStackTrace();
		}		
	}
	



	public static String takeName(String s){
		int wordIdx = s.lastIndexOf("#");
		int wordIdx2 = s.lastIndexOf(")");
//		String name = s.substring(wordIdx + 1,	s.length() - 1);
		String name = s.substring(wordIdx + 1,  wordIdx2);
		return name;
	}
	
	
}
	

