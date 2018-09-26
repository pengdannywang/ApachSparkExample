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
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.MapFunction;
// $example off:programmatic_schema$
import org.apache.spark.sql.AnalysisException;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
// $example on:init_session$
import org.apache.spark.sql.SparkSession;

import scala.Tuple2;


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
	public static void accessLog(SparkSession spark) throws AnalysisException{
		Dataset<Row> ds = spark.read().json("W:/pwang/central_logs/*.txt");

		Dataset<Row> messSet = ds.filter("level=='INFO'").filter(ds.col("message").contains("Login Name:"))
				.select("message");

		//messSet.show(2,false);
		Dataset<String> messSets = messSet.flatMap(new FlatMapFunction<Row, String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Iterator<String> call(Row row) throws Exception {

				return parser(row);

			}

		}, Encoders.STRING());
		//messSets.show(20,false);
		//		Dataset<String> dds=messSets.filter(messSets.col("value").contains(":"));
		Dataset<Tuple2<String,String>> tps = messSets.map(new MapFunction<String, Tuple2<String,String>>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Tuple2<String,String> call(String r) {

				String[] split = r.split(":",2);

				Tuple2 tuple=Tuple2.apply(split[0].trim(),split[1].trim());
				return tuple;

			}

		},Encoders.tuple(Encoders.STRING(), Encoders.STRING()));


		Dataset<Row> trs=tps.toDF("keys","values");

		trs.groupBy("keys","values").count().orderBy(trs.col("keys"),org.apache.spark.sql.functions.col("count").desc()).show(500,false);
		trs.groupBy("keys").count().orderBy(org.apache.spark.sql.functions.col("count").desc()).show(50);
	}
	public static void runBasicDataFrameExample(SparkSession spark) throws AnalysisException {

		Dataset<Row> ds = spark.read().json("W:/pwang/central_logs/dev.log");

		Dataset<Row> messSet = ds.filter("level=='INFO'").filter(ds.col("message").contains("Login Name:"))
				.select("message");

		//messSet.show(2,false);
		Dataset<String> messSets = messSet.flatMap(new FlatMapFunction<Row, String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Iterator<String> call(Row row) throws Exception {

				return parser(row);

			}

		}, Encoders.STRING());
		//messSets.show(20,false);
		//		Dataset<String> dds=messSets.filter(messSets.col("value").contains(":"));
		Dataset<Tuple2<String,String>> tps = messSets.map(new MapFunction<String, Tuple2<String,String>>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Tuple2<String,String> call(String r) {

				String[] split = r.split(":",2);

				Tuple2 tuple=Tuple2.apply(split[0].trim(),split[1].trim());
				return tuple;

			}

		},Encoders.tuple(Encoders.STRING(), Encoders.STRING()));


		Dataset<Row> trs=tps.toDF("keys","values");

		trs.groupBy("keys","values").count().orderBy(trs.col("keys"),org.apache.spark.sql.functions.col("count").desc()).show(500,false);
		trs.groupBy("keys").count().orderBy(org.apache.spark.sql.functions.col("count").desc()).show(50);
		//System.out.println(rowRDD.first());
	}
	public static Iterator<String> parser(Row row){
		String line=row.toString();
		Pattern pattern = Pattern.compile(
				"(([\\w]+[\\w\\s]*):([\\w\\s]+)[\\w\\s\\.\\=]*(\\:[\\w\\s]+[-0-9\\s\\.\\,]+[-0-9\\s\\.]+)*)|(([\\w]+[\\w\\s]*)=(([\\w\\s]+)([\\w\\.]+)|([\\[]*[\\w\\.\\,\\s]+[\\]*])))");
		java.util.regex.Matcher m = pattern.matcher(line);
		List<String> list = new ArrayList<>();
		while (m.find()) {
			String li = m.group();
			li = li.replace("=", ":");
			if(!li.contains("Internal API User IP address registry:")&&!li.trim().isEmpty()){
				list.add(li);
			}
		}
		return list.iterator();
	}

}
