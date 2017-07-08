/**
 * 
 */
package com.knowshare.enterprise.bean.rules.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author miguel
 *
 */
public class OperacionsConjuntos {
	
	@SuppressWarnings("unchecked")
	public static <T> List<T> union(List<T> set1, List<T> set2){
		return ((List<T>)Arrays.asList(set1,set2));
	}
	
	public static <T> List<T> interseccion(List<T> set1, List<T> set2){
		List<T> interseccion = new ArrayList<>(set1);
		interseccion.retainAll(set2);
		return interseccion;
	}

}
