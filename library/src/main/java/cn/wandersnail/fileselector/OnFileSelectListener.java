package cn.wandersnail.fileselector;

import java.util.List;

/**
 * date: 2019/8/8 14:40
 * author: zengfansheng
 */
public interface OnFileSelectListener {
    void onFileSelect(int requestCode, List<String> paths);
}
