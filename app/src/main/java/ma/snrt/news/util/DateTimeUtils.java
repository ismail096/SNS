package ma.snrt.news.util;


import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public static String getTimeAgoFr(String date) {
        long time = getTimeStamp(date);
        if (time < 1000000000000L) {
            time *= 1000;
        }
        long now = System.currentTimeMillis();
        /*if (time > now || time <= 0) {
            return "maintenant";
        }*/

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "maintenant";
        } else if (diff < 60 * MINUTE_MILLIS) {
            return "Il y a "+diff / MINUTE_MILLIS +" m";
        } else if (diff < 24 * HOUR_MILLIS) {
            return "Il y a "+diff / HOUR_MILLIS + " h";
        }
        else if (diff < 48 * HOUR_MILLIS) {
            return "Hier";
        }
        else if(diff < 168 * HOUR_MILLIS){
            return  "Il y a "+diff / DAY_MILLIS + " j";
        }
        else {
            return getFlashFormat(date);
        }
    }

    public static String getTimeAgoAr(String date) {
        long time = getTimeStamp(date);
        if (time < 1000000000000L) {
            time *= 1000;
        }
        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return "لحظات";
        }

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "منذ لحظات";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "منذ دقيقة";
        } else if (diff < 3 * MINUTE_MILLIS) {
            return "منذ دقيقتين";
        }
        else if (diff < 60 * MINUTE_MILLIS) {
            return "منذ "+diff / MINUTE_MILLIS + " د";
        } else if (diff < 120 * MINUTE_MILLIS) {
            return "منذ "+"ساعة";
        }
        else if (diff < 11 * HOUR_MILLIS) {
            return "منذ "+diff / HOUR_MILLIS + " ساعات";
        } else if (diff < 24 * HOUR_MILLIS) {
            return "منذ "+diff / HOUR_MILLIS + " ساعة";
        }
        else if (diff < 48 * HOUR_MILLIS) {
            return "الأمس";
        }
        else if (diff < 7 * DAY_MILLIS) {
            return "منذ "+diff / DAY_MILLIS + " أيام";
        }
        else {
            return getFlashFormat(date);
        }
    }

    public static long getTimeStamp(String date){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy - hh:mm");
        Date date_long = null;
        try {
            date_long = (Date)formatter.parse(date);
            return date_long.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }

    }

    public static String getFlashFormat(String dateString) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy - hh:mm", Locale.US);
        DateFormat targetFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String formattedDate = null;
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(dateString);
            System.out.println(dateString);
            formattedDate = targetFormat.format(convertedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formattedDate;
    }


    public static String getDayFromDate(String dateAsString){
        String day = "";
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy - hh:mm");
        Date date = null;
        try {
            date = formatter.parse(dateAsString);
            day  = (String) android.text.format.DateFormat.format("dd",   date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return day;
    }

    public static String getMonthFromDate(String dateAsString){
        String month = "";
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy - hh:mm");
        Date date = null;
        try {
            date = formatter.parse(dateAsString);
            month  = (String) android.text.format.DateFormat.format("MMM",   date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return month;
    }

    public static String getHourFromDate(String dateAsString){
        String day = "";
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy - hh:mm");
        Date date = null;
        try {
            date = formatter.parse(dateAsString);
            day  = (String) android.text.format.DateFormat.format("HH:mm",   date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return day;
    }

    public static String getRelativeDayFr(String date) {
        long time = getTimeStamp(date);
        if (time < 1000000000000L) {
            time *= 1000;
        }
        long now = System.currentTimeMillis();
        final long diff = now - time;
        if (diff < 24 * HOUR_MILLIS) {
            return "Aujourd'hui";
        }
        else if (diff < 48 * HOUR_MILLIS) {
            return "Hier";
        }
        else {
            return "";
        }
    }

    public static String getRelativeDayAr(String date) {
        long time = getTimeStamp(date);
        if (time < 1000000000000L) {
            time *= 1000;
        }
        long now = System.currentTimeMillis();
        final long diff = now - time;
        if (diff < 24 * HOUR_MILLIS) {
            return "اليوم";
        }
        else if (diff < 48 * HOUR_MILLIS) {
            return "الأمس";
        }
        else {
            return "";
        }
    }
}
