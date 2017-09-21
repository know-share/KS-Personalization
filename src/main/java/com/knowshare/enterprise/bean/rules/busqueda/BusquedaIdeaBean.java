/**
 * 
 */
package com.knowshare.enterprise.bean.rules.busqueda;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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
import com.knowshare.entities.ludificacion.CualidadAval;
import com.knowshare.entities.ludificacion.HabilidadAval;
import com.knowshare.entities.perfilusuario.InfoUsuario;
import com.knowshare.entities.perfilusuario.Usuario;
import com.knowshare.enums.TipoCualidadEnum;
import com.knowshare.enums.TipoHabilidadEnum;
import com.knowshare.enums.TipoIdeaEnum;
import com.knowshare.enums.TipoOperacionEnum;
import com.knowshare.enums.TipoUsuariosEnum;
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
	private static final int PAGE_SIZE = 10;

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
	public Page<IdeaDTO> findRed(String username,Integer page){
		final Usuario usu = usuRep.findByUsernameIgnoreCase(username);
		final List<InfoUsuario> red = usu.getAmigos();
		red.addAll(usu.getSiguiendo());
		final List<String> usernamesRed = new ArrayList<>();
		for (InfoUsuario inf : red)
			usernamesRed.add(inf.getUsername());
		final List<ObjectId> usuariosId = usuRep.findUsuariosByUsername(usernamesRed).stream().map(Usuario::getId)
				.collect(Collectors.toList());
		final Page<Idea> pageable = ideaRep.findIdeaRed(usuariosId,new PageRequest(page, PAGE_SIZE)); 
		final List<Idea> ideas = pageable.getContent();
		List<IdeaDTO> dtos = mapIdeas(username, ideas);
		return new PageImpl<>(dtos, new PageRequest(page, pageable.getSize()), pageable.getTotalElements());
	}
	
	private  List<IdeaDTO> findIdeasProfesor(List<Idea> red,Usuario usuario){
		double relevancia;
		Set<String> set ;
		Map<String,Integer> tags;
		List<Idea> paraRecomendar = new ArrayList<>();
		List<Idea> paraNoRecomendar = new ArrayList<>();
		Map<String,Idea> mapIdea = new HashMap<>();
		List<IdeaFact> facts = new ArrayList<>();
		Map<String,String> mapRet;	
		List<CualidadAval> cualidadesProfesionalesUsuario = new ArrayList<>();
		List<CualidadAval> cualidadesProfesionales = new ArrayList<>();
		for (CualidadAval cualidadAval : usuario.getCualidadesProfesor()) {
			if(cualidadAval.getCualidad().getTipo().equals(TipoCualidadEnum.PROFESIONAL))
				cualidadesProfesionalesUsuario.add(cualidadAval);
		}
		for (Idea idea : red) {
			for (CualidadAval cualidadAval : idea.getUsuario().getCualidadesProfesor()) {
				if(cualidadAval.getCualidad().getTipo().equals(TipoCualidadEnum.PROFESIONAL))
					cualidadesProfesionales.add(cualidadAval);
			}
			relevancia = 0;
			tags = new HashMap<>();
			for (Tag t :idea.getTags()) {
				tags.put(t.getId(), Integer.valueOf(1));
			}
			if(idea.getUsuario().getTipo().equals(TipoUsuariosEnum.ESTUDIANTE)){
				relevancia += distBean.calcularDistanciaPrefIdeaTags(usuario.getPreferenciaIdeas(), tags);
				relevancia += distBean.calcularDistanciaJaccard(cualidadesProfesionalesUsuario, cualidadesProfesionales);//PENDIENTE
				relevancia = distBean.normalizarDistancia(relevancia, 2);
			}else if(idea.getUsuario().getTipo().equals(TipoUsuariosEnum.PROFESOR)){
				if(idea.getTipo().equals(TipoIdeaEnum.PE) || idea.getTipo().equals(TipoIdeaEnum.PR)){
//					for (Tag t :idea.getTags()) {
//						tags.put(t.getId(), new Integer(1));
//					}
					relevancia += distBean.calcularDistanciaPrefIdeaTags(usuario.getPreferenciaIdeas(), tags);
					relevancia += distBean.calcularDistanciaJaccard(usuario.getAreasConocimiento(), idea.getUsuario().getAreasConocimiento());
					relevancia = distBean.normalizarDistancia(relevancia, 2);	
				}else{
					relevancia = -1;
				}
			}else if(usuario.getTipo().equals(TipoUsuariosEnum.EGRESADO)){
				
				if(idea.getTipo().equals(TipoIdeaEnum.NU)){
//					List<HabilidadAval> hb = new ArrayList<>();
//					for (HabilidadAval habilidadAval : idea.getUsuario().getHabilidades()) {
//						if(habilidadAval.getHabilidad().getTipo().equals(TipoHabilidadEnum.PROFESIONALES)){
//							hb.add(habilidadAval);
//						}
//					}
					relevancia += distBean.calcularDistanciaPrefIdeaTags(usuario.getPreferenciaIdeas(), tags);
					relevancia += distBean.calcularDistanciaJaccard(cualidadesProfesionalesUsuario, cualidadesProfesionales);					
					relevancia = distBean.normalizarDistancia(relevancia, 2);
				}else{
					relevancia = -1;
				}
			}
			mapIdea.put(idea.getId(),idea);
			facts.add(new IdeaFact(idea.getId(), relevancia,false));
			//TODO reglas en drools
		}
		return mapIdeas(usuario.getUsername(), paraRecomendar);
	}
	
	private  List<IdeaDTO> findIdeasEgresado(List<Idea> red,Usuario usuario){
		double relevancia;
		Set<String> set ;
		Map<String,Integer> tags;
		List<Idea> paraRecomendar = new ArrayList<>();
		List<Idea> paraNoRecomendar = new ArrayList<>();
		Map<String,Idea> mapIdea = new HashMap<>();
		List<IdeaFact> facts = new ArrayList<>();
		Map<String,String> mapRet;
		List<HabilidadAval> habilidadesUsuario = new ArrayList<>();
		List<HabilidadAval> habilidadesUsuarioIdea = new ArrayList<>();
		for (HabilidadAval habilidadAval : usuario.getHabilidades()) {
			if(habilidadAval.getHabilidad().getTipo().equals(TipoHabilidadEnum.PROFESIONALES)){
				habilidadesUsuario.add(habilidadAval);
			}
		}
		for (Idea idea : red) {
			for (HabilidadAval habilidadAval : idea.getUsuario().getHabilidades()) {
				if(habilidadAval.getHabilidad().getTipo().equals(TipoHabilidadEnum.PROFESIONALES)){
					habilidadesUsuarioIdea.add(habilidadAval);
				}
			}
			relevancia = 0;
			tags = new HashMap<>();
			for (Tag t :idea.getTags()) {
				tags.put(t.getId(), Integer.valueOf(1));
			}
			if(idea.getUsuario().getTipo().equals(TipoUsuariosEnum.ESTUDIANTE)){
				relevancia += distBean.calcularDistanciaPrefIdeaTags(usuario.getPreferenciaIdeas(), tags);
				relevancia += distBean.calcularDistanciaJaccard(habilidadesUsuario, habilidadesUsuarioIdea);//PENDIENTE
				relevancia = distBean.normalizarDistancia(relevancia, 2);
			}else if(idea.getUsuario().getTipo().equals(TipoUsuariosEnum.PROFESOR)){
				if(idea.getTipo().equals(TipoIdeaEnum.PR)){
//					for (Tag t :idea.getTags()) {
//						tags.put(t.getId(), new Integer(1));
//					}
					relevancia += distBean.calcularDistanciaPrefIdeaTags(usuario.getPreferenciaIdeas(), tags);
//					relevancia = distBean.normalizarDistancia(relevancia, 2);	
				}else{
					relevancia = -1;
				}
			}else if(usuario.getTipo().equals(TipoUsuariosEnum.EGRESADO)){
				if(idea.getTipo().equals(TipoIdeaEnum.NU)){
					relevancia += distBean.calcularDistanciaPrefIdeaTags(usuario.getPreferenciaIdeas(), tags);
					relevancia += distBean.calcularDistanciaJaccard(habilidadesUsuario, habilidadesUsuarioIdea);//PENDIENTE
					relevancia = distBean.normalizarDistancia(relevancia, 2);
				}else{
					relevancia = -1;
				}
			}
			mapIdea.put(idea.getId(),idea);
			facts.add(new IdeaFact(idea.getId(), relevancia,false));
			//TODO reglas en drools
		}
		return mapIdeas(usuario.getUsername(), paraRecomendar);
	}
	
	private  List<IdeaDTO> findIdeasEstudiante(List<Idea> red,Usuario usuario){
		double relevancia;
		Set<String> set ;
		Map<String,Integer> tags;
		List<Idea> paraRecomendar = new ArrayList<>();
		List<Idea> paraNoRecomendar = new ArrayList<>();
		Map<String,Idea> mapIdea = new HashMap<>();
		List<IdeaFact> facts = new ArrayList<>();
		Map<String,String> mapRet;	
		for (Idea idea : red) {
			relevancia = 0;
			tags = new HashMap<>();
			for (Tag t :idea.getTags()) {
				tags.put(t.getId(), Integer.valueOf(1));
			}
			if(idea.getUsuario().getTipo().equals(TipoUsuariosEnum.ESTUDIANTE)){
				relevancia += distBean.calcularDistanciaPrefIdeaTags(usuario.getPreferenciaIdeas(), tags);
				relevancia += distBean.calcularDistanciaEnfasis(usuario.getEnfasis(), idea.getUsuario().getEnfasis());
				relevancia = distBean.normalizarDistancia(relevancia, 2);
			}else if(idea.getUsuario().getTipo().equals(TipoUsuariosEnum.PROFESOR)){
				if(idea.getTipo().equals(TipoIdeaEnum.PC) || idea.getTipo().equals(TipoIdeaEnum.PE)){
//					for (Tag t :idea.getTags()) {
//						tags.put(t.getId(), new Integer(1));
//					}
					if(idea.getTipo().equals(TipoIdeaEnum.PC)){
						set = idea.getUsuario().getPreferenciaIdeas().keySet();
						for (String idTag : set) {
							if(!tags.containsKey(idTag)){
								tags.put(idTag, idea.getUsuario().getPreferenciaIdeas().get(idTag));
							}
						}
					}
					
					relevancia += distBean.calcularDistanciaPrefIdeaTags(usuario.getPreferenciaIdeas(), tags);
					relevancia += distBean.calcularDistanciaAreasExperticia(usuario.getAreasConocimiento(), idea.getUsuario().getAreasConocimiento());
					relevancia = distBean.normalizarDistancia(relevancia, 2);	
				}else{
					relevancia = -1;
				}
			}else if(usuario.getTipo().equals(TipoUsuariosEnum.EGRESADO)){
				if(idea.getTipo().equals(TipoIdeaEnum.PC)){
					for (Tag t :idea.getTags()) {
						tags.put(t.getId(), new Integer(1));
					}
				}
				relevancia += distBean.calcularDistanciaPrefIdeaTags(usuario.getPreferenciaIdeas(), tags);
				//TODO no hay que normalizar???
				
			}
			mapIdea.put(idea.getId(),idea);
			facts.add(new IdeaFact(idea.getId(), relevancia,false));
			//TODO reglas en drools
		}
		return mapIdeas(usuario.getUsername(), paraRecomendar);
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
		List<ObjectId> ids;
		if(tipo.equals(TipoIdeaEnum.PR))
			ids = usuRep.findUsuariosByUsernameProfesor(usuariosConexion)
				.stream()
				.map(Usuario::getId)
				.collect(Collectors.toList());
		else
			ids = usuRep.findUsuariosByUsername(usuariosConexion)
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