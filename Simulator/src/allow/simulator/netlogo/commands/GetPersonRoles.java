package allow.simulator.netlogo.commands;

import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultReporter;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Syntax;

import allow.simulator.entity.Person.Profile;

public class GetPersonRoles extends DefaultReporter
{
	@Override
	public Object report(Argument[] args, Context context) throws ExtensionException, LogoException {
		LogoListBuilder bldr = new LogoListBuilder();
		
		for (Profile profile : Profile.values()) {
			bldr.add(profile.toString());
		}
		return bldr.toLogoList();
	}
	
	public Syntax getSyntax() {
		return Syntax.reporterSyntax(Syntax.StringType());
	}
}
