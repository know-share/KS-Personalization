/**
 * 
 */
package com.knowshare.enterprise.bean.rules.usuarios;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.knowshare.dto.perfilusuario.UsuarioDTO;
import com.knowshare.enterprise.bean.rules.RuleFireFacade;
import com.knowshare.enterprise.bean.rules.distancias.DistanciasUsuarioFacade;
import com.knowshare.enterprise.bean.usuario.UsuarioFacade;
import com.knowshare.fact.rules.UsuarioFact;

/**
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

	@SuppressWarnings("unchecked")
	@Override
	public List<?> recomendacionesUsuario(UsuarioDTO usuario) {
		final List<UsuarioDTO> usuarios = usuarioBean.getAllEstudiantesExceptOne(usuario.getUsername());
		final List<UsuarioFact> usuariosFact =new ArrayList<>();
		Map<String,String> map = null;
		switch(usuario.getTipoUsuario()){
			case ESTUDIANTE:
				for(UsuarioDTO u:usuarios){
					usuariosFact.add(new UsuarioFact().setUsername(u.getUsername())
							.setDistancia(distanciasUsuarioBean.calcularDistanciaEntreEstudiantes(usuario, u)));
				}
				map = (Map<String,String>)ruleFireBean.fireRules(usuariosFact);
				break;
			case EGRESADO:
				break;
			case PROFESOR:
				break;
			default:
				break;
		}
		for(String s:map.keySet()){
			System.out.println(s);
		}
		return null;
	}
}
