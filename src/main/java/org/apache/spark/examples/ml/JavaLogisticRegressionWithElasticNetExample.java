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

package org.apache.spark.examples.ml;

import org.apache.derby.impl.sql.catalog.SYSROUTINEPERMSRowFactory;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.ml.classification.BinaryLogisticRegressionTrainingSummary;
// $example on$
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.LogisticRegressionModel;
import org.apache.spark.ml.classification.LogisticRegressionSummary;
import org.apache.spark.ml.linalg.Vector;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
// $example off$
import org.apache.spark.sql.functions;

import scala.Tuple2;

public class JavaLogisticRegressionWithElasticNetExample {
	public static void main(String[] args) {
		SparkSession spark = SparkSession.builder().appName("JavaLogisticRegressionWithElasticNetExample")
				.config("spark.master", "local[*]").getOrCreate();

		// $example on$
		// // Load training data
		// Dataset<Row> training = spark.read().format("libsvm")
		// .load("data/mllib/sample_libsvm_data.txt");
		Dataset<Row> training = spark.read().format("libsvm").option("numFeatures", "123")
				.load("E:\\data\\logisticData\\a1a");

		Dataset<Row> testing = spark.read().format("libsvm").option("numFeatures", "123")
				.load("E:\\data\\logisticData\\a1a.t");
		LogisticRegression lr = new LogisticRegression().setMaxIter(10).setRegParam(0.3).setElasticNetParam(0.8);
		System.out.println(lr.getLabelCol());

		// Fit the model
		
		LogisticRegressionModel lrModel = lr.fit(training);
		// Print the coefficients and intercept for logistic regression
		
		System.out.println("testing summary::");
		LogisticRegressionSummary testingSummary = lrModel.evaluate(testing);
		// Obtain the loss per iteration.
		System.out.println("accuracy==");
		System.out.println(testingSummary.accuracy());
		JavaPairRDD<Object, Object> predictionAndLabels = testing.toJavaRDD()
				.mapToPair(p ->{ System.out.println(lrModel.predict((Vector)p.get(1)) + ":::" + p.get(0));
					return new Tuple2<>(lrModel.predict((Vector)p.get(1)),p.get(0));
				});
		MulticlassMetrics metrics = new MulticlassMetrics(predictionAndLabels.rdd());
		double accuracy = metrics.accuracy();
		// testing.foreach(row->System.out.println(row));

		// // We can also use the multinomial family for binary classification
		// LogisticRegression mlr = new LogisticRegression()
		// .setMaxIter(10)
		// .setRegParam(0.3)
		// .setElasticNetParam(0.8)
		// .setFamily("multinomial");
		//
		// // Fit the model
		// LogisticRegressionModel mlrModel = mlr.fit(training);
		//
		// // Print the coefficients and intercepts for logistic regression with
		// multinomial family
		// System.out.println("Multinomial coefficients: " +
		// lrModel.coefficientMatrix()
		// + "\nMultinomial intercepts: " + mlrModel.interceptVector());
		// // $example off$

		spark.stop();
	}
}
