package hello;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.nio.charset.Charset;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;



@RestController
public class GreetingController {
	private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

//    @Autowired
//    bboxevolutionTripleFromDB processor;
  
    
    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return new Greeting(counter.incrementAndGet(),
                            String.format(template, name));
    }
    
    
    @RequestMapping(value="/request", method = RequestMethod.POST)
    public ResponseEntity<String> doDemo(@RequestHeader("X-AUTH-TOKEN") String authToken) {
    
    	try {
			int[] tids = bboxevolutionTripleFromDB.doDemo(null);
			StringBuffer sb = new StringBuffer("[");
			for(int tid : tids) {
				sb.append(""+tid);
				sb.append(",");
			}
			sb.append("]");
	    	HttpHeaders responseHeaders = new HttpHeaders();
	    	MediaType mediaType = new MediaType("application", "json", Charset.forName("UTF-8"));
	    	responseHeaders.setContentType(mediaType);
			return  new ResponseEntity(sb.toString(), responseHeaders, HttpStatus.OK);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	HttpHeaders responseHeaders = new HttpHeaders();
    	MediaType mediaType = new MediaType("application", "json", Charset.forName("UTF-8"));
    	responseHeaders.setContentType(mediaType);
    	
    	return new ResponseEntity("[]", responseHeaders, HttpStatus.OK);
    }
    
}
