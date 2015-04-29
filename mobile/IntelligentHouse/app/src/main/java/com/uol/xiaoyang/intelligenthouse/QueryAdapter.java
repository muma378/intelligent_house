package com.uol.xiaoyang.intelligenthouse;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class QueryAdapter extends BaseAdapter {

    private Activity activity;
    private ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
    private static int counter = 0;

    public QueryAdapter(NewSong a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    static class ViewHolder{
        public HashMap<String, String> data;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.result_list, null);

        TextView name = (TextView)vi.findViewById(R.id.name); // title
        TextView description = (TextView)vi.findViewById(R.id.description); // artist - album
        TextView duration = (TextView)vi.findViewById(R.id.duration); // duration
        ImageView add = (ImageView)vi.findViewById(R.id.add);   //add symbol

        HashMap<String, String> song = new HashMap<String, String>();
        song = data.get(position);

        // Setting all values in listview
        name.setText(song.get(NewSong.KEY_NAME));
        description.setText(song.get(NewSong.KEY_ARTIST)+" - "+song.get(NewSong.KEY_ALBUM));

        Date date = new Date(Integer.parseInt(song.get(NewSong.KEY_DURATION)));
        DateFormat formatter = new SimpleDateFormat("mm:ss");
        String dateFormatted = formatter.format(date);
        duration.setText(dateFormatted);

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.data = (HashMap<String, String>)song.clone();
        add.setTag(viewHolder);

        return vi;
    }
}