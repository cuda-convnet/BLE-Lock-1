package com.iboxshare.testble.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.iboxshare.testble.R;
import com.iboxshare.testble.model.DeviceInfo;
import com.iboxshare.testble.myInterface.RecyclerViewOnItemClickListener;

import java.util.HashMap;
import java.util.List;

/**
 * Created by KN on 16/8/8.
 */
public class DevicesAdapter extends RecyclerView.Adapter<DevicesViewHolder> implements View.OnClickListener {
    private List<DeviceInfo> list;
    private HashMap<String, DeviceInfo> deviceHashMap;
    private Context context;
    private RecyclerViewOnItemClickListener clickListener;

    public void setData(Context context, List<DeviceInfo> data) {
        this.list = data;
        this.context = context;
    }

    @Override
    public DevicesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_device_details, parent, false);
        view.setOnClickListener(this);
        return new DevicesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DevicesViewHolder holder, int position) {
        DeviceInfo deviceInfo = list.get(position);
        int signalStrength = deviceInfo.getSignal();

        holder.signal.setVisibility(View.VISIBLE);
        //更新信号强度
        if (signalStrength == -999) {
            holder.signal.setVisibility(View.INVISIBLE);
        } else if (signalStrength > -70 && signalStrength <= 0) {
            Glide.with(context)
                    .load(R.drawable.ic_signal_cellular_4_bar_blue_300_48dp)
                    .asBitmap()
                    .into(holder.signal);
            Log.e("SignalLevel===", "4");
        } else if (signalStrength > -90 && signalStrength <= -70) {
            Glide.with(context)
                    .load(R.drawable.ic_signal_cellular_3_bar_blue_300_48dp)
                    .asBitmap()
                    .into(holder.signal);
            Log.e("SignalLevel===", "3");
        } else if (signalStrength > -110 && signalStrength <= -90) {
            Glide.with(context)
                    .load(R.drawable.ic_signal_cellular_2_bar_blue_300_48dp)
                    .asBitmap()
                    .into(holder.signal);
            Log.e("SignalLevel===", "2");
        } else {
            Glide.with(context)
                    .load(R.drawable.ic_signal_cellular_1_bar_blue_300_48dp)
                    .asBitmap()
                    .into(holder.signal);
            Log.e("SignalLevel===", "1");
        }
        //设置Tag来记录位置
        holder.mac.setTag(position);


        Log.e("Signal", String.valueOf(signalStrength));
        holder.name.setText(deviceInfo.getName());
        holder.mac.setText(deviceInfo.getMac());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    @Override
    public void onClick(View v) {
        if (clickListener != null) clickListener.onItemClick(v);
    }

    public void setOnItemClickListener(RecyclerViewOnItemClickListener listener) {
        this.clickListener = listener;
    }
}


class DevicesViewHolder extends RecyclerView.ViewHolder {
    public ImageView ico, signal;
    public TextView name, mac;

    public DevicesViewHolder(View itemView) {
        super(itemView);
        ico = (ImageView) itemView.findViewById(R.id.item_device_details_ico);
        signal = (ImageView) itemView.findViewById(R.id.item_device_details_signal);
        name = (TextView) itemView.findViewById(R.id.item_device_details_name);
        mac = (TextView) itemView.findViewById(R.id.item_device_details_mac);
    }

}
