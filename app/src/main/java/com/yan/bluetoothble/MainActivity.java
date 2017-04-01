package com.yan.bluetoothble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.yan.bluetoothble.adapter.DevicesAdp;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";

    Button btn_search, btn_close;

    // 蓝牙设备列表
    RecyclerView rc_devices_list;

    //蓝牙
    private BluetoothAdapter bluetoothAdapter;

    private final int REQUEST_BT_BLUETOOTH = 0x0001;

    private static final long SCAN_TIME = 20000;


    DevicesAdp devicesAdp;

    List<BluetoothDevice> listMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_search = (Button) findViewById(R.id.btn_search);
        btn_close = (Button) findViewById(R.id.btn_close);
        rc_devices_list = (RecyclerView) findViewById(R.id.rc_devices_list);
        btn_search.setOnClickListener(this);
        btn_close.setOnClickListener(this);
        devices = new HashSet<>();
        listMessage = new ArrayList<>();

        devicesAdp = new DevicesAdp(this, listMessage);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rc_devices_list.setLayoutManager(linearLayoutManager);
        rc_devices_list.setAdapter(devicesAdp);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_search:
                openBlueTooth();
                break;
            case R.id.btn_close:
                break;
            default:
                break;
        }
    }

    //根据版本开启蓝牙
    public void openBlueTooth() {
        //TODO 6.0+权限问题
//        requestPermissions();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            //4.3+不支持低功耗蓝牙设备的处理
            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                dealNormalBluetooth();
                return;
            }
            BluetoothManager bluetoothManager =
                    (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
            //正常的蓝牙处理
            if (!bluetoothAdapter.isEnabled()) {
                if (!bluetoothAdapter.isEnabled()) {
                    Intent enableBtBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtBluetooth, REQUEST_BT_BLUETOOTH);
                }
            } else {
                showToast("打开蓝牙成功");
                searchBluetooth(SCAN_TIME);
            }
        } else {
            dealNormalBluetooth();
        }
    }


    void dealNormalBluetooth() {
        showToast("不支持低功耗蓝牙");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            showToast("当前没有蓝牙硬件");
            finish();
        } else {
            //TODO　非低功耗蓝牙的处理
        }
    }

    BluetoothScanCallback callback;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_BT_BLUETOOTH && resultCode == RESULT_OK) {
            searchBluetooth(SCAN_TIME);
        } else {
            //Fail
            showToast("打开蓝牙失败,请重新打开蓝牙");
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    BluetoothLeScanner bluetoothLeScanner;

    private void searchBluetooth() {
        //打开成功
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0以上
            bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            callback = new BluetoothScanCallback();

            bluetoothLeScanner.startScan(callback);
        } else {
            //4.3-5.0
//
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothDevice.ACTION_UUID);
            filter.addAction(BluetoothDevice.EXTRA_UUID);
            filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//状态改变
            filter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
            bluetoothAdapter.startDiscovery();
            BluetoothReceiver receiver = new BluetoothReceiver();
            registerReceiver(receiver, filter);
        }
    }


    /**
     * 查找特定的
     *
     * @param macAddress
     */
    private void searchBluetooth(String macAddress) {
        //TODO
    }

    private void searchBluetooth(long times) {
        mHandler.sendEmptyMessage(1);
        mHandler.sendEmptyMessageDelayed(2, times + 2000);
    }


    private Set<BluetoothDevice> devices;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class BluetoothScanCallback extends ScanCallback {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.e(TAG, "callbackType:" + callbackType + " " + "result :" + result.getDevice().getName());
            devices.add(result.getDevice());

        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, errorCode + "");
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.e(TAG, " " + "result :" + results.size());
        }
    }
    private Handler mHandler = new Handler() {
        WeakReference<MainActivity> activityWeakReference=new WeakReference<>(MainActivity.this);

        @Override
        public void dispatchMessage(Message msg) {
            MainActivity activity=   activityWeakReference.get();
            if (activity==null){
                return;
            }
            switch (msg.what) {
                case 1:
                    setTitle("查找中...请稍后");
                    searchBluetooth();
                    break;
                case 2:
                    setTitle("查找结束");
                    if (bluetoothLeScanner != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            bluetoothLeScanner.stopScan(callback);
                            if (!devices.isEmpty()) {
                                Log.e(TAG, devices.size() + " 个设备");
                                Iterator iterator = devices.iterator();
                                while (iterator.hasNext()) {
                                    listMessage.add((BluetoothDevice) iterator.next());
                                }
                                if (!listMessage.isEmpty()) {
                                    devicesAdp.notifyDataSetChanged(listMessage);
                                }
                            }
                        }else{

                        }
                    }
                    break;
            }
        }
    };


    private class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    /**
     * 显示Toast
     *
     * @param text
     */
    public void showToast(String text) {
        if (isFinishing()) {
            return;
        }
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

}
