/**
 * 
 */
package com.knowshare.enterprise.bean.rules.distancias;

import java.util.List;
import java.util.Map;

import com.knowshare.dto.perfilusuario.UsuarioDTO;
import com.knowshare.entities.academia.AreaConocimiento;
import com.knowshare.entities.perfilusuario.Enfasis;
import com.knowshare.enums.TipoUsuariosEnum;

/**
 * Lógica para el cálculo de distancias entre los diferentes tipos
 * de usuarios que posee la aplicación.
 * @author Miguel Montañez
 *
 */
public interface DistanciasUsuarioFacade {
	
	/**
	 * Dado dos usuarios y según el tipo (ver {@link TipoUsuariosEnum}) se
	 * calculan las distancias según atributos que constituyen el perfil de cada uno. 
	 * @param usuario1 Usuario actual de tipo ESTUDIANTE
	 * @param usuario2 Usuario que posiblemente es conexión de cualquier tipo
	 * @return distancia entre los dos usuarios
	 */
	double calcularDistanciaEstudianteUsuario(UsuarioDTO usuario1, UsuarioDTO usuario2);
	
	/**
	 * Dado dos usuarios y según el tipo (ver {@link TipoUsuariosEnum}) se
	 * calculan las distancias según atributos que constituyen el perfil de cada uno. 
	 * @param usuario1 Usuario actual de tipo PROFESOR
	 * @param usuario2 Usuario que posiblemente es conexión de cualquier tipo
	 * @return distancia entre los dos usuarios
	 */
	double calcularDistanciaProfesorUsuario(UsuarioDTO usuario1, UsuarioDTO usuario2);
	
	/**
	 * Dado dos usuarios y según el tipo (ver {@link TipoUsuariosEnum}) se
	 * calculan las distancias según atributos que constituyen el perfil de cada uno. 
	 * @param usuario1 Usuario actual de tipo EGRESADO
	 * @param usuario2 Usuario que posiblemente es conexión de cualquier tipo
	 * @return distancia entre los dos usuarios
	 */
	double calcularDistanciaEgresadoUsuario(UsuarioDTO usuario1, UsuarioDTO usuario2);
	
	/**
	 * Dado dos conjuntos set1 y set2 se operan ambos conjuntos para obtener
	 * la distancia de jaccard.
	 * @param set1
	 * @param set2
	 * @return distancia de jaccard entre set1 y set2
	 */
	<T> double calcularDistanciaJaccard(List<T> set1, List<T> set2);
	
	/**
	 * Calcula la distancia entre las preferencias de idea de dos usuarios.
	 * Se arma la matriz con los pesos y con base en eso se calcula
	 * dicha distancia.
	 * @param tags1 preferencias de idea del Usuario1
	 * @param tags2 preferencias de idea del Usuario2
	 * @return distancia entre ambas preferencias
	 */
	double calcularDistanciaPrefIdeaTags(Map<String,Integer> tags1, Map<String,Integer> tags2);
	
	/**
	 * Calcula la distancia que hay entre varios énfasis de un usuario dado.
	 * @param enfasis1 Lista del usuario actual
	 * @param enfasis2 Lista de la posible conexión
	 * @return distancia entre los énfasis de cada usuario.
	 */
	double calcularDistanciaEnfasis(List<Enfasis> enfasis1, List<Enfasis> enfasis2);
	
	/**
	 * Normaliza la distancia según el parámetro max.
	 * @param distancia a normalizar
	 * @param max 
	 * @return distancia normalizada
	 */
	double normalizarDistancia(double distancia, int max);
	/**
	 * Calcula la distancia que hay entre varias areas de conocmiento de un usuario dado.
	 * @param areas1 areas de conocimiento del usuario 1
	 * @param areas2 areas de conocimiento del usuario 2
	 * @return
	 */
	double calcularDistanciaAreasExperticia(List<AreaConocimiento> areas1,List<AreaConocimiento> areas2);
}
