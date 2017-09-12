/**
 * 
 */
package com.knowshare.enterprise.bean.rules.busqueda;

import java.util.ArrayList;
import java.util.List;
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
import com.knowshare.enterprise.repository.idea.IdeaRepository;
import com.knowshare.enterprise.repository.perfilusuario.UsuarioRepository;
import com.knowshare.enterprise.utils.MapEntities;
import com.knowshare.entities.idea.Idea;
import com.knowshare.entities.idea.OperacionIdea;
import com.knowshare.entities.idea.Tag;
import com.knowshare.entities.perfilusuario.InfoUsuario;
import com.knowshare.entities.perfilusuario.Usuario;
import com.knowshare.enums.TipoOperacionEnum;

/**
 * @author HP
 *
 */
@Component
public class BusquedaIdeaBean implements BusquedaIdeaFacade {
	
	@Autowired
	private IdeaRepository ideaRep;
	
	@Autowired
	private UsuarioRepository usuRep;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
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
//		for (Idea i : ideas) {
//			dtos.add(MapEntities.mapIdeaToDTO(i));
//		}
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
		return null;
	}
}
