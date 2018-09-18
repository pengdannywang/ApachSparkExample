package world.livn.log.server.configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class CustomPropertiesConfiguration extends PropertiesConfiguration {

	public CustomPropertiesConfiguration(final String fileName) throws ConfigurationException {

		super();

		this.setReloadingStrategy(new ConfigReloadingStrategy());
		this.setDelimiterParsingDisabled(true);

		this.setFileName(fileName);
		this.load(fileName);
	}



}