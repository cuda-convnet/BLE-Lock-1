package com.iboxshare.testble.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.iboxshare.testble.R;
import com.iboxshare.testble.model.DeviceInfo;

import java.util.HashMap;
import java.util.List;

/**
 * Created by KN on 16/8/8.
 */
public class DevicesAdapter extends RecyclerView.Adapter<DevicesViewHolder> {
    private List<DeviceInfo> list;
    private HashMap<String,DeviceInfo> deviceHashMap;
    public void setData(List<DeviceInfo> data){
        this.list = data;
    }

    @Override
    public DevicesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device_details,parent,false);
        return new DevicesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DevicesViewHolder holder, int position) {
        DeviceInfo deviceInfo = list.get(position);
        int signalStrength = deviceInfo.getSignal();
        {
            if (signalStrength > -50){

            }else if (signalStrength > -100){

            }
        }

        holder.name.setText(deviceInfo.getName());
        holder.mac.setText(deviceInfo.getMac());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}


class DevicesViewHolder extends RecyclerView.ViewHolder{
    public ImageView ico,signal;
    public TextView name,mac;
    public DevicesViewHolder(View itemView) {
        super(itemView);
        ico = (ImageView) itemView.findViewById(R.id.item_device_details_ico);
        signal = (ImageView) itemView.findViewById(R.id.item_device_details_signal);
        name = (TextView) itemView.findViewById(R.id.item_device_details_name);
        mac = (TextView) itemView.findViewById(R.id.item_device_details_mac);
    }

}
