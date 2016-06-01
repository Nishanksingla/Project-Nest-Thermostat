import java.io.PrintWriter

import scala.io.Source

object DataPreparator extends App{

//  val writer = new PrintWriter("dataset-hour-and-minutes-individual.csv", "UTF-8")
//  Source.fromFile("/Users/vjivane/Desktop/01-Int-elligentsia/blink-stars-project-repo/blink-stars/Flash/iot-data.csv").getLines().foreach(entry => formatandwrite(entry))
//  writer.close()
//
//  def formatandwrite(line: String) ={
//    val values = line.split(",")
//    writer.println(values(5)+","+ values(6)+"|"+values(7)+"|"+values(3) + "|" + values(4))

Elastic.initializeElastic("54.67.96.197","flash-cluster","data","record")

val records = Elastic.getDocuments(10, "data", "record")


  val writer = new PrintWriter("DataBackup5.txt", "UTF-8")

  records.foreach(record => writer.println(record))

  writer.close()


}