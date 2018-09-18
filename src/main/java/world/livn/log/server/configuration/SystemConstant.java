package world.livn.log.server.configuration;
public class SystemConstant {

	public static final String NEW_LINE = System.getProperty("line.separator");

	/**
	 * File.separator is either / or \ that is used to split up the path to a
	 * specific file. For example on Windows it is \ or C:\Documents\Test
	 */
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");

	/**
	 * File.pathSeparator is used to separate individual file paths in a list of
	 * file paths. Consider on windows, the PATH environment variable. You use a
	 * ; to separate the file paths so on Windows File.pathSeparator would be ;.
	 */
	public static final String PATH_SEPARATOR = System.getProperty("path.separator");

	private SystemConstant() {
		super();
	}

}