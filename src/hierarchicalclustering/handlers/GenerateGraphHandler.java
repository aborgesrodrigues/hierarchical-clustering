package hierarchicalclustering.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import br.ufpe.cin.GraphGeneration;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class GenerateGraphHandler extends AbstractHandler {
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		GraphGeneration.main();
		return null;
	}
	
}
