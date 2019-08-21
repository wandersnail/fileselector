package cn.wandersnail.fileselector;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * date: 2019/8/8 13:43
 * author: zengfansheng
 */
abstract class BaseListAdapter<T> extends BaseAdapter {
    protected Context context;
    protected List<T> data; 

    BaseListAdapter(Context context) {
        this(context, new ArrayList<T>());
    }

    BaseListAdapter(Context context, List<T> data) {
        this.context = context;
        this.data = data;
    }
    
    void refresh(List<T> data) {
        this.data.clear();
        this.data.addAll(data);
        notifyDataSetChanged();
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

    
    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BaseHolder<T> holder;
        if (convertView == null) {
            holder = getHolder(position);
        } else {
            holder = (BaseHolder<T>) convertView.getTag();
        }
        holder.setData(data.get(position), position);
        return holder.convertView;
    }

    abstract BaseHolder<T> getHolder(int position);
}
