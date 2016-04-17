package com.sam_chordas.android.stockhawk.widget;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.Constants;
import com.sam_chordas.android.stockhawk.ui.DetailActivity;

/**
 * Created by asalfo on 17/04/16.
 */
public class QuoteWidgetIntentService extends IntentService {

    public QuoteWidgetIntentService() {
        super("QuoteWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String symbol = prefs.getString(getApplicationContext().getString(R.string.pref_symbol_key),
                getApplicationContext().getString(R.string.pref_symbol_default));

        // Retrieve all of the Today widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                QuoteWidgetProvider.class));

        // Get quotes data from the ContentProvider
        Cursor data = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ? AND "+QuoteColumns.SYMBOL+ " = ?",
                new String[]{"1",symbol},
                null);
        if (data == null) {
            return;
        }
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        // Extract the quote data from the Cursor

        long id = data.getLong(data.getColumnIndex("_id"));
        symbol = data.getString(data.getColumnIndex("symbol"));
        String change = data.getString(data.getColumnIndex("change"));
        String bid_price = data.getString(data.getColumnIndex("bid_price"));
        int is_up = data.getInt(data.getColumnIndex("bid_price"));
        data.close();

        // Perform this loop procedure for each Today widget
        for (int appWidgetId : appWidgetIds) {
            // Find the correct layout based on the widget's width
            int widgetWidth = getWidgetWidth(appWidgetManager, appWidgetId);
            int largeWidth = getResources().getDimensionPixelSize(R.dimen.widget_large_width);
            int layoutId = R.layout.widget;
            if (widgetWidth >= largeWidth) {
                layoutId = R.layout.widget_large;
            }

            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            views.setTextViewText(R.id.stock_symbol, symbol);
            views.setTextViewText(R.id.change, change);
            if(layoutId == R.layout.widget_large){
                views.setTextViewText(R.id.bid_price, bid_price);
            }

            if(is_up == 1) {
                views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
            }else{
                views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
            }


            // Create an Intent to launch MainActivity

            Uri contentUri = QuoteProvider.Quotes.withId(id);
            Intent launchIntent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.detail_widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }


    private int getWidgetWidth(AppWidgetManager appWidgetManager, int appWidgetId) {
        // Prior to Jelly Bean, widgets were always their default size
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            return getResources().getDimensionPixelSize(R.dimen.widget_default_width);
        }
        // For Jelly Bean and higher devices, widgets can be resized - the current size can be
        // retrieved from the newly added App Widget Options
        return getWidgetWidthFromOptions(appWidgetManager, appWidgetId);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private int getWidgetWidthFromOptions(AppWidgetManager appWidgetManager, int appWidgetId) {
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        if (options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            // The width returned is in dp, but we'll convert it to pixels to match the other widths
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, minWidthDp,
                    displayMetrics);
        }
        return  getResources().getDimensionPixelSize(R.dimen.widget_default_width);
    }



}
