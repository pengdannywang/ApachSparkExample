package world.livn.log.server.configuration;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.exception.ExceptionUtils;

public abstract class AbstractServletContextListener implements ServletContextListener {

	protected void initialiseServletContextPath(ServletContextEvent sce) {

		final String cp = sce.getServletContext().getContextPath();
		/*
		System.out.println("+++++++++++++++++++++++++++++++");
		System.out.println("+  Context Path: "+cp);
		System.out.println("+++++++++++++++++++++++++++++++");
		 */

		//This absolutely HAS TO HAPPEN BEFORE RegistryGlobal Singleton can be instantiated!!!
		ServletContextPath.INSTANCE.initialise(cp);
	}

	/**
	 * Attempts to log the provided Throwable as FATAL error (using System.err as backup), then immediately aborts the server start-up by issuing System.exit(-1).
	 * @param t Throwable that caused the abort
	 */
	protected void abortServerStartup(Throwable t) {
		try {
			Registry.INSTANCE.getLogger().fatal("== EPIC FAIL IN "+this.getClass().getSimpleName()+" !!! ===", t);
		} catch (Throwable t2) {
			System.err.println("== EPIC FAIL IN "+this.getClass().getSimpleName()+" !!! ===\n"+ExceptionUtils.getMessage(t)+"\n"+ExceptionUtils.getFullStackTrace(t));
		} finally {
			System.exit(-1);
		}
	}
}