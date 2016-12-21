package ru.megazlo.apnea.extend;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import ru.megazlo.apnea.R;

/** Created by iGurkin on 21.12.2016. */

public class BluetoothDeviceAdapter extends ArrayAdapter<BluetoothDevice> {

	private final LayoutInflater inflater;

	private final ArrayList<BluetoothDevice> leDevices = new ArrayList<>();
	private final HashMap<BluetoothDevice, Integer> rssiMap = new HashMap<>();

	public BluetoothDeviceAdapter(Context context) {
		super(context, R.layout.table_detail_row);
		inflater = LayoutInflater.from(context);
	}

	@NonNull
	@Override
	public View getView(int position, View cView, @NonNull ViewGroup parent) {
		BluetoothDeviceAdapter.ViewHolder holder;
		if (cView == null) {
			holder = new BluetoothDeviceAdapter.ViewHolder();
			cView = inflater.inflate(R.layout.table_device_row, null);
			holder.name = (TextView) cView.findViewById(R.id.dv_name);
			holder.address = (TextView) cView.findViewById(R.id.dv_address);
			cView.setTag(holder);
		} else {
			holder = (BluetoothDeviceAdapter.ViewHolder) cView.getTag();
		}
		BluetoothDevice item = getItem(position);
		holder.name.setText(item.getName());
		holder.address.setText(item.getAddress());
		return cView;
	}

	public void addDevice(BluetoothDevice device, int rssi) {
		if (!leDevices.contains(device) && device.getName() != null) {
			leDevices.add(device);
			notifyDataSetChanged();
		}
		rssiMap.put(device, rssi);
	}

	public BluetoothDevice getDevice(int position) {
		return leDevices.get(position);
	}

	public void clear() {
		leDevices.clear();
	}

	@Override
	public int getCount() {
		return leDevices.size();
	}

	@Override
	@NonNull
	public BluetoothDevice getItem(int i) {
		return leDevices.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	private class ViewHolder {
		TextView name;
		TextView address;
	}
}
