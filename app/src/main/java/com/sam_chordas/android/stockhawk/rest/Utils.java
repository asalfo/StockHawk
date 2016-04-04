package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;

  public static ArrayList quoteJsonToContentVals(String JSON){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    try{
      jsonObject = new JSONObject(JSON);
      if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject("query");
        int count = Integer.parseInt(jsonObject.getString("count"));
        if (count == 1){
          jsonObject = jsonObject.getJSONObject("results")
              .getJSONObject("quote");
          batchOperations.add(buildBatchOperation(jsonObject));
        } else{
          resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

          if (resultsArray != null && resultsArray.length() != 0){
            for (int i = 0; i < resultsArray.length(); i++){
              jsonObject = resultsArray.getJSONObject(i);
              batchOperations.add(buildBatchOperation(jsonObject));
            }
          }
        }
      }
    } catch (JSONException e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }

  public static String truncateBidPrice(String bidPrice){
    bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
    return bidPrice;
  }

  public static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format("%.2f", round);
    StringBuffer changeBuffer = new StringBuffer(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
            QuoteProvider.Quotes.CONTENT_URI);
    try {
      String change = jsonObject.getString("Change");
      builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
      builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
      builder.withValue(QuoteColumns.NAME, jsonObject.getString("Name"));
      builder.withValue(QuoteColumns.DAYS_LOW, jsonObject.getString("DaysLow"));
      builder.withValue(QuoteColumns.DAYS_HIGH,jsonObject.getString("DaysHigh"));
      builder.withValue(QuoteColumns.YEAR_LOW, jsonObject.getString("YearLow"));
      builder.withValue(QuoteColumns.YEAR_HIGH,jsonObject.getString("YearHigh"));
      builder.withValue(QuoteColumns.OPEN,jsonObject.getString("Open"));
      builder.withValue(QuoteColumns.PREVIOUS_CLOSE,jsonObject.getString("PreviousClose"));
      builder.withValue(QuoteColumns.VOLUME,jsonObject.getString("Volume"));
      builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
          jsonObject.getString("ChangeinPercent"), true));
      builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
      builder.withValue(QuoteColumns.ISCURRENT, 1);
      if (change.charAt(0) == '-'){
        builder.withValue(QuoteColumns.ISUP, 0);
      }else{
        builder.withValue(QuoteColumns.ISUP, 1);
      }
      builder.withValue(QuoteColumns.CREATED,Utils.currentTime());

    } catch (JSONException e){
      e.printStackTrace();
    }
    return builder.build();
  }


  public static  String currentTime(){
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Calendar cal = Calendar.getInstance();
    return dateFormat.format(cal.getTime());
  }

  public static  String formatDate(String string, String format) throws ParseException {
    DateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    DateFormat outFormat = new SimpleDateFormat(format);
    Calendar cal = Calendar.getInstance();
    cal.setTime(inFormat.parse(string));
    Log.d(LOG_TAG, string);
    Log.d(LOG_TAG,outFormat.format(cal.getTime()));
    return outFormat.format(cal.getTime());
  }

  public static  long DateToTimeStamp(String stringDate) throws ParseException {
    DateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    Calendar cal = Calendar.getInstance();
    cal.setTime(inFormat.parse(stringDate));
    return cal.getTimeInMillis();
  }

  public static String timeStampToDate(long timeStamp,String format){
    DateFormat outFormat = new SimpleDateFormat(format);
    Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(timeStamp);
    return outFormat.format(calendar.getTime());
  }

  public static Spanned formatText(Context context,int format,String originalText){
    Resources res = context.getResources();
    String text = String.format(res.getString(format), originalText);
    return  Html.fromHtml(text);

  }


  public static Boolean isOpenDay(){

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());


    return  calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY &&
                         calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY;

  }
  public static  String openDay(){

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    int day = calendar.get(Calendar.DAY_OF_WEEK);
    switch (day){
      case Calendar.SATURDAY:
        calendar.add(Calendar.DATE, -1);
        break;
      case Calendar.SUNDAY:
        calendar.add(Calendar.DATE, -2);
        break;

      default:

    }
    return new SimpleDateFormat("yyyy-MM-dd").format(calendar.getTime());
  }


  public static  String openDay(String format){
    if (null == format){
      format = "yyyy-MM-dd";
    }
    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    int day = calendar.get(Calendar.DAY_OF_WEEK);
    switch (day){
      case Calendar.SATURDAY:
        calendar.add(Calendar.DATE, -1);
        break;
      case Calendar.SUNDAY:
        calendar.add(Calendar.DATE, -2);
        break;

      default:

    }
    return new SimpleDateFormat(format).format(calendar.getTime());
  }


  public static int divisor(int number)
  {
    int i;
    for (i = 2; i <=Math.sqrt(number); i++)
    {
      if (number % i == 0)
      {
        return number/i;
      }
    }
    return 1;
  }
}
