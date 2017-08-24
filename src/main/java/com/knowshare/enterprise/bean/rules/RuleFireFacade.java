/**
 * 
 */
package com.knowshare.enterprise.bean.rules;

/**
 * Encargado de disparar las reglas de negocio para que la aplicación
 * pueda usarlas posteriormente.
 * @author Miguel Montañez
 *
 */
public interface RuleFireFacade {

	/**
	 * Dispara las reglas de negocio según unos parámetros.
	 * @param item, parámetro a ser evaluado por las reglas para su operación.
	 * @param global, nomble de la variable global donde será guardado el resultado
	 * @param clazz, Tipo de dato que se esperar retornar.
	 * @return
	 */
	<T> T fireRules(Object item,String global,T clazz);
}
