package com.sam_chordas.android.stockhawk.service;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Created by asalfo on 21/03/16.
 */
class BroadcastNotifier {
    private final LocalBroadcastManager mBroadcaster;

    public BroadcastNotifier(Context context) {

        // Gets an instance of the support library local broadcastmanager
        mBroadcaster = LocalBroadcastManager.getInstance(context);

    }
    public void broadcastIntentState(int state) {

        Intent localIntent = new Intent();

        // The Intent contains the custom broadcast action for this app
        localIntent.setAction(Constants.BROADCAST_ACTION);

        // Puts the status into the Intent
        localIntent.putExtra(Constants.EXTENDED_DATA_STATUS, state);
        localIntent.addCategory(Intent.CATEGORY_DEFAULT);

        // Broadcasts the Intent
        mBroadcaster.sendBroadcast(localIntent);

    }
}
