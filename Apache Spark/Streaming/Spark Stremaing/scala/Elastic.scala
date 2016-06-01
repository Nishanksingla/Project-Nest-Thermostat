import java.net.InetAddress
import java.util

import org.elasticsearch.action.index.IndexRequest
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.InetSocketTransportAddress

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

  def insertDocument(json: util.HashMap[String, Object]) = client.get.index( new IndexRequest(_index.get, _type.get).source(json)).get
}





