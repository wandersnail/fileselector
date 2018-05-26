package cn.zfs.fileselector;

import java.io.File;

/**
 * 描述:
 * 时间: 2018/5/26 22:26
 * 作者: zengfansheng
 */
class DirCell {
    int index;
    File location;

    DirCell(int index, File location) {
        this.index = index;
        this.location = location;
    }
}
