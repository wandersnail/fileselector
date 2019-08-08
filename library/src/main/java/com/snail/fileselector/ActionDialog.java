package com.snail.fileselector;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * date: 2019/8/8 13:34
 * author: zengfansheng
 */
class ActionDialog extends Dialog {
    ActionDialog(@NonNull Activity context, ArrayList<String> items, final Callback callback) {
        super(context);
        Window window = getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.getDecorView().setPadding(0, 0, 0, 0);
            window.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.fs_action_dialog_bg));
            window.setWindowAnimations(R.style.FsDialogAnimFromBottom);
            window.setGravity(Gravity.BOTTOM);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = items.size() * Utils.dp2px(context, 50f) + items.size();
            window.setAttributes(lp);
        }
        setContentView(R.layout.fs_listview);
        ListView lv = findViewById(R.id.lv);
        lv.setAdapter(new ActionAdapter(context, items));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dismiss();
                callback.onSelect(position);
            }
        });
    }
    
    interface Callback {
        void onSelect(int position);
    }

    class ActionAdapter extends BaseListAdapter<String> {
        ActionAdapter(Context context, List<String> data) {
            super(context, data);
        }

        @Override
        BaseHolder<String> getHolder(int position) {
            return new BaseHolder<String>() {
                private TextView tv;
                
                @Override
                void setData(String data, int position) {
                    tv.setText(data);
                }

                @Override
                View createConvertView() {
                    View view = View.inflate(context, R.layout.fs_item_popup_menu, null);
                    tv = view.findViewById(R.id.fsTv);
                    return view;
                }
            };
        }
    }
}
