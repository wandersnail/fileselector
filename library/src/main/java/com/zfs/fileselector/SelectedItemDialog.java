package com.zfs.fileselector;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zeng on 2017/3/2.
 */

public class SelectedItemDialog extends Dialog implements View.OnClickListener {
    private TextView tvTitle;
    private FileListAdapter adapter;
    private List<Item> itemList = new ArrayList<>();
    private SelectFileActivity activity;
    
    SelectedItemDialog(SelectFileActivity context) {
        super(context);
        activity = context;
        Window window = getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.getDecorView().setPadding(0, 0, 0, 0);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setWindowAnimations(R.style.DialogAnimFromBottom);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(lp);
        }
        setContentView(inflateView(context));
    }

    private View inflateView(Context context) {
        View view = View.inflate(context, R.layout.dialog_selected_item, null);
        tvTitle = view.findViewById(R.id.tvTitle);
        view.findViewById(R.id.tvClose).setOnClickListener(this);
        view.findViewById(R.id.tvClear).setOnClickListener(this);
        adapter = new FileListAdapter();
        ListView lv = view.findViewById(R.id.lv);
        lv.setAdapter(adapter);
        return view;
    }

    void updateList(List<Item> selectItemList) {
        itemList = selectItemList;
        tvTitle.setText(String.format(activity.getString(R.string.selected_item_pattern), itemList.size()));
        adapter.notifyDataSetChanged();
    }
    
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tvClear) {
            activity.clearSelectedFileList();
        } else if (v.getId() == R.id.tvClose) {
            dismiss();
        }
    }

    private class FileListAdapter extends BaseAdapter {

        FileListAdapter() {
            ImageLoader.getInstance().setDefautImageResoure(R.drawable.file);
            ImageLoader.getInstance().setLoadErrorImageResoure(R.drawable.file);
        }

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Item getItem(int position) {
            return itemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.item_view, null);
                holder = new ViewHolder();
                convertView.setTag(holder);
                holder.tvName = convertView.findViewById(R.id.tvName);
                holder.tvDesc = convertView.findViewById(R.id.tvDesc);
                holder.iv = convertView.findViewById(R.id.iv);
                holder.chkBox = convertView.findViewById(R.id.chkBox);
                holder.chkBox.setVisibility(View.INVISIBLE);
                holder.chkView = convertView.findViewById(R.id.chkView);
                holder.chkView.setTag(holder);
                holder.chkView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewHolder h = (ViewHolder) v.getTag();
                        Item item = getItem(h.position);
                        item.checked = false;
                        activity.updateSelectedFileList(item);
                    }
                });
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Item item = getItem(position);
            holder.position = position;
            holder.tvName.setText(item.file.getName());
            String path = item.file.getAbsolutePath();
            if (item.file.isDirectory()) {
                holder.tvDesc.setVisibility(View.GONE);
                holder.iv.setImageResource(R.drawable.folder);
            } else {
                holder.tvDesc.setText(item.file.getParentFile().getAbsolutePath());
                if (Utils.isApk(path) || Utils.isImage(path) || Utils.isVideo(path)) {
                    ImageLoader.getInstance().loadImage(path, holder.iv);
                } else if (Utils.isAudio(path)) {
                    holder.iv.setImageResource(R.drawable.audio);
                } else if (Utils.isText(path)) {
                    holder.iv.setImageResource(R.drawable.text);
                } else if (Utils.isPdf(path)) {
                    holder.iv.setImageResource(R.drawable.pdf);
                } else if (Utils.isExcel(path)) {
                    holder.iv.setImageResource(R.drawable.excel);
                } else if (Utils.isWord(path)) {
                    holder.iv.setImageResource(R.drawable.word);
                } else if (Utils.isPPT(path)) {
                    holder.iv.setImageResource(R.drawable.ppt);
                } else if (Utils.isZip(path)) {
                    holder.iv.setImageResource(R.drawable.zip);
                } else {
                    holder.iv.setImageResource(R.drawable.file);
                }
            }
            holder.chkBox.setChecked(item.checked);
            return convertView;
        }
    }
}
