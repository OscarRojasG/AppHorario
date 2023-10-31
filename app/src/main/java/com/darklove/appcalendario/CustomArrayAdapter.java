package com.darklove.appcalendario;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class CustomArrayAdapter extends ArrayAdapter {
    private Context context;
    private final static int resource = R.layout.spinner_item;
    private final static int textViewResourceId = R.id.spinnerItemText;

    public CustomArrayAdapter(Context context, List objects) {
        super(context, resource, textViewResourceId, objects);
        this.context = context;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resource, parent, false);
        }

        TextView textView = convertView.findViewById(textViewResourceId);
        String itemText = getItem(position).toString();
        textView.setText(itemText);

        return textView;
    }

}
