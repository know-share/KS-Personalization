/**
 * 
 */
package com.knowshare.enterprise.bean.rules.usuarios;

import java.util.List;

import com.knowshare.dto.perfilusuario.UsuarioDTO;

/**
 * @author miguel
 *
 */
public interface RecomendacionConexionFacade {
	
	List<?> recomendacionesUsuario(UsuarioDTO usuario);

}
