/**
 * 
 */
package com.knowshare.enterprise.bean.rules.distancias;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowshare.dto.academia.CarreraDTO;
import com.knowshare.dto.perfilusuario.UsuarioDTO;
import com.knowshare.enterprise.bean.rules.utils.OperacionsConjuntos;
import com.knowshare.entities.perfilusuario.Enfasis;
import com.knowshare.enums.TipoRelacionesPersonalidadEnum;

/**
 * {@link DistanciasUsuarioFacade}
 * @author Miguel Montañez
 *
 */
@Component
public class DistanciasUsuarioBean implements DistanciasUsuarioFacade{
	
	@Autowired
	private ApplicationContext ctx;

	@Override
	public double calcularDistanciaEstudianteUsuario(UsuarioDTO usuario1, UsuarioDTO usuario2) {
		double distancia = 0.0;
		distancia += calcularDistanciaPersonalidad(usuario1.getPersonalidad().getNombre(), 
				usuario2.getPersonalidad().getNombre());
		
		distancia += calcularDistanciaCarreras(Arrays.asList(usuario1.getCarrera(),usuario1.getSegundaCarrera()),
				Arrays.asList(usuario2.getCarrera(),usuario2.getSegundaCarrera()));
		
		switch(usuario2.getTipoUsuario()){
			case ESTUDIANTE:
				distancia += calcularDistanciaJaccard(usuario1.getGustos(),usuario2.getGustos());
				break;
			case PROFESOR:
				distancia += calcularDistanciaJaccard(usuario1.getHabilidades(),usuario2.getHabilidades());
				break;
			case EGRESADO:
//				distancia += calcularDistanciaEnfasis(usuario1.getEnfasis(),usuario2.getEnfasis());
//				preferencia de ideas tags (no sé)
				break;
			default:
				break;
		}
		
		return normalizarDistancia(distancia, 3);
	}
	
	@Override
	public double calcularDistanciaProfesorUsuario(UsuarioDTO usuario1, UsuarioDTO usuario2) {
		double distancia = 0.0;
		distancia += calcularDistanciaPersonalidad(usuario1.getPersonalidad().getNombre(), 
				usuario2.getPersonalidad().getNombre());
		
		distancia += calcularDistanciaCarreras(Arrays.asList(usuario1.getCarrera()),
				Arrays.asList(usuario2.getCarrera(),usuario2.getSegundaCarrera()));
		
		switch(usuario2.getTipoUsuario()){
			case ESTUDIANTE:
				distancia += calcularDistanciaJaccard(usuario1.getHabilidades(),usuario2.getHabilidades());
				break;
			case PROFESOR:
				distancia += calcularDistanciaJaccard(usuario1.getAreasConocimiento(),usuario2.getAreasConocimiento());
				break;
			case EGRESADO:
				distancia += calcularDistanciaEnfasis(usuario1.getEnfasis(),usuario2.getEnfasis());
				break;
			default:
				break;
		}
		
		return normalizarDistancia(distancia, 3);
	}
	
	@Override
	public double calcularDistanciaEgresadoUsuario(UsuarioDTO usuario1, UsuarioDTO usuario2) {
		double distancia = 0.0;
		distancia += calcularDistanciaPersonalidad(usuario1.getPersonalidad().getNombre(), 
				usuario2.getPersonalidad().getNombre());
		
		distancia += calcularDistanciaCarreras(Arrays.asList(usuario1.getCarrera(),usuario1.getSegundaCarrera()),
				Arrays.asList(usuario2.getCarrera(),usuario2.getSegundaCarrera()));
		
		switch(usuario2.getTipoUsuario()){
			case ESTUDIANTE:
			case PROFESOR:
				distancia += calcularDistanciaEnfasis(usuario1.getEnfasis(),usuario2.getEnfasis());
				break;
			case EGRESADO:
				if(null != usuario1.getGustos() && null != usuario2.getGustos())
					distancia += calcularDistanciaJaccard(usuario1.getGustos(),usuario2.getGustos());
				break;
			default:
				break;
		}
		
		return normalizarDistancia(distancia, 3);
	}
	
	/**
	 * Cálculo de distancia entre dos personalidades
	 * @param personalidad1
	 * @param personalidad2
	 * @return distancia
	 */
	private double calcularDistanciaPersonalidad(String personalidad1,String personalidad2){
		try {
			TipoRelacionesPersonalidadEnum relacion = 
					relacionPersonalidades(personalidad1, personalidad2);
			if (null != relacion)
				switch(relacion){
					case DI:
						return 0.50;
					case CP:
						return 0.75;
					case OD:
						return 1;
					case EA:
						return 0.25;
					default:
						return 0;
				}
		} catch (Exception e) {
		}
		return 0;
	}
	
	/**
	 * Según las dos personalidades se retorna el tipo de relación que
	 * ambas poseen.
	 * @param personalidad1
	 * @param personalidad2
	 * @return {@link TipoRelacionesPersonalidadEnum}
	 * @throws JsonParseException
	 * @throws JsonMappingException
	 * @throws IOException
	 */
	private TipoRelacionesPersonalidadEnum relacionPersonalidades(
			String personalidad1,
			String personalidad2)
			throws IOException{
		ObjectMapper jsonMapper = new ObjectMapper();
		File jsonFile = ctx.getResource("classpath:personalidades/relaciones.json").getFile();
		JsonNode array = jsonMapper.readValue(jsonFile, JsonNode.class);
		for (JsonNode jsonNode : array) {
			if(jsonNode.get("nombre").asText().equals(personalidad1)){
				JsonNode relaciones = jsonNode.get("relaciones");
				for (JsonNode jsonNode2 : relaciones) {
					if(isInArray(jsonNode2.get("personalidades"),personalidad2))
						return TipoRelacionesPersonalidadEnum
								.valueOfAbr(jsonNode2.get("tipo").asText());
				}
			}else{
				if(jsonNode.get("nombre").asText().equals(personalidad2)){
					JsonNode relaciones = jsonNode.get("relaciones");
					for (JsonNode jsonNode2 : relaciones) {
						if(isInArray(jsonNode2.get("personalidades"),personalidad1))
							return TipoRelacionesPersonalidadEnum
									.valueOfAbr(jsonNode2.get("tipo").asText());
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Calcula la distancia que hay entre varios énfasis de un usuario dado.
	 * @param enfasis1 Lista del usuario actual
	 * @param enfasis2 Lista de la posible conexión
	 * @return distancia entre los énfasis de cada usuario.
	 */
	private double calcularDistanciaEnfasis(List<Enfasis> enfasis1, List<Enfasis> enfasis2){
		List<Enfasis> principales1 = null;
		List<Enfasis> principales2 = null;
		if(enfasis1.size() > 2)
			principales1 = Arrays.asList(enfasis1.get(0),enfasis1.get(2));
		else
			principales1 = Arrays.asList(enfasis1.get(0));
		
		if(enfasis2.size() > 2)
			principales2 = Arrays.asList(enfasis2.get(0),enfasis2.get(2));
		else
			principales2 = Arrays.asList(enfasis2.get(0));
		
		if(!OperacionsConjuntos.interseccion(principales1, principales2).isEmpty())
			return 0;
		List<Enfasis> interseccion = OperacionsConjuntos.interseccion(enfasis1, enfasis2);
		if(!OperacionsConjuntos.interseccion(interseccion, principales2).isEmpty())
			return 0.5;
		return 1;
	}
	
	/**
	 * Método que busca una personalidad dentro de un nodo de un archivo 
	 * JSON
	 * @param array
	 * @param personalidad
	 * @return true si encuentra la personalidad en el array si no, false.
	 */
	private boolean isInArray(JsonNode array, String personalidad){
		for(JsonNode obj: array)
			if(obj.asText().equals(personalidad))
				return true;
		return false;
	}
	
	/**
	 * Cálculo de distancia entre dos carreras.
	 * @param carreras1
	 * @param carreras2
	 * @return distancia de las carreras.
	 */
	private double calcularDistanciaCarreras(List<CarreraDTO> carreras1,List<CarreraDTO> carreras2){
		List<CarreraDTO> interseccion = OperacionsConjuntos
				.interseccion(carreras1, carreras2);
		if(carreras1.get(0).getNombre().equals(carreras2.get(0).getNombre()))
			return 0;
		else if (interseccion.contains(carreras2.get(0)))
			return 0.5;
		else{
			List<String> carrerasAfines = carreras1.get(0).getCarrerasAfines();
			List<String> interseccionAfines = new ArrayList<>();
			for (CarreraDTO carrera : carreras2) {
				if(null != carrera && carrerasAfines.contains(carrera.getNombre()))
					interseccionAfines.add(carrera.getNombre());
			}
			if(!interseccionAfines.isEmpty())
				return 0.5;
		}
		return 1;
	}
	
	/**
	 * Dado dos conjuntos set1 y set2 se operan ambos conjuntos para obtener
	 * la distancia de jaccard.
	 * @param set1
	 * @param set2
	 * @return distancia de jaccard entre set1 y set2
	 */
	private <T> double calcularDistanciaJaccard(List<T> set1, List<T> set2){
		List<T> interseccion = OperacionsConjuntos.interseccion(set1, set2);
		
		List<T> union = OperacionsConjuntos.union(set1, set2);
		return ((union.size() - interseccion.size()) / union.size());
	}
	
	/**
	 * Normaliza la distancia según el parámetro max.
	 * @param distancia a normalizar
	 * @param max 
	 * @return distancia normalizada
	 */
	private double normalizarDistancia(double distancia, int max){
		double min=0;
		double newMax = 1;
		double newMin = 0;
		return ((distancia - min) * (newMax - newMin)/(max - min) + newMin);
	}
}
