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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de conexion con el motor de reglas.
 * En rulesProperties se encuentran los datos de conexión
 * y de artefactos para traer las reglas en tiempo de 
 * ejecución.
 * @author Miguel Montañez
 *
 */
@Configuration
public class RulesConfig {

	@Autowired
	private RulesProperties rulesProperties;

	@Bean
	public KieContainer kieContainer() throws IOException {
		KieServices ks = KieServices.Factory.get();

		KieResources resources = ks.getResources();
		UrlResource urlResource = (UrlResource) resources.newUrlResource(rulesProperties.getRulesPath());
		urlResource.setUsername(rulesProperties.getUsername());
		urlResource.setPassword(rulesProperties.getPassword());
		urlResource.setBasicAuthentication("enabled");
		InputStream stream = urlResource.getInputStream();

		KieRepository repo = ks.getRepository();
		KieModule k = repo.addKieModule(resources.newInputStreamResource(stream));

		return ks.newKieContainer(k.getReleaseId());
	}

}
