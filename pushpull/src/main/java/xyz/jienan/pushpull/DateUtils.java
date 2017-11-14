package xyz.jienan.pushpull;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Jienan on 2017/11/14.
 */

public class DateUtils {

    /**
     * Parse UTC date 2017-11-12T12:14:22.585Z
     */
    public static String parseMongoUTC(String from, SimpleDateFormat toFormat) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            Date date = sdf.parse(from);
            return toFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static long getTimeDiffFromNow(String timeString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        try {
            Date date = sdf.parse(timeString);
            return date.getTime() - new Date().getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
