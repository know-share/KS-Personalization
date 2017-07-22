/**
 * 
 */
package com.knowshare.enterprise.bean.rules.usuarios;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.knowshare.dto.perfilusuario.UsuarioDTO;
import com.knowshare.enterprise.bean.rules.RuleFireFacade;
import com.knowshare.enterprise.bean.rules.distancias.DistanciasUsuarioFacade;
import com.knowshare.enterprise.bean.usuario.UsuarioFacade;
import com.knowshare.entities.perfilusuario.InfoUsuario;
import com.knowshare.enums.TipoUsuariosEnum;
import com.knowshare.fact.rules.TipoConexionEnum;
import com.knowshare.fact.rules.UsuarioFact;

/**
 * {@link RecomendacionConexionFacade}
 * @author miguel
 *
 */
@Component
public class RecomendacionConexionBean implements RecomendacionConexionFacade {

	@Autowired
	private RuleFireFacade ruleFireBean;
	
	@Autowired
	private DistanciasUsuarioFacade distanciasUsuarioBean;
	
	@Autowired
	private UsuarioFacade usuarioBean;

	@Override
	public List<?> recomendacionesUsuario(UsuarioDTO usuario) {
		final List<UsuarioDTO> usuarios = usuarioBean.getMyNoConnections(usuario.getUsername(), TipoUsuariosEnum.ESTUDIANTE);
		final List<UsuarioFact> usuariosFact =new ArrayList<>();
		final List<InfoUsuario> recomendaciones = new ArrayList<>();
		final Map<String,UsuarioDTO> mapUsuarios = new HashMap<>();
		Map<String,String> map = null;
		switch(usuario.getTipoUsuario()){
			case ESTUDIANTE:
				for(UsuarioDTO u:usuarios){
					usuariosFact.add(new UsuarioFact().setUsername(u.getUsername())
							.setDistancia(distanciasUsuarioBean.calcularDistanciaEntreEstudiantes(usuario, u)));
					mapUsuarios.put(u.getUsername(), u);
				}
				break;
			case EGRESADO:
				break;
			case PROFESOR:
				break;
			default:
				break;
		}
		map = ruleFireBean.fireRules(usuariosFact,"mapRecomendaciones",new HashMap<String,String>());
		for(String s:map.keySet()){
			if(map.get(s).equals(TipoConexionEnum.CONFIANZA.getValue())){
				InfoUsuario info = new InfoUsuario()
						.setNombre(mapUsuarios.get(s).getNombre() + " " +mapUsuarios.get(s).getApellido())
						.setUsername(mapUsuarios.get(s).getUsername());
				recomendaciones.add(info);
			}
		}
		return recomendaciones;
	}
}
