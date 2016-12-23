package ru.megazlo.apnea;

import android.app.Service;
import android.bluetooth.*;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.sharedpreferences.Pref;

import ru.megazlo.apnea.service.ApneaPrefs_;

/** Created by iGurkin on 04.10.2016. */
@EService
public class BluetoothLeService extends Service {
	private final static String TAG = BluetoothLeService.class.getSimpleName();

	@Pref
	ApneaPrefs_ pref;
	@SystemService
	BluetoothManager bluetoothManager;
	private BluetoothAdapter mBluetoothAdapter;
	private String mBluetoothDeviceAddress;
	private BluetoothGatt mBluetoothGatt;
	private int mConnectionState = STATE_DISCONNECTED;

	private static final int STATE_DISCONNECTED = 0;
	private static final int STATE_CONNECTING = 1;
	private static final int STATE_CONNECTED = 2;

	public final static String ACTION_GATT_CONNECTED = "ru.megazlo.apnea.le.ACTION_GATT_CONNECTED";
	public final static String ACTION_GATT_DISCONNECTED = "ru.megazlo.apnea.le.ACTION_GATT_DISCONNECTED";
	public final static String ACTION_GATT_SERVICES_DISCOVERED = "ru.megazlo.apnea.le.ACTION_GATT_SERVICES_DISCOVERED";
	public final static String ACTION_DATA_AVAILABLE = "ru.megazlo.apnea.le.ACTION_DATA_AVAILABLE";
	public final static String EXTRA_DATA = "ru.megazlo.apnea.le.EXTRA_DATA";

	//public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

	@Override
	public IBinder onBind(Intent intent) {
		int i = Build.VERSION_CODES.JELLY_BEAN_MR2;
		return null;
	}
}
