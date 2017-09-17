/**
 * 
 */
package com.knowshare.enterprise.bean.rules.busqueda;

import java.util.List;

import org.springframework.data.domain.Page;

import com.knowshare.dto.idea.IdeaDTO;
import com.knowshare.entities.idea.Tag;

/**
 * Permite la búsqueda de ideas por los filtros
 * disponibles.
 * @author Pablo Gaitan
 *
 */
public interface BusquedaIdeaFacade {
	
	/**
	 * Carga las ideas de la red de un usuario
	 * dado.
	 * @param username del usuario actual
	 * @return lista de {@link IdeaDTO ideas}
	 */
	Page<IdeaDTO> findRed(String username,Integer page);
	
	/**
	 * Realiza la búsqueda de ideas según el criterio
	 * seleccionado.
	 * @param tags, necesario si el criterio es 'tag'
	 * @param criterio
	 * @param username
	 * @return lista de {@link IdeaDTO ideas}
	 */
	List<IdeaDTO> findIdeas(List<Tag> tags, String criterio,String username);
}
