package com.snail.fileselector;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * date: 2019/1/13 14:02
 * author: zengfansheng
 */
abstract class BaseHolder<T> {
    private View convertView;

    BaseHolder() {
        convertView = createConvertView();
        convertView.setTag(this);
    }

    public View getConvertView() {
        return convertView;
    }

    /**
     * 设置数据
     */
    protected abstract void setData(@NonNull T data, int position);

    /**
     * 创建界面
     */
    protected abstract View createConvertView();
}
