package com.snail.fileselector

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.os.Handler
import android.os.Looper
import androidx.annotation.ColorInt
import androidx.fragment.app.Fragment
import java.io.File
import java.io.FilenameFilter

/**
 * 描述:
 * 时间: 2018/6/2 14:16
 * 作者: zengfansheng
 */
class FileSelector {
    private var listener: OnFileSelectListener? = null
    private var isLandscape = false
    private var root: File? = Environment.getExternalStorageDirectory()
    private var selectionMode = FILES_ONLY
    private var isMultiSelectionEnabled = false
    private var filenameFilter: FilenameFilter? = null
    private var title: String? = null
    private var themeColors: IntArray? = null    
    private var language = Language.SIMPLIFIED_CHINESE
    
    fun setRoot(root: File?): FileSelector {
        this.root = root
        return this
    }

    /**
     * 设置显示语言
     */
    fun setLanguage(language: Language): FileSelector {
        this.language = language
        return this
    }

    /**
     * 设置主题颜色
     */
    fun setThemeColor(@ColorInt colorPrimary: Int, @ColorInt colorPrimaryDark: Int): FileSelector {
        themeColors = intArrayOf(colorPrimary, colorPrimaryDark)
        return this
    }

    /**
     * 设置屏幕方向
     */
    fun setScreenOrientation(landscape: Boolean): FileSelector {
        this.isLandscape = landscape
        return this
    }

    /**
     * 设置文件选择回调
     */
    fun setOnFileSelectListener(listener: OnFileSelectListener): FileSelector {
        this.listener = listener
        return this
    }

    /**
     * 设置是选择模式
     * 
     * @param mode [FILES_ONLY], [FILES_AND_DIRECTORIES], [DIRECTORIES_ONLY]
     */
    fun setSelectionMode(mode: Int): FileSelector {
        this.selectionMode = mode
        return this
    }

    /**
     * 设置是否多选
     */
    fun setMultiSelectionEnabled(isMultiSelectionEnabled: Boolean): FileSelector {
        this.isMultiSelectionEnabled = isMultiSelectionEnabled
        return this
    }

    /**
     * 设置文件名过滤器
     */
    fun setFilenameFilter(filenameFilter: FilenameFilter): FileSelector {
        this.filenameFilter = filenameFilter
        return this
    }

    /**
     * 设置标题
     */
    fun setTitle(title: String): FileSelector {
        this.title = title
        return this
    }

    /**
     * 开始选择
     */
    fun select(activity: Activity) {
        activity.startActivityForResult(obtainIntent(activity), REQUEST_CODE)
    }

    /**
     * 开始选择
     */
    fun select(fragment: Fragment) {
        if (fragment.activity != null) {
            fragment.startActivityForResult(obtainIntent(fragment.activity!!), REQUEST_CODE)
        }
    }

    private fun obtainIntent(context: Context): Intent {
        SelectFileActivity.filenameFilter = filenameFilter
        val intent = Intent(context, SelectFileActivity::class.java)
        intent.putExtra(SelectFileActivity.EXTRA_IS_LANDSCAPE, isLandscape)
        intent.putExtra(SelectFileActivity.EXTRA_SELECTION_MODE, selectionMode)
        intent.putExtra(SelectFileActivity.EXTRA_IS_MULTI_SELECT, isMultiSelectionEnabled)
        intent.putExtra(SelectFileActivity.EXTRA_LANGUAGE, language.value)
        if (title != null) {
            intent.putExtra(SelectFileActivity.EXTRA_TITLE, title)
        }
        if (root != null) {
            intent.putExtra(SelectFileActivity.EXTRA_ROOT, root)
        }
        if (themeColors != null) {
            intent.putExtra(SelectFileActivity.EXTRA_THEME_COLORS, themeColors)
        }
        return intent
    }

    /**
     * 将Activty或Fragment的结果传过来处理
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (listener != null && data != null) {
                Handler(Looper.getMainLooper()).postDelayed({ 
                    listener!!.onFileSelect(data.getStringArrayListExtra(SelectFileActivity.EXTRA_SELECTED_FILE_PATH_LIST)) 
                }, 200)
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 21516
        
        const val FILES_ONLY = 0
        const val DIRECTORIES_ONLY = 1
        const val FILES_AND_DIRECTORIES = 2
    }
}
