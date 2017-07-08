/**
 * 
 */
package com.knowshare.enterprise.bean.rules.distancias;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knowshare.dto.academia.CarreraDTO;
import com.knowshare.dto.perfilusuario.UsuarioDTO;
import com.knowshare.enterprise.bean.rules.utils.OperacionsConjuntos;
import com.knowshare.enums.TipoRelacionesPersonalidadEnum;

/**
 * @author miguel
 *
 */
@Component
public class DistanciasUsuarioBean implements DistanciasUsuarioFacade{

	@Override
	public double calcularDistanciaEntreEstudiantes(UsuarioDTO usuario1, UsuarioDTO usuario2) {
		double distancia = 0.0;
		distancia += calcularDistanciaPersonalidad(usuario1.getPersonalidad().getNombre(), 
				usuario2.getPersonalidad().getNombre());
		
		distancia += calcularDistanciaCarreras(Arrays.asList(usuario1.getCarrera(),usuario1.getSegundaCarrera()),
				Arrays.asList(usuario2.getCarrera(),usuario2.getSegundaCarrera()));
		
		distancia += calcularDistanciaJaccard(usuario1.getGustos(),usuario2.getGustos());
		
		return normalizarDistancia(distancia, 3);
	}
	
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
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
	
	private TipoRelacionesPersonalidadEnum relacionPersonalidades(
			String personalidad1,
			String personalidad2)
			throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper jsonMapper = new ObjectMapper();
		File jsonFile = new File("classpath:personalidades/relaciones.json");
		JsonNode array = jsonMapper.readValue(jsonFile, JsonNode.class);
		for (JsonNode jsonNode : array) {
			if(jsonNode.get("nombre").asText().equals(personalidad1)){
				JsonNode relaciones = jsonNode.get("relaciones");
				for (JsonNode jsonNode2 : relaciones) {
					List<String> relacionesStr = jsonNode2.get("personalidades")
							.findValuesAsText(personalidad2);
					if(!relacionesStr.isEmpty())
						return TipoRelacionesPersonalidadEnum
								.valueOfAbr(jsonNode2.get("tipo").asText());
				}
			}else{
				if(jsonNode.get("nombre").asText().equals(personalidad2)){
					JsonNode relaciones = jsonNode.get("relaciones");
					for (JsonNode jsonNode2 : relaciones) {
						List<String> relacionesStr = jsonNode2.get("personalidades")
								.findValuesAsText(personalidad1);
						if(!relacionesStr.isEmpty())
							return TipoRelacionesPersonalidadEnum
									.valueOfAbr(jsonNode2.get("tipo").asText());
					}
				}
			}
		}
		return null;
	}
	
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
	
	private <T> double calcularDistanciaJaccard(List<T> set1, List<T> set2){
		List<T> interseccion = OperacionsConjuntos.interseccion(set1, set2);
		
		List<T> union = OperacionsConjuntos.union(set1, set2);
		return ((union.size() - interseccion.size()) / union.size());
	}
	
	private double normalizarDistancia(double distancia, int max){
		double min=0;
		double newMax = 1;
		double newMin = 0;
		return ((distancia - min) * (newMax - newMin)/(max - min) + newMin);
	}
}
