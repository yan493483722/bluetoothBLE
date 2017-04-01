package com.yan.bluetoothble.adapter;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yan.bluetoothble.ConnectActivity;
import com.yan.bluetoothble.R;

import java.util.List;

/**
 * Created by YanZi on 2017/3/20.
 * describe：
 * modify:
 * modify date:
 */
public class DevicesAdp extends RecyclerView.Adapter<DevicesAdp.MessageHolder> {


    private Context context;

    private List<BluetoothDevice> listMessage;

    int current;

    public DevicesAdp(Context context, List<BluetoothDevice> listMessage) {
        this.context = context;
        this.listMessage = listMessage;
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);//状态改变
    }

    public void notifyDataSetChanged(List<BluetoothDevice> listMessage) {
        this.listMessage = listMessage;
        notifyDataSetChanged();
    }

    @Override
    public MessageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_devices, null);
        return new MessageHolder(view);
    }

    @Override
    public void onBindViewHolder(MessageHolder holder, final int position) {
        if (!isNull(listMessage.get(position).getName())) {
            holder.tvDevicesName.setText("设备名称:" + listMessage.get(position).getName());
        } else {
            holder.tvDevicesName.setText("设备名称:" + "未知设备");
        }

        //返回设备的蓝牙地址, 这个蓝牙地址是17位的, 并且字母都是大写字母;
        if (!!isNull(listMessage.get(position).getAddress())) {
            holder.tvDevicesAddress.setText("设备地址:" + listMessage.get(position).getAddress());
        } else {
            holder.tvDevicesAddress.setText("设备地址:" + "没有地址");
        }

//    getBondState     BOND_BONDED, BOND_BONDING, BOND_NONE

        if (listMessage.get(position).getBondState() == BluetoothDevice.BOND_BONDED) {
            holder.tvDevicesBondState.setText("设备绑定状态:" + listMessage.get(position).getBondState() + " \n绑定状态为:已绑定");
        } else if (listMessage.get(position).getBondState() == BluetoothDevice.BOND_BONDING) {
            holder.tvDevicesBondState.setText("设备绑定状态:" + listMessage.get(position).getBondState() + " \n绑定状态为:绑定中");
        } else {
            holder.tvDevicesBondState.setText("设备绑定状态:" + listMessage.get(position).getBondState() + " \n绑定状态为:未绑定");
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            holder.tvDevicesType.setText("设备类型:" + listMessage.get(position).getType());
        } else {
            holder.tvDevicesType.setText("不支持");
        }


//        如果两个安卓手机之间进行连接需要生成专用的UUID, 如果是对蓝牙串口进行连接, 就使用总所周知的SPP UUID 00001101-0000-1000-8000-00805F9B34FB

//        参数 : 用来识别远程蓝牙设备的UUID, 该UUID用来查询RFCOMM通道的服务记录;
        holder.tvDevicesUuid.setText("设备UUID:" + listMessage.get(position).getUuids());

        //作用 : 获取远程设备的蓝牙类, 需要BLUETOOTH权限, 如果出现错误, 返回null;
        holder.tvDevicesClass.setText(listMessage.get(position).getBluetoothClass().describeContents() + "===" + listMessage.get(position).getBluetoothClass().toString());

        holder.devicesRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context).setTitle("确定连接此蓝牙？").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //连接的操作
                        //TODO
                        Intent intent =new Intent(context,ConnectActivity.class);
                        intent.putExtra("devices",listMessage.get(position));
                        context.startActivity(intent);

                    }
                }).setNegativeButton("取消", null).show();

            }
        });
    }

    @Override
    public int getItemCount() {
        return listMessage == null ? 0 : listMessage.size();
    }

    class MessageHolder extends RecyclerView.ViewHolder {

        LinearLayout devicesRoot;
        TextView tvDevicesName;
        TextView tvDevicesAddress;
        TextView tvDevicesBondState;
        TextView tvDevicesType;
        TextView tvDevicesUuid;
        TextView tvDevicesClass;

        public MessageHolder(View itemView) {
            super(itemView);
            devicesRoot = (LinearLayout) itemView.findViewById(R.id.devices_root);
            tvDevicesName = (TextView) itemView.findViewById(R.id.tv_devices_name);
            tvDevicesAddress = (TextView) itemView.findViewById(R.id.tv_devices_address);
            tvDevicesBondState = (TextView) itemView.findViewById(R.id.tv_devices_bond_state);
            tvDevicesType = (TextView) itemView.findViewById(R.id.tv_devices_type);
            tvDevicesUuid = (TextView) itemView.findViewById(R.id.tv_devices_uuid);
            tvDevicesClass = (TextView) itemView.findViewById(R.id.tv_devices_class);
        }
    }


    boolean isNull(String str) {
        if (str != null && !"".equals(str)) {
            return false;
        }
        return true;
    }

}

