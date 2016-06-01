import java.text.SimpleDateFormat
import java.util.TimeZone

import org.joda.time.format.ISODateTimeFormat

/**
  * Created by vjivane on 5/1/16.
  */
object TimeZoneParser extends App{

  val utcdatetime = "2016-05-02T02:48:20.351Z"

//  val simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
//  simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
//  val myDate = simpleDateFormat.parse(utcdatetime)



  val mydate = ISODateTimeFormat.dateTimeParser().parseDateTime(utcdatetime)
  println("Parsed => " + mydate)

  println("hour => " + mydate.hourOfDay().get())

  println("minute => " + mydate.minuteOfHour().get())


}
