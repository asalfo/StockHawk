package com.sam_chordas.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.sam_chordas.android.stockhawk.service.Constants;

/**
 * Created by asalfo on 17/04/16.
 */
public class QuoteWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, QuoteWidgetIntentService.class));
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        context.startService(new Intent(context, QuoteWidgetIntentService.class));
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (Constants.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            context.startService(new Intent(context, QuoteWidgetIntentService.class));
        }
    }


    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, String symbol) {
        Intent intent = new  Intent(context, QuoteWidgetIntentService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetId);
        intent.putExtra(Constants.SYMBOL,symbol);
        context.startService(intent);
    }
}
