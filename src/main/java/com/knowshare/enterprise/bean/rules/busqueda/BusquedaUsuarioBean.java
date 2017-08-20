/**
 * 
 */
package com.knowshare.enterprise.bean.rules.busqueda;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.knowshare.dto.perfilusuario.UsuarioDTO;
import com.knowshare.dto.rules.RecomendacionDTO;
import com.knowshare.enterprise.bean.rules.usuarios.RecomendacionConexionFacade;
import com.knowshare.enterprise.bean.usuario.UsuarioFacade;
import com.knowshare.enums.TipoUsuariosEnum;

/**
 * {@link BusquedaUsuarioFacade}
 * @author Miguel Montañez
 *
 */
@Component
public class BusquedaUsuarioBean implements BusquedaUsuarioFacade{
	
	@Autowired
	private UsuarioFacade usuarioBean;
	
	@Autowired
	private RecomendacionConexionFacade recomendacionBean;

	@Override
	public List<RecomendacionDTO> buscarUsuario(UsuarioDTO usuario, String filtro,String parametro) {
		List<RecomendacionDTO> busqueda = null;
		switch(filtro.toUpperCase()){
			case "HABILIDAD":
				busqueda = buscarPorHabilidad(usuario,parametro);
				break;
			case "AREA":
				busqueda = buscarPorAreaConocimiento(usuario,parametro);
				break;
			default:
				busqueda = buscarPorNombre(usuario,parametro);
				break;
		}
		return busqueda;
	}
	
	private List<RecomendacionDTO> buscarPorNombre(UsuarioDTO usuario, String parametro){
		final List<RecomendacionDTO> busqueda = new ArrayList<>();
		final List<UsuarioDTO> usuariosBusqueda = usuarioBean.buscarPorNombre(usuario, parametro);
		final List<RecomendacionDTO> recomendacionesConfianza = new ArrayList<>();
		final List<RecomendacionDTO> recomendacionesRelevante = new ArrayList<>();
		final List<RecomendacionDTO> recomendacionesNoRecomendar = new ArrayList<>();
		recomendacionBean.recomendacionesUsuario(usuario, usuariosBusqueda, recomendacionesConfianza, 
				recomendacionesRelevante, recomendacionesNoRecomendar);
		
		busqueda.addAll(recomendacionesConfianza);
		busqueda.addAll(recomendacionesRelevante);
		busqueda.addAll(recomendacionesNoRecomendar);
		return busqueda;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<RecomendacionDTO> buscarPorHabilidad(UsuarioDTO usuario, String parametro){
		final List<RecomendacionDTO> busqueda = new ArrayList<>();
		final List<Map> usuariosBusqueda = usuarioBean.buscarPorHabilidad(parametro);
		for(Map m : usuariosBusqueda){
			if(!usuario.getUsername().equalsIgnoreCase(m.get("username").toString())){
				RecomendacionDTO dto = new RecomendacionDTO()
						.setNombre(m.get("nombre") +" "+m.get("apellido"))
						.setUsername(m.get("username").toString())
						.setGenero(m.get("genero").toString())
						.setCarrera(((List<Map>)m.get("carreras")).get(0).get("_id").toString())
						.setTipoUsuario(TipoUsuariosEnum.valueOf(m.get("tipo").toString()));
				busqueda.add(dto);
			}
		}
		return busqueda;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<RecomendacionDTO> buscarPorAreaConocimiento(UsuarioDTO usuario, String parametro){
		final List<RecomendacionDTO> busqueda = new ArrayList<>();
		final List<Map> usuariosBusqueda = usuarioBean.buscarPorAreaConocimiento(parametro);
		for(Map m : usuariosBusqueda){
			if(!usuario.getUsername().equalsIgnoreCase(m.get("username").toString())){
				RecomendacionDTO dto = new RecomendacionDTO()
						.setNombre(m.get("nombre") +" "+m.get("apellido"))
						.setUsername(m.get("username").toString())
						.setGenero(m.get("genero").toString())
						.setCarrera(((List<Map>)m.get("carreras")).get(0).get("_id").toString())
						.setTipoUsuario(TipoUsuariosEnum.valueOf(m.get("tipo").toString()));
				busqueda.add(dto);
			}
		}
		return busqueda;
	}

}
