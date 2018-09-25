/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package world.livn.log.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.examples.JavaLogQuery.Stats;
// $example off:programmatic_schema$
import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
// $example on:init_session$
import org.apache.spark.sql.SparkSession;

import scala.Tuple2;
import scala.Tuple3;


public class SparkAccessLogService {

	public static void main(String[] args) throws AnalysisException {
		// $example on:init_session$
		SparkSession spark = SparkSession.builder().appName("Java Spark SQL basic example")
				.config("spark.master", "local[*]").getOrCreate();

		runBasicDataFrameExample(spark);
		// runDatasetCreationExample(spark);
		// runInferSchemaExample(spark);
		// runProgrammaticSchemaExample(spark);

		spark.stop();
	}

	public static void runBasicDataFrameExample(SparkSession spark) throws AnalysisException {
		// $example on:create_df$
		// Dataset<Row> df =
		// spark.read().json("src/main/resources/people.json");
		// Dataset<Row> jdbcDF = spark.read()
		// .format("jdbc")
		// .option("url",
		// "jdbc:postgresql://10.10.0.18:5432/pw_satellite_sales")
		// .option("dbtable", "public.txnItem")
		// .option("user", "bookingmate")
		// .option("password", "jR7!vf8X")
		// .load();
		//
		// // Displays the content of the DataFrame to stdout
		// jdbcDF.show();
		Dataset<Row> ds = spark.read().json("W:/pwang/central_logs/*.log");
		Dataset<Row> messSet = ds.filter("level=='INFO'").filter(ds.col("message").contains("Login Name:"))
				.select("message");

		messSet.show(2,false);
		Dataset<String> messSets = messSet.flatMap(new FlatMapFunction<Row, String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Iterator<String> call(Row row) throws Exception {

				return Arrays.asList(row.getString(0).split(",",3)).iterator();

			}

		}, Encoders.STRING());
		messSets.show(20,false);
		//		Dataset<String> dds=messSets.filter(messSets.col("value").contains(":"));
		//		Dataset<Tuple2<String,String>> tps = dds.map(new MapFunction<String, Tuple2<String,String>>() {
		//			private static final long serialVersionUID = 1L;
		//
		//			@Override
		//			public Tuple2<String,String> call(String r) {
		//
		//				String[] split = r.split(":");
		//				Tuple2 tuple=Tuple2.apply(split[0].trim(),split[1].trim());
		//				return tuple;
		//
		//			}
		//
		//		},Encoders.tuple(Encoders.STRING(), Encoders.STRING()));
		JavaRDD<Row> rowRDD=messSets.javaRDD().map((Function<String,Row>) record->{


			List<Tuple2<String,String>> tuple=parser(record);


			return RowFactory.create(tuple.toArray());
		});
//		rowRDD.reduce(Function f->{
//
//		});
		//		Dataset<Row> trs=tps.toDF("keys","values");
		//		trs.groupBy("keys").count().show(20,false);
		//		trs.filter(trs.col("keys").contains("Backend User")).groupBy("values").count().orderBy("count").show();
		//		trs.filter(trs.col("keys").contains("Login Name")).groupBy("values").count().orderBy("count").show();
		//		trs.filter("keys='Name'").groupBy("values").count().orderBy("count").show();
		//		trs.groupBy("keys","values").count().orderBy("keys","count").show();
	}
	public static List<Tuple2<String,String>> parser(String line){
		Pattern pattern = Pattern.compile(
				"(([\\w\\s]+):[\\w\\.\\s]+(:[\\w\\s\\.\\,]+[-\\w\\s\\.]+)*)|([\\w\\s]+=[\\[]+[\\w\\.\\,\\s]+[\\]+])|([\\w\\s]+=[\\w\\.\\s]+)");
		java.util.regex.Matcher m = pattern.matcher(line);
		List<Tuple2<String,String>> list = new ArrayList<>();
		while (m.find()) {
			String li = m.group();
			li = li.replace("=", ":");

			String[] split = li.split(":", 2);

			Tuple2<String,String> jo = new Tuple2<>(split[0],split[1]);
			list.add(jo);

		}
		return list;
	}
	public static final Pattern apacheLogRegex = Pattern.compile(
			"^([\\d.]+) (\\S+) (\\S+) \\[([\\w\\d:/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) ([\\d\\-]+) \"([^\"]+)\" \"([^\"]+)\".*");

	public static Stats extractStats(String line) {
		Matcher m = apacheLogRegex.matcher(line);
		if (m.find()) {
			int bytes = Integer.parseInt(m.group(7));
			return new Stats(1, bytes);
		} else {
			return new Stats(1, 0);
		}

	}

	public static Tuple3<String, String, String> extractKey(String line) {
		Matcher m = apacheLogRegex.matcher(line);
		if (m.find()) {
			String ip = m.group(1);
			String user = m.group(3);
			String query = m.group(5);
			if (!user.equalsIgnoreCase("-")) {
				return new Tuple3<>(ip, user, query);
			}
		}
		return new Tuple3<>(null, null, null);
	}

}
