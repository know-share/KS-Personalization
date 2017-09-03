/**
 * 
 */
package com.knowshare.enterprise.bean.rules.config;

/**
 * Bean encargado de la gestión de las reglas, es decir,
 * de la actualización de las reglas con el motor de reglas.
 * @author Miguel Montañez
 *
 */
public interface RulesAdminFacade {
	
	/**
	 * Actualiza las reglas que se hayan editado en el motor de reglas
	 * para contar con la última versión de reglas.
	 * @return true si pudo actualizar, de lo contrario false.
	 */
	boolean updateRules();
	
	/**
	 * Revisa si las reglas en el sistema están encendidas o no
	 * @return verdadero si lo están de los contrario, falso.
	 */
	boolean isRulesOn();
	
	/**
	 * Actualiza el atributo rules de las preferencias del sistema
	 * @param state, estado de las reglas en el sistema 1 o 0
	 * @return verdadero si pudo realizar la actualización, de lo contrario
	 * falso
	 */
	boolean updateRulesSystem(short state);

}
