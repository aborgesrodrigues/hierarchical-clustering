package hierarchicalclustering.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import br.ufpe.cin.Properties;
import br.ufpe.cin.RemoveUselessClasses;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class RemoveUselessClassesHandler extends AbstractHandler {
	
	private Properties config = Properties.getInstance();
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		RemoveUselessClasses.main(config.getPathToUselessClassesCSVFile());
		return null;
	}
	
}
