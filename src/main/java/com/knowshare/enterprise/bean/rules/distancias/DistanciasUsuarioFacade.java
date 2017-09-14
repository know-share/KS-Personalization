/**
 * 
 */
package com.knowshare.enterprise.bean.rules.distancias;

import java.util.List;

import com.knowshare.dto.perfilusuario.UsuarioDTO;
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
}
