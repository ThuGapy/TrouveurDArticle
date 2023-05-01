package net.info420.trouveurarticle;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Utils {
    public static String FormatPrice(double price) {
        String priceTotal = String.valueOf((int)Math.floor(price));
        String priceDecimalText = String.valueOf(price);

        int dotIndex = priceDecimalText.indexOf(".");
        if (dotIndex == -1) {
            return String.valueOf(price) + ".00$";
        } else {
            String decimal = priceDecimalText.substring(dotIndex + 1);
            if (decimal.length() == 0) {
                return priceTotal + ".00$";
            } else if (decimal.length() == 1) {
                return priceTotal + "." + decimal + "0$";
            } else {
                return priceTotal + "." + decimal + "$";
            }
        }
    }

    public static long GetStartOfDayTimeStamp(Date date) {
        Calendar calendar = GetStartOfDayCalendar(date);
        return calendar.getTimeInMillis();
    }

    public static Calendar GetStartOfDayCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static String getFormattedTime(Date date) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return dateFormat.format(date);
        } catch(NullPointerException ex) {
            return "";
        }
    }
}
