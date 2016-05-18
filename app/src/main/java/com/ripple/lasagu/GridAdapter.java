package com.ripple.lasagu;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by royce on 15-05-2016.
 */
public class GridAdapter extends BaseAdapter {

    private Context context;
    private List<Integer> items = new ArrayList<>(9);
    LayoutInflater mInflater;
    int height;
    int maxIndex = -1;
    Random random;
    ResultPass resultPass;

    public GridAdapter(Context context, int height, ResultPass resultPass) {
        this.context = context;
        this.resultPass = resultPass;
        mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        this.height = height;
        random = new Random();
        fillItems();

    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Integer getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;
        if (convertView == null) {

            convertView = mInflater.inflate(R.layout.row_number, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.number = ((TextView) convertView.findViewById(R.id.number));
            viewHolder.layout = ((RelativeLayout) convertView.findViewById(R.id.number_layout));
            viewHolder.layout.setMinimumHeight(height / 4);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Integer m = items.get(position);

        try {
            viewHolder.number.setText(String.valueOf(m));
            viewHolder.number.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkMax(position);
                }
            });
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        return convertView;
    }

    private void checkMax(int position) {
        if (position == maxIndex) {
            fillItems();
            resultPass.passResult(true);
            notifyDataSetChanged();
        } else {
            resultPass.passResult(false);
        }
    }

    private void fillItems() {
        if (items != null && items.size() != 0)
            items.clear();
        for (int i = 0; ; i++) {
            int randomNumber = random.nextInt(99 - 10) + 10;
            if (!items.contains(randomNumber))
                items.add(randomNumber);
            if (items.size() == 9)
                break;
        }
        maxIndex = maxIndex(items);
    }

    public int maxIndex(List<Integer> list) {
        Integer i = 0, maxIndex = -1, max = null;
        for (Integer x : list) {
            if ((x != null) && ((max == null) || (x > max))) {
                max = x;
                maxIndex = i;
            }
            i++;
        }
        return maxIndex;
    }

    static class ViewHolder {
        TextView number;
        RelativeLayout layout;
    }

}