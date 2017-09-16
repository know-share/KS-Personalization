/**
 * 
 */
package com.knowshare.enterprise.bean.rules.busqueda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.knowshare.dto.idea.IdeaDTO;
import com.knowshare.enterprise.bean.rules.RuleFireFacade;
import com.knowshare.enterprise.bean.rules.distancias.DistanciasUsuarioFacade;
import com.knowshare.enterprise.repository.idea.IdeaRepository;
import com.knowshare.enterprise.repository.perfilusuario.UsuarioRepository;
import com.knowshare.enterprise.utils.MapEntities;
import com.knowshare.entities.idea.Idea;
import com.knowshare.entities.idea.OperacionIdea;
import com.knowshare.entities.idea.Tag;
import com.knowshare.entities.perfilusuario.InfoUsuario;
import com.knowshare.entities.perfilusuario.Usuario;
import com.knowshare.enums.TipoIdeaEnum;
import com.knowshare.enums.TipoOperacionEnum;
import com.knowshare.fact.rules.IdeaFact;
import com.knowshare.fact.rules.TipoIdeaRecomendacionEnum;

/**
 * {@link BusquedaIdeaFacade}
 * 
 * @author Pablo Gaitan
 *
 */
@Component
public class BusquedaIdeaBean implements BusquedaIdeaFacade {

	@Autowired
	private RuleFireFacade ruleBean;

	@Autowired
	private DistanciasUsuarioFacade distBean;

	@Autowired
	private IdeaRepository ideaRep;

	@Autowired
	private UsuarioRepository usuRep;

	@Autowired
	private MongoTemplate mongoTemplate;

	private static final String GLOBAL_RULES = "mapRecomendaciones";
	private static final String SORT_ATTR = "lights";

	/**
	 * Revisa si una idea ya tiene un light del usuario
	 * que ingresa como parámetro
	 * @param idea
	 * @param username
	 * @return Verdadero si ya dio light, de lo contrario Falso.
	 */
	private OperacionIdea isLight(Idea idea, String username) {
		for (OperacionIdea o : idea.getOperaciones()) {
			if (o.getTipo().equals(TipoOperacionEnum.LIGHT) && o.getUsername().equalsIgnoreCase(username))
				return o;
		}
		return null;
	}

	@Override
	public List<IdeaDTO> findRed(String username) {
		final Usuario usu = usuRep.findByUsernameIgnoreCase(username);
		List<InfoUsuario> red = usu.getAmigos();
		red.addAll(usu.getSiguiendo());
		List<String> usernamesRed = new ArrayList<>();
		for (InfoUsuario inf : red)
			usernamesRed.add(inf.getUsername());
		List<ObjectId> usuariosId = usuRep.findUsuariosByUsername(usernamesRed).stream().map(Usuario::getId)
				.collect(Collectors.toList());
		List<Idea> ideas = ideaRep.findIdeaRed(usuariosId);
		List<IdeaDTO> dtos = new ArrayList<>();
		IdeaDTO dto;
		for (Idea idea : ideas) {
			dto = MapEntities.mapIdeaToDTO(idea);
			if (isLight(idea, username) != null)
				dto.setIsLight(true);
			else
				dto.setIsLight(false);
			dtos.add(dto);
		}
		return dtos;
	}

	private List<IdeaDTO> findByTags(List<Tag> tags, String username) {
		final Query query = new Query(Criteria.where("tags").all(tags));
		List<Idea> ideas = mongoTemplate.find(query, Idea.class);
		return mapIdeas(username, ideas);
	}

	private List<IdeaDTO> findContinuarNueva(String username, TipoIdeaEnum tipo) {
		Usuario usuario = usuRep.findByUsernameIgnoreCase(username);
		final List<InfoUsuario> conexiones = usuario.getAmigos();
		conexiones.addAll(usuario.getSiguiendo());
		final List<String> usuariosConexion = new ArrayList<>();
		for (InfoUsuario i : conexiones) {
			usuariosConexion.add(i.getUsername());
		}
		final List<ObjectId> ids = usuRep.findUsuariosByUsername(usuariosConexion)
				.stream()
				.map(Usuario::getId)
				.collect(Collectors.toList());
		Sort sort = new Sort(Direction.DESC, SORT_ATTR);
		final List<Idea> cercanas = ideaRep.findIdea(ids, sort, tipo.name());
		final List<Usuario> noRed = usuRep.findMyNoConnections(username);
		final List<ObjectId> idsNoRed = noRed.stream().map(Usuario::getId).collect(Collectors.toList());
		final List<Idea> lejanas = ideaRep.findIdea(idsNoRed, sort, tipo.name());
		cercanas.addAll(lejanas);
		return mapIdeas(username, cercanas);
	}

	private List<IdeaDTO> findProyectosEmpezar(String username,TipoIdeaEnum tipo){
		Map<String,Idea> mapIdea = new HashMap<>();
		Map<String,String> mapRet;
		List<IdeaFact> facts = new ArrayList<>();
		final List<IdeaDTO> cercanas = new ArrayList<>();
		final List<IdeaDTO> lejanas = new ArrayList<>();
		final List<IdeaDTO> muyLejanas = new ArrayList<>();
		final Usuario usuario = usuRep.findByUsernameIgnoreCase(username);
		final List<InfoUsuario> conexiones = usuario.getAmigos();
		conexiones.addAll(usuario.getSiguiendo());
		final List<String> usuariosConexion = new ArrayList<>();
		for (InfoUsuario i : conexiones) {
			usuariosConexion.add(i.getUsername());
		}
		Double d;
		final List<ObjectId> ids = usuRep.findUsuariosByUsernameProfesor(usuariosConexion)
				.stream()
				.map(Usuario::getId)
				.collect(Collectors.toList());
		Sort sort = new Sort(Direction.DESC,SORT_ATTR);
		final List<Idea> ideasUsuarios = ideaRep.findIdea(ids,sort,tipo.name());
		for (Idea idea : ideasUsuarios) {
			d = distBean.calcularDistanciaJaccard(
				usuario.getAreasConocimiento(),idea.getUsuario().getAreasConocimiento());
			idea.getUsuario();
			mapIdea.put(idea.getId(), idea);
			facts.add(new IdeaFact(idea.getId(),d,true));
		}
		mapRet = ruleBean.fireRules(facts,GLOBAL_RULES, new HashMap<String,String>());
		mapFacts(username, mapRet, mapIdea, cercanas, lejanas, muyLejanas);
		
		final List<Usuario> noConexiones = usuRep.findMyNoConnections(username);
		final List<ObjectId> idNoConexiones = noConexiones
				.stream()
				.map(Usuario::getId)
				.collect(Collectors.toList());
		final List<Idea> noConexionesIdea = ideaRep.findIdea(idNoConexiones,sort,tipo.name());
		mapIdea = new HashMap<>();
		facts = new ArrayList<>();
		for (Idea idea : noConexionesIdea) {
			d = distBean.calcularDistanciaJaccard(usuario.getAreasConocimiento(),
					idea.getUsuario().getAreasConocimiento());
			mapIdea.put(idea.getId(), idea);
			facts.add(new IdeaFact(idea.getId(),d,false));
		}
		mapRet= ruleBean.fireRules(facts,GLOBAL_RULES, new HashMap<String,String>());
		mapFacts(username, mapRet, mapIdea, cercanas, lejanas, muyLejanas);
		
		cercanas.addAll(lejanas);
		cercanas.addAll(muyLejanas);
		return cercanas;
	}

	/**
	 * Revisa el mapa enviado por el motor de reglas y agrega
	 * la idea correspondiente al tipo de lista para retornar
	 * @param username
	 * @param mapRet
	 * @param mapIdea
	 * @param cercanas
	 * @param lejanas
	 * @param muyLejanas
	 */
	private void mapFacts(String username, Map<String, String> mapRet,Map<String,Idea> mapIdea, 
			List<IdeaDTO> cercanas, List<IdeaDTO> lejanas,List<IdeaDTO> muyLejanas) {
		IdeaDTO dto;
		for (String idea : mapRet.keySet()) {
			dto = MapEntities.mapIdeaToDTO(mapIdea.get(idea));
			if (mapRet.get(idea).equals(TipoIdeaRecomendacionEnum.CERCANA.name())) {
				if (isLight(mapIdea.get(idea), username) != null)
					dto.setIsLight(true);
				else
					dto.setIsLight(false);
				cercanas.add(dto);
			}
			if (mapRet.get(idea).equals(TipoIdeaRecomendacionEnum.LEJANA.name())) {
				if (isLight(mapIdea.get(idea), username) != null)
					dto.setIsLight(true);
				else
					dto.setIsLight(false);
				lejanas.add(dto);
			}
			if (mapRet.get(idea).equals(TipoIdeaRecomendacionEnum.MUY_LEJANA.name())) {
				if (isLight(mapIdea.get(idea), username) != null)
					dto.setIsLight(true);
				else
					dto.setIsLight(false);
				muyLejanas.add(dto);
			}
		}
	}

	/**
	 * Mapea una lista de ideas a sus respectivos dtos y revisa
	 * si la idea ya tiene light por parte del usuario actual.
	 * @param username
	 * @param ideas
	 * @return lista de {@link IdeaDTO ideas}
	 */
	private List<IdeaDTO> mapIdeas(String username, List<Idea> ideas) {
		final List<IdeaDTO> dtos = new ArrayList<>();
		IdeaDTO dto;
		for (Idea idea : ideas) {
			dto = MapEntities.mapIdeaToDTO(idea);
			if (isLight(idea, username) != null)
				dto.setIsLight(true);
			else
				dto.setIsLight(false);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public List<IdeaDTO> findIdeas(List<Tag> tags, String criterio, String username) {
		if (criterio.equals("tag")) {
			return findByTags(tags, username);
		}
		if (criterio.equals("continuar")) {
			return findContinuarNueva(username, TipoIdeaEnum.PC);
		}
		if (criterio.equals("nueva")) {
			return findContinuarNueva(username, TipoIdeaEnum.NU);
		}
		if (criterio.equals("proyecto")) {
			return findProyectosEmpezar(username, TipoIdeaEnum.PR);
		}
		if (criterio.equals("empezar")) {
			return findProyectosEmpezar(username, TipoIdeaEnum.PE);
		}
		return null;
	}
}