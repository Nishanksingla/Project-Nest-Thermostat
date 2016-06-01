import java.net.InetAddress
import java.util

import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.update.UpdateRequest
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.sort.SortOrder
import org.joda.time.format.ISODateTimeFormat

import scala.collection.mutable.ListBuffer
import scala.util.parsing.json.JSONObject

object Elastic {

  var client: Option[TransportClient] = None
  var _index : Option[String] = None
  var _type : Option[String] = None

  def initializeElastic(nodeEndpoint: String,clusterName: String, index: String, recType: String) = {
    client = Some(TransportClient.builder()
      .settings(Settings.settingsBuilder().put("cluster.name", clusterName).build()).build()
      .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(nodeEndpoint), 9300)))
    _index = Some(index)
    _type = Some(recType)
  }

  def createIndex(_index: String, _type: String) = client.get.prepareIndex(_index, _type).setSource(new util.HashMap[String, Object]()).get()
  def updateDocument(_id: String)(json: util.HashMap[String, Object]) = client.get.update(new UpdateRequest(_index.get, _type.get, _id).doc(json)).get
  def upsertDocument(_id: String)(json: util.HashMap[String, Object]) = client.get.update(new UpdateRequest(_index.get, _type.get, _id).doc(json).upsert(json)).get
  def deleteDocument(_id: String) = client.get.prepareDelete(_index.get, _type.get, _id).get
  def bulkUpsert(documents: ListBuffer[util.HashMap[String, Object]]): Unit = {
    val bulkRequest = client.get.prepareBulk
    documents.foreach({ document => bulkRequest.add(new UpdateRequest(_index.get, _type.get, document.get("host").toString).doc(document).upsert(document)) })
  }
  def insertDocument(_id: String)(json: util.HashMap[String, Object]) = client.get.index( new IndexRequest(_index.get, _type.get).source(json)).get

  def getDocuments(scrollSize: Integer,_index: String, _type: String) = {

    val esData = new ListBuffer[String]//new util.ArrayList[util.HashMap[String,Object]]()
    var response : SearchResponse= null
    var i = 0
    while( response == null || response.getHits.hits().length != 0){
      response = client.get.prepareSearch(_index)
        .setTypes(_type)
        .setQuery(QueryBuilders.matchAllQuery())
        .setSize(scrollSize)
        .setFrom(i * scrollSize)
        .execute()
        .actionGet()

      response.getHits.getHits.foreach(entry => esData.append(entry.sourceAsString()))
      i+=1
    }
    esData
  }

  def getUniqueTeams(_index: String, _type: String): ListBuffer[String] = {
    val teams = new ListBuffer[String]
    var response : SearchResponse= null
    var aggTerms : Terms = null
    val agg = AggregationBuilders.terms("agg").field("team")
    while( response == null || response.getHits.hits().length != 0){
      response = client.get.prepareSearch(_index)
        .setTypes(_type)
        .setQuery(QueryBuilders.matchAllQuery())
        .addAggregation(agg)
        .setSize(0)
        .execute()
        .actionGet()
        aggTerms = response.getAggregations.get("agg")
    }
    for(i <- 0 until aggTerms.getBuckets.size ){
      teams.append(aggTerms.getBuckets.get(i).getKeyAsString)
    }
    teams
  }

  def retrieveDocumentsOfATeam(_index: String, _type: String, team: Int) = {
    val teamDocuments = new ListBuffer[String]
    var response : SearchResponse= null
    response = client.get.prepareSearch(_index)
        .addSort("@timestamp", SortOrder.DESC)
        .setTypes(_type)
        .setQuery(QueryBuilders.termsQuery("team",team))
        .setSize(10000)
        .execute()
        .actionGet()
    response.getHits.getHits.foreach({entry =>
      val timestamp = ISODateTimeFormat.dateTimeParser().parseDateTime(entry.getSource.get("@timestamp").toString)
      val res_del = PropertiesManager.spark_ml_response_delimiter.toString
      val pred_del = PropertiesManager.spark_ml_predictor_delimiter.toString
      teamDocuments.append( entry.getSource.get("temp") +  res_del + entry.getSource.get("outTemp") + pred_del + entry.getSource.get("humidity") + pred_del  + timestamp.hourOfDay().get() + pred_del + timestamp.minuteOfHour().get())
    })

//    println(s"Total records for Team $team => "+ teamDocuments.length)
//    teamDocuments.foreach(println)

    teamDocuments
  }

  def upsertMLDocument(_id: String)(json: util.HashMap[String, Object]) = client.get.update(new UpdateRequest(PropertiesManager.elasticsearch_ml_data_index, PropertiesManager.elasticsearch_ml_data_type, _id).doc(json).upsert(json)).get

  def pushMLResult (team: String, intercept: Double, weights: Array[Double], mean: Array[Double], std: Array[Double]) = {

    val json = new util.HashMap[String, Object]()
    json.put("intercept", intercept.asInstanceOf[Object])
    json.put("out", weights(0).asInstanceOf[Object])
    json.put("humidity", weights(1).asInstanceOf[Object])
    json.put("hour", weights(2).asInstanceOf[Object])
    json.put("min", weights(3).asInstanceOf[Object])

    json.put("out_mean", mean(0).asInstanceOf[Object])
    json.put("humidity_mean", mean(1).asInstanceOf[Object])
    json.put("hour_mean", mean(2).asInstanceOf[Object])
    json.put("min_mean", mean(3).asInstanceOf[Object])

    json.put("out_std", std(0).asInstanceOf[Object])
    json.put("humidity_std", std(1).asInstanceOf[Object])
    json.put("hour_std", std(2).asInstanceOf[Object])
    json.put("min_std", std(3).asInstanceOf[Object])

    upsertMLDocument(team)(json)

    println("Pushing ML Reasult tp Elasticsearch Complete.")

    println("ML Index => " + PropertiesManager.elasticsearch_ml_data_index)

    println("ML Type => " + PropertiesManager.elasticsearch_ml_data_type)



  }




}





