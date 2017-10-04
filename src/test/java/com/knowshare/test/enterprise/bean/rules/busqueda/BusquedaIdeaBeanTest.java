/**
 * 
 */
package com.knowshare.test.enterprise.bean.rules.busqueda;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

import com.knowshare.dto.idea.IdeaDTO;
import com.knowshare.enterprise.bean.rules.busqueda.BusquedaIdeaFacade;
import com.knowshare.enterprise.bean.usuario.UsuarioFacade;
import com.knowshare.enums.PreferenciaIdeaEnum;
import com.knowshare.test.enterprise.general.AbstractTest;

/**
 * @author Miguel Montañez
 *
 */
public class BusquedaIdeaBeanTest extends AbstractTest{
	
	@Autowired
	private BusquedaIdeaFacade busquedaIdeaBean;
	
	@Autowired
	private UsuarioFacade usuarioBean;
	
	@Before
	public void setup(){
		// Relationship for Estudiante 1
		usuarioBean.seguir("Estudiante 1", "Estudiante 2");
		usuarioBean.seguir("Estudiante 1", "Estudiante 5");
		usuarioBean.seguir("Estudiante 1", "Profesor 2");
		usuarioBean.seguir("Estudiante 1", "Profesor 3");
		usuarioBean.seguir("Estudiante 1", "Egresado 2");
		usuarioBean.seguir("Estudiante 1", "Egresado 6");
		
		// Relationship for Egresado 1
		usuarioBean.seguir("Egresado 1", "Estudiante 2");
		usuarioBean.seguir("Egresado 1", "Estudiante 5");
		usuarioBean.seguir("Egresado 1", "Profesor 2");
		usuarioBean.seguir("Egresado 1", "Profesor 3");
		usuarioBean.seguir("Egresado 1", "Egresado 2");
		usuarioBean.seguir("Egresado 1", "Egresado 6");
				
		// Relationship for Profesor 1
		usuarioBean.seguir("Profesor 1", "Estudiante 2");
		usuarioBean.seguir("Profesor 1", "Estudiante 5");
		usuarioBean.seguir("Profesor 1", "Profesor 2");
		usuarioBean.seguir("Profesor 1", "Profesor 3");
		usuarioBean.seguir("Profesor 1", "Egresado 2");
		usuarioBean.seguir("Profesor 1", "Egresado 6");
	}
	
	@Test
	public void findRedTest(){
		processTest("Estudiante 1");
		processTest("Profesor 1");
		processTest("Egresado 1");
	}
	
	private void processTest(String username){
		Page<IdeaDTO> page = busquedaIdeaBean.findRed(username,0);
		assertFalse(page.isLast());
		assertTrue(page.isFirst());
		assertEquals(20, page.getTotalElements());
		assertEquals(2, page.getTotalPages());
		assertsIdeas(page.getContent(), 10, PreferenciaIdeaEnum.ORDEN_CRONOLOGICO);
		
		page = busquedaIdeaBean.findRed(username,1);
		assertTrue(page.isLast());
		assertFalse(page.isFirst());
		assertEquals(20, page.getTotalElements());
		assertEquals(2, page.getTotalPages());
		assertsIdeas(page.getContent(), 10, PreferenciaIdeaEnum.ORDEN_CRONOLOGICO);
		
		usuarioBean.updatePreferenciaIdea(username, PreferenciaIdeaEnum.POR_RELEVANCIA);
		
		page = busquedaIdeaBean.findRed(username,0);
		assertFalse(page.isLast());
		assertTrue(page.isFirst());
		assertEquals(20, page.getTotalElements());
		assertEquals(2, page.getTotalPages());
		assertsIdeas(page.getContent(), 10, PreferenciaIdeaEnum.POR_RELEVANCIA);
		
		page = busquedaIdeaBean.findRed(username,1);
		assertTrue(page.isLast());
		assertFalse(page.isFirst());
		assertEquals(20, page.getTotalElements());
		assertEquals(2, page.getTotalPages());
		assertsIdeas(page.getContent(), 10, PreferenciaIdeaEnum.POR_RELEVANCIA);
	}
	
	private void assertsIdeas(List<IdeaDTO> ideas, int size, PreferenciaIdeaEnum preferencia){
		assertNotNull(ideas);
		assertEquals(size, ideas.size());
		
		long last = preferencia.equals(PreferenciaIdeaEnum.ORDEN_CRONOLOGICO) ? ideas.get(0).getFechaCreacion().getTime()
					: ideas.get(0).getLights();
		for(int i = 1; i < ideas.size(); i++){
			long current = preferencia.equals(PreferenciaIdeaEnum.ORDEN_CRONOLOGICO) ? ideas.get(i).getFechaCreacion().getTime()
					: ideas.get(i).getLights();
			
			assertTrue(last >= current);
			last = current;
		}
	}
}
