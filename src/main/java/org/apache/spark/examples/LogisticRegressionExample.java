package org.apache.spark.examples;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.examples.ml.LogisticRegressionWithElasticNetExample;
import org.apache.spark.ml.classification.LabelConverter;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.mllib.classification.LogisticRegressionModel;
import org.apache.spark.mllib.classification.LogisticRegressionWithLBFGS;
import org.apache.spark.mllib.classification.LogisticRegressionWithSGD;
import org.apache.spark.mllib.evaluation.MulticlassMetrics;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.util.MLUtils;
import org.apache.spark.sql.SparkSession;

import scala.Tuple2;

public class LogisticRegressionExample {

	public static void main(String[] args) {
		 SparkSession spark = SparkSession
			      .builder()
			      .appName("JavaLogisticRegressionWithElasticNetExample").config("spark.master","local[*]")
			      .getOrCreate();
		 
		String path = "E:\\data\\logisticData\\a1a1";
		SparkContext sc=spark.sparkContext();
		//JavaRDD<LabeledPoint> data = MLUtils.loadLibSVMFile(sc, path).toJavaRDD();
		JavaRDD<LabeledPoint> data =MLUtils.loadLibSVMFile(sc, path,123).toJavaRDD();
		// Split initial RDD into two... [60% training data, 40% testing data].

		JavaRDD<LabeledPoint> training = data;
		JavaRDD<LabeledPoint> test = MLUtils.loadLibSVMFile(sc, "E:\\data\\logisticData\\a1a1.t",123).toJavaRDD();
		System.out.println(test.first());
		test.first();
		System.out.println(training.first());
		// Run training algorithm to build the model.
		

		
		LogisticRegressionModel model = new LogisticRegressionWithLBFGS().setNumClasses(100).run(training.rdd());
		
		// Compute raw scores on the test set.
		JavaPairRDD<Object, Object> predictionAndLabels = test
				.mapToPair(p -> {
					System.out.println(model.predict(p.features())+":::"+p.label());
					return new Tuple2<>(model.predict(p.features()), p.label());
					});

		// Get evaluation metrics.
		MulticlassMetrics metrics = new MulticlassMetrics(predictionAndLabels.rdd());
		double accuracy = metrics.accuracy();
		
		System.out.println("Accuracy = " + accuracy);

		// Save and load model
		model.save(sc, "target/tmp/javaLogisticRegressionWithLBFGSModel");
		LogisticRegressionModel sameModel = LogisticRegressionModel.load(sc,
				"target/tmp/javaLogisticRegressionWithLBFGSModel");
	}
}