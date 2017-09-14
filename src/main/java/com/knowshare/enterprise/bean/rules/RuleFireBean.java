/**
 * 
 */
package com.knowshare.enterprise.bean.rules;

import java.util.List;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link RuleFireFacade}
 * @author Miguel Montañez
 *
 */
@Component
public class RuleFireBean implements RuleFireFacade{
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private KieContainer kieContainer;

	@SuppressWarnings("unchecked")
	public <T> T fireRules(Object item,String global,T clazz) {
		logger.info("Fire rule with global {}.",global);
		final KieSession kieSession = kieContainer.newKieSession();
		if(item instanceof List){
			List<?> list = (List<?>)item;
			for (Object object : list) {
				kieSession.insert(object);
			}
		}
		kieSession.setGlobal(global, clazz);
		kieSession.fireAllRules();
		logger.info("Successful called to drools engine.",global);
		return (T)kieSession.getGlobal(global);
	}
}
