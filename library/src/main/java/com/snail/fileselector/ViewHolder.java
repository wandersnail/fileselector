package com.snail.fileselector;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * date: 2019/8/8 14:41
 * author: zengfansheng
 */
abstract class ViewHolder extends BaseHolder<Item> {
    TextView tvName;
    TextView tvDesc;
    ImageView iv;
    ImageView ivSelect;
    View chkView;
}
