/**
 * 
 */
package com.knowshare.test.enterprise.general;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.drools.core.io.impl.UrlResource;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.io.KieResources;
import org.kie.api.runtime.KieContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowshare.enterprise.bean.habilidad.HabilidadBean;
import com.knowshare.enterprise.bean.habilidad.HabilidadFacade;
import com.knowshare.enterprise.bean.habilidad.HabilidadListBean;
import com.knowshare.enterprise.bean.habilidad.HabilidadListFacade;
import com.knowshare.enterprise.bean.habilidad.HabilidadModBean;
import com.knowshare.enterprise.bean.habilidad.HabilidadModFacade;
import com.knowshare.enterprise.bean.rules.RuleFireBean;
import com.knowshare.enterprise.bean.rules.RuleFireFacade;
import com.knowshare.enterprise.bean.rules.distancias.DistanciasUsuarioBean;
import com.knowshare.enterprise.bean.rules.distancias.DistanciasUsuarioFacade;
import com.knowshare.enterprise.bean.rules.usuarios.RecomendacionConexionBean;
import com.knowshare.enterprise.bean.rules.usuarios.RecomendacionConexionFacade;
import com.knowshare.enterprise.bean.usuario.UsuarioBean;
import com.knowshare.enterprise.bean.usuario.UsuarioFacade;
import com.knowshare.enterprise.bean.usuario.UsuarioListBean;
import com.knowshare.enterprise.bean.usuario.UsuarioListFacade;
import com.knowshare.enterprise.bean.usuario.UsuarioModBean;
import com.knowshare.enterprise.bean.usuario.UsuarioModFacade;
import com.knowshare.entities.academia.Carrera;
import com.knowshare.entities.idea.Idea;
import com.knowshare.entities.idea.Tag;
import com.knowshare.entities.perfilusuario.Cualidad;
import com.knowshare.entities.perfilusuario.Gusto;
import com.knowshare.entities.perfilusuario.Habilidad;
import com.knowshare.entities.perfilusuario.Personalidad;
import com.knowshare.entities.perfilusuario.Usuario;
import com.mongodb.MongoClient;

/**
 * Configuración de contexto para las pruebas. Se cargan los bean de negocio que
 * serán necesarios para la ejecución de las pruebas
 * 
 * @author Miguel Montañez
 *
 */
@Lazy
@Configuration
@EnableMongoRepositories(basePackages = { "com.knowshare.enterprise.repository" })
@PropertySource("classpath:test.properties")
public class ConfigContext {
	
	@Autowired
	private Environment env;
	
	@PostConstruct
	public void initData() throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		
		Carrera[] carreras = mapper.readValue(
				ResourceUtils.getURL("classpath:data/carreras.json").openStream(),Carrera[].class);
		Habilidad[] habilidades = mapper.readValue(
				ResourceUtils.getURL("classpath:data/habilidades.json").openStream(),Habilidad[].class);
		Cualidad[] cualidades = mapper.readValue(
				ResourceUtils.getURL("classpath:data/cualidades.json").openStream(),Cualidad[].class);
		Gusto[] gustos = mapper.readValue(
				ResourceUtils.getURL("classpath:data/gustos.json").openStream(),Gusto[].class);
		Personalidad[] personalidades = mapper.readValue(
				ResourceUtils.getURL("classpath:data/personalidades.json").openStream(),Personalidad[].class);
		Tag[] tags = mapper.readValue(
				ResourceUtils.getURL("classpath:data/tags.json").openStream(),Tag[].class);
		Usuario[] usuarios = mapper.readValue(
				ResourceUtils.getURL("classpath:data/usuarios.json").openStream(),Usuario[].class);
		Idea[] ideas = mapper.readValue(
				ResourceUtils.getURL("classpath:data/ideas.json").openStream(),Idea[].class);
		
		this.mongoTemplate().insertAll(Arrays.asList(carreras));
		this.mongoTemplate().insertAll(Arrays.asList(habilidades));
		this.mongoTemplate().insertAll(Arrays.asList(cualidades));
		this.mongoTemplate().insertAll(Arrays.asList(gustos));
		this.mongoTemplate().insertAll(Arrays.asList(personalidades));
		this.mongoTemplate().insertAll(Arrays.asList(tags));
		this.mongoTemplate().insertAll(Arrays.asList(usuarios));
		this.mongoTemplate().insertAll(Arrays.asList(ideas));
		
		String command = "mongodump --host " +env.getProperty("db.host") + " --port " + env.getProperty("db.port")
	            + " -d " + env.getProperty("db.name") +" -o \"./target/\"";
		Runtime.getRuntime().exec(command);
	}
	
	@Bean
    public MongoTemplate mongoTemplate() {
        return new MongoTemplate(
        		new MongoClient(env.getProperty("db.host"),
        						Integer.parseInt(env.getProperty("db.port"))),
        			env.getProperty("db.name"));
    }
	
	/**
	 * Bean encargado de conectarse con el motor de reglas en su creación
	 * @return
	 * @throws IOException
	 */
	@Bean
	public KieContainer kieContainer() throws IOException {
		KieServices ks = KieServices.Factory.get();

		KieResources resources = ks.getResources();
		UrlResource urlResource = (UrlResource) resources.newUrlResource(getRulesPath());
		urlResource.setUsername(env.getProperty("rules.username"));
		urlResource.setPassword(env.getProperty("rules.password"));
		urlResource.setBasicAuthentication("enabled");
		InputStream stream = urlResource.getInputStream();

		KieRepository repo = ks.getRepository();
		KieModule k = repo.addKieModule(resources.newInputStreamResource(stream));

		return ks.newKieContainer(k.getReleaseId());
	}
	
	@Bean
	public RecomendacionConexionFacade getRecomendacionConexionFacade(){
		return new RecomendacionConexionBean();
	}
	
	@Bean
	public RuleFireFacade getRuleFireFacade(){
		return new RuleFireBean();
	}
	
	@Bean
	public DistanciasUsuarioFacade getDistanciasUsuarioFacade(){
		return new DistanciasUsuarioBean();
	}
	
	@Bean
	public UsuarioFacade getUsuarioFacade(){
		return new UsuarioBean();
	}
	
	@Bean
	public UsuarioListFacade getUsuarioListFacade(){
		return new UsuarioListBean();
	}
	
	@Bean
	public UsuarioModFacade getUsuarioModFacade(){
		return new UsuarioModBean();
	}
	
	@Bean
	public HabilidadFacade getHabilidadFacade(){
		return new HabilidadBean();
	}
	
	@Bean
	public HabilidadModFacade getHabilidadModFacade(){
		return new HabilidadModBean();
	}
	
	@Bean HabilidadListFacade getHabilidadListFacade(){
		return new HabilidadListBean();
	}
	
	@PreDestroy
	public void destroy() throws IOException{
		this.mongoTemplate().getDb().dropDatabase();
	}
	
	private String getRulesPath() {
        return env.getProperty("rules.url") + env.getProperty("rules.maven") + env.getProperty("rules.groupId") + "/" +
        		env.getProperty("rules.artifactId") + "/" + env.getProperty("rules.version") +
        		"/" + env.getProperty("rules.artifactId") + "-" + env.getProperty("rules.version") + 
        		".jar";
    }
}
