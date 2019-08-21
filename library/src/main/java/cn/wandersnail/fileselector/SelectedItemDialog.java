package cn.wandersnail.fileselector;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import cn.wandersnail.commons.util.UiUtils;

/**
 * date: 2019/8/8 16:30
 * author: zengfansheng
 */
class SelectedItemDialog extends Dialog {
    private SelectFileActivity activity;
    private TextView tvTitle;
    private FileListAdapter adapter;
    private List<Item> itemList = new ArrayList<>();

    SelectedItemDialog(@NonNull SelectFileActivity activity) {
        super(activity);
        this.activity = activity;
        Window window = getWindow();
        if (window != null) {
            window.requestFeature(Window.FEATURE_NO_TITLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setPadding(0, 0, 0, 0);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setWindowAnimations(R.style.FsDialogAnimFromBottom);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(lp);
        }
        setContentView(inflateView(activity));
    }

    private View inflateView(Context context) {
        View view = View.inflate(context, R.layout.fs_dialog_selected_item, null);
        View layoutTitle = view.findViewById(R.id.fsLayoutTitle);
        layoutTitle.setBackgroundColor(activity.themeColors[0]);
        tvTitle = view.findViewById(R.id.fsTvTitle);
        tvTitle.setText(String.format(activity.textHolder.getText(TextHolder.SELECTED_ITEM_PATTERN), itemList.size()));
        View statusBar = view.findViewById(R.id.fsStatusBar);
        statusBar.setBackgroundColor(activity.themeColors[0]);
        ViewGroup.LayoutParams params = statusBar.getLayoutParams();
        params.height = UiUtils.getStatusBarHeight();
        statusBar.setLayoutParams(params);
        TextView tvClose = view.findViewById(R.id.fsTvClose);
        tvClose.setText(activity.textHolder.getText(TextHolder.CLOSE));
        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        TextView tvClear = view.findViewById(R.id.fsTvClear);
        tvClear.setText(activity.textHolder.getText(TextHolder.CLEAR));
        tvClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.clearSelectedFileList();
            }
        });
        adapter = new FileListAdapter(activity, itemList);
        ListView lv = view.findViewById(R.id.fsLv);
        lv.setAdapter(adapter);
        return view;
    }

    void updateList(List<Item> selectItemList) {
        itemList.clear();
        itemList.addAll(selectItemList);
        tvTitle.setText(String.format(activity.textHolder.getText(TextHolder.SELECTED_ITEM_PATTERN), itemList.size()));
        adapter.notifyDataSetChanged();
    }

    private class FileListAdapter extends BaseListAdapter<Item> {
        FileListAdapter(Context context, List<Item> data) {
            super(context, data);
        }

        @Override
        BaseHolder<Item> getHolder(int position) {
            return new ViewHolder() {
                @Override
                void setData(Item data, int position) {
                    chkView.setTag(position);
                    tvName.setText(data.file.getName());
                    String path = data.file.getAbsolutePath();
                    if (data.file.isDirectory()) {
                        tvDesc.setVisibility(View.GONE);
                        Glide.with(context).load(R.drawable.fs_folder).into(iv);
                    } else {
                        tvDesc.setText(data.file.getParentFile().getAbsolutePath());
                        if (Utils.isApk(path)) {
                            Glide.with(context).load(Utils.getApkThumbnail(context, path)).into(iv);
                        } else if (Utils.isImage(path) || Utils.isVideo(path)) {
                            Glide.with(context).setDefaultRequestOptions(new RequestOptions()
                                    .error(R.drawable.fs_file)).load(path).into(iv);
                        } else if (Utils.isAudio(path)) {
                            Glide.with(context).load(R.drawable.fs_audio).into(iv);
                        } else if (Utils.isText(path)) {
                            Glide.with(context).load(R.drawable.fs_text).into(iv);
                        } else if (Utils.isPdf(path)) {
                            Glide.with(context).load(R.drawable.fs_pdf).into(iv);
                        } else if (Utils.isExcel(path)) {
                            Glide.with(context).load(R.drawable.fs_excel).into(iv);
                        } else if (Utils.isWord(path)) {
                            Glide.with(context).load(R.drawable.fs_word).into(iv);
                        } else if (Utils.isPPT(path)) {
                            Glide.with(context).load(R.drawable.fs_ppt).into(iv);
                        } else if (Utils.isZip(path)) {
                            Glide.with(context).load(R.drawable.fs_zip).into(iv);
                        } else if (Utils.isFlash(path)) {
                            Glide.with(context).load(R.drawable.fs_flash).into(iv);
                        } else if (Utils.isPs(path)) {
                            Glide.with(context).load(R.drawable.fs_ps).into(iv);
                        } else if (Utils.isHtml(path)) {
                            Glide.with(context).load(R.drawable.fs_html).into(iv);
                        } else if (Utils.isDeveloper(path)) {
                            Glide.with(context).load(R.drawable.fs_developer).into(iv);
                        } else {
                            Glide.with(context).load(R.drawable.fs_file).into(iv);
                        }
                    }
                    ivSelect.setSelected(data.checked);
                    if (data.checked) {
                        ivSelect.setColorFilter(activity.themeColors[0]);
                    } else {
                        ivSelect.setColorFilter(Color.LTGRAY);
                    }
                }

                @Override
                View createConvertView() {
                    View view = View.inflate(context, R.layout.fs_file_item_view, null);
                    tvName = view.findViewById(R.id.fsTvName);
                    tvDesc = view.findViewById(R.id.fsTvDesc);
                    iv = view.findViewById(R.id.iv);
                    ivSelect = view.findViewById(R.id.fsIvSelect);
                    ivSelect.setVisibility(View.INVISIBLE);
                    chkView = view.findViewById(R.id.fsChkView);
                    chkView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int position = (int) chkView.getTag();
                            Item item = getItem(position);
                            item.checked = false;
                            activity.updateSelectedFileList(item, true);
                        }
                    });
                    return view;
                }
            };
        }
    }
}
