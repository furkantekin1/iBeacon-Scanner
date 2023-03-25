package com.furkotek.ibeacon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomListViewAdapter extends ArrayAdapter<BeaconItem> {

    private final LayoutInflater inflater;
    private final ArrayList<BeaconItem> BeaconItems;
    private final Context context;
    private ViewHolder holder;

    public CustomListViewAdapter(Context context, ArrayList<BeaconItem> BeaconItems) {
        super(context,0, BeaconItems);
        this.BeaconItems = BeaconItems;
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return BeaconItems.size();
    }

    @Override
    public BeaconItem getItem(int position) {
        return BeaconItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return BeaconItems.get(position).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_lv, null);
            holder = new ViewHolder();
            holder.txt_uuid = (TextView) convertView.findViewById(R.id.txt_uuid);
            holder.txt_major = (TextView) convertView.findViewById(R.id.txt_major);
            holder.txt_minor = (TextView) convertView.findViewById(R.id.txt_minor);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder)convertView.getTag();
        }

        BeaconItem BeaconItem = BeaconItems.get(position);
        if(BeaconItem != null){
            holder.txt_minor.setText(context.getString(R.string.minor) + ": "+BeaconItem.getMinor());
            holder.txt_uuid.setText(BeaconItem.getUuid());
            holder.txt_major.setText(context.getString(R.string.major) + ": "+BeaconItem.getMajor());

        }
        return convertView;
    }

    private static class ViewHolder {
        TextView txt_uuid;
        TextView txt_major;
        TextView txt_minor;

    }
}