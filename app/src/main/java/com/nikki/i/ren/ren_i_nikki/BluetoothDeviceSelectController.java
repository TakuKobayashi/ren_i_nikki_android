package com.nikki.i.ren.ren_i_nikki;

import java.util.ArrayList;
import java.util.Set;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

public class BluetoothDeviceSelectController{
	private BluetoothAdapter mBluetoothAdapter;
	private Context mContext;
	private ConnectableDeviceCallback mCallback;

    public BluetoothDeviceSelectController(Context context){
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBluetoothAdapter.enable();
		mContext = context;
	}

	public void setConnectableDeviceCallback(ConnectableDeviceCallback callback){
		mCallback = callback;
	}

	public void startDiscovery(){
		Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
		Log.d(Config.TAG, devices.toString());
		SearchBluetoothDevice();
	}

	public interface ConnectableDeviceCallback{
		public void devices(ArrayList<BluetoothDevice> deviceList);
		public void singleDevice(BluetoothDevice device);
	}

	private void SearchBluetoothDevice(){
		BluetoothBroadcastReceiver receiver = new BluetoothBroadcastReceiver();
		receiver.setOnReceiveCallback(new BluetoothBroadcastReceiver.ReceiveCallback() {
			@Override
			public void onDiscoveryStart() {
				
			}
			
			@Override
			public void onDiscoverFinished(ArrayList<BluetoothDevice> foundDevices) {
				mCallback.devices(foundDevices);
			}
			
			@Override
			public void onDeviceFound(BluetoothDevice device) {
				mCallback.singleDevice(device);
			}
			
			@Override
			public void onDeviceChanged(BluetoothDevice device) {
				mCallback.singleDevice(device);
			}
		});
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		mContext.registerReceiver(receiver, filter);
		if(mBluetoothAdapter.isDiscovering()){
			//検索中の場合は検出をキャンセルする
			mBluetoothAdapter.cancelDiscovery();
		}
		//デバイスを検索する
		//一定時間の間検出を行う
		mBluetoothAdapter.startDiscovery();
		Log.d(Config.TAG, "hogehoge");
	}
}
