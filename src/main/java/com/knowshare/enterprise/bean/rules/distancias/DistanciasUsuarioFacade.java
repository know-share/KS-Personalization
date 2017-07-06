/**
 * 
 */
package com.knowshare.enterprise.bean.rules.distancias;

import com.knowshare.dto.perfilusuario.UsuarioDTO;

/**
 * @author miguel
 *
 */
public interface DistanciasUsuarioFacade {
	
	double calcularDistanciaEntreEstudiantes(UsuarioDTO usuario1, UsuarioDTO usuario2);

}
