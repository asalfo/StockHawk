package com.sam_chordas.android.stockhawk.service;

/**
 * Created by asalfo on 21/03/16.
 */
public final class Constants {

    public static final String SYMBOL = "symbol";
    public static final String BROADCAST_ACTION = "com.sam_chordas.android.BROADCAST";
    public static final String EXTENDED_DATA_STATUS = "com.sam_chordas.android.STATUS";
    public static final String ACTION_DATA_UPDATED = "com.sam_chordas.android.ACTION_DATA_UPDATED";
    public static final int STATE_ERROR = 1;
    public static final int STATE_ACTION_COMPLETE = 2;
    public static final String TAG = "tag";
    public static final String INIT = "init";
    public static final String ADD = "add";
    public static final String SET_BACKGROUND_RESOURCE = "setBackgroundResource";
    public static final String PERIODIC_TAG = "periodic";
    public static final String YAHOOAPIS_QUERY = "https://query.yahooapis.com/v1/public/yql?q=";
    public static final String SELECT_STATEMENT = "select * from yahoo.finance.quotes where symbol ";
    public static final String SELECT_HISTORICALDATA = "select * from yahoo.finance.historicaldata where symbol ";
    public static final String DEFAULT_QUOTE = "\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\")";
    public static final String YQL_FORMAT = "&format=json&diagnostics=true&env=store%3A%2F%2Fdatatables."
            + "org%2Falltableswithkeys&callback=";

}
