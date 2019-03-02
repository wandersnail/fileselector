package com.snail.fileselector

import java.io.File

/**
 * Created by zeng on 2017/3/1.
 */

internal class Item @JvmOverloads constructor(var file: File?, var checked: Boolean, var desc: String = "") : Comparable<Item> {

    override fun compareTo(other: Item): Int {
        if (file == null) {
            return -1
        } else if (other.file == null) {
            return 1
        }
        val s = CharacterParser.getSelling(file!!.name)
        val s1 = CharacterParser.getSelling(other.file!!.name)
        return s.compareTo(s1)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Item) return false
        val item = other as Item?
        return if (file != null) file == item!!.file else item!!.file == null

    }

    override fun hashCode(): Int {
        return if (file != null) file!!.hashCode() else 0
    }
}
