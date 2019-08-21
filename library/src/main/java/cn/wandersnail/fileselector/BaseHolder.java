package cn.wandersnail.fileselector;

import android.view.View;

/**
 * date: 2019/8/8 13:40
 * author: zengfansheng
 */
abstract class BaseHolder<T> {
    final View convertView;
    
    BaseHolder() {
        convertView = createConvertView();
        convertView.setTag(this);
    }
    

    /**
     * 设置数据
     */
    abstract void setData(T data, int position);

    /**
     * 创建界面
     */
    abstract View createConvertView();
}
