package world.livn.log.server.configuration;
import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;

import org.apache.commons.io.FileUtils;
import org.joda.time.DateTime;

public class Versioning implements Serializable {

	private static final long serialVersionUID = 201306211156L;

	public static void main(String[] args) {
		System.out.println(Versioning.getLastModifiedDate().toString("yyyy/MM/dd - HH:mm:ss zzz"));
	}

	private static DateTime getLastModifiedDateOld() {
		try {
			URLConnection conn = Versioning.class.getResource(Versioning.class.getSimpleName()+".class").openConnection();
			return new DateTime(conn.getLastModified());
		} catch (Exception e) {
			return new DateTime(0L); //Just a fallback
		}
	}

	private static File getLastModified() {
		try {

			//LogTracer.trace("Java class path: " + System.getProperty("java.class.path"), 0);

			URL url = Versioning.class.getResource(Versioning.class.getSimpleName()+".class");
			//LogTracer.trace("Versioning.class.url: "+url, 0);

			File dir = new File(url.toURI()).getParentFile().getParentFile().getParentFile().getParentFile().getParentFile();
			//LogTracer.trace("classes.dir: "+dir, 0);

			String[] extensions = null;
			//String[] extensions = {"class","jar"};

			File lastModified = null;
			for (Iterator<File> iterator = FileUtils.iterateFiles(dir, extensions, true); iterator.hasNext();) {
				File file = iterator.next();
				if(lastModified==null || FileUtils.isFileNewer(file, lastModified)) {
					lastModified = file;
				}
			}

			//LogTracer.trace("lastModified: "+lastModified, 0);

			return lastModified;

		} catch (Exception e) {
			return null;
		}
	}

	public static DateTime getLastModifiedDate() {
		try {
			DateTime lastModified = new DateTime(Versioning.getLastModified().lastModified());
			return lastModified;
		} catch (Exception e) {
			return Versioning.getLastModifiedDateOld(); //Just a fallback
		}
	}
}
