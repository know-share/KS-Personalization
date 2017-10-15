/**
 * 
 */
package com.knowshare.test.enterprise.general;

import java.io.IOException;

import javax.annotation.PostConstruct;

import org.junit.AfterClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

/**
 * 
 * @author Miguel Montañez
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ConfigContext.class, loader = AnnotationConfigContextLoader.class)
@PropertySource("classpath:test.properties")
public abstract class AbstractTest {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	protected MongoTemplate mongoTemplate;
	
	private static MongoTemplate mongoTemplateStatic;
	
	@Autowired
	protected Environment env;
	
	private static Environment envStatic;
	
	@PostConstruct
	private void init(){
		envStatic = this.env;
		mongoTemplateStatic = this.mongoTemplate;
	}
	
	@AfterClass
	public static void tearDown() throws IOException {
		mongoTemplateStatic.getDb().dropDatabase();
		String command = "mongorestore --host " +envStatic.getProperty("db.host") + " --port " + envStatic.getProperty("db.port")
        	+ " -d " + envStatic.getProperty("db.name") +" ./target/"+envStatic.getProperty("db.name");
		Process p = Runtime.getRuntime().exec(command);
		while(p.isAlive());
	}
}
