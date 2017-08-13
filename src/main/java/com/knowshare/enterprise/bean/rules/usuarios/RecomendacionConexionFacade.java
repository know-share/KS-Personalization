/**
 * 
 */
package com.knowshare.enterprise.bean.rules.usuarios;

import java.util.List;

import com.knowshare.dto.perfilusuario.UsuarioDTO;
import com.knowshare.dto.rules.RecomendacionDTO;

/**
 * Recomendacioines de conexion entre usuarios.
 * @author Miguel Montañez
 * 
 */
public interface RecomendacionConexionFacade {
	
	/**
	 * Obtiene la lista con las recomendaciones de forma
	 * aleatoria para mostrarle al usuario.
	 * @param usuario
	 * @return
	 */
	List<?> setDeRecomendaciones(UsuarioDTO usuario);
	
	/**
	 * Dado el usuario, se busca las conexiones más cercanas
	 * segun varios parámetros y se agrega en la lista que
	 * más convenga.
	 * @param usuario al que se le busca las conexiones
	 * @param usuarios lista de usuarios que se mirará el
	 * tipo de conexión que tiene con el usuario actual
	 * @param recomendacionesConfianza lista con los usuarios
	 * de confianza para el usuario actual
	 * @param recomendacionesRelevante lista con los usuarios
	 * relevantes para el usuario actual
	 * @param recomendacionesNoRecomendar lista con los usuarios
	 * que no se le recomendarán al actual usuario.
	 */
	void recomendacionesUsuario(UsuarioDTO usuario,List<UsuarioDTO> usuarios,
			List<RecomendacionDTO> recomendacionesConfianza,
			List<RecomendacionDTO> recomendacionesRelevante,
			List<RecomendacionDTO> recomendacionesNoRecomendar);

}
