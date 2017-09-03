/**
 * 
 */
package com.knowshare.enterprise.bean.rules.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Clase utilitario que contiene operaciones entre conjuntos.
 * @author Miguel Montañez
 *
 */
public class OperacionsConjuntos {
	
	private OperacionsConjuntos(){}
	
	/**
	 * Union entre dos conjuntos
	 * @param set1 Conjunto 1
	 * @param set2 Conjunto 2
	 * @return Union de los conjuntos.
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> union(List<T> set1, List<T> set2){
		return ((List<T>)Arrays.asList(set1,set2));
	}
	
	/**
	 * Interseccion entre dos conjuntos
	 * @param set1 Conjunto 1
	 * @param set2 Conjunto 2
	 * @return Interseccion de los dos conjuntos
	 */
	public static <T> List<T> interseccion(List<T> set1, List<T> set2){
		List<T> interseccion = new ArrayList<>(set1);
		interseccion.retainAll(set2);
		return interseccion;
	}

}
