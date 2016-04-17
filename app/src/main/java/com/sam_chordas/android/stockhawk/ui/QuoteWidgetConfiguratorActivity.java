package com.sam_chordas.android.stockhawk.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

public class QuoteWidgetConfiguratorActivity extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if they press the back button.
        setResult(RESULT_CANCELED);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        Bundle arguments = new Bundle();
        if (extras != null) {
            arguments.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID));

        }


        SettingsFragment fragment = new  SettingsFragment();
        fragment.setArguments(arguments);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                fragment).commit();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public Intent getParentActivityIntent() {
        return super.getParentActivityIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }
}
