/**
 * 
 */
package com.knowshare.enterprise.bean.rules.distancias;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import com.knowshare.entities.academia.AreaConocimiento;
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
				distancia += calcularDistanciaPrefIdeaTags(usuario1.getPreferenciaIdeasTag(),usuario2.getPreferenciaIdeasTag());
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
	
	public double calcularDistanciaPrefIdeaTags(Map<String,Integer> tags1, Map<String,Integer> tags2){
		int size = Double.valueOf(Math.pow(2, tags1.size())).intValue() - 1;
		final ArrayList<ArrayList<Integer>> matrizBinaria = generarMatrizBinaria(tags1.size(),size);
		double pesoTotal = 0d;
		final List<String> tagsRelacionados = OperacionsConjuntos.interseccion(tags1.keySet(), tags2.keySet());
		if( matrizBinaria != null && !tagsRelacionados.isEmpty()){
			int numCol = tags1.size();
			llenarMatrizConPesos(matrizBinaria, tags1.values(), numCol);
			final ArrayList<Integer> vectorPesos = sumarPesosPorFilas(matrizBinaria, numCol);
			final ArrayList<Integer> cuantosPesosUnicos = obtenerNumerosUnicos(vectorPesos);
			final ArrayList<ArrayList<Double>> tuplaNormalizada = 
					normalizarPesos(cuantosPesosUnicos,cuantosPesosUnicos.size());
			for(String id: tagsRelacionados)
				pesoTotal += tags2.get(id);
			pesoTotal *= tagsRelacionados.size();
			for(int i = 0; i < cuantosPesosUnicos.size() ; i++){
				if(tuplaNormalizada.get(i).get(0) == pesoTotal)
					return tuplaNormalizada.get(i).get(1);
			}
		}
		return 1;
	}
	
	public double calcularDistanciaAreasExperticia(List<AreaConocimiento> areas1,List<AreaConocimiento> areas2){
		int size = Double.valueOf(Math.pow(2, areas1.size())).intValue() - 1;
		final ArrayList<ArrayList<Double>> matrizBinaria = generarMatrizBinariaAreasConocimiento(areas1.size(),size);
		double pesoTotal = 0;
		List<Double> porcentajes = new ArrayList<>();
		List<AreaConocimiento> areasRelacionadas = OperacionsConjuntos.interseccion(areas1, areas2);
		if(null != matrizBinaria && !areasRelacionadas.isEmpty()){
			for (AreaConocimiento areaConocimiento : areas2) {
				porcentajes.add(areaConocimiento.getPorcentaje());
			}
			llenarMatrizConPesosAreasConocimiento(matrizBinaria, porcentajes, areas2.size());
			final ArrayList<Double> vectorPesos = sumarPesosPorFilasAreasConocimiento(matrizBinaria, areas2.size());
			final ArrayList<Double> cuantosPesosUnicos = obtenerNumerosUnicos(vectorPesos);
			final ArrayList<ArrayList<Double>> tuplaNormalizada = 
					normalizarPesos(cuantosPesosUnicos,cuantosPesosUnicos.size());
			for (AreaConocimiento area : areasRelacionadas) {
				pesoTotal += area.getPorcentaje();	
			}
			pesoTotal = pesoTotal * areasRelacionadas.size();
			for(int i = 0; i < cuantosPesosUnicos.size() ; i++){
				if(tuplaNormalizada.get(i).get(0) == pesoTotal)
					return tuplaNormalizada.get(i).get(1);
			}
		}
		return 1;
	}
	
	/**
	 * Genera la matriz binaria del 0 hasta size con Doubles.
	 * @param columns número de columnas que tendrá la matriz.
	 * @param size número de filas que tendrá la matriz
	 * @return Una matriz con los números binarios.
	 */
	private ArrayList<ArrayList<Double>> generarMatrizBinariaAreasConocimiento(int columns, int size){
		if( columns == 0 )
			return null;
		ArrayList<ArrayList<Double>> matrizBinaria = new ArrayList<>();
		for(int i = 0; i <= size;i++){
			ArrayList<Double> row = new ArrayList<>();
			for(int j = 0; j < columns ; j++){
				int pow = Double.valueOf(Math.pow(2, j)).intValue();
				int result = (i&pow) == pow?1:0;
				row.add(0, new Double(result));
			}
			matrizBinaria.add(row);
		}
		return matrizBinaria;
	}
	
	/**
	 * Genera la matriz binaria del 0 hasta size.
	 * @param columns número de columnas que tendrá la matriz.
	 * @param size número de filas que tendrá la matriz
	 * @return Una matriz con los números binarios.
	 */
	private ArrayList<ArrayList<Integer>> generarMatrizBinaria(int columns, int size){
		if( columns == 0 )
			return null;
		ArrayList<ArrayList<Integer>> matrizBinaria = new ArrayList<>();
		for(int i = 0; i <= size;i++){
			ArrayList<Integer> row = new ArrayList<>();
			for(int j = 0; j < columns ; j++){
				int pow = Double.valueOf(Math.pow(2, j)).intValue();
				int result = (i&pow) == pow?1:0;
				row.add(0, result);
			}
			matrizBinaria.add(row);
		}
		return matrizBinaria;
	}
	
	/**
	 * Se encarga de multiplicar la matriz por los pesos de
	 * los tags.
	 * @param matrizBinaria
	 * @param atributoConPeso
	 * @param numCol
	 */
	private void llenarMatrizConPesos(
			ArrayList<ArrayList<Integer>> matrizBinaria,
			Collection<Integer> atributoConPeso,
			int numCol
		){
		Integer[] pesos = atributoConPeso.toArray(new Integer[0]);
		for(int i = 0;i < Math.pow(2, numCol); i++){
			for(int j = 0; j < numCol ; j++){
				matrizBinaria.get(i).set(j, matrizBinaria.get(i).get(j)*pesos[j]);
			}
		}
	}
	
	/**
	 * Se encarga de multiplicar la matriz por los porcentajes de
	 * las areas de conocimiento.
	 * @param matrizBinaria
	 * @param atributoConPeso
	 * @param numCol
	 */
	private void llenarMatrizConPesosAreasConocimiento(
			ArrayList<ArrayList<Double>> matrizBinaria,
			Collection<Double> atributoConPeso,
			int numCol
		){
		Double[] pesos = atributoConPeso.toArray(new Double[0]);
		for(int i = 0;i < Math.pow(2, numCol); i++){
			for(int j = 0; j < numCol ; j++){
				matrizBinaria.get(i).set(j, matrizBinaria.get(i).get(j)*pesos[j]);
			}
		}
	}
	
	/**
	 * Suma las filas para obtener un vector con los pesos
	 * totales en Double.
	 * @param matrizBinaria
	 * @param numCol
	 * @return Vector con los pesos totales por filas.
	 */
	private ArrayList<Double> sumarPesosPorFilasAreasConocimiento(
			ArrayList<ArrayList<Double>> matrizBinaria,
			int numCol
		){
		final ArrayList<Double> vectorPesos = new ArrayList<>();
		for(int i = 0; i < Math.pow(2, numCol) ; i++){
			int sum = 0;
			double contUnos = 0;
			for(int j = 0; j < numCol ; j++){
				if(matrizBinaria.get(i).get(j) != 0)
					contUnos++;
				sum += matrizBinaria.get(i).get(j);
			}
			vectorPesos.add(sum * contUnos);
		}
		return vectorPesos;
	}
	
	/**
	 * Suma las filas para obtener un vector con los pesos
	 * totales.
	 * @param matrizBinaria
	 * @param numCol
	 * @return Vector con los pesos totales por filas.
	 */
	private ArrayList<Integer> sumarPesosPorFilas(
			ArrayList<ArrayList<Integer>> matrizBinaria,
			int numCol
		){
		final ArrayList<Integer> vectorPesos = new ArrayList<>();
		for(int i = 0; i < Math.pow(2, numCol) ; i++){
			int sum = 0;
			int contUnos = 0;
			for(int j = 0; j < numCol ; j++){
				if(matrizBinaria.get(i).get(j) != 0)
					contUnos++;
				sum += matrizBinaria.get(i).get(j);
			}
			vectorPesos.add(sum * contUnos);
		}
		return vectorPesos;
	}
	
	/**
	 * Crea un nuevo vector con valores únicos y la ordena de forma
	 * descendente.
	 * @param vectorPesos
	 * @return Vector ordenado de forma descendente.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T extends Comparable> ArrayList<T> obtenerNumerosUnicos(ArrayList<T> vectorPesos){
		ArrayList<T> unicos = new ArrayList<>();
		for(T i: vectorPesos)
			if(!unicos.contains(i))
				unicos.add(i);
		Collections.sort(unicos);
		Collections.reverse(unicos);
		return unicos;
	}
	
	/**
	 * Se obtiene el vector con las distancias según el peso,
	 * donde el mayor del vector de pesos representa la distancia
	 * más cercana (0), y el menor peso representa la más lejana
	 * (1)
	 * @param vectorPesos
	 * @param cuantosPesosUnicos
	 * @return Tupla normalizada
	 */
	private <T extends Number> ArrayList<ArrayList<Double>> normalizarPesos(ArrayList<T> vectorPesos, int cuantosPesosUnicos){
		final ArrayList<ArrayList<Double>> tuplaNormalizada = new ArrayList<>();
		double normalizacion = 1d/(cuantosPesosUnicos - 1);
		double aumento = normalizacion;
		for(int i = 0; i < cuantosPesosUnicos;i++){
			ArrayList<Double> row = new ArrayList<>();
			row.add(vectorPesos.get(i).doubleValue());
			if(i==0)
				row.add(0d);
			else if(i == (cuantosPesosUnicos - 1))
				row.add(1d);
			else
				row.add(normalizacion);
			normalizacion += aumento;
			tuplaNormalizada.add(row);
		}
		return tuplaNormalizada;
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
	
	public double calcularDistanciaEnfasis(List<Enfasis> enfasis1, List<Enfasis> enfasis2){
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
	
	public <T> double calcularDistanciaJaccard(List<T> set1, List<T> set2){
		List<T> interseccion = OperacionsConjuntos.interseccion(set1, set2);
		
		List<T> union = OperacionsConjuntos.union(set1, set2);
		return ((union.size() - interseccion.size()) / union.size());
	}
	
	public double normalizarDistancia(double distancia, int max){
		double min=0;
		double newMax = 1;
		double newMin = 0;
		return ((distancia - min) * (newMax - newMin)/(max - min) + newMin);
	}
}
