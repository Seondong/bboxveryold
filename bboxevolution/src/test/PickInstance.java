package test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.list.SetUniqueList;

import edu.stanford.smi.protegex.owl.jena.JenaOWLModel;


public class PickInstance {
/*
	This class is for sampling subjects from triples in order to validate our results
	Input : Triple files
	Output : Instance subject list
	by Sundong Kim (sundong.kim@kaist.ac.kr)
*/	
	public static void main(String[] args) {
		String triplePath1 = "C:/Users/user/git/bbox/bboxevolution/useful_triples_sorted_filtered.txt";
		String triplePath2 = "C:/Users/user/git/bbox/bboxevolution/populated_triples_1700000_formatted.txt";
		String outputInstancePath = "C:/Users/user/git/bbox/bboxevolution/instancenameenrichedAll.txt";
		int instSize = 20000;
		loadTriples(triplePath1, triplePath2, outputInstancePath, instSize);
	}
	
	public static void loadTriples(String inputPath1, String inputPath2, String outputPath, Integer size){
		List list = new ArrayList();
		
		BufferedReader br = null;
		int count = 0;
		try{
			FileWriter writer = new FileWriter(outputPath);
			
			
			try{
				String sCurrentLine;
				FileInputStream is1 = new FileInputStream(inputPath1);
				FileInputStream is2 = new FileInputStream(inputPath2);
				SequenceInputStream is = new SequenceInputStream(is1, is2);
				br = new BufferedReader(new InputStreamReader(is, "UTF8"));
			
//				while(count <= size){
					while((sCurrentLine = br.readLine()) != null){
//						if(Math.random() < 0.01){
//							System.out.println(sCurrentLine);
							String name = extractSubject(sCurrentLine);
							list.add(name);
							count = count + 1;
						}
//					}
//				}
				
			}catch(IOException e){
				e.printStackTrace();
			}finally{
				try{
					if(br != null){
						br.close();
					}
				}catch(IOException ex){
					ex.printStackTrace();
				}
			}
			
			Set<String> s = new LinkedHashSet<String>(list);
			for(String instName : s){
				writer.append(instName);
				writer.append("\n");
			}
			
			
			
			writer.flush();
			writer.close();
		}catch(IOException e){
			e.printStackTrace();
		}	
	}
	
	
	public static String extractSubject(String s){
		String[] values = s.split("\\t", -1);
		String outName = values[0].replaceAll("http://ko.dbpedia.org/resource/", "");
		outName = outName.replaceAll(":", "_");
		return outName;
	}
	
	

	public static String takeName(String s){
		int wordIdx = s.lastIndexOf("#");
		int wordIdx2 = s.lastIndexOf(")");
//		String name = s.substring(wordIdx + 1,	s.length() - 1);
		String name = s.substring(wordIdx + 1,  wordIdx2 - 1);
		return name;
	}
	
	
	
}
