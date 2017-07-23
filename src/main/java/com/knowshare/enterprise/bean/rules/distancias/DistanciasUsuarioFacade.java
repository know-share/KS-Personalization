/**
 * 
 */
package com.knowshare.enterprise.bean.rules.distancias;

import com.knowshare.dto.perfilusuario.UsuarioDTO;
import com.knowshare.enums.TipoUsuariosEnum;

/**
 * Lógica para el cálculo de distancias entre los diferentes tipos
 * de usuarios que posee la aplicación.
 * @author miguel
 *
 */
public interface DistanciasUsuarioFacade {
	
	/**
	 * Dado dos usuarios de tipo estudiante (ver {@link TipoUsuariosEnum}) se
	 * calculan las distancias según atributos que constituyen el perfil de cada uno. 
	 * @param usuario1 Usuario actual
	 * @param usuario2 Usuario que posiblemente es conexión
	 * @return distancia entre los dos usuarios
	 */
	double calcularDistanciaEntreEstudiantes(UsuarioDTO usuario1, UsuarioDTO usuario2);

}
