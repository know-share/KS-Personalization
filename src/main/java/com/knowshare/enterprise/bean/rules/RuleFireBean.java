/**
 * 
 */
package com.knowshare.enterprise.bean.rules;

import java.util.List;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author miguel
 *
 */
@Component
public class RuleFireBean implements RuleFireFacade{

	@Autowired
	private KieContainer kieContainer;

	@SuppressWarnings("unchecked")
	public <T> T fireRules(Object item,String global,T clazz) {
		final KieSession kieSession = kieContainer.newKieSession();
		if(item instanceof List){
			List<?> list = (List<?>)item;
			for (Object object : list) {
				kieSession.insert(object);
			}
		}
		kieSession.setGlobal(global, clazz);
		kieSession.fireAllRules();
		return (T)kieSession.getGlobal(global);
	}
}
