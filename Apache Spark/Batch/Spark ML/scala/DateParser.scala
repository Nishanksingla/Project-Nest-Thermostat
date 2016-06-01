object DateParser {

  def parse(esTimestamp: String): (Int, Int, Int, Int, Int, Int) =
  {
    val dateRecoerd = esTimestamp.split("T")(0).split("-")
    val timeRecord = esTimestamp.split("T")(1).replace("Z","").split(":")
    (dateRecoerd(0).toInt, dateRecoerd(1).toInt, dateRecoerd(2).toInt, timeRecord(0).toInt, timeRecord(1).toInt, timeRecord(2).split("\\.")(0).toInt )
  }
}
