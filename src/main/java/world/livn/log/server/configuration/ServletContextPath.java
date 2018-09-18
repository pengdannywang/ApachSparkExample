package world.livn.log.server.configuration;
import org.apache.commons.lang.StringUtils;

public enum ServletContextPath {

	INSTANCE;

	/**
	 * The Servlet Context Path. This is required to locate the application
	 * instance's configuration files based on the global application name as
	 * well as the unique instance's servlet context, i.e. WAR file name. This
	 * is necessary to allow multiple instances of the same application on one
	 * tomcat server instance
	 */
	private String _servletContextPath = null;
	private final Object _lock = new Object();

	/**
	 * Initialises the Servlet Context Path. This is required once to locate the
	 * application instance's configuration files based on the global
	 * application name as well as the unique instance's servlet context, i.e.
	 * WAR file name. This is necessary to allow multiple instances of the same
	 * application on one tomcat server instance
	 *
	 * @param servletContextPath
	 */
	public void initialise(final String servletContextPath) {
		synchronized (this._lock) {
			if (this._servletContextPath == null) {

				if(SystemConstant.FILE_SEPARATOR.equals("\\")) {
					//Replace all / with \
					this._servletContextPath = StringUtils.replaceChars(servletContextPath, '/', '\\');
				} else {
					//Replace all \ with /
					this._servletContextPath = StringUtils.replaceChars(servletContextPath, '\\', '/');
				}
			}
		}
	}

	public String get() {
		synchronized (this._lock) {
			return this._servletContextPath;
		}
	}

}