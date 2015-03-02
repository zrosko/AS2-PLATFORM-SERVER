package hr.as2.inf.server.rules;
/* Draft:
 * Primjer upotrebe je CreditApplicationUI koji poziva CreditApplicationProxy.addCreditApplication.
 * Ulazni parametri su: iznos, placa, hipoteka (sa ekrana).
 * 
 *
 */
import hr.as2.inf.common.core.AS2Context;
import hr.as2.inf.common.exceptions.AS2Exception;
import hr.as2.inf.common.logging.AS2Trace;

import java.util.ArrayList;
import java.util.List;

//import com.yasutech.qrules.engine.*;
//import com.yasutech.qrules.io.FileRulePersistenceHandler;

public class J2EERuleService
{
	private static J2EERuleService _instance = null;
	//private RuleEngineFactory factory;
	private static String RULESET_DIR;
	private J2EERuleService() throws AS2Exception{
		initialize();
		AS2Context.setSingletonReference(this);
	}
	public static J2EERuleService getInstance() {
		if (_instance == null)
			try {
				_instance = new J2EERuleService();
			} catch (AS2Exception e) {
				AS2Trace.trace(AS2Trace.E, e, "J2EERuleService.Constructor failed?");
			}
		return _instance;
	}
	private void initialize() throws AS2Exception {
		try {
			RULESET_DIR = AS2Context.getInstance().RULESET_DIR;
			// This is how you get an instance of RuleEngineFactory. 
			//factory = RuleEngineFactory.getFactory();
			// PersistenceHandler can not be reset once it is set 

			//Check whether the persistence Handler is set or not. 
			//if it is not set then set it. 
			//if (!factory.isPersistenceHandlerSet())
				//factory.setPersistenceHandler(new FileRulePersistenceHandler(RULESET_DIR));
		} catch (Exception e) {
			AS2Exception ex = new AS2Exception("351");
			ex.addCauseException(e);
			throw ex;
		}
	}
	public List invokeRuleset(String RULESETNAME, List businessObjects) {
		try {
			//temp return
			return new ArrayList(1);
			//return factory.getRuleEngine().invokeRuleset(RULESETNAME, businessObjects); 
		//} catch (NoSuchRuleException e1) {
			//J2EETrace.trace(J2EETrace.E, e1, "J2EERuleService.NoSuchRuleException ?"+RULESETNAME);
		//} catch (RuleException e2) {
			//J2EETrace.trace(J2EETrace.E, e2, "J2EERuleService.RuleException ?"+RULESETNAME);
		} catch (Exception e3) {
			AS2Trace.trace(AS2Trace.E, e3, "J2EERuleService.Exception ?"+RULESETNAME);
		}
		return new ArrayList(1);
	}
}
