package com.yan.bluetoothble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by YanZi on 2017/3/31.
 * describe：
 * modify:
 * modify date:
 */

public class ConnectActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "ConnectActivity";

    Button btn_address_conn, btn_pair_conn, btn_send;

    EditText et_input;
    TextView tv_desc;
//    RecyclerView rc_connect_list;

    BluetoothDevice bluetoothDevice;

    public static final UUID TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");

    //服务
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    //写
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    //读
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    //要显示的数据
    StringBuffer textShow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
        btn_pair_conn = (Button) findViewById(R.id.btn_pair_conn);
        btn_address_conn = (Button) findViewById(R.id.btn_address_conn);
        btn_send = (Button) findViewById(R.id.btn_send);
        et_input = (EditText) findViewById(R.id.et_input);
        tv_desc = (TextView) findViewById(R.id.tv_desc);
//        rc_connect_list = (RecyclerView) findViewById(R.id.rc_connect_list);

        bluetoothDevice = (BluetoothDevice) getIntent().getExtras().get("devices");
        textShow = new StringBuffer(
                "名称:" + bluetoothDevice.getName() + "\n" +
                        "mac地址:" + bluetoothDevice.getAddress() + "\n" +
                        "uuid:" + bluetoothDevice.getUuids() + "\n" +
                        "");
        tv_desc.setText(textShow.toString());
        btn_pair_conn.setOnClickListener(this);
        btn_address_conn.setOnClickListener(this);
        btn_send.setOnClickListener(this);
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.btn_pair_conn:
                //TODO
                showToast("稍后开发");
                break;
            case R.id.btn_address_conn:

                new ConnectThread(bluetoothDevice).start();


                break;
            case R.id.btn_send:
                if (mBluetoothGatt == null) {
                    //请先建立链接后再操作
                    return;
                }
                String message = et_input.getText().toString();
                if (message == null || "".equals(message)) {
                    showToast("请输出文字后发送");
                    return;
                }
                byte[] value;
                try {
                    //send data to service
                    value = message.getBytes("UTF-8");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
//                        writeRXCharacteristic(value);
//                        16进制测试
//                        String str = bytesToHexString(value);
                        //
                        //     writeRXCharacteristic(str.getBytes());
                    }
                    //Update the log with time stamp
                    String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                break;
            default:
                break;
        }
    }


    private class ConnectThread extends Thread {

        private BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;

        }

        @Override
        public void run() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mmDevice.connectGatt(ConnectActivity.this, true, mGattCallback);

            }

        }
    }

    BluetoothGatt mBluetoothGatt;


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {//已结连接
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showToast("链接成功，正在建立通讯");
                    }
                });
//                    连接成功时获取特征值
                mBluetoothGatt = gatt;
                //  开启服务
                if (!gatt.discoverServices()) {
                    new ConnectThread(bluetoothDevice).start();
                }
                Log.e(TAG, "onConnectionStateChange connect success");

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {//断开连接
                mBluetoothGatt = null;
                Log.e(TAG, "onConnectionStateChange connect fail");

//                new ConnectThread(bluetoothDevice).start();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {//链接成功
                Log.e(TAG, "onServicesDiscovered success");
                //开启
                enableTXNotification();
            } else {
                Log.e(TAG, "onServicesDiscovered fail");
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {     //终端来数据成功了
//                Log.e(TAG, "onCharacteristicRead " + new String(characteristic.getValue()));
//                //
//                textShow.append(new String(characteristic.getValue()));
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        tv_desc.setText(textShow.toString());
//                    }
//                });
//
////                    broadcastUpdate(characteristic);
//            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                textShow.append("\n");
                textShow.append("写入的数据：");
                textShow.append(new String(characteristic.getValue()));
                //写入成功了
                Log.e(TAG, "mBluetoothGatt = " + mBluetoothGatt + "\n" + "BluetoothGattCharacteristic :" + new String(characteristic.getValue()));
                //写入成功了
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        et_input.setText("");
                        tv_desc.setText(textShow.toString());
                    }
                });

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                showToast("写入失败，请重试");
            }

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
//            Log.e(TAG, "onCharacteristicChanged" + characteristic);


            Log.e(TAG, "onCharacteristicChanged " + new String(characteristic.getValue()));
            //
            textShow.append("\n");
            textShow.append("接受到的数据：");
            textShow.append(new String(characteristic.getValue()));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv_desc.setText(textShow.toString());
                }
            });

//                    broadcastUpdate(characteristic);


        }
    };


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void enableTXNotification() {
        /*
        if (mBluetoothGatt == null) {
    		showMessage("mBluetoothGatt null" + mBluetoothGatt);
    		broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
    		return;
    	}
    		*/
        BluetoothGattService rxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
        if (rxService == null) {
            Log.e(TAG, "enableTXNotification-->BluetoothGattService-->rxService == null");
            return;
        }

        BluetoothGattCharacteristic txChar = rxService.getCharacteristic(TX_CHAR_UUID);
        if (txChar == null) {
            Log.e(TAG, "enableTXNotification  BluetoothGattCharacteristic txChar == null");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(txChar, true);

        BluetoothGattDescriptor descriptor = txChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showToast("建立通讯成功");
            }
        });


    }


    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void writeRXCharacteristic(byte[] value) {
        BluetoothGattService rxService = mBluetoothGatt.getService(RX_SERVICE_UUID);

        if (rxService == null) {
            Log.e(TAG, "writeRXCharacteristic-->BluetoothGattService-->rxService == null");
            return;
        }
        BluetoothGattCharacteristic rxChar = rxService.getCharacteristic(RX_CHAR_UUID);
        if (rxChar == null) {
            Log.e(TAG, "writeRXCharacteristic-->BluetoothGattService-->rxChar == null");
            return;
        }
        rxChar.setValue(value);
        boolean status = mBluetoothGatt.writeCharacteristic(rxChar);

        Log.d(TAG, "write TXchar - status=" + status);
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


    public static String bytesToHexString(byte[] bytes) {
        String result = "";
        for (int i = 0; i < bytes.length; i++) {
            String hexString = Integer.toHexString(bytes[i] & 0xFF);
            if (hexString.length() == 1) {
                hexString = '0' + hexString;
            }
            result += hexString.toUpperCase();
        }
        return result;
    }


}
