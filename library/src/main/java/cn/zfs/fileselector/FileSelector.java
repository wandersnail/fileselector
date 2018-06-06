package cn.zfs.fileselector;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;

import java.io.File;
import java.io.FilenameFilter;

/**
 * 描述:
 * 时间: 2018/6/2 14:16
 * 作者: zengfansheng
 */
public class FileSelector {
    private static final int REQUEST_CODE = 21516;
    private OnFileSelectListener listener;
    private boolean isLandscape;
    private File root;
    private boolean isSelectFile;
    private boolean isMultiSelect;
    private FilenameFilter filenameFilter;
    
    public FileSelector setRoot(File root) {
        this.root = root;
        return this;
    }

    /**
     * 设置屏幕方向
     */
    public FileSelector setScreenOrientation(boolean isLandscape) {
        this.isLandscape = isLandscape;
        return this;
    }

    /**
     * 设置文件选择回调
     */
    public FileSelector setOnFileSelectListener(OnFileSelectListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 设置是选择文件不是文件夹
     */
    public FileSelector setSelectFile(boolean isSelectFile) {
        this.isSelectFile = isSelectFile;
        return this;
    }

    /**
     * 设置是否多选
     */
    public FileSelector setMultiSelect(boolean isMultiSelect) {
        this.isMultiSelect = isMultiSelect;
        return this;
    }

    /**
     * 设置文件名过滤器
     */
    public FileSelector setFilenameFilter(FilenameFilter filenameFilter) {
        this.filenameFilter = filenameFilter;
        return this;
    }

    /**
     * 开始选择
     */
    public void select(Activity activity) {
        activity.startActivityForResult(obtainIntent(activity, SelectFileActivity.class), REQUEST_CODE);
    }

    /**
     * 开始选择
     */
    public void select(Fragment fragment) {
        fragment.startActivityForResult(obtainIntent(fragment.getActivity(), SelectFileActivity.class), REQUEST_CODE);
    }

    private Intent obtainIntent(Context context, Class<?> cls) {
        SelectFileActivity.filenameFilter = filenameFilter;
        Intent intent = new Intent(context, cls);
        intent.putExtra(SelectFileActivity.EXTRA_IS_LANDSCAPE, isLandscape);
        intent.putExtra(SelectFileActivity.EXTRA_IS_SELECT_FILE, isSelectFile);
        intent.putExtra(SelectFileActivity.EXTRA_IS_MULTI_SELECT, isMultiSelect);
        if (root != null) {
            intent.putExtra(SelectFileActivity.EXTRA_ROOT, root);
        }
        return intent;
    }
    
    /**
     * 将Activty或Fragment的结果传过来处理
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (listener != null) {
                listener.onFileSelect(data.getStringArrayListExtra(SelectFileActivity.EXTRA_SELECTED_FILE_PATH_LIST));
            }
        }
    }
}
