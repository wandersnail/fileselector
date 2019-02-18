package com.snail.fileselector;

import androidx.annotation.NonNull;

import java.io.File;

/**
 * Created by zeng on 2017/3/1.
 */

class Item implements Comparable<Item> {
    String desc;
	File file;
	boolean checked;

	Item(File file, boolean checked) {
		this.file = file;
		this.checked = checked;
	}
	
	Item(String desc, File file, boolean checked) {
	    this(file, checked);
	    this.desc = desc;
    }

	@Override
	public int compareTo(@NonNull Item o) {
		if (file == null) {
		    return -1;
		} else if (o.file == null) {
		    return 1;
		}
		String s = CharacterParser.getSelling(file.getName());
		String s1 = CharacterParser.getSelling(o.file.getName());
		return s.compareTo(s1);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Item)) return false;
		Item item = (Item) o;
		return file != null ? file.equals(item.file) : item.file == null;

	}

	@Override
	public int hashCode() {
		return file != null ? file.hashCode() : 0;
	}
}
