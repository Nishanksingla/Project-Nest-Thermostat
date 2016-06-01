import org.apache.spark.SparkContext
import org.apache.spark.mllib.evaluation.RegressionMetrics
import org.apache.spark.mllib.feature.StandardScaler
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.{LabeledPoint, LinearRegressionWithSGD}


object Model {

  val numIterations = 200
  val algorithm = new LinearRegressionWithSGD()
  algorithm.setIntercept(true)
  algorithm.optimizer.setNumIterations(200)

  def buildModel(sc: SparkContext, team: Integer) = {

    val data = sc.parallelize(Elastic.retrieveDocumentsOfATeam(PropertiesManager.elasticsearch_raw_data_index, PropertiesManager.elasticsearch_raw_data_type, team))

   val Array(training, test) = data.randomSplit(Array(PropertiesManager.spark_ml_train_data_split, PropertiesManager.spark_ml_test_data_split))

    val parsedData = data.map { line =>
      val parts = line.split(PropertiesManager.spark_ml_response_delimiter)
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(PropertiesManager.spark_ml_predictor_delimiter).map(_.toDouble)))
    }.cache()

    val parsedTrainingData = training.map { line =>
      val parts = line.split(PropertiesManager.spark_ml_response_delimiter)
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(PropertiesManager.spark_ml_predictor_delimiter).map(_.toDouble)))
    }.cache()

    val parsedTestData = test.map { line =>
      val parts = line.split(PropertiesManager.spark_ml_response_delimiter)
      LabeledPoint(parts(0).toDouble, Vectors.dense(parts(1).split(PropertiesManager.spark_ml_predictor_delimiter).map(_.toDouble)))
    }.cache()

    val scaler = new StandardScaler(withMean = true, withStd = true)
      .fit(parsedData.map(x => x.features))

    println(s"Mean and Standard Deviation => $team => start")
    println("Mean => "+ scaler.mean)
    println("Mean => "+ scaler.std)
    println(s"Mean and Standard Deviation => $team => stop")
    val scaledData = parsedTrainingData
      .map(x =>
        LabeledPoint(x.label,
          scaler.transform(Vectors.dense(x.features.toArray))))

    println("Scaled Data - Start")
  //  scaledData.foreach(println)
    println("Scaled Data - Stop")

    val scaledTestData = parsedTestData
      .map(x =>
        LabeledPoint(x.label,
          scaler.transform(Vectors.dense(x.features.toArray))))

    val model = algorithm.run(scaledData)
    println("Model Coeffs => " + model.weights)
    println("Intercept => " + model.intercept)
    val valuesAndPreds = scaledTestData.map { point =>
      val prediction = model.predict(point.features)
      (point.label.round.toDouble, prediction.round.toDouble)
    }
   // valuesAndPreds.foreach(println)

    valuesAndPreds.foreach(entry => { println(s"Actual Value: ${entry._1} \t Predicted Value: ${entry._2}")})

    val testMetrics = new RegressionMetrics(valuesAndPreds)
    println("Metrics: Training Dataset Count => " + training.count())
    println("Metrics: Test Dataset Count => " + test.count())
    println("Metrics: Model Intercept => " + model.intercept)
    println("Metrics: Model Weights => " + model.weights)
    println("Metrics: RMSE => " + testMetrics.rootMeanSquaredError)
    println("Metrics: MSE => " + testMetrics.meanSquaredError)
    println("Metrics: MAE => " + testMetrics.meanAbsoluteError)
    println("Metrics: R2 => " + testMetrics.r2.abs)

    (model.intercept, model.weights, scaler.mean, scaler.std)
  }
}
