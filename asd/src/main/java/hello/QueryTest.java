package hello;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class QueryTest {
	
	public void queryExecute(String service, String query){
		

		
	    QueryExecution qe = QueryExecutionFactory.sparqlService(service, query);
	    try {
	       ResultSet results = qe.execSelect() ;
	       int count = 0; 
	       for ( ; results.hasNext() ; ) {
	           QuerySolution soln = results.nextSolution() ;
//	           RDFNode x = soln.get("s") ;
//	           RDFNode r = soln.get("p") ; 
//	           RDFNode l = soln.get("o") ;
	           count++;
	           System.out.println(soln.toString());
	        
	       }
	    } catch (Exception e) {
	        System.out.println("Query error:"+e);
	    } finally {
	        qe.close();
	    }			
	}
	
	
	
	public static void main(String[] args) {
		
		QueryTest qt = new QueryTest();
		
//		String service = "http://143.248.135.60:3001/sparql";
//		String service = "http://your.virtuososerver.org/sparql";  
//	    String query = "SELECT ?p (COUNT(?p) as ?pCount) WHERE {?s ?p ?o} GROUP BY ?p"
		String service = "http://dmserver5.kaist.ac.kr:8890/sparql";
		
		
//		String service = "http://dbpedia.org/sparql";
		
	    String query1 = "SELECT * WHERE {?s ?p ?o}";
	    String query2 = "PREFIX foaf: <http://xmlns.com/foaf/0.1/>"
	    		+ "SELECT ?name WHERE {?person foaf:name ?name}";
	    String query3 = "PREFIX BBox: <http://bbox.kaist.ac.kr/>"
	    		+ "SELECT * WHERE {?s ?p ?o}";
	    String query4 = "PREFIX leipzig: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/>"
	    		+ "SELECT * WHERE {?s ?p leipzig:Context}";
	    String query5 = "PREFIX foaf:  <http://xmlns.com/foaf/0.1/>PREFIX card: <http://www.w3.org/People/Berners-Lee/card#>SELECT ?homepage FROM <http://dig.csail.mit.edu/2008/webdav/timbl/foaf.rdf>WHERE { card:i foaf:knows ?known .?known foaf:homepage ?homepage .}";
		String query6 = "SELECT DISTINCT ?concept WHERE {?s a ?concept} LIMIT 50";
//		String query7 = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
//				+ "PREFIX type: <http://dbpedia.org/class/yago/"
//				+ "PREFIX prop: <http://dbpedia.org/property"
//				+ "SELECT ?country_name ?population"
//				+ "WHERE {"
//				+ "?country a type:LandlockedCountries;"
//				+ "rdfs:label ?country_name;"
//				+ "prop:populationEstimate ?population."
//				+ "FILTER (?population > 15000000 && langMatches(lang(?country_name), "EN")) ."
//			+"} ORDER BY DESC(?population)";
		String query8 = "SELECT ?s ?p ?o WHERE {?s http://dbpedia.org/property/ ?o}"; 
				
		
	    qt.queryExecute(service, query1);
		
	}
	
	
	
}