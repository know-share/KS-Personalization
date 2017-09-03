/**
 * 
 */
package com.knowshare.enterprise.bean.rules.distancias;

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
}
