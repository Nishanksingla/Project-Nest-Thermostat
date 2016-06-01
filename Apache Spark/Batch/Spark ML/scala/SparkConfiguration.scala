import org.apache.spark.{SparkConf, SparkContext}

object SparkConfiguration {

  var conf: Option[SparkConf] = None
  var context: Option[SparkContext] = None

  def initializeSpark(appName: String, master: String): Unit = {
    conf = Some(new SparkConf().setAppName(appName).setMaster(master)
      .set("spark.driver.memory", "4G")
      .set("spark.executor.memory", "4G"))
    context = Some(new SparkContext(conf.get)) }

  def getConfiguredSpark: SparkContext = {
    context.orNull
  }
}
