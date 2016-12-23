package ru.megazlo.apnea;

import android.annotation.TargetApi;
import android.app.Service;
import android.bluetooth.*;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import org.androidannotations.annotations.EService;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.List;
import java.util.UUID;

import ru.megazlo.apnea.service.ApneaPrefs_;

/** Created by iGurkin on 04.10.2016. */
@EService
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothLeService extends Service {
	private final static String TAG = BluetoothLeService.class.getSimpleName();

	@Pref
	ApneaPrefs_ pref;
	@SystemService
	BluetoothManager bluetoothManager;

	private BluetoothAdapter bluetoothAdapter;
	private BluetoothGatt bluetoothGatt;
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
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		bluetoothAdapter = bluetoothManager.getAdapter();
		if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
			stopSelf();
		}
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(pref.deviceAddress().get());
		bluetoothGatt = device.connectGatt(getBaseContext().getApplicationContext(), false, bleGattCallback);
		bluetoothGatt.discoverServices();
		List<BluetoothGattService> services = bluetoothGatt.getServices();
		for (BluetoothGattService service : services) {
			List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
			for (BluetoothGattCharacteristic characteristic : characteristics) {
				bluetoothGatt.setCharacteristicNotification(characteristic, true);
				BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
				descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				bluetoothGatt.writeDescriptor(descriptor);
			}
			/*for (BluetoothGattDescriptor descriptor : characteristics.get(1).getDescriptors()) {
				//find descriptor UUID that matches Client Characteristic Configuration (0x2902)
				// and then call setValue on that descriptor
				descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
				bluetoothGatt.writeDescriptor(descriptor);
			}*/
		}

		int i = Build.VERSION_CODES.JELLY_BEAN_MR2;
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		bluetoothGatt.disconnect();
		bluetoothGatt.close();
	}

	private final BluetoothGattCallback bleGattCallback = new BluetoothGattCallback() {

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			Log.d(TAG, "onCharacteristicChanged");
			// this will get called anytime you perform a read or write characteristic operation
		}

		@Override
		public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
			Log.d(TAG, "onConnectionStateChange");
			// this will get called when a device connects or disconnects
		}

		@Override
		public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
			Log.d(TAG, "onServicesDiscovered");
			// this will get called after the client initiates a BluetoothGatt.discoverServices() call
		}
	};
}
