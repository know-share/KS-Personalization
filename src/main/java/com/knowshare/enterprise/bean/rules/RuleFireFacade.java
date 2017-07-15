/**
 * 
 */
package com.knowshare.enterprise.bean.rules;

/**
 * @author miguel
 *
 */
public interface RuleFireFacade {

	<T> T fireRules(Object item,String global,T clazz);
}
