package allow.adaptation;

import allow.adaptation.api.CollectiveAdaptationCommandExecution;
import allow.adaptation.api.RoleCommand;

public class DummyExecution implements CollectiveAdaptationCommandExecution {

    @Override
    public void applyCommand(String ensemble, RoleCommand command) {
	// TODO Auto-generated method stub
	System.out.println(command);

    }

    @Override
    public void endCommand() {
	// TODO Auto-generated method stub
	System.out.println("End Command");

    }

}
