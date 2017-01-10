package ru.megazlo.apnea.component;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.*;
import android.bluetooth.le.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.*;
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
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothPreference extends DialogPreference {

	private final static int SCAN_PERIOD = 2000;

	private int REQUEST_ENABLE_BT = 1;

	private String prefix;

	private BluetoothAdapter mBluetoothAdapter;
	private ListView list;
	private BluetoothDeviceAdapter adapter;
	private Scanner scanner;
	private BluetoothAdapter.LeScanCallback mLeScanCallback;
	private ScanCallback mScanCallback;
	private BluetoothLeScanner mLEScanner;

	public BluetoothPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		initStyles(context, attrs, 0);
	}

	public BluetoothPreference(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initStyles(context, attrs, defStyleAttr);
	}

	private void initStyles(Context context, AttributeSet attrs, int defStyleAttr) {
		setDialogLayoutResource(R.layout.bth_list_dialog);
		setPositiveButtonText("");
		setNegativeButtonText(R.string.cancel);
		TypedArray attributes = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TimePreference, defStyleAttr, 0);
		prefix = attributes.getString(R.styleable.TimePreference_prefix);
		prefix = prefix == null ? "" : prefix + " ";
	}

	@Override
	protected void showDialog(Bundle state) {
		final BluetoothManager bluetoothManager = (BluetoothManager) getContext().getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			((Activity) getContext()).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			Toast.makeText(getContext(), R.string.bt_must_enable, Toast.LENGTH_SHORT).show();
			return;
		}
		super.showDialog(state);
		scanLeDevice(true);
	}

	@Override
	protected void onBindDialogView(View view) {
		super.onBindDialogView(view);

		list = (ListView) view.findViewById(R.id.bth_found_list);
		adapter = new BluetoothDeviceAdapter(getContext());
		list.setAdapter(adapter);
		list.setOnItemClickListener((adapterView, v, i, l) -> {
			Log.d("BluetoothPreference", "item selected " + i);
			final BluetoothDevice item = adapter.getItem(i);
			setBluetoothAddress(item.getAddress());
			BluetoothPreference.this.getDialog().dismiss();
		});

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
			createScanCallback();
			createLeScanner();
		} else {
			mLeScanCallback = (device, rssi, bytes) -> ((Activity) getContext()).runOnUiThread(() -> adapter.addDevice(device));
		}
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void createScanCallback() {
		mScanCallback = new ScanCallback() {
			@Override
			public void onScanResult(int callbackType, ScanResult result) {
				BluetoothDevice device = result.getDevice();
				Log.i("callbackType name ", device.getName() == null ? "null name " : device.getName());
				adapter.addDevice(device);
			}

			@Override
			public void onBatchScanResults(List<ScanResult> results) {
				for (ScanResult sr : results) {
					Log.i("Scan Item: ", sr.toString());
				}
			}
		};
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void createLeScanner() {
		if (mBluetoothAdapter != null) {
			mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
		}
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
		setSummary(val);
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
		}
		scanLeDevice(false);
	}

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			if (scanner == null) {
				scanner = new Scanner(mBluetoothAdapter, mLeScanCallback, mLEScanner, mScanCallback);
			}
			scanner.startScanning();
			AsyncTask.execute(scanner);
		} else if (scanner != null) {
			scanner.stopScanning();
		}
	}

	private static class Scanner implements Runnable {
		private final BluetoothAdapter bluetoothAdapter;
		private final BluetoothAdapter.LeScanCallback mLeScanCallback;
		private ScanCallback mScanCallback;
		private BluetoothLeScanner mLEScanner;
		private ScanSettings scanSettings;
		private List<ScanFilter> filters;

		private volatile boolean isScanning = false;

		Scanner(BluetoothAdapter adapter, BluetoothAdapter.LeScanCallback callback, BluetoothLeScanner leScaner, ScanCallback leCallback) {
			bluetoothAdapter = adapter;
			mLeScanCallback = callback;
			mLEScanner = leScaner;
			mScanCallback = leCallback;
			if (Build.VERSION.SDK_INT > 21) {
				scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();
				filters = new ArrayList<>();
			}
		}

		public boolean isScanning() {
			return isScanning;
		}

		void startScanning() {
			synchronized (this) {
				isScanning = true;
			}
		}

		void stopScanning() {
			synchronized (this) {
				isScanning = false;
				stopScan();
				//bluetoothAdapter.stopLeScan(mLeScanCallback);
			}
		}

		@Override
		public void run() {
			try {
				while (true) {
					synchronized (this) {
						if (!isScanning) {
							return;
						}
						startScan();
						//bluetoothAdapter.startLeScan(mLeScanCallback);
					}
					Thread.sleep(SCAN_PERIOD);
					synchronized (this) {
						startScan();
						//bluetoothAdapter.stopLeScan(mLeScanCallback);
					}
				}
			} catch (Exception ignore) {
			} finally {
				stopScan();
				//bluetoothAdapter.stopLeScan(mLeScanCallback);
			}
		}

		private void stopScan() {
			if (Build.VERSION.SDK_INT < 21) {
				bluetoothAdapter.stopLeScan(mLeScanCallback);
			} else {
				mLEScanner.stopScan(mScanCallback);
			}
		}

		private void startScan() {
			if (Build.VERSION.SDK_INT < 21) {
				bluetoothAdapter.startLeScan(mLeScanCallback);
			} else {
				mLEScanner.startScan(filters, scanSettings, mScanCallback);
			}
		}
	}
}
