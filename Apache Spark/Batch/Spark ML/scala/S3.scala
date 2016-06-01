import java.io.FileInputStream
import java.util.Properties
import com.amazonaws.{ClientConfiguration, Protocol}
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model._
import org.apache.commons.io.IOUtils

import scala.collection.mutable.ListBuffer

object S3 {

  var s3Client: Option[AmazonS3Client] = None
  var accessKey,secretKey: Option[String] = None

  def initializeS3Client(): Unit = {
    //loadClientProperties()
    accessKey = Some("XXXXXXXXXXXXXXXX")
    secretKey = Some("XXXXXXXXXXXXXXXXXXXXXXXX")

    s3Client = Some(new AmazonS3Client(new BasicAWSCredentials(accessKey.get, secretKey.get),
      new ClientConfiguration() withProtocol Protocol.HTTP))
  }

  def s3Cleint = s3Client

  def listAllInputObjects(bucket: String, folder: String) : ListBuffer[String] ={
    val objectKeyList = new ListBuffer[String]
    try {
      var objectList = s3Client.get.listObjects(bucket, folder)
      do {
        val iteratorForObjectSummaries = objectList.getObjectSummaries.iterator()
        while (iteratorForObjectSummaries.hasNext) {
          val entry = iteratorForObjectSummaries.next()
          objectKeyList.append(entry.getKey)
        }
        objectList = s3Client.get.listNextBatchOfObjects(objectList)
      } while (objectList.isTruncated)
    } catch {
      case exception: Exception => exception.printStackTrace()
    }
    objectKeyList
  }

  def getObjectContentsAsString(bucket: String, objectKey: String) = IOUtils.toString(s3Client.get.getObject(
    new GetObjectRequest(bucket, objectKey)).getObjectContent,"UTF-8")

//  def loadClientProperties() : Unit = {
//    val properties = new Properties()
//    properties.load(new FileInputStream("client.properties"))
//    accessKey = Some(properties.getProperty("accessKey"))
//    secretKey = Some(properties.getProperty("secretKey"))
//  }

  def suhtDown() = s3Client.get.shutdown()
}