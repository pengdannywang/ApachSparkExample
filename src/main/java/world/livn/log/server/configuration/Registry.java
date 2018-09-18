package world.livn.log.server.configuration;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import javax.naming.ConfigurationException;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.goodkarmacompanies.commons.logging.LogTracer;
import com.goodkarmacompanies.commons.logging.SystemOutErrToLog;

public enum Registry {

	INSTANCE;

	private static class _ConfigKey {

		private static final String DEVELOPER_MODE = "developerMode";

		/**
		 * The application root, should always start and end with a /<br>
		 * E.g.: '<code>/livn-geocoding/</code>' or just '<code>/</code>'
		 */
		private static final String APP_ROOT = "appRoot";
		/**
		 * The application hostname without protocol and trailing <code>/</code>
		 * , e.g. localhost or central.livngds.com
		 */
		private static final String APP_HOSTNAME = "appHostname";
		private static final String APP_DISPLAY_NAME = "appDisplayName";

		/**
		 * The list of IPs and host names that "belong" to Livn
		 */
		private static final String LIVN_IPS_AND_HOSTS = "livnIPsAndHosts";
	}

	private static class _DefaultValue {

		/**
		 * The list of IPs and host names that "belong" to Livn
		 * <p>
		 * <ul>
		 *
		 * <li>Livn HQ: 203.219.109.178</li>
		 * <li>Andi: 27.32.55.204</li>
		 * <li>Hans: 110.174.189.97</li>
		 *
		 * <li>central.livngds.com: 45.33.63.90</li>
		 * <li>dev1.livngds.com: 192.155.85.185</li>
		 * <li>gds1.livngds.com: 192.155.83.114</li>
		 * <li>gh1.livngds.com: 74.207.253.177</li>
		 * <li>rb1.livngds.com: 45.33.34.143</li>
		 * <li>boe1.livngds.com: 50.116.2.140</li>
		 *
		 * <li>sp1.livngds.com: 66.175.219.199</li>
		 * <li>stag01.livngds.com: 66.175.223.47</li>
		 * <li>stag02.livngds.com: 66.228.35.149</li>
		 *
		 * <li>vh1.goodkarmacompanies.net: 173.230.147.210</li>
		 * <li>vh2.goodkarmacompanies.net: 74.207.252.84</li>
		 * <li>vh3.goodkarmacompanies.net: 66.175.216.38</li>
		 * </ul>
		 * </p>
		 */
		private static final String LIVN_IPS_AND_HOSTS = String.join(",", new String[] {
				"203.219.109.178", //Livn HQ
				"27.32.55.204", //Andi Home
				"110.174.189.97", //Hans Home

				"45.33.63.90", //central
				"192.155.85.185", //dev1
				"192.155.83.114", //gds1
				"74.207.253.177", //gh1
				"45.33.34.143", //rb1
				"50.116.2.140", //boe1

				"66.175.219.199", //sp1
				"66.175.223.47", //stag01
				"66.228.35.149", //stag02

				"173.230.147.210", //vh1
				"74.207.252.84", //vh2
				"66.175.216.38", //vh3
		});
	}

	public static final String APPLICATION_NAME = "livn-log";

	private static final String LOGGER_NAME = "world.livn.log";
	private static final String DEV_LOGGER_NAME = "world.livn.log.devLogger";

	private static final String TIMEZONE = "Australia/Sydney"; // Or rather UTC?

	/** Name of the log4j configuration file */
	private static final String LOG4J_PROPERTIES_FILE_NAME = "_log4j.properties";

	/*
	 * REST API path
	 */
	public static final String GEOCODING_API_BASE_PATH = "api";

	/**
	 * Array of all application configuration .properties files in the order
	 * they are to be loaded
	 */
	private final String[] _configFiles;

	/** The base directory for all external data */
	private final String _baseDir;

	/** Full name of the log4j configuration file */
	private final String _log4jConfigFile;

	private final Locale _serverLocale = new Locale("en", "AU");

	private Logger _logger;
	private Logger _traceLogger;
	private Logger _devLogger;

	/** The combined configuration used throughout the application */
	private final CompositeConfiguration _configuration = new CompositeConfiguration();

	private Registry() {

		TimeZone.setDefault(TimeZone.getTimeZone(Registry.TIMEZONE));
		DateTimeZone.setDefault(DateTimeZone.forID(Registry.TIMEZONE));

		String servletContextPath = ServletContextPath.INSTANCE.get();

		if (servletContextPath == null) {
			throw new NullPointerException(
					"The Servlet Context Path has not been set. It is required to locate the application configuration files.");
		}

		this._baseDir = System.getProperty("appconfig.base") + SystemConstant.FILE_SEPARATOR
				+ Registry.APPLICATION_NAME + servletContextPath + SystemConstant.FILE_SEPARATOR;

		this._log4jConfigFile = this._baseDir + Registry.LOG4J_PROPERTIES_FILE_NAME;

		try {
			// Set up the log4j logger
			this._logger = Logger.getLogger(Registry.LOGGER_NAME);
			this._traceLogger = Logger.getLogger(LogTracer.class);
			this._devLogger = Logger.getLogger(Registry.DEV_LOGGER_NAME);
			BasicConfigurator.resetConfiguration(); //In case there is an interfering log4j.properties hidden anywhere in our classpath
			PropertyConfigurator.configureAndWatch(this._log4jConfigFile);
			LogTracer.trace(this._log4jConfigFile + " loaded.", 0);

		} catch (final Exception e) {
			// loading log4j.properties failed
			this._logger = Logger.getRootLogger();
			this._traceLogger = Logger.getRootLogger();
			this._devLogger = Logger.getRootLogger();
			System.out.println("LOADING OF log4j.properties FAILED !!! Cause: " + e.toString());
			System.out.println("INITIALISATION OF RegistryGlobal FAILED !!! Cause: " + e.toString());
			throw new ExceptionInInitializerError(e);
		}

		try {

			/*
			 * Array of all application configuration .properties files in the
			 * order they are to be loaded
			 */
			this._configFiles = new String[] {"_config.properties"};

			for (String fileName : this._configFiles) {
				final String filePath = this._baseDir + fileName;
				CustomPropertiesConfiguration conf = new CustomPropertiesConfiguration(filePath);
				this._configuration.addConfiguration(conf);
			}

			if (this.logging.getPurgeLogsAtStart()) {
				PropertiesConfiguration c = new PropertiesConfiguration(this._log4jConfigFile);
				String log4jDirStr = c.getString("log.dir", null);
				File log4jDir = StringUtils.isNotBlank(log4jDirStr) ? new File(log4jDirStr) : null;

				if (log4jDir != null && log4jDir.canRead() && log4jDir.canWrite()) {
					Collection<File> files = FileUtils.listFiles(log4jDir, null, false);
					String backupName = "logs_backup_" + DateTime.now().toString("yyyy-MM-dd_HH-mm-ss");
					File backupDir = new File(log4jDir, backupName);
					for (File f : files) {

						if (!f.isFile()) {
							continue;
						}

						if (!f.canRead() || !f.canWrite()) {
							continue; // Skip files that Tomcat cannot handle,
							// e.g. because they belong to root (we
							// had the case with zip-ped log files,
							// causing the application to fail on
							// boot)
						}

						if (FileUtils.sizeOf(f) > 0) {
							FileUtils.copyFileToDirectory(f, backupDir, true);
						}

						if (FilenameUtils.isExtension(f.getName(), "log")) {
							// Purge *.log files for reuse
							FileUtils.write(f, "", false);
						} else {
							// Delete any files that aren't called *.log, such
							// as dev.log.2016-06-14, since these are not being
							// reused by log4j
							FileUtils.deleteQuietly(f);
						}
					}
				}
			}

			if (StringUtils.isBlank(this.getAppHostname())) {
				throw new ConfigurationException("Missing mandatory config property: " + _ConfigKey.APP_HOSTNAME);
			}

			if (StringUtils.isBlank(this.getAppDisplayName())) {
				throw new ConfigurationException("Missing mandatory config property: " + _ConfigKey.APP_DISPLAY_NAME);
			}

			if (StringUtils.isBlank(this.getAppRoot())) {
				throw new ConfigurationException("Missing mandatory config property: " + _ConfigKey.APP_ROOT);
			}

			try {
				this.getDeveloperMode();
			} catch (Exception e) {
				throw new ConfigurationException("Missing mandatory config property: " + _ConfigKey.DEVELOPER_MODE);
			}

			// Enable System.out and System.err log4j logging
			if (this.logging.getLogSystemOutToLogger()) {
				SystemOutErrToLog.setOutToLog();
			}

			if (this.logging.getLogSystemErrToLogger()) {
				SystemOutErrToLog.setErrToLog();
			}

		} catch (final Exception e) {
			LogTracer.trace("INITIALISATION OF RegistryGlobal FAILED !!! Cause: " + e.toString());
			throw new ExceptionInInitializerError(e);
		} finally {

		}
	}

	/**
	 * @return Mandatory! The application hostname without the protocol and
	 *         without a trailing <code>/</code>
	 */
	public String getAppHostname() {
		return this.getConfiguration().getString(_ConfigKey.APP_HOSTNAME, null);
	}

	/**
	 * @return Mandatory! The application root. Should always start and end with
	 *         a /<br>
	 *         E.g.: '<code>/livngds/</code>' or just '<code>/</code>'
	 */
	public String getAppRoot() {
		return this.getConfiguration().getString(_ConfigKey.APP_ROOT, null);
	}

	/**
	 * @return The full application base URL. Including trailing "/"<br>
	 *         This is equal to "https://" + {@link #getAppHostname()} +
	 *         {@link #getAppRoot()}
	 */
	public String getAppUrl() {
		return "https://" + this.getAppHostname() + this.getAppRoot();
	}

	/**
	 *
	 * @return Mandatory! Indicates whether application is run on a development
	 *         or production server.
	 * @throws IllegalStateException
	 */
	public boolean getDeveloperMode() {
		try {
			Boolean devMode = this.getConfiguration().getBoolean(_ConfigKey.DEVELOPER_MODE, null);
			return devMode.booleanValue();
		} catch (Exception e) {
			throw new IllegalStateException("The boolean configuration key developerMode must be set!", e);
		}
	}



	public Logger getLogger() {
		return this._logger;
	}

	public String getLog4jConfigFile() {
		return this._log4jConfigFile;
	}

	public Logger getTraceLogger() {
		return this._traceLogger;
	}

	/**
	 *
	 * @return a Logger that is meant to be configured to log to
	 *         RollingFileAppender on ALL levels
	 */
	public Logger getDevLogger() {
		return this._devLogger;
	}

	public Locale getServerLocale() {
		return this._serverLocale;
	}

	/**
	 *
	 * NOTE!: Use of this method and the properties is discouraged as they do
	 * not necessarily represent the final state of the respective Registry
	 * fields
	 *
	 * @return the Configuration object we initialise with values form the
	 *         external config.properties file
	 */
	public CompositeConfiguration getConfiguration() {
		return this._configuration;
	}

	/**
	 * @return the configuration base directory with a trailing /, e.g.
	 *         /opt/tomcatAppSettings/livn-geocoding/livn-geocoding/
	 */
	public String getBaseDir() {
		return this._baseDir;
	}

	public String[] getConfigFiles() {
		return this._configFiles;
	}

	/**
	 * @return a Set of all IP addresses and hostnames that "belong" to Livn
	 */
	public Set<String> getLivnIPsAndHosts() {

		final String ipsStr = Registry.this.getConfiguration().getString(_ConfigKey.LIVN_IPS_AND_HOSTS,
				_DefaultValue.LIVN_IPS_AND_HOSTS);

		// System.out.println("ipsStr: "+ipsStr);

		final HashSet<String> ret = new HashSet<>();

		if (StringUtils.isBlank(ipsStr)) {
			return ret;
		}

		final String[] ipsArr = ipsStr.split(",");

		// System.out.println(Arrays.toString(ipsArr));

		if (ipsArr == null || ipsArr.length == 0) {
			return ret;
		}

		for (String ip : ipsArr) {
			ip = StringUtils.trim(ip);
			if (StringUtils.isNotBlank(ip)) {
				ret.add(ip);
			}
		}

		return ret;
	}

	/**
	 * @return Mandatory! The application display name. E.g. Livn Geocoding API
	 */
	public String getAppDisplayName() {
		return this.getConfiguration().getString(_ConfigKey.APP_DISPLAY_NAME, null);
	}

	/** Everything related to logging, log4j & Co */
	public final Logging logging = new Logging();

	/**
	 * Everything related to the external user area, i.e claims etc
	 */
	public class Logging {

		private class _ConfigKey {
			private static final String PURGE_LOGS_AT_START = "logging.purgeLogsAtStart";
			private static final String SYSTEM_OUT_TO_LOGGER = "logging.systemOut";
			private static final String SYSTEM_ERR_TO_LOGGER = "logging.systemErr";
		}

		private Logging() {

		}

		/**
		 * Indicates whether all log4j log files are to be purged on startup.
		 * All non-empty files will be moved to a date labelled folder before
		 * all files are deleted/truncated. Default: true
		 */
		public boolean getPurgeLogsAtStart() {
			return Registry.this.getConfiguration().getBoolean(_ConfigKey.PURGE_LOGS_AT_START, true);
		}

		/**
		 * If set to true, System.out writes to a log4j logger called
		 * log4j.logger.sysout. Default: false
		 */
		private boolean getLogSystemOutToLogger() {
			return Registry.this.getConfiguration().getBoolean(_ConfigKey.SYSTEM_OUT_TO_LOGGER, false);
		}

		/**
		 * If set to true, we System.err writes to a log4j logger called
		 * log4j.logger.syserr. Default: false
		 */
		private boolean getLogSystemErrToLogger() {
			return Registry.this.getConfiguration().getBoolean(_ConfigKey.SYSTEM_ERR_TO_LOGGER, false);
		}
	}

}// end class
