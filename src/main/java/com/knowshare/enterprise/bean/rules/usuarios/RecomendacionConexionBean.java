/**
 * 
 */
package com.knowshare.enterprise.bean.rules.usuarios;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.knowshare.dto.perfilusuario.UsuarioDTO;
import com.knowshare.dto.rules.RecomendacionDTO;
import com.knowshare.enterprise.bean.rules.RuleFireFacade;
import com.knowshare.enterprise.bean.rules.distancias.DistanciasUsuarioFacade;
import com.knowshare.enterprise.bean.usuario.UsuarioFacade;
import com.knowshare.fact.rules.TipoConexionEnum;
import com.knowshare.fact.rules.UsuarioFact;

/**
 * {@link RecomendacionConexionFacade}
 * @author Miguel Montañez
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
	public List<?> setDeRecomendaciones(UsuarioDTO usuario) {
		final List<UsuarioDTO> usuarios = usuarioBean.getMyNoConnections(usuario.getUsername());
		final List<RecomendacionDTO> recomendacionesConfianza = new ArrayList<>();
		final List<RecomendacionDTO> recomendacionesRelevante = new ArrayList<>();
		final List<RecomendacionDTO> recomendacionesNoRecomendar = new ArrayList<>();
		this.recomendacionesUsuario(usuario,usuarios,recomendacionesConfianza,
				recomendacionesRelevante,recomendacionesNoRecomendar);
		return recomendacionesRandom(recomendacionesConfianza, recomendacionesRelevante, recomendacionesNoRecomendar);
	}

	@Override
	public void recomendacionesUsuario(UsuarioDTO usuario, List<UsuarioDTO> usuarios,
			List<RecomendacionDTO> recomendacionesConfianza, List<RecomendacionDTO> recomendacionesRelevante,
			List<RecomendacionDTO> recomendacionesNoRecomendar) {
		final List<UsuarioFact> usuariosFact =new ArrayList<>();
		final Map<String,UsuarioDTO> mapUsuarios = new HashMap<>();
		final Map<String,Double> mapDistancias = new HashMap<>();
		double distancia;
		Map<String,String> map = null;
		switch(usuario.getTipoUsuario()){
			case ESTUDIANTE:
				for(UsuarioDTO u:usuarios){
					distancia = distanciasUsuarioBean.calcularDistanciaEstudianteUsuario(usuario, u);
					usuariosFact.add(new UsuarioFact().setUsername(u.getUsername())
							.setDistancia(distancia));
					mapUsuarios.put(u.getUsername(), u);
					mapDistancias.put(u.getUsername(), distancia);
				}
				break;
			case PROFESOR:
				for(UsuarioDTO u:usuarios){
					distancia = distanciasUsuarioBean.calcularDistanciaProfesorUsuario(usuario, u);
					usuariosFact.add(new UsuarioFact().setUsername(u.getUsername())
							.setDistancia(distancia));
					mapUsuarios.put(u.getUsername(), u);
					mapDistancias.put(u.getUsername(), distancia);
				}
				break;
			case EGRESADO:
				for(UsuarioDTO u:usuarios){
					distancia = distanciasUsuarioBean.calcularDistanciaEgresadoUsuario(usuario, u);
					usuariosFact.add(new UsuarioFact().setUsername(u.getUsername())
							.setDistancia(distancia));
					mapUsuarios.put(u.getUsername(), u);
					mapDistancias.put(u.getUsername(), distancia);
				}
				break;
			default:
				break;
		}
		map = ruleFireBean.fireRules(usuariosFact,"mapRecomendaciones",new HashMap<String,String>());
		
		for(String s:map.keySet()){
			Double truncatedDouble = BigDecimal.valueOf((1-mapDistancias.get(s))*100)
				    .setScale(2, RoundingMode.HALF_UP)
				    .doubleValue();
			
			RecomendacionDTO info = new RecomendacionDTO()
					.setNombre(mapUsuarios.get(s).getNombre() + " " +mapUsuarios.get(s).getApellido())
					.setUsername(mapUsuarios.get(s).getUsername())
					.setGenero(mapUsuarios.get(s).getGenero())
					.setCarrera(mapUsuarios.get(s).getCarrera().getNombre())
					.setPorcentaje(truncatedDouble)
					.setTipoUsuario(mapUsuarios.get(s).getTipoUsuario());
			if(map.get(s).equals(TipoConexionEnum.CONFIANZA.getValue())){
				info.setConexion(TipoConexionEnum.CONFIANZA);
				recomendacionesConfianza.add(info);
			}else if(map.get(s).equals(TipoConexionEnum.RELEVANTE.getValue())){
				info.setConexion(TipoConexionEnum.RELEVANTE);
				recomendacionesRelevante.add(info);
			}else{
				info.setConexion(TipoConexionEnum.NO_RECOMENDAR);
				recomendacionesNoRecomendar.add(info);
			}
		}
	}
	
	/**
	 * Coger 2 elementos random de cada lista excepto de no recomendar, para esta 
	 * se coge solamente 1
	 * @param confianza
	 * @param relevante
	 * @param noRecomendar
	 * @return lista con los elementos aleatorios.
	 */
	private List<RecomendacionDTO> recomendacionesRandom(List<RecomendacionDTO> confianza,List<RecomendacionDTO> relevante,
			List<RecomendacionDTO> noRecomendar){
		
		final List<RecomendacionDTO> finalList = new ArrayList<>();
		int numRandom = 0;
		int lastNum = 0;
		
		if(!confianza.isEmpty()){
			lastNum = (int)(Math.random()*confianza.size());
			finalList.add(confianza.get(lastNum));
			
			if(confianza.size() > 1){
				while((numRandom = (int)(Math.random()*confianza.size())) == lastNum);
				finalList.add(confianza.get(numRandom));
			}
		}
		if(!relevante.isEmpty()){
			lastNum = (int)(Math.random()*relevante.size());
			finalList.add(relevante.get(lastNum));
			
			if(relevante.size() > 1){
				while((numRandom = (int)(Math.random()*relevante.size())) == lastNum);
				finalList.add(relevante.get(numRandom));
			}
		}
		if(!noRecomendar.isEmpty()){
			lastNum = (int)(Math.random()*noRecomendar.size());
			finalList.add(noRecomendar.get(lastNum));
		}
		
		return finalList;
	}
}
