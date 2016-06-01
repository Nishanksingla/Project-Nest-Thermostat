import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.regression.{LabeledPoint, LinearRegressionWithSGD}
import org.apache.spark.mllib.util.MLUtils

object Driver {

  def main(args: Array[String]) {


    println("Application Start")

    /* Initialization Block */

    System.setProperty("archaius.fixedDelayPollingScheduler.delayMills", "55555555")
    PropertiesManager.initializePropertiesManager("iot-properties", "iot.config")
    SparkConfiguration.initializeSpark(PropertiesManager.spark_app_name, PropertiesManager.spark_app_master)
    Elastic.initializeElastic(PropertiesManager.elasticsearch_host, PropertiesManager.elasticsearch_cluster, PropertiesManager.elasticsearch_raw_data_index, PropertiesManager.elasticsearch_raw_data_type)

    /* Initialization Block */


    /* Fetch unique teams  */

    val teams = Elastic.getUniqueTeams(PropertiesManager.elasticsearch_raw_data_index, PropertiesManager.elasticsearch_raw_data_type)

    /* Fetch unique teams  */

    println("Total teams currently present => " + teams.length)
    teams.foreach(println)

    /* Model building  */

    teams.foreach({ team =>
      println("Start: Processing for Team => " + team)
      val model = Model.buildModel(SparkConfiguration.getConfiguredSpark,team.toInt)
      Elastic.pushMLResult(team, model._1,model._2.toArray, model._3.toArray,model._4.toArray )
    })

    /* Model building  */

    println("Application Stop")
  }
}
