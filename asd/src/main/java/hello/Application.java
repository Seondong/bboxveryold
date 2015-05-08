package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;



@SpringBootApplication
public class Application {
	
	private static final Logger logger = LoggerFactory.getLogger(Application.class);
	public static void main(String[] args) {
		
//		AnnotationConfigApplicationContext ac = new AnnotationConfigApplicationContext();
//    	for(String name : ac.getBeanFactory().getBeanDefinitionNames()) {
//    		System.out.println(name);
//    	}
        logger.debug("START");
		// TODO Auto-generated method stub
		SpringApplication.run(Application.class, args);
		logger.debug("END");
	}

}
