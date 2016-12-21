package ru.megazlo.apnea.component;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;
import java.util.List;

import ru.megazlo.apnea.BuildConfig;
import ru.megazlo.apnea.R;
import ru.megazlo.apnea.extend.BluetoothDeviceAdapter;

/** Created by iGurkin on 26.10.2016. */
// BT LE поддерживается с android 4.3
//@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BluetoothPreference extends DialogPreference {

	private final static int SCAN_PERIOD = 2000;

	private int REQUEST_ENABLE_BT = 1;

	private String prefix;

	private BluetoothAdapter mBluetoothAdapter;

	private ListView list;

	private BluetoothDeviceAdapter adapter;

	private Scanner scanner;

	public BluetoothPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initStyles(context, attrs, 0);
	}

	public BluetoothPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initStyles(context, attrs, defStyleAttr);
	}

	private void initStyles(Context context, AttributeSet attrs, int defStyleAttr) {
		setPositiveButtonText("");
		setNegativeButtonText(R.string.cancel);
		TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TimePreference, defStyleAttr, 0);
		prefix = attributes.getString(R.styleable.TimePreference_prefix);
		prefix = prefix == null ? "" : prefix + " ";
	}

	@Override
	protected View onCreateDialogView() {
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.gravity = Gravity.CENTER;

		list = new ListView(getContext());
		list.setLayoutParams(layoutParams);

		adapter = new BluetoothDeviceAdapter(getContext());
		list.setAdapter(adapter);
		list.setOnItemClickListener((adapterView, view, i, l) -> {
			Log.d("BluetoothPreference", "item selected " + i);
			final BluetoothDevice item = adapter.getItem(i);
			setBluetoothAddress(item.getAddress());
			BluetoothPreference.this.getDialog().dismiss();
		});

		FrameLayout dialogView = new FrameLayout(getContext());
		dialogView.addView(list);

		final BluetoothManager bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			((Activity) getContext()).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			Toast.makeText(getContext(), "You must enable bluetooth", Toast.LENGTH_SHORT).show();
		} else {
			scanLeDevice(true);
		}
		return dialogView;
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		if (!getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(getContext(), "Bluetooth LE not supported", Toast.LENGTH_SHORT).show();
			if (!BuildConfig.DEBUG) {
				return super.onCreateView(parent);
			}
		}
		return super.onCreateView(parent);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
		String val = restorePersistedValue ? getPersistedString("") : defaultValue.toString();
		// need to persist here for default value to work
		//setTime(getMinutes (time), getSeconds(time));
		setSummary(val);
	}

	@Override
	public void onActivityDestroy() {
		super.onActivityDestroy();
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);
		//list.setValue(getValue());
	}

	private void setBluetoothAddress(String address) {
		scanLeDevice(false);
		persistString(address);
		notifyDependencyChange(shouldDisableDependents());
		notifyChanged();
		setSummary(address);
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			list.clearFocus();
			/*int newValue = list.getValue();
			if (callChangeListener(newValue)) {
				setValue(newValue);
			}*/
		}
		super.onDialogClosed(positiveResult);
	}

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			if (scanner == null) {
				scanner = new Scanner(mBluetoothAdapter, mLeScanCallback);
			}
			scanner.startScanning();
		} else if (scanner != null) {
			scanner.stopScanning();
		}
	}

	private BluetoothAdapter.LeScanCallback mLeScanCallback = (device, rssi, bytes) -> ((Activity) getContext()).runOnUiThread(() -> {
		adapter.addDevice(device, rssi);
		Log.d("onLeScan", device.toString());
	});

	private static class Scanner extends Thread {
		private final BluetoothAdapter bluetoothAdapter;
		private final BluetoothAdapter.LeScanCallback mLeScanCallback;

		private volatile boolean isScanning = false;

		Scanner(BluetoothAdapter adapter, BluetoothAdapter.LeScanCallback callback) {
			bluetoothAdapter = adapter;
			mLeScanCallback = callback;
		}

		public boolean isScanning() {
			return isScanning;
		}

		void startScanning() {
			synchronized (this) {
				isScanning = true;
				start();
			}
		}

		void stopScanning() {
			synchronized (this) {
				isScanning = false;
				bluetoothAdapter.stopLeScan(mLeScanCallback);
			}
		}

		@Override
		public void run() {
			try {
				while (true) {
					synchronized (this) {
						if (!isScanning) {
							break;
						}
						bluetoothAdapter.startLeScan(mLeScanCallback);
					}
					sleep(SCAN_PERIOD);
					synchronized (this) {
						bluetoothAdapter.stopLeScan(mLeScanCallback);
					}
				}
			} catch (InterruptedException ignore) {
			} finally {
				bluetoothAdapter.stopLeScan(mLeScanCallback);
			}
		}
	}
}
