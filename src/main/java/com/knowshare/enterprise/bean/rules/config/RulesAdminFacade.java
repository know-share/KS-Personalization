/**
 * 
 */
package com.knowshare.enterprise.bean.rules.config;

/**
 * Bean encargado de la gestión de las reglas, es decir,
 * de la actualización de las reglas con el motor de reglas.
 * @author miguel
 *
 */
public interface RulesAdminFacade {
	
	/**
	 * Actualiza las reglas que se hayan editado en el motor de reglas
	 * para contar con la última versión de reglas.
	 * @return true si pudo actualizar, de lo contrario false.
	 */
	boolean updateRules();

}
