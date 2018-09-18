package world.livn.log.server.configuration;

import java.lang.reflect.Field;

import javax.servlet.ServletContextEvent;

import org.apache.log4j.LogManager;
import org.apache.log4j.helpers.FileWatchdog;

import com.goodkarmacompanies.commons.logging.LogTracer;

public class ConfigurationListener extends AbstractServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {

		try {
			//Enabling this will instruct the JVM to include the Unicode Consortium's Common Locale Data Repository (CLDR) in its Locale data.
			//This might yield better localisation coverage, e.g. better localised country names etc
			//System.setProperty("java.locale.providers", "JRE,CLDR");

			this.initialiseServletContextPath(sce);

			@SuppressWarnings("unused")
			Registry reg = Registry.INSTANCE;

			LogTracer.trace("== ConfigurationListener initialising ===",0);

			LogTracer.trace("== ConfigurationListener complete ===",0);

		} catch (Throwable t) {
			this.abortServerStartup(t);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {

		LogTracer.trace("== ConfigurationListener shutting down ===",0);

		//Stop watching the config file
		//RegistryGlobal.INSTANCE.getConfiguration().setReloadingStrategy(null);

		LogTracer.trace("== ConfigurationListener shut down (...almost, just need to shutdown Log4j ;) ) ===",0);

		// Shut down log4j (and its config file watchdog)
		try {
			Thread[] threads = new Thread[Thread.activeCount()*2];
			Thread.enumerate(threads);
			FileWatchdog fileWatchDog = null;
			for (Thread thread : threads) {
				if (thread instanceof FileWatchdog) {
					fileWatchDog = (FileWatchdog) thread;
					break;
				}
			}

			if(fileWatchDog!=null) {
				//Set field interrupted to true. Access modifier is default so need to elevate permission
				Field f = FileWatchdog.class.getDeclaredField("interrupted");
				f.setAccessible(true);
				f.setBoolean(fileWatchDog, true);
				//Now interrupt the thread, to skip forward to the next internal check of interrupted boolean and subsequently terminate thread.
				fileWatchDog.interrupt();
			}
		} catch (Exception e) {
			System.err.println("Failed to stop Log4j config FileWatchdog thread");
			e.printStackTrace(System.err);
		}

		try {
			LogManager.shutdown();
		} catch (Exception e) {}

		try {
			Thread.sleep(1500);
		} catch (InterruptedException e) {}

	}
}
