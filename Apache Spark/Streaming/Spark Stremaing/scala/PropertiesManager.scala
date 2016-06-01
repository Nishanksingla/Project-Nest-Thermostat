import com.netflix.config._
import com.netflix.config.sources.S3ConfigurationSource

object PropertiesManager {

  def initializePropertiesManager(bucket: String, key: String): Unit = {
    S3.initializeS3Client()
    var s3ConfigurationClient: Option[S3ConfigurationSource] = None
    s3ConfigurationClient = Some(new S3ConfigurationSource(S3.s3Client.get, bucket, key))
    ConfigurationManager.install(new DynamicConfiguration(s3ConfigurationClient.get, new FixedDelayPollingScheduler()))
  }

  def f(key: String) = DynamicPropertyFactory.getInstance().getStringProperty(key, null).getValue

  def spark_app_name = f("spark.app.name")

  def spark_app_master = f("spark.app.master")

  def kafka_broker = f("kafka.broker")

  def kafka_receivers_group = f("kafka.receivers.group")

  def kafka_topics = f("kafka.topics").split(",").map((_, kafka_parallelism)).toMap

  def kafka_parallelism = f("kafka.parallelism").toInt

  def kafka_polling   = f("kafka.polling").toInt

  def elasticsearch_host = f("elasticsearch.host")

  def elasticsearch_cluster = f("elasticsearch.cluster")

  def elasticsearch_raw_data_index = f("elasticsearch.raw.data.index")

  def elasticsearch_raw_data_type = f("elasticsearch.raw.data.type")

}