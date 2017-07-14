/**
 * 
 */
package com.knowshare.enterprise.bean.rules;

import java.util.HashMap;
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

	public Object fireRules(Object item) {
		final KieSession kieSession = kieContainer.newKieSession();// .newStatelessKieSession();
		if(item instanceof List){
			List<?> list = (List<?>)item;
			for (Object object : list) {
				kieSession.insert(object);
			}
		}
		kieSession.setGlobal("mapRecomendaciones", new HashMap<String,String>());
		kieSession.fireAllRules();
		return kieSession.getGlobal("mapRecomendaciones");
	}
}
