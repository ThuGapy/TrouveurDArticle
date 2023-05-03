package net.info420.trouveurarticle.database;

import android.text.TextUtils;

import net.info420.trouveurarticle.scrappers.StoreFront;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class LinkStatus implements Comparable<LinkStatus> {
    public boolean EnStock;
    public double Prix;
    public Date TimeStamp;
    public String Link;
    public StoreFront Boutique;
    private PriceStatus status = PriceStatus.NO_DATA;


    public LinkStatus(boolean stock, double price, String time, String link, StoreFront storeFront) {
        EnStock = stock;
        Prix = price;
        TimeStamp = convertToDate(time);
        Link = link;
        Boutique = storeFront;
    }

    public void DetermineStatus(double desiredPrice) {
        if(EnStock && Prix <= desiredPrice) {
            status = PriceStatus.GOOD;
        } else if(EnStock && Prix > desiredPrice) {
            status = PriceStatus.OVERPRICED;
        } else if(!EnStock && Prix == 0) {
            status = PriceStatus.NO_DATA;
        } else {
            status = PriceStatus.OOS;
        }
    }

    public PriceStatus getStatus() {
        return status;
    }

    public String getElapsedTime() {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            return dateFormat.format(TimeStamp);
        } catch(NullPointerException ex) {
            return "";
        }
    }

    @Override
    public int compareTo(LinkStatus linkStatus) {
        if(this.EnStock && !linkStatus.EnStock) {
            return -1;
        } else if(!this.EnStock && linkStatus.EnStock) {
            return 1;
        } else {
            return Double.compare(this.Prix, linkStatus.Prix);
        }
    }

    private Date convertToDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try {
            if(TextUtils.isEmpty(date)) {
                return null;
            }
            return dateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}
