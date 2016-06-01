import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.{ClientConfiguration, Protocol}

object S3 {

  var s3Client: Option[AmazonS3Client] = None
  var accessKey,secretKey: Option[String] = None

  def initializeS3Client(): Unit = {
    accessKey = Some("XXXXXXXXXXXXXXXXXXXXXXXXXX")
    secretKey = Some("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX")

    s3Client = Some(new AmazonS3Client(new BasicAWSCredentials(accessKey.get, secretKey.get),
      new ClientConfiguration() withProtocol Protocol.HTTP))
  }

  def s3Cleint = s3Client
  def suhtDown() = s3Client.get.shutdown()
}