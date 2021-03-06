package ru.megazlo.apnea.receivers;

import android.content.BroadcastReceiver;

public abstract class DetailFragmentReceiver extends BroadcastReceiver {
    public final static String ACTION_UPDATER = "ru.megazlo.apnea.APNEA_DETAIL_UPDATE";

    public final static String KEY_TABLE = "KEY_TABLE";
    public final static String KEY_MAX = "KEY_MAX";
    public final static String KEY_ENDED = "KEY_ENDED";
    public final static String KEY_PROGRESS = "KEY_PROGRESS";
    public final static String KEY_ROW = "KEY_ROW";
    public final static String KEY_ROW_TYPE = "KEY_ROW_TYPE";
}
