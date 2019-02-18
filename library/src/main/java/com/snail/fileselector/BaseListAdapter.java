package com.snail.fileselector;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

/**
 * date: 2019/1/13 14:01
 * author: zengfansheng
 */
abstract class BaseListAdapter<T> extends BaseAdapter {
    private Context context;
    private List<T> data;

    BaseListAdapter(@NonNull Context context) {
        data = new ArrayList<>();
        this.context = context;
    }

    BaseListAdapter(@NonNull Context context, @NonNull List<T> data) {
        this.context = context;
        this.data = data;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(@NonNull List<T> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public Context getContext() {
        return context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public T getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BaseHolder<T> holder;
        if (convertView == null) {
            holder = getHolder(position);
        } else {
            holder = (BaseHolder<T>) convertView.getTag();
        }
        holder.setData(data.get(position), position);
        return holder.getConvertView();
    }

    protected abstract BaseHolder<T> getHolder(int position);
}
