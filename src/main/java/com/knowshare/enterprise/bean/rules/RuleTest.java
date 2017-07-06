/**
 * 
 */
package com.knowshare.enterprise.bean.rules;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.knowshare.dto.academia.CarreraDTO;

/**
 * @author miguel
 *
 */
@Component
public class RuleTest {

	@Autowired
	private KieContainer kieContainer;

	public void methodTest(String item) {
		StatelessKieSession testSession = kieContainer.newStatelessKieSession();

		testSession.execute(new CarreraDTO().setNombre("Probando"));
	}
}
