package cn.wandersnail.fileselector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FilenameFilter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

/**
 * date: 2019/8/8 15:44
 * author: zengfansheng
 */
public class FileSelector {
    private static final int REQUEST_CODE = 21516;
    
    public static final int FILES_ONLY = 0;
    public static final int DIRECTORIES_ONLY = 1;
    public static final int FILES_AND_DIRECTORIES = 2;

    private OnFileSelectListener listener;
    private boolean isLandscape;
    private File root = Environment.getExternalStorageDirectory();
    private int selectionMode = FILES_ONLY;
    private boolean isMultiSelectionEnabled;
    private FilenameFilter filenameFilter;
    private String title;
    private int[] themeColors;
    private Language language = Language.SIMPLIFIED_CHINESE;
    private boolean showHiddenFiles;
    private Handler handler;

    /**
     * 设置文件选择回调
     */
    public FileSelector setOnFileSelectListener(OnFileSelectListener listener) {
        this.listener = listener;
        return this;
    }

    /**
     * 设置屏幕方向
     */
    public FileSelector setScreenOrientation(boolean landscape) {
        isLandscape = landscape;
        return this;
    }

    /**
     * 设置根目录，默认是内部存储
     *
     * @param root 当设置为null时，列出当前所有可用储存设备
     */
    public FileSelector setRoot(File root) {
        this.root = root;
        return this;
    }

    /**
     * 设置是选择模式
     *
     * @param selectionMode {@link #FILES_ONLY}, {@link #FILES_AND_DIRECTORIES}, {@link #DIRECTORIES_ONLY}
     */
    public FileSelector setSelectionMode(int selectionMode) {
        this.selectionMode = selectionMode;
        return this;
    }

    /**
     * 设置是否多选
     */
    public FileSelector setMultiSelectionEnabled(boolean multiSelectionEnabled) {
        isMultiSelectionEnabled = multiSelectionEnabled;
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
     * 设置标题
     */
    public FileSelector setTitle(String title) {
        this.title = title;
        return this;
    }

    /**
     * 设置主题颜色
     */
    public FileSelector setThemeColor(int colorPrimary, int colorPrimaryDark) {
        this.themeColors = new int[]{colorPrimary, colorPrimaryDark};
        return this;
    }

    /**
     * 设置显示语言
     */
    public FileSelector setLanguage(Language language) {
        this.language = language;
        return this;
    }

    /**
     * 设置是否显示隐藏文件和文件夹
     */
    public FileSelector showHiddenFiles(boolean showHiddenFiles) {
        this.showHiddenFiles = showHiddenFiles;
        return this;
    }
    
    /**
     * 开始选择
     */
    public void select(@NonNull Activity activity) {
        activity.startActivityForResult(obtainIntent(activity), REQUEST_CODE);
    }

    /**
     * 开始选择
     */
    public void select(@NonNull Fragment fragment) {
        if (fragment.getActivity() != null) {
            fragment.startActivityForResult(obtainIntent(fragment.getActivity()), REQUEST_CODE);
        }
    }

    private Intent obtainIntent(Context context) {
        SelectFileActivity.filenameFilter = filenameFilter;
        Intent intent = new Intent(context, SelectFileActivity.class);
        intent.putExtra(SelectFileActivity.EXTRA_SELECTOR_HASH, toString());
        intent.putExtra(SelectFileActivity.EXTRA_IS_LANDSCAPE, isLandscape);
        intent.putExtra(SelectFileActivity.EXTRA_SELECTION_MODE, selectionMode);
        intent.putExtra(SelectFileActivity.EXTRA_IS_MULTI_SELECT, isMultiSelectionEnabled);
        intent.putExtra(SelectFileActivity.EXTRA_LANGUAGE, language.value);
        intent.putExtra(SelectFileActivity.EXTRA_SHOW_HIDDEN_FILES, showHiddenFiles);
        
        if (title != null) {
            intent.putExtra(SelectFileActivity.EXTRA_TITLE, title);
        }
        if (root != null) {
            intent.putExtra(SelectFileActivity.EXTRA_ROOT, root);
        }
        if (themeColors != null) {
            intent.putExtra(SelectFileActivity.EXTRA_THEME_COLORS, themeColors);
        }
        return intent;
    }

    /**
     * 将Activty或Fragment的结果传过来处理
     */
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (listener != null && data != null) {
                String classHash = data.getStringExtra(SelectFileActivity.EXTRA_SELECTOR_HASH);
                if (toString().equals(classHash)) {
                    if (handler == null) {
                        handler = new Handler(Looper.getMainLooper());
                    }
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFileSelect(data.getStringArrayListExtra(SelectFileActivity.EXTRA_SELECTED_FILE_PATH_LIST));
                        }
                    }, 200);
                }
            }
        }
    }
}
