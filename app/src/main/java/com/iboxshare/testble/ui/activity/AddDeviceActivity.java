package com.iboxshare.testble.ui.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;

import com.iboxshare.testble.R;
import com.iboxshare.testble.adapter.DevicesAdapter;
import com.iboxshare.testble.model.DeviceInfo;
import com.iboxshare.testble.myInterface.RecyclerViewOnItemClickListener;
import com.iboxshare.testble.util.BLEUtils;
import com.iboxshare.testble.util.BluetoothLeClass;
import com.iboxshare.testble.util.Constant;
import com.jude.easyrecyclerview.EasyRecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddDeviceActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "AddDeviceActivity";

    String[] PERMISSIONS = {"android.permission.ACCESS_FINE_LOCATION", "android.permission.ACCESS_COARSE_LOCATION"};
    private Context context = AddDeviceActivity.this;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    private static BluetoothLeClass BLE;
    private BluetoothLeScanner bluetoothLeScanner;
    private EasyRecyclerView easyRecyclerView;
    private List<DeviceInfo> deviceInfoList = new ArrayList<>();
    private HashMap<String, DeviceInfo> devicesHashMap = new HashMap<>();
    private DevicesAdapter deviceAdapter = new DevicesAdapter();
    private LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                //setAdapter
                case 1:
                    easyRecyclerView.setAdapter(deviceAdapter);
                    break;
                //notifyDataChanged
                case 2:
                    deviceAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };


    //回调
    BluetoothAdapter.LeScanCallback leScanCallback;
    BluetoothLeClass.OnDataAvailableListener dataAvailableListener;
    BluetoothLeClass.OnServiceDiscoverListener serviceDiscoverListener;


    //Flags
    private static int DEVICES_NUM = 0;


    /**
     * Android 6.0以上设备申请权限返回结果
     *
     * @param requestCode  请求码
     * @param permissions  请求的权限
     * @param grantResults 授权结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * OnCreate
     *
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        bindView();
        init();
        initCallback();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");
        Log.e("本机mac地址", bluetoothAdapter.getAddress());
        scanDevices(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        scanDevices(false);
        BLE.disconnect();

        //清零后在onResume时才会刷新UI
        DEVICES_NUM = 0;
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestory");
        super.onDestroy();
    }


    /**
     * 绑定控件
     */
    void bindView() {
        easyRecyclerView = (EasyRecyclerView) findViewById(R.id.activity_main_easyRecyclerView);
    }

    /**
     * 初始化数据
     */
    void init() {
        //为EasyRecyclerView设置LayoutManager
        easyRecyclerView.setLayoutManager(linearLayoutManager);
        //为DevicesAdapter设置数据
        deviceAdapter.setData(context, deviceInfoList);

        //为每个Item设置监听事件
        deviceAdapter.setOnItemClickListener(new RecyclerViewOnItemClickListener() {
            @Override
            public void onItemClick(View view) {
                //获取到Item对应的位置
                int position = (int) view.findViewById(R.id.item_device_details_mac).getTag();
                Log.e(TAG, String.valueOf(position));
                Log.e(TAG, deviceInfoList.get(position).getMac());
                boolean connectResult = BLE.connect(deviceInfoList.get(position).getMac());
                if (connectResult) {
                    Log.e(TAG, "蓝牙连接成功");

                    //连接蓝牙后停止扫描
                    scanDevices(false);

                    //为BLE设置OnServiceDiscoverListener
                    BLE.setOnServiceDiscoverListener(serviceDiscoverListener);

                    //为BLE设置OnDataAvailableListener
                    BLE.setOnDataAvailableListener(dataAvailableListener);


                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //aa151 0d
                    BLEUtils.writeChar(BLEUtils.hexStringToBytes(Constant.HEAD_CHAR + "01" + "05" + BLEUtils.toHexString("123456789abcdefg") + Constant.END_CHAR),BLE);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    BLEUtils.writeChar(BLEUtils.hexStringToBytes(Constant.HEAD_CHAR + Constant.SEVER_BLUETOOTH + "04" + "09" + BLEUtils.toHexString("root") + Constant.END_CHAR),BLE);

                }

            }
        });

        //针对Android M及以上设备申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            this.requestPermissions(PERMISSIONS, 1);
        }
        //启动蓝牙
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothAdapter.enable();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
//        }
        String bluetoothAddress = bluetoothAdapter.getAddress();
        Log.e(TAG, bluetoothAddress);

        BLE = new BluetoothLeClass(this);
        if (!BLE.initialize()) {
            Log.e(TAG, "初始化失败");
            finish();
        }

    }

    /**
     * 扫描BLE设备
     */
    void scanDevices(boolean enable) {
        if (enable) {
            bluetoothAdapter.startLeScan(leScanCallback);
        } else {
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    /**
     * 初始化回调
     */
    void initCallback() {

/*        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            scanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                }
            };
        }

*/

        //扫描回调
        leScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                Log.e(TAG, device.getName() + "   " + device.getAddress());
                String name = device.getName();
                String address = device.getAddress();
                int signal = rssi;


                DeviceInfo temp = new DeviceInfo(name, address, signal);
                devicesHashMap.put(address, temp);

                //通知相应mac地址的设备的信号强度变化
                signalStrengthChanged(signal, address);
                handler.sendEmptyMessage(2);

                Log.e(TAG, String.valueOf(devicesHashMap.get(address).getSignal()));
                Log.e(TAG, String.valueOf(devicesHashMap.size()));

                //当被发现设备数目发生变化则更新DeviceInfoList
                if (devicesHashMap.size() != DEVICES_NUM) {
                    deviceInfoList.clear();
                    for (Map.Entry<String, DeviceInfo> entry : devicesHashMap.entrySet()) {
                        deviceInfoList.add(entry.getValue());

                        //通知easyRecyclerView数据更改
                        if (easyRecyclerView.getAdapter() == null) {
                            handler.sendEmptyMessage(1);
                        } else {
                            handler.sendEmptyMessage(2);
                        }
                    }
                    DEVICES_NUM = devicesHashMap.size();
                    Log.e(TAG, "LIST SIZE = " + deviceInfoList.size());
                }

            }
        };


        //发现BLE设备Services时的回调
        serviceDiscoverListener = new BluetoothLeClass.OnServiceDiscoverListener() {
            @Override
            public void onServiceDiscover(BluetoothGatt gatt) {
                BLEUtils.displayGattServices(BLE.getSupportedGattServices(),BLE);
            }

        };

        //数据交互回调
        dataAvailableListener = new BluetoothLeClass.OnDataAvailableListener() {
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.e("读取终端数据回调: ", gatt.getDevice().getAddress()
                        + "===>"
                        + characteristic.getUuid().toString()
                        + BLEUtils.bytesToHexString(characteristic.getValue()));
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                String str = BLEUtils.bytesToHexString(characteristic.getValue());
                Log.e("终端写入数据回调", str);

            }
        };

    }

    /**
     * 点击事件
     *
     * @param v 传入View
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }
    }

    /**
     * 通知信号变化
     *
     * @param signalStrength 信号强度
     * @param mac            mac地址
     */
    void signalStrengthChanged(int signalStrength, String mac) {
        if (devicesHashMap.containsKey(mac)) {
            for (DeviceInfo device : deviceInfoList) {
                if (device.getMac().equals(mac)) {
                    device.setSignal(signalStrength);
                }
            }
        }
    }






}
