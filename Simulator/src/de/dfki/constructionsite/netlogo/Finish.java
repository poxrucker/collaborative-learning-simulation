package de.dfki.constructionsite.netlogo;

import org.nlogo.api.Argument;
import org.nlogo.api.Context;
import org.nlogo.api.DefaultCommand;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.Syntax;

import allow.simulator.netlogo.agent.NetLogoWrapper;

public class Finish extends DefaultCommand {
		
	@Override
	public void perform(Argument[] args, Context context) throws ExtensionException, LogoException {
		int runId = args[0].getIntValue();
		
		try {
			NetLogoWrapper.delete(runId);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Syntax getSyntax() {
		int right[] = new int[] { Syntax.NumberType() };
		return Syntax.commandSyntax(right);
	}
}
