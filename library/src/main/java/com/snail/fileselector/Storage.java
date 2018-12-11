package com.snail.fileselector;

import android.os.Environment;
import android.support.v4.os.EnvironmentCompat;

/**
 * 描述: 存储
 * 时间: 2018/5/27 13:18
 * 作者: zengfansheng
 */
class Storage {
    /**
     * 路径
     */
    String path;
    /**
     * 描述
     */
    String description;
    /**
     * 可用空间
     */
    long availaleSize;
    /**
     * 总空间
     */
    long totalSize;
    /**
     * one of {@link EnvironmentCompat#MEDIA_UNKNOWN}, {@link Environment#MEDIA_REMOVED},
     *         {@link Environment#MEDIA_UNMOUNTED},
     *         {@link Environment#MEDIA_CHECKING},
     *         {@link Environment#MEDIA_NOFS},
     *         {@link Environment#MEDIA_MOUNTED},
     *         {@link Environment#MEDIA_MOUNTED_READ_ONLY},
     *         {@link Environment#MEDIA_SHARED},
     *         {@link Environment#MEDIA_BAD_REMOVAL}, or
     *         {@link Environment#MEDIA_UNMOUNTABLE}.
     */
    String state;
    /**
     * 是否可移除
     */
    boolean isRemovable;
    /**
     * 是否USB存储
     */
    boolean isUsb;
    /**
     * 是否主存储
     */
    boolean isPrimary;

    /**
     * 是否支持UMS功能
     */
    boolean isAllowMassStorage;
        
    String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path = path;
    }

    long getAvailaleSize() {
        return availaleSize;
    }

    void setAvailaleSize(long availaleSize) {
        this.availaleSize = availaleSize;
    }

    long getTotalSize() {
        return totalSize;
    }

    void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    String getState() {
        return state;
    }

    void setState(String state) {
        this.state = state;
    }

    boolean isRemovable() {
        return isRemovable;
    }

    void setRemovable(boolean removable) {
        this.isRemovable = removable;
    }

    boolean isUsb() {
        return isUsb;
    }

    void setUsb(boolean usb) {
        isUsb = usb;
    }

    String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    boolean isPrimary() {
        return isPrimary;
    }

    void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    boolean isAllowMassStorage() {
        return isAllowMassStorage;
    }

    void setAllowMassStorage(boolean allowMassStorage) {
        this.isAllowMassStorage = allowMassStorage;
    }
}
