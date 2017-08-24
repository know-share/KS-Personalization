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
	
	/**
	 * Busca un usuario según los filtros
	 * @param usuario Usuario que está buscando.
	 * @param filtro HABILIDAD, AREA o NOMBRE
	 * @param parametro el valor de la consulta
	 * @return Lista con los usuarios ordenados según el tipo de
	 * filtro
	 */
	List<RecomendacionDTO> buscarUsuario(UsuarioDTO usuario, String filtro,String parametro);

}
