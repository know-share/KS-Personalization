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
import com.knowshare.enums.TipoOperacionEnum;
import com.knowshare.fact.rules.IdeaFact;
import com.knowshare.fact.rules.TipoIdeaRecomendacionEnum;

/**
 * {@link BusquedaIdeaFacade}
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
	
	public OperacionIdea isLight(Idea idea, String username){
		for (OperacionIdea o : idea.getOperaciones()) {
			if(o.getTipo().equals(TipoOperacionEnum.LIGHT) &&
					o.getUsername().equalsIgnoreCase(username))
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
		List<ObjectId> usuariosId = usuRep.findUsuariosByUsername(usernamesRed)
				.stream()
				.map(Usuario::getId)
				.collect(Collectors.toList());
		List<Idea> ideas = ideaRep.findIdeaRed(usuariosId);
		List<IdeaDTO> dtos = new ArrayList<>();
		IdeaDTO dto;
		for (Idea idea : ideas) {
			dto = MapEntities.mapIdeaToDTO(idea);
			if(isLight(idea, username)!=null)
				dto.setIsLight(true);
			else
				dto.setIsLight(false);
			dtos.add(dto);
		}
		return dtos;
	}
	
	public List<IdeaDTO> findByTags(List<Tag> tags,String username) {
		final Query query = new Query(Criteria.where("tags")
				.all(tags));
		List<Idea> ideas = mongoTemplate.find(query, Idea.class);
		List<IdeaDTO> dtos = new ArrayList<>();
		IdeaDTO dto;
		for (Idea idea : ideas) {
			dto = MapEntities.mapIdeaToDTO(idea);
			if(isLight(idea, username)!=null)
				dto.setIsLight(true);
			else
				dto.setIsLight(false);
			dtos.add(dto);
		}
		return dtos;
	}
	
	public List<IdeaDTO> findContinuar(String username){
		Usuario usuario = usuRep.findByUsernameIgnoreCase(username);
		List<InfoUsuario> conexiones = usuario.getAmigos();
		conexiones.addAll(usuario.getSiguiendo());
		List<String> usuariosConexion = new ArrayList<>();
		for (InfoUsuario i : conexiones) {
			usuariosConexion.add(i.getUsername());
		}
		List<ObjectId> ids = usuRep.findUsuariosByUsername(usuariosConexion)
				.stream().map(Usuario::getId).collect(Collectors.toList());
		Sort sort = new Sort(Direction.DESC,"lights");
		List<Idea> cercanas = ideaRep.findIdeaContinuar(ids,sort);
		List<Usuario> noRed = usuRep.findMyNoConnections(username);
		List<String> usuariosNoConexion = new ArrayList<>();
		for (Usuario usu : noRed) {
			usuariosNoConexion.add(usu.getUsername());
		}
		List<ObjectId> idsNoRed = usuRep.findUsuariosByUsername(usuariosNoConexion)
				.stream().map(Usuario::getId).collect(Collectors.toList());
		List<Idea> lejanas = ideaRep.findIdeaContinuar(idsNoRed,sort);
		cercanas.addAll(lejanas);
		List<IdeaDTO> dtos = new ArrayList<>();
		IdeaDTO dto;
		for (Idea idea : cercanas) {
			dto = MapEntities.mapIdeaToDTO(idea);
			if(isLight(idea, username)!=null)
				dto.setIsLight(true);
			else
				dto.setIsLight(false);
			dtos.add(dto);
		}
		return dtos;
	}
	
	public List<IdeaDTO> findNuevas(String username){
		Usuario usuario = usuRep.findByUsernameIgnoreCase(username);
		List<InfoUsuario> conexiones = usuario.getAmigos();
		conexiones.addAll(usuario.getSiguiendo());
		List<String> usuariosConexion = new ArrayList<>();
		for (InfoUsuario i : conexiones) {
			usuariosConexion.add(i.getUsername());
		}
		List<ObjectId> ids = usuRep.findUsuariosByUsername(usuariosConexion)
				.stream().map(Usuario::getId).collect(Collectors.toList());
		Sort sort = new Sort(Direction.DESC,"lights");
		List<Idea> cercanas = ideaRep.findIdeaNueva(ids,sort);
		List<Usuario> noRed = usuRep.findMyNoConnections(username);
		List<String> usuariosNoConexion = new ArrayList<>();
		for (Usuario usu : noRed) {
			usuariosNoConexion.add(usu.getUsername());
		}
		List<ObjectId> idsNoRed = usuRep.findUsuariosByUsername(usuariosNoConexion)
				.stream().map(Usuario::getId).collect(Collectors.toList());
		List<Idea> lejanas = ideaRep.findIdeaNueva(idsNoRed,sort);
		cercanas.addAll(lejanas);
		List<IdeaDTO> dtos = new ArrayList<>();
		IdeaDTO dto;
		for (Idea idea : cercanas) {
			dto = MapEntities.mapIdeaToDTO(idea);
			if(isLight(idea, username)!=null)
				dto.setIsLight(true);
			else
				dto.setIsLight(false);
			dtos.add(dto);
		}
		return dtos;
	}
	
	public List<IdeaDTO> findProyectos(String username){
		Map<String,Idea> mapIdea = new HashMap<>();
		Map<String,String> mapRet;
		List<IdeaFact> facts = new ArrayList<>();
		List<IdeaDTO> cercanas = new ArrayList<>();
		List<IdeaDTO> lejanas = new ArrayList<>();
		List<IdeaDTO> muyLejanas = new ArrayList<>();
		Usuario usuario = usuRep.findByUsernameIgnoreCase(username);
		List<InfoUsuario> conexiones = usuario.getAmigos();
		conexiones.addAll(usuario.getSiguiendo());
		List<String> usuariosConexion = new ArrayList<>();
		for (InfoUsuario i : conexiones) {
			usuariosConexion.add(i.getUsername());
		}
		Double d ;
		List<ObjectId> ids = usuRep.findUsuariosByUsernameProfesor(usuariosConexion)
				.stream().map(Usuario::getId).collect(Collectors.toList());
		List<Idea> ideasUsuarios = ideaRep.findIdeaRedProyectos(ids);
		for (Idea idea : ideasUsuarios) {
			d = distBean.calcularDistanciaJaccard(
				usuario.getAreasConocimiento(),idea.getUsuario().getAreasConocimiento());
			idea.getUsuario();
			mapIdea.put(idea.getId(), idea);
			facts.add(new IdeaFact(idea.getId(),d,true));
		}
		IdeaDTO dto;
		mapRet = ruleBean.fireRules(facts,GLOBAL_RULES, new HashMap<String,String>());
		for (String idea : mapRet.keySet()){
			dto = MapEntities.mapIdeaToDTO(mapIdea.get(idea));
			if(mapRet.get(idea).equals(TipoIdeaRecomendacionEnum.CERCANA.name())){
				if(isLight(mapIdea.get(idea), username)!=null)
					dto.setIsLight(true);
				else
					dto.setIsLight(false);
				cercanas.add(dto);
			}
			if(mapRet.get(idea).equals(TipoIdeaRecomendacionEnum.LEJANA.name())){
				if(isLight(mapIdea.get(idea), username)!=null)
					dto.setIsLight(true);
				else
					dto.setIsLight(false);
				lejanas.add(dto);
			}
			if(mapRet.get(idea).equals(TipoIdeaRecomendacionEnum.MUY_LEJANA.name())){
				if(isLight(mapIdea.get(idea), username)!=null)
					dto.setIsLight(true);
				else
					dto.setIsLight(false);
				muyLejanas.add(dto);
			}
		}
		
		List<Usuario> noConexiones = usuRep.findMyNoConnections(username);
		List<ObjectId> idNoConexiones = noConexiones.stream().map(Usuario::getId).collect(Collectors.toList());
		List<Idea> noConexionesIdea = ideaRep.findIdeaRedProyectos(idNoConexiones);
		mapIdea = new HashMap<>();
		facts = new ArrayList<>();
		for (Idea idea : noConexionesIdea) {
			d = distBean.calcularDistanciaJaccard(usuario.getAreasConocimiento(),
					idea.getUsuario().getAreasConocimiento());
			mapIdea.put(idea.getId(), idea);
			facts.add(new IdeaFact(idea.getId(),d,false));
		}
		mapRet= ruleBean.fireRules(facts,GLOBAL_RULES, new HashMap<String,String>());
		for (String id : mapRet.keySet()) {
			dto = MapEntities.mapIdeaToDTO(mapIdea.get(id));
			if(mapRet.get(id).equals(TipoIdeaRecomendacionEnum.LEJANA.name())){
				if(isLight(mapIdea.get(id), username)!=null)
					dto.setIsLight(true);
				else
					dto.setIsLight(false);
				lejanas.add(dto);
			}
			if(mapRet.get(id).equals(TipoIdeaRecomendacionEnum.MUY_LEJANA.name())){
				if(isLight(mapIdea.get(id), username)!=null)
					dto.setIsLight(true);
				else
					dto.setIsLight(false);
				muyLejanas.add(dto);
			}
		}
		cercanas.addAll(lejanas);
		cercanas.addAll(muyLejanas);
		return cercanas;
	}
	
	public List<IdeaDTO> findEmpezar(String username){
		Map<String,Idea> mapIdea = new HashMap<>();
		Map<String,String> mapRet;
		List<IdeaFact> facts = new ArrayList<>();
		List<IdeaDTO> cercanas = new ArrayList<>();
		List<IdeaDTO> lejanas = new ArrayList<>();
		List<IdeaDTO> muyLejanas = new ArrayList<>();
		Usuario usuario = usuRep.findByUsernameIgnoreCase(username);
		List<InfoUsuario> conexiones = usuario.getAmigos();
		conexiones.addAll(usuario.getSiguiendo());
		List<String> usuariosConexion = new ArrayList<>();
		for (InfoUsuario i : conexiones) {
			usuariosConexion.add(i.getUsername());
		}
		Double d ;
		List<ObjectId> ids = usuRep.findUsuariosByUsernameProfesor(usuariosConexion)
				.stream().map(Usuario::getId).collect(Collectors.toList());
		List<Idea> ideasUsuarios = ideaRep.findIdeaRedEmpezar(ids);
		for (Idea idea : ideasUsuarios) {
			d = distBean.calcularDistanciaJaccard(
				usuario.getAreasConocimiento(),idea.getUsuario().getAreasConocimiento());
			idea.getUsuario();
			mapIdea.put(idea.getId(), idea);
			facts.add(new IdeaFact(idea.getId(),d,true));
		}
		IdeaDTO dto;
		mapRet = ruleBean.fireRules(facts,GLOBAL_RULES, new HashMap<String,String>());
		for (String idea : mapRet.keySet()){
			dto = MapEntities.mapIdeaToDTO(mapIdea.get(idea));
			if(mapRet.get(idea).equals(TipoIdeaRecomendacionEnum.CERCANA.name())){
				if(isLight(mapIdea.get(idea), username)!=null)
					dto.setIsLight(true);
				else
					dto.setIsLight(false);
				cercanas.add(dto);
			}
			if(mapRet.get(idea).equals(TipoIdeaRecomendacionEnum.LEJANA.name())){
				if(isLight(mapIdea.get(idea), username)!=null)
					dto.setIsLight(true);
				else
					dto.setIsLight(false);
				lejanas.add(dto);
			}
			if(mapRet.get(idea).equals(TipoIdeaRecomendacionEnum.MUY_LEJANA.name())){
				if(isLight(mapIdea.get(idea), username)!=null)
					dto.setIsLight(true);
				else
					dto.setIsLight(false);
				muyLejanas.add(dto);
			}
		}
		
		List<Usuario> noConexiones = usuRep.findMyNoConnections(username);
		List<ObjectId> idNoConexiones = noConexiones.stream().map(Usuario::getId).collect(Collectors.toList());
		List<Idea> noConexionesIdea = ideaRep.findIdeaRedEmpezar(idNoConexiones);
		mapIdea = new HashMap<>();
		facts = new ArrayList<>();
		for (Idea idea : noConexionesIdea) {
			d = distBean.calcularDistanciaJaccard(usuario.getAreasConocimiento(),
					idea.getUsuario().getAreasConocimiento());
			mapIdea.put(idea.getId(), idea);
			facts.add(new IdeaFact(idea.getId(),d,false));
		}
		mapRet= ruleBean.fireRules(facts,GLOBAL_RULES, new HashMap<String,String>());
		for (String id : mapRet.keySet()) {
			dto = MapEntities.mapIdeaToDTO(mapIdea.get(id));
			if(mapRet.get(id).equals(TipoIdeaRecomendacionEnum.LEJANA.name())){
				if(isLight(mapIdea.get(id), username)!=null)
					dto.setIsLight(true);
				else
					dto.setIsLight(false);
				lejanas.add(dto);
			}
			if(mapRet.get(id).equals(TipoIdeaRecomendacionEnum.MUY_LEJANA.name())){
				if(isLight(mapIdea.get(id), username)!=null)
					dto.setIsLight(true);
				else
					dto.setIsLight(false);
				muyLejanas.add(dto);
			}
		}
		cercanas.addAll(lejanas);
		cercanas.addAll(muyLejanas);
		return cercanas;
	}
	
	@Override
	public List<IdeaDTO> findIdeas(List<Tag> tags, String criterio,String username) {
		if(criterio.equals("tag")){
			return findByTags(tags,username);
		}
		if(criterio.equals("continuar")){
			return findContinuar(username);
		}
		if(criterio.equals("nueva")){
			return findNuevas(username);
		}
		if(criterio.equals("proyecto")){
			return findProyectos(username);
		}
		if(criterio.equals("continuar")){
			return findContinuar(username);
		}
		return null;
	}
}
