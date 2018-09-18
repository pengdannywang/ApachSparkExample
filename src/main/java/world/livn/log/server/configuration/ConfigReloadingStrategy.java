package world.livn.log.server.configuration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;

public class ConfigReloadingStrategy extends FileChangedReloadingStrategy {

	private static final long REFRESH_DELAY = 20 * 1000L; // By default only check if the config file has been modified every 20s

	public ConfigReloadingStrategy() {
		this.setRefreshDelay(ConfigReloadingStrategy.REFRESH_DELAY);
	}

}