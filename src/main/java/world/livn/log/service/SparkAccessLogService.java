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
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
// $example on:init_session$
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;

import scala.Tuple2;

public class SparkAccessLogService {
	private static final int COLUMNS_QUERY_END_DATE_NUM = 2;
	private static final int COLUMNS_QUERY_START_DATE_NUM = 1;
	private static final String COLUMNS_QUERY_END_DATE = "endDate";
	private static final String COLUMNS_QUERY_START_DATE = "startDate";
	// Constants
	private static final String COLUMNS_MESSAGE = "message";
	private static final String VALUES = "values";
	private static final String KEYS = "keys";
	private static final String COLUMNS_REQUEST_PATH = "request_path";
	private static final String COLUMNS_REMOTE_ADDR = "remote_addr";
	private static final String COLUMNS_QUERY = "query";
	private static final String COLUMNS_TIMESTAMP = "@timestamp";
	private static final String API_TYPE_ID_COUNT = "count";
	private static final String API = "api";
	private static final int API_NUM = 2;
	private static final String API_TYPE = "type";
	private static final int API_TYPE_NUM = 3;
	private static final String API_TYPE_ID = "id";
	private static final int API_TYPE_ID_NUM = 4;

	public static void main(String[] args) throws AnalysisException {
		// $example on:init_session$
		SparkSession spark = SparkSession.builder().appName("Java Spark SQL basic example")
				.config("spark.master", "local[*]").getOrCreate();
		accessLog(spark);
		spark.stop();
	}

	public static void accessLog(SparkSession spark) throws AnalysisException {

		Dataset<Row> ds = spark.read().json("W:/pwang/central_logs/*.txt");
		String start = "2018-01-26 00:00:00";
		String end = "2018-09-30 00:00:00";
		String api = null;
		String api_type = null;
		Dataset<Row> dsrow = getLogByDate(ds);
		dsrow=getLogByConditions(dsrow,start,end,api,api_type);
		Column split_col2 = org.apache.spark.sql.functions.split(dsrow.col(COLUMNS_QUERY), "");
		dsrow.show(2,false);
		dsrow.groupBy(API_TYPE).count().show(200);
		dsrow.groupBy(API,API_TYPE,API_TYPE_ID).count().orderBy(functions.col(API_TYPE),functions.col(API_TYPE_ID_COUNT).desc()).show(200, false);
	}

	public static Dataset<Row> getLogByConditions(Dataset<Row> dsrow, String start, String end, String cond_api,
			String cond_type) {
		//fill out null id
		dsrow=dsrow.where(functions.col(API_TYPE_ID).isNotNull());
		//
		dsrow=dsrow.filter(functions.col(COLUMNS_TIMESTAMP).between(start,end));

		if(cond_api!=null){
			dsrow=dsrow.filter(functions.col(API).equalTo(cond_api));
		}
		if(cond_type!=null){
			dsrow=dsrow.filter(functions.col(API_TYPE).equalTo(cond_type));
		}
		return dsrow;
	}

	public static Dataset<Row> getLogByDate(Dataset<Row> dsrow) {

		Column split_col = org.apache.spark.sql.functions.split(dsrow.col(COLUMNS_REQUEST_PATH), "/");
		dsrow = dsrow.select(COLUMNS_TIMESTAMP, COLUMNS_QUERY, COLUMNS_REMOTE_ADDR, COLUMNS_REQUEST_PATH)
				.withColumn(API, split_col.getItem(API_NUM));
		dsrow = dsrow.withColumn(API_TYPE, split_col.getItem(API_TYPE_NUM));
		dsrow = dsrow.withColumn(API_TYPE_ID, split_col.getItem(API_TYPE_ID_NUM));

		Pattern pattern =Pattern.compile("\\?startDate=|&endDate=");
		Column splits = functions.split(dsrow.col(COLUMNS_QUERY), pattern.pattern());
		dsrow = dsrow.withColumn(COLUMNS_QUERY_START_DATE, splits.getItem(COLUMNS_QUERY_START_DATE_NUM));
		dsrow = dsrow.withColumn(COLUMNS_QUERY_END_DATE, splits.getItem(COLUMNS_QUERY_END_DATE_NUM));

		return dsrow;
	}

	public static void runBasicDataFrameExample(SparkSession spark) throws AnalysisException {

		Dataset<Row> ds = spark.read().json("W:/pwang/central_logs/dev.log");
		Dataset<Row> messSet = ds.filter("level=='INFO'").filter(ds.col(COLUMNS_MESSAGE).contains("Login Name:"))
				.select(COLUMNS_MESSAGE);
		// messSet.show(2,false);
		Dataset<String> messSets = messSet.flatMap(new FlatMapFunction<Row, String>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Iterator<String> call(Row row) throws Exception {

				return parser(row);
			}

		}, Encoders.STRING());
		// messSets.show(20,false);
		// Dataset<String>
		// dds=messSets.filter(messSets.col("value").contains(":"));
		Dataset<Tuple2<String, String>> tps = messSets.map(new MapFunction<String, Tuple2<String, String>>() {
			private static final long serialVersionUID = 1L;

			@Override
			public Tuple2<String, String> call(String r) {

				String[] split = r.split(":", 2);

				Tuple2 tuple = Tuple2.apply(split[0].trim(), split[1].trim());
				return tuple;

			}

		}, Encoders.tuple(Encoders.STRING(), Encoders.STRING()));
		Dataset<Row> trs = tps.toDF(KEYS, VALUES);
		trs.groupBy(KEYS, VALUES).count()
		.orderBy(trs.col(KEYS), org.apache.spark.sql.functions.col(API_TYPE_ID_COUNT).desc())
		.show(500, false);
		trs.groupBy(KEYS).count().orderBy(org.apache.spark.sql.functions.col(API_TYPE_ID_COUNT).desc()).show(50);
		// System.out.println(rowRDD.first());
	}

	public static Iterator<String> parser(Row row) {
		String line = row.toString();
		Pattern pattern = Pattern.compile(
				"(([\\w]+[\\w\\s]*):([\\w\\s]+)[\\w\\s\\.\\=]*(\\:[\\w\\s]+[-0-9\\s\\.\\,]+[-0-9\\s\\.]+)*)|(([\\w]+[\\w\\s]*)=(([\\w\\s]+)([\\w\\.]+)|([\\[]*[\\w\\.\\,\\s]+[\\]*])))");
		java.util.regex.Matcher m = pattern.matcher(line);
		List<String> list = new ArrayList<>();
		while (m.find()) {
			String li = m.group();
			li = li.replace("=", ":");
			if (!li.contains("Internal API User IP address registry:") && !li.trim().isEmpty()) {
				list.add(li);
			}
		}
		return list.iterator();
	}

}
