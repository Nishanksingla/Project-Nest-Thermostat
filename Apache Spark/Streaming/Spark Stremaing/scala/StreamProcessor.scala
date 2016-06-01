import java.util

import org.json.simple.JSONObject
import org.json.simple.parser.JSONParser

import scala.concurrent.{ExecutionContext, Future}

object StreamProcessor {

  Elastic.initializeElastic(PropertiesManager.elasticsearch_host, PropertiesManager.elasticsearch_cluster, PropertiesManager.elasticsearch_raw_data_index, PropertiesManager.elasticsearch_raw_data_type)


  def process(message: String): Unit = {
    pushToElasticSearch(message)
  }

  def pushToElasticSearch(message: String): Unit = {
    import ExecutionContext.Implicits.global
    Future {
      val parser = new JSONParser()
      val json = parser.parse(message).asInstanceOf[JSONObject]
      val team = new Integer(json.get("team").toString)
      val temp = new Integer(json.get("temp").toString)
      val humidity = new Integer(json.get("humidity").toString)
      val outTemp = new Integer(json.get("outTemp").toString)
      val timestamp = json.get("@timestamp").toString

      val document = new util.HashMap[String, Object]()

      document.put("team", team)
      document.put("temp", temp)
      document.put("humidity", humidity)
      document.put("outTemp", outTemp)
      document.put("@timestamp", timestamp)

      //"@timestamp":"2016-05-02T07:20:28.477Z","team":5,"temp":22,"humidity":38,"outTemp":16

      Elastic.insertDocument(document)
    }
  }
}
