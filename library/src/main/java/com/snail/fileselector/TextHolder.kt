package com.snail.fileselector

import android.util.SparseArray

/**
 * 字符串管理器
 *
 * date: 2019/3/2 14:47
 * author: zengfansheng
 */
internal class TextHolder {
    private val lanArr = SparseArray<SparseArray<String>>()
    var language = Language.SIMPLIFIED_CHINESE

    fun getText(id: Int): String {
        return lanArr[language.value][id, ""]
    }
    
    init {
        val zhCN = SparseArray<String>()
        zhCN.put(ALL_FILES, "全部文件")
        zhCN.put(ROOT, "根目录")
        zhCN.put(OK, "确定")
        zhCN.put(CANCEL, "取消")
        zhCN.put(SELECTED_PATTERN, "已选(%d)")
        zhCN.put(CLEAR, "清空")
        zhCN.put(CLOSE, "关闭")
        zhCN.put(SINGLE_ITEM_PATTERN, "%d项")
        zhCN.put(MULTI_ITEM_PATTERN, "%d项")
        zhCN.put(SELECTED_ITEM_PATTERN, "已选%d项")
        zhCN.put(NEW_FOLDER, "新建文件夹")
        zhCN.put(FOLDER_CREATE_SUCCESS, "文件夹创建成功")
        zhCN.put(FOLDER_CREATE_FAILED, "文件夹创建失败")
        zhCN.put(RENAME, "重命名")
        zhCN.put(RENAME_SUCCESS, "重命名成功")
        zhCN.put(RENAME_FAILED, "重命名失败")
        zhCN.put(DELETE, "删除")
        zhCN.put(ENSURE_DELETE_PROMPT, "确定删除吗")
        zhCN.put(SHOW_HIDDEN_FILES, "显示隐藏文件")
        zhCN.put(DONOT_SHOW_HIDDEN_FILES, "不显示隐藏文件")
        lanArr.put(Language.SIMPLIFIED_CHINESE.value, zhCN)

        val zhTW = SparseArray<String>()
        zhTW.put(ALL_FILES, "全部文件")
        zhTW.put(ROOT, "根目錄")
        zhTW.put(OK, "確定")
        zhTW.put(CANCEL, "取消")
        zhTW.put(SELECTED_PATTERN, "已選(%d)")
        zhTW.put(CLEAR, "清空")
        zhTW.put(CLOSE, "關閉")
        zhTW.put(SINGLE_ITEM_PATTERN, "%d項")
        zhTW.put(MULTI_ITEM_PATTERN, "%d項")
        zhTW.put(SELECTED_ITEM_PATTERN, "已選%d項")
        zhTW.put(NEW_FOLDER, "新建資料夾")
        zhTW.put(FOLDER_CREATE_SUCCESS, "資料夾創建成功")
        zhTW.put(FOLDER_CREATE_FAILED, "資料夾創建失敗")
        zhTW.put(RENAME, "重命名")
        zhTW.put(RENAME_SUCCESS, "重命名成功")
        zhTW.put(RENAME_FAILED, "重命名失敗")
        zhTW.put(DELETE, "刪除")
        zhTW.put(ENSURE_DELETE_PROMPT, "確定刪除嗎")
        zhTW.put(SHOW_HIDDEN_FILES, "顯示隱藏文件")
        zhTW.put(DONOT_SHOW_HIDDEN_FILES, "不顯示隱藏文件")
        lanArr.put(Language.TRADITIONAL_CHINESE.value, zhTW)

        val en = SparseArray<String>()
        en.put(ALL_FILES, "All files")
        en.put(ROOT, "root")
        en.put(OK, "OK")
        en.put(CANCEL, "Cancel")
        en.put(SELECTED_PATTERN, "Selected(%d)")
        en.put(CLEAR, "Clear")
        en.put(CLOSE, "Close")
        en.put(SINGLE_ITEM_PATTERN, "%d item")
        en.put(MULTI_ITEM_PATTERN, "%d items")
        en.put(SELECTED_ITEM_PATTERN, "%d selected")
        en.put(NEW_FOLDER, "New Folder")
        en.put(FOLDER_CREATE_SUCCESS, "The folder creation successfully")
        en.put(FOLDER_CREATE_FAILED, "The folder creation failed")
        en.put(RENAME, "Rename")
        en.put(RENAME_SUCCESS, "Rename successfully")
        en.put(RENAME_FAILED, "Rename failed")
        en.put(DELETE, "Delete")
        en.put(ENSURE_DELETE_PROMPT, "Are you sure you want to delete it?")
        en.put(SHOW_HIDDEN_FILES, "Show hidden files")
        en.put(DONOT_SHOW_HIDDEN_FILES, "Don't show hidden files")
        lanArr.put(Language.ENGLISH.value, en)
    }
    
    companion object {
        const val ALL_FILES = 0
        const val ROOT = 1
        const val OK = 2
        const val CANCEL = 3
        const val SELECTED_PATTERN = 4
        const val CLEAR = 7
        const val CLOSE = 8
        const val SINGLE_ITEM_PATTERN = 9
        const val MULTI_ITEM_PATTERN = 10
        const val SELECTED_ITEM_PATTERN = 11
        const val NEW_FOLDER = 12
        const val FOLDER_CREATE_SUCCESS = 13
        const val FOLDER_CREATE_FAILED = 14
        const val RENAME = 15
        const val DELETE = 16
        const val ENSURE_DELETE_PROMPT = 17
        const val RENAME_SUCCESS = 18
        const val RENAME_FAILED = 19
        const val SHOW_HIDDEN_FILES = 20
        const val DONOT_SHOW_HIDDEN_FILES = 21
    }
}