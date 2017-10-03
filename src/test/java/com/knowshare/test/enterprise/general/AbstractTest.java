/**
 * 
 */
package com.knowshare.test.enterprise.general;

import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
public abstract class AbstractTest {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	protected MongoTemplate mongoTemplate;
}
