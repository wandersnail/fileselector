package cn.zfs.fileselector;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * 描述: 文件选择结果监听
 * 时间: 2018/6/2 14:17
 * 作者: zengfansheng
 */
public interface OnFileSelectListener {
    void onFileSelect(@NonNull List<String> paths);
}
