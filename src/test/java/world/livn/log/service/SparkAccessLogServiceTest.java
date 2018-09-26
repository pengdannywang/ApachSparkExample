package world.livn.log.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

@RunWith(JUnitPlatform.class)
public class SparkAccessLogServiceTest {
	// static SparkSession spark;

	@BeforeAll
	public static void setUpBeforeClass() throws Exception {
		// spark = SparkSession.builder().appName("Java Spark SQL basic
		// example").config("spark.master", "local[*]")
		// .getOrCreate();

	}

	@Test
	public void testRunBasicDataFrameExample() {

		String str = "IP: 192.155.83.114 , Host Name: gds1.livngds.com , Login Name: livn , Internal API User Host=192.155.83.114, Roles=[WEBFRONT, HAL, GDS, CMS] , Geolocation: US United States: Fremont 37.5483,-121.9886\n";
		str=str+"IP: 203.219.109.178 , Host Name: 203-219-109-178.tpgi.com.au , Login Name: mpark , Backend User: id=74, Name: Michael Park , Distributor: id=1, code=LIVN , Geolocation: AU Australia: Sydney -33.8591,151.2002";
		// Pattern pattern =
		// Pattern.compile("(/([?!Geolocation])([\\w\\s]+):[\\w\\.\\s]+[^:])|([\\w\\s]+=[\\[]*[\\w\\.\\,\\s]+[\\]*])|([\\w\\s]+=[\\w\\.\\,\\s]+)");
		//"|([\\w]+[\\w\\s]*):([\\w\\.\\s]+:[-\\w\\s\\.\\,]*[-\\w\\s\\.]+)*)1|([\\w]+[\\w\\s]*=[\\[]+[\\w\\.\\,\\s]+[\\]+])|([\\w]+[\\w\\s]*=[\\w\\.\\s]+)"
		Pattern pattern = Pattern.compile(
				"(([\\w]+[\\w\\s]*):([\\w\\s]+)[\\w\\s\\.\\=]*(\\:[\\w\\s]+[-0-9\\s\\.\\,]+[-0-9\\s\\.]+)*)|(([\\w]+[\\w\\s]*)=(([\\w\\s]+)([\\w\\.]+)|([\\[]*[\\w\\.\\,\\s]+[\\]*])))");
		java.util.regex.Matcher m = pattern.matcher(str);
		List<String> list = new ArrayList<>();
		while (m.find()) {
			String li = m.group();


			//String[] split = li.split(":", 2);

			//Tuple2<String,String> jo = new Tuple2<>(split[0],split[1]);
			list.add(li);

		}
		for (String str1 : list) {
			System.out.println(str1);
		}

	}

}
