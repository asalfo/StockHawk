package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.github.mikephil.charting.data.Entry;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static final String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;



  public enum Option {
    ONEDAY, FIVEDAYS, THREEMONTHS, SIXMONTHS,YEAR
  }

  public static ArrayList quoteJsonToContentVals(String JSON){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject;
    JSONArray resultsArray;
    try{
      jsonObject = new JSONObject(JSON);
      if (jsonObject.length() != 0){
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

  private static String truncateBidPrice(String bidPrice){
    bidPrice = String.format(Locale.getDefault(),"%.2f", Float.parseFloat(bidPrice));
    return bidPrice;
  }

  private static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format(Locale.getDefault(),"%.2f", round);
    StringBuilder changeBuffer = new StringBuilder(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  private static ContentProviderOperation buildBatchOperation(JSONObject jsonObject){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
            QuoteProvider.Quotes.CONTENT_URI);
    try {
      String change = jsonObject.getString("Change");
      builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol").toUpperCase());
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


  private static  String currentTime(){
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
    Calendar cal = Calendar.getInstance();
    return dateFormat.format(cal.getTime());
  }


  public static  String formatDate(String string, String format) throws ParseException {
    DateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
    DateFormat outFormat = new SimpleDateFormat(format,Locale.getDefault());
    Calendar cal = Calendar.getInstance();
    cal.setTime(inFormat.parse(string));
    return outFormat.format(cal.getTime());
  }

    public static  String formatDate(String string, String inFormat,String outFormat) throws ParseException {
        DateFormat in = new SimpleDateFormat(inFormat,Locale.getDefault());
        DateFormat out = new SimpleDateFormat(outFormat,Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.setTime(in.parse(string));
        return out.format(cal.getTime());
    }

  public static  long DateToTimeStamp(String stringDate) throws ParseException {
    DateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
    Calendar cal = Calendar.getInstance();
    cal.setTime(inFormat.parse(stringDate));
    return cal.getTimeInMillis();
  }

  public static String timeStampToDate(long timeStamp,String format){
    DateFormat outFormat = new SimpleDateFormat(format,Locale.getDefault());
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

  public static String startDate(Option option){

    Calendar calendar = Calendar.getInstance();
    calendar.setTime(new Date());
    switch (option){

      case FIVEDAYS:
        calendar.add(Calendar.DATE,-7);
        break;
      case THREEMONTHS:
        calendar.add(Calendar.DATE,-90);
        break;
      case SIXMONTHS:
        calendar.add(Calendar.DATE,-180);
        break;
      case YEAR:
        calendar.add(Calendar.DATE,-360);
        break;
    }

      return new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(calendar.getTime());
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
    return new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(calendar.getTime());
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
    return new SimpleDateFormat(format,Locale.getDefault()).format(calendar.getTime());
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




  public static ChartVal buildChartValsJson(String JSON){
    ChartVal<ArrayList<String>, ArrayList<Entry>> chartVal =null;
    ArrayList<String> xVals = new ArrayList<>();
    ArrayList<Entry>  yVals = new ArrayList<>();
    JSONObject jsonObject;
    JSONArray resultsArray;
    try{

      jsonObject = new JSONObject(JSON);

      if (jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject("query");

        resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

        if (resultsArray != null && resultsArray.length() != 0){
          for (int i = 0; i < resultsArray.length(); i++){
            try {
            jsonObject = resultsArray.getJSONObject(i);

              String date = Utils.formatDate(jsonObject.getString("Date"),"yyyy-MM-dd", "MMM dd");

              xVals.add(date);
              Float value = Float.valueOf(jsonObject.getString("Close"));
              yVals.add(new Entry(value, i));
                Log.d(LOG_TAG,"value->"+date);

            } catch (ParseException e) {
              e.printStackTrace();
                Log.d(LOG_TAG,"value->"+e.getMessage());
            }


          }

          chartVal = new ChartVal<>(xVals, yVals);
        }


      }
    } catch (JSONException e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return chartVal;
  }

  public static ChartVal buildChartValsFromCursor(Context context, String symbol) {

    ChartVal<ArrayList<String>, ArrayList<Entry>> chartVal =null;
    ArrayList<String> xVals = new ArrayList<>();
    ArrayList<Entry>  yVals = new ArrayList<>();

    Uri uri = QuoteProvider.Quotes.withSymbol(symbol);
    Cursor cursor = context.getContentResolver().query(
                              uri,
                              new String[]{QuoteColumns.BIDPRICE,QuoteColumns.CREATED},
                              QuoteColumns.CREATED + " BETWEEN ? AND ?",
                              new String[]{Utils.openDay()+" 00:00:00",Utils.openDay()+" 23:59:59"},
                              QuoteColumns.CREATED);

    assert cursor != null;
    if (cursor.moveToFirst()) {

      int xIndex = 0;
      do {

        try {
          xVals.add(Utils.formatDate(cursor.getString(cursor.getColumnIndex("created")), "H:mm"));
          Float value = Float.valueOf(cursor.getString(cursor.getColumnIndex("bid_price")));
          yVals.add(new Entry(value, xIndex));
          xIndex++;

        } catch (ParseException e) {
          e.printStackTrace();
        }

      } while (cursor.moveToNext());

      cursor.close();

      chartVal = new ChartVal<>(xVals, yVals);

    }
    return chartVal;
  }
}
