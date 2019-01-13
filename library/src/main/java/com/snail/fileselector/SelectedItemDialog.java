package com.snail.fileselector;

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

class SelectedItemDialog extends Dialog implements View.OnClickListener {
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
            //沉浸状态栏
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setPadding(0, 0, 0, 0);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setWindowAnimations(R.style.FsDialogAnimFromBottom);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(lp);
        }
        setContentView(inflateView(context));
    }

    private View inflateView(Context context) {
        View view = View.inflate(context, R.layout.fs_dialog_selected_item, null);
        View layoutTitle = view.findViewById(R.id.fslayoutTitle);
        layoutTitle.setBackgroundColor(activity.getThemeColors()[0]);        
        tvTitle = view.findViewById(R.id.fstvTitle);
        View statusBar = view.findViewById(R.id.fsstatusBar);
        statusBar.setBackgroundColor(activity.getThemeColors()[0]);
        ViewGroup.LayoutParams params = statusBar.getLayoutParams();
        params.height = Utils.getStatusBarHeight(context);
        statusBar.setLayoutParams(params);
        view.findViewById(R.id.fstvClose).setOnClickListener(this);
        view.findViewById(R.id.fstvClear).setOnClickListener(this);
        adapter = new FileListAdapter();
        ListView lv = view.findViewById(R.id.fslv);
        lv.setAdapter(adapter);
        return view;
    }

    void updateList(List<Item> selectItemList) {
        itemList = selectItemList;
        tvTitle.setText(String.format(activity.getString(R.string.fs_selected_item_pattern), itemList.size()));
        adapter.notifyDataSetChanged();
    }
    
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.fstvClear) {
            activity.clearSelectedFileList();
        } else if (v.getId() == R.id.fstvClose) {
            dismiss();
        }
    }

    private class FileListAdapter extends BaseAdapter {

        FileListAdapter() {
            ImageLoader.getInstance().setDefautImageResoure(R.drawable.fs_file);
            ImageLoader.getInstance().setLoadErrorImageResoure(R.drawable.fs_file);
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
                convertView = View.inflate(getContext(), R.layout.fs_file_item_view, null);
                holder = new ViewHolder();
                convertView.setTag(holder);
                holder.tvName = convertView.findViewById(R.id.fstvName);
                holder.tvDesc = convertView.findViewById(R.id.fstvDesc);
                holder.iv = convertView.findViewById(R.id.iv);
                holder.ivSelect = convertView.findViewById(R.id.fsivSelect);
                holder.ivSelect.setVisibility(View.INVISIBLE);
                holder.chkView = convertView.findViewById(R.id.fschkView);
                holder.chkView.setTag(holder);
                holder.chkView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ViewHolder h = (ViewHolder) v.getTag();
                        Item item = getItem(h.position);
                        item.checked = false;
                        activity.updateSelectedFileList(item, true);
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
                ImageLoader.getInstance().loadImage(R.drawable.fs_folder, holder.iv);
            } else {
                holder.tvDesc.setText(item.file.getParentFile().getAbsolutePath());
                if (Utils.isApk(path) || Utils.isImage(path) || Utils.isVideo(path)) {
                    ImageLoader.getInstance().loadImage(path, holder.iv);
                } else if (Utils.isAudio(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_audio, holder.iv);
                } else if (Utils.isText(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_text, holder.iv);
                } else if (Utils.isPdf(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_pdf, holder.iv);
                } else if (Utils.isExcel(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_excel, holder.iv);
                } else if (Utils.isWord(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_word, holder.iv);
                } else if (Utils.isPPT(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_ppt, holder.iv);
                } else if (Utils.isZip(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_zip, holder.iv);
                } else if (Utils.isFlash(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_flash, holder.iv);
                } else if (Utils.isPs(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_ps, holder.iv);
                } else if (Utils.isHtml(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_html, holder.iv);
                } else if (Utils.isDeveloper(path)) {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_developer, holder.iv);
                } else {
                    ImageLoader.getInstance().loadImage(R.drawable.fs_file, holder.iv);
                }
            }
            holder.ivSelect.setSelected(item.checked);
            if (item.checked) {
                holder.ivSelect.setColorFilter(activity.getThemeColors()[0]);
            } else {
                holder.ivSelect.setColorFilter(Color.LTGRAY);
            }
            return convertView;
        }
    }
}
