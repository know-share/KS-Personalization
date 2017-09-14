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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.knowshare.entities.app.SystemPreferences;

/**
 * {@link RulesAdminFacade}
 * @author Miguel Montañez
 *
 */
@Component
public class RulesAdminBean implements RulesAdminFacade {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private RulesProperties rulesProperties;
	
	@Autowired
	private KieContainer kieContainer;
	
	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public boolean updateRules() {
		try {
			logger.info("Synchronizing with rule engine");
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
			logger.info("Rules: Synchronization complete.");
			return true;
		} catch (IOException e) {
			logger.error("::::: Error actualizando reglas del repositorio. Error: " + e.getMessage() + ":::::");
			return false;
		}
	}
	
	public boolean updateRulesSystem(short state){
		logger.info("Updating use of rules in system with state: {}",state);
		final Update update = new Update()
				.set("rules", state);
		return mongoTemplate.updateFirst(new Query(Criteria.where("_id").is("system")), update, SystemPreferences.class)
				.getN() > 0;
	}
	
	public boolean isRulesOn(){
		final SystemPreferences preferences = mongoTemplate
				.findOne(new Query(Criteria.where("_id").is("system")), SystemPreferences.class);
		return preferences != null && preferences.getRules() == 1;
	}

}
