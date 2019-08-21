package cn.wandersnail.fileselector;

import java.io.File;

/**
 * date: 2019/8/8 14:39
 * author: zengfansheng
 */
class DirCell {
    int index;
    File location;

    DirCell() {
    }

    DirCell(int index, File location) {
        this.index = index;
        this.location = location;
    }
}
