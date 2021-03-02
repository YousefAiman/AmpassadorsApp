package hashed.app.ampassadors.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TimeFormatter {

  private static final long SECOND_MILLIS = 1000,
          MINUTE_MILLIS = 60 * SECOND_MILLIS,
          HOUR_MILLIS = 60 * MINUTE_MILLIS,
          DAY_MILLIS = 24 * HOUR_MILLIS,
          WEEK_MILLIS = 7 * DAY_MILLIS,
          MONTH_MILLIS = 30 * DAY_MILLIS,
          YEAR_MILLIS = 12 * MONTH_MILLIS;

  public static final String
          HOUR_MINUTE= "h:mm a",
          WEEK_DAY = "EEE",
          MONTH_DAY = "MMM dd",
          MONTH_DAY_YEAR = "MMM dd yyyy";

  public static String formatTime(long time){

    if (time < 1000000000000L) {
      time *= 1000;
    }

    final long timeAgo = System.currentTimeMillis() - time;

    String format ="";
    if(timeAgo < DAY_MILLIS){

      format = HOUR_MINUTE;

    }else if(timeAgo < WEEK_MILLIS){

      format = WEEK_DAY;
    }else if(timeAgo < YEAR_MILLIS){

      format = MONTH_DAY;
    }else{

      format = MONTH_DAY_YEAR;
    }

    return new SimpleDateFormat(format,Locale.getDefault()).format(time);
  }


  public static String formatWithPattern(long time,String format){
    return new SimpleDateFormat(format,Locale.getDefault()).format(time);
  }

}
