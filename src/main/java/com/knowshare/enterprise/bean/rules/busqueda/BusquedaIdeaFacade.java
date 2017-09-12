/**
 * 
 */
package com.knowshare.enterprise.bean.rules.busqueda;

import java.util.List;

import com.knowshare.dto.idea.IdeaDTO;
import com.knowshare.entities.idea.Tag;

/**
 * @author Pablo Gaitan
 *
 */
public interface BusquedaIdeaFacade {
	
	List<IdeaDTO> findRed(String username);
	List<IdeaDTO> findIdeas(List<Tag> tags, String criterio,String username);

}
