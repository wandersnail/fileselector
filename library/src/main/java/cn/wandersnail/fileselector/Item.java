package cn.wandersnail.fileselector;

import java.io.File;
import java.util.Objects;

import androidx.annotation.NonNull;

/**
 * date: 2019/8/8 14:44
 * author: zengfansheng
 */
class Item implements Comparable<Item> {
    File file;
    boolean checked;
    String desc;

    Item(File file, boolean checked) {
        this.file = file;
        this.checked = checked;
    }

    Item(File file, boolean checked, String desc) {
        this.file = file;
        this.checked = checked;
        this.desc = desc;
    }

    @Override
    public int compareTo(@NonNull Item other) {
        if (file == null) {
            return -1;
        } else if (other.file == null) {
            return 1;
        }
        String s = CharacterParser.getSpelling(file.getName());
        String s1 = CharacterParser.getSpelling(other.file.getName());
        return s.compareTo(s1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return Objects.equals(file, item.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file);
    }
}
