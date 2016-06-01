import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka.KafkaUtils

object StreamingDriver {

  def main(args: Array[String]) {

    System.setProperty("archaius.fixedDelayPollingScheduler.delayMills","55555555")
    PropertiesManager.initializePropertiesManager("iot-properties", "iot-streaming.config")

    val sparkConf = new SparkConf().setAppName(PropertiesManager.spark_app_name).setMaster(PropertiesManager.spark_app_master)

    val ssc = new StreamingContext(sparkConf, Seconds(PropertiesManager.kafka_polling))

    val lines = KafkaUtils.createStream(ssc, PropertiesManager.kafka_broker, PropertiesManager.kafka_receivers_group, PropertiesManager.kafka_topics).map(_._2)
    lines.foreachRDD({
      entry => entry.foreach({message =>
        println(message)
       StreamProcessor.process(message)
      })
    })

    ssc.start()
    ssc.awaitTermination()

  }
}
