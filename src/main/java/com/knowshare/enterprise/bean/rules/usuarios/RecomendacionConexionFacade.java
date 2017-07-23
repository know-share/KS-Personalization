/**
 * 
 */
package com.knowshare.enterprise.bean.rules.usuarios;

import java.util.List;

import com.knowshare.dto.perfilusuario.UsuarioDTO;

/**
 * Recomendacioines de conexion entre usuarios.
 * @author miguel
 * 
 */
public interface RecomendacionConexionFacade {
	
	/**
	 * Dado el usuario, se busca las conexiones más cercanas
	 * segun varios parámetros.
	 * @param usuario al que se le busca las conexiones
	 * @return Lista de recomendaciones
	 */
	List<?> recomendacionesUsuario(UsuarioDTO usuario);

}
