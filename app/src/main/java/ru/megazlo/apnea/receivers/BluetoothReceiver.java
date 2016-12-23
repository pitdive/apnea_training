package ru.megazlo.apnea.receivers;

import android.bluetooth.BluetoothAdapter;
import android.content.*;
import android.util.Log;

/** Created by iGurkin on 23.12.2016. */

public class BluetoothReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		final String action = intent.getAction();

		if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
			final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
			switch (state) {
				case BluetoothAdapter.STATE_OFF:
					setChangeState("Bluetooth off");
					break;
				case BluetoothAdapter.STATE_TURNING_OFF:
					setChangeState("Turning Bluetooth off...");
					break;
				case BluetoothAdapter.STATE_ON:
					setChangeState("Bluetooth on");
					break;
				case BluetoothAdapter.STATE_TURNING_ON:
					setChangeState("Turning Bluetooth on...");
					break;
			}
		}
	}

	private void setChangeState(String msg) {
		Log.d(this.getClass().getSimpleName(), msg);
	}
}
