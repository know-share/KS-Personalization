/**
 * 
 */
package com.knowshare.enterprise.bean.rules.config;

import java.io.IOException;
import java.io.InputStream;

import org.drools.core.io.impl.UrlResource;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.io.KieResources;
import org.kie.api.runtime.KieContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * {@link RulesAdminFacade}
 * @author miguel
 *
 */
@Component
public class RulesAdminBean implements RulesAdminFacade {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private RulesProperties rulesProperties;
	
	@Autowired
	private KieContainer kieContainer;

	@Override
	public boolean updateRules() {
		try {
			KieServices ks = KieServices.Factory.get();

			KieResources resources = ks.getResources();
			UrlResource urlResource = (UrlResource) resources.newUrlResource(rulesProperties.getRulesPath());
			urlResource.setUsername(rulesProperties.getUsername());
			urlResource.setPassword(rulesProperties.getPassword());
			urlResource.setBasicAuthentication("enabled");
			InputStream stream = urlResource.getInputStream();
			
			KieRepository repo = ks.getRepository();
			KieModule k = repo.addKieModule(resources.newInputStreamResource(stream));
			kieContainer.updateToVersion(k.getReleaseId());
			return true;
		} catch (IOException e) {
			logger.error("::::: Error actualizando reglas del repositorio. Error: " + e.getMessage() + ":::::");
			return false;
		}
	}

}
