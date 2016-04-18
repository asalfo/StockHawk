package com.sam_chordas.android.stockhawk.ui;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.Constants;
import com.sam_chordas.android.stockhawk.widget.QuoteWidgetProvider;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by asalfo on 17/04/16.
 */
public class SettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener  {

    public static final String DEFAULT_VALUE = "GOOG";
    int mWidgetId =  AppWidgetManager.INVALID_APPWIDGET_ID ;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {

           mWidgetId = arguments.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

        }

        if (mWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            getActivity().finish();
        }
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_widget);


        final ListPreference listPreference = (ListPreference) findPreference(getString(R.string.pref_symbol_key));

        setListPreferenceData(listPreference,getActivity());


        // For all preferences, attach an OnPreferenceChangeListener so the UI summary can be
        // updated when the preference changes.
        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_symbol_key)));
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * Attaches a listener so the summary is always updated with the preference value.
     * Also fires the listener once, to initialize the summary (so it shows up before the value
     * is changed.)
     */
    private void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(this);

        // Trigger the listener immediately with the preference's
        // current value.
        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }

        // Set widget result
        final Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mWidgetId);
        resultValue.putExtra(Constants.SYMBOL, stringValue);
        getActivity().setResult(Activity.RESULT_OK, resultValue);

        // Request widget update
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getActivity());
        QuoteWidgetProvider.updateAppWidget(getActivity(), appWidgetManager, mWidgetId, stringValue);
        return true;
    }

    protected static void setListPreferenceData(ListPreference lp, Context context) {

        List<String> entries = new ArrayList<>();
        List<String>   entryValues =  new ArrayList<>();
        // Get quotes data from the ContentProvider
        Cursor data = context.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
        if (data == null) {
            return;
        }
        while (data.moveToNext()) {
            entries.add(data.getString(data.getColumnIndex(QuoteColumns.SYMBOL)));
            entryValues.add(data.getString(data.getColumnIndex(QuoteColumns.SYMBOL)));
        }
        data.close();


        lp.setEntries(entries.toArray(new CharSequence[entries.size()]));
        lp.setDefaultValue(DEFAULT_VALUE);
        lp.setEntryValues(entryValues.toArray(new CharSequence[entryValues.size()]));
    }
}
