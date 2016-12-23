package ru.megazlo.apnea;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.IntentFilter;
import android.os.Build;

import org.androidannotations.annotations.EApplication;

import ru.megazlo.apnea.receivers.BluetoothReceiver;

/** Created by iGurkin on 23.12.2016. */
@EApplication
public class ApneaApplication extends Application {

	BluetoothReceiver receiver = new BluetoothReceiver();

	public void onCreate() {
		super.onCreate();
		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
		registerReceiver(receiver, filter);
		BluetoothLeService_.intent(getBaseContext()).start();
	}
}
