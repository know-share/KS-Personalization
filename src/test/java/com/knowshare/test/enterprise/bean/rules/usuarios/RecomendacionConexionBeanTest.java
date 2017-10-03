/**
 * 
 */
package com.knowshare.test.enterprise.bean.rules.usuarios;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.util.ResourceUtils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowshare.dto.rules.RecomendacionDTO;
import com.knowshare.enterprise.bean.rules.usuarios.RecomendacionConexionFacade;
import com.knowshare.enterprise.utils.MapEntities;
import com.knowshare.entities.perfilusuario.Usuario;
import com.knowshare.fact.rules.TipoConexionEnum;
import com.knowshare.test.enterprise.general.AbstractTest;

/**
 * @author Miguel Montañez
 *
 */
public class RecomendacionConexionBeanTest extends AbstractTest{
	
	@Autowired
	private RecomendacionConexionFacade recomendacionBean;
	
	private Map<String,HashMap<TipoConexionEnum, List<String>>> conexiones;
	
	@Before
	@SuppressWarnings("unchecked")
	public void setup()throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper mapper = new ObjectMapper();
		conexiones = mapper.readValue(
				ResourceUtils.getURL("classpath:recomendaciones/lista_recomendaciones.json").openStream(),HashMap.class);
	}
	
	/**
	 * Get the set of recomendations of an user of type
	 * Estudiante.
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void setDeRecomendacionesTestEstudiantes() {
		Usuario estudiante = null;
		List<RecomendacionDTO> recomendaciones = null;
		
		for(int i = 1;i < 11;i++){
			estudiante = mongoTemplate.findOne(new Query(Criteria.where("username").is("Estudiante "+i)), Usuario.class);
			assertNotNull(estudiante);
			
			recomendaciones = (List<RecomendacionDTO>) recomendacionBean.setDeRecomendaciones(MapEntities.mapUsuarioToDTO(estudiante));
			assertConexiones(estudiante.getUsername(), recomendaciones);
		}
	}
	
	/**
	 * Get the set of recomendations of an user of type
	 * Profesor.
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void setDeRecomendacionesTestProfesores() {
		Usuario estudiante = null;
		List<RecomendacionDTO> recomendaciones = null;
		
		for(int i = 1;i < 9;i++){
			estudiante = mongoTemplate.findOne(new Query(Criteria.where("username").is("Profesor "+i)), Usuario.class);
			assertNotNull(estudiante);
			
			recomendaciones = (List<RecomendacionDTO>) recomendacionBean.setDeRecomendaciones(MapEntities.mapUsuarioToDTO(estudiante));
			assertConexiones(estudiante.getUsername(), recomendaciones);
		}
	}
	
	/**
	 * Get the set of recomendations of an user of type
	 * Egresado.
	 * @throws IOException 
	 * @throws JsonMappingException 
	 * @throws JsonGenerationException 
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void setDeRecomendacionesTestEgresados(){
		Usuario estudiante = null;
		List<RecomendacionDTO> recomendaciones = null;
		
		for(int i = 1;i < 7;i++){
			estudiante = mongoTemplate.findOne(new Query(Criteria.where("username").is("Egresado "+i)), Usuario.class);
			assertNotNull(estudiante);
			
			recomendaciones = (List<RecomendacionDTO>) recomendacionBean.setDeRecomendaciones(MapEntities.mapUsuarioToDTO(estudiante));
			assertConexiones(estudiante.getUsername(), recomendaciones);
		}
	}
	
	private void assertConexiones(String username, List<RecomendacionDTO> recomendaciones) {
		final Map<TipoConexionEnum,List<String>> recomendacionesMap = conexiones.get(username);
		final List<String> listUsuariosConfianza = recomendacionesMap.get(TipoConexionEnum.CONFIANZA.name());
		final List<String> listUsuariosRelevante = recomendacionesMap.get(TipoConexionEnum.RELEVANTE.name());
		final List<String> listUsuariosNoRecomendar = recomendacionesMap.get(TipoConexionEnum.NO_RECOMENDAR.name());
		
		assertNotNull(listUsuariosConfianza);
		assertNotNull(listUsuariosRelevante);
		assertNotNull(listUsuariosNoRecomendar);
		assertNotNull(recomendaciones);
		for(RecomendacionDTO dto : recomendaciones){
			switch(dto.getConexion()){
				case CONFIANZA:
					assertTrue(listUsuariosConfianza.contains(dto.getUsername()));
					break;
				case RELEVANTE:
					assertTrue(listUsuariosRelevante.contains(dto.getUsername()));
					break;
				case NO_RECOMENDAR:
					assertTrue(listUsuariosNoRecomendar.contains(dto.getUsername()));
					break;
			}
		}
	}
}
