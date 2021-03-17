package hashed.app.ampassadors.Utils;

import android.text.format.DateUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class TimeFormatter {

//  public static final long SECOND_MILLIS = 1000,
//          MINUTE_MILLIS = 60 * SECOND_MILLIS,
//          HOUR_MILLIS = 60 * MINUTE_MILLIS,
//          DAY_MILLIS = 24 * HOUR_MILLIS,
//          WEEK_MILLIS = 7 * DAY_MILLIS,
//          MONTH_MILLIS = 30 * DAY_MILLIS,
//          YEAR_MILLIS = 12 * MONTH_MILLIS;

  public static final String
          HOUR_MINUTE= "h:mm a",
          WEEK_DAY = "EEE",
          MONTH_DAY = "MMM dd",
          MONTH_DAY_YEAR = "MMM dd yyyy",
          MONTH_DAY_YEAR_HOUR_MINUTE = "dd/mm/yyyy h:mm a";

  public static String formatTime(long time){

    if (time < 1000000000000L) {
      time *= 1000;
    }

    final long timeAgo = System.currentTimeMillis() - time;

    String format ="";
    if(timeAgo < DateUtils.DAY_IN_MILLIS){

      format = HOUR_MINUTE;

    }else if(timeAgo < DateUtils.WEEK_IN_MILLIS){

      format = WEEK_DAY;
    }else if(timeAgo < DateUtils.YEAR_IN_MILLIS){

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
