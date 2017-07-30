/**
 * 
 */
package com.knowshare.enterprise.bean.rules.busqueda;

import java.util.List;

import com.knowshare.dto.perfilusuario.UsuarioDTO;
import com.knowshare.dto.rules.RecomendacionDTO;

/**
 * Permite la búsqueda de usuarios por las tres filtros
 * disponibles.
 * @author Miguel Montañez
 *
 */
public interface BusquedaUsuarioFacade {
	
	List<RecomendacionDTO> buscarUsuario(UsuarioDTO usuario, String filtro,String parametro);

}
