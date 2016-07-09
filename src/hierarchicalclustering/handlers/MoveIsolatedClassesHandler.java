package hierarchicalclustering.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import br.ufpe.cin.MoveIsolatedClasses;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class MoveIsolatedClassesHandler extends AbstractHandler {
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		MoveIsolatedClasses.main();
		return null;
	}
	
}
