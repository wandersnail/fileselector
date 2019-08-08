package com.snail.fileselector;

import androidx.annotation.NonNull;
import androidx.core.os.EnvironmentCompat;

/**
 * date: 2019/8/6 13:52
 * author: zengfansheng
 */
class Storage {
    private String path;
    private String description;
    private String state;
    private long availaleSize;
    private long totalSize;
    private boolean isRemovable;
    private boolean isUsb;
    private boolean isPrimary;
    private boolean isAllowMassStorage;

    /**
     * 路径
     */
    @NonNull
    String getPath() {
        return path;
    }

    void setPath(String path) {
        this.path = path;
    }

    /**
     * 描述
     */
    String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    /**
     * 状态。
     * <p>
     * {@link EnvironmentCompat#MEDIA_UNKNOWN}    
     */
    String getState() {
        return state;
    }

    void setState(String state) {
        this.state = state;
    }

    /**
     * 可用空间
     */
    long getAvailaleSize() {
        return availaleSize;
    }

    void setAvailaleSize(long availaleSize) {
        this.availaleSize = availaleSize;
    }

    /**
     * 总空间
     */
    long getTotalSize() {
        return totalSize;
    }

    void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }

    /**
     * 是否可移除
     */
    boolean isRemovable() {
        return isRemovable;
    }

    void setRemovable(boolean removable) {
        isRemovable = removable;
    }

    /**
     * 是否USB存储
     */
    boolean isUsb() {
        return isUsb;
    }

    void setUsb(boolean usb) {
        isUsb = usb;
    }

    /**
     * 是否主存储
     */
    boolean isPrimary() {
        return isPrimary;
    }

    void setPrimary(boolean primary) {
        isPrimary = primary;
    }

    /**
     * 是否支持UMS功能
     */
    boolean isAllowMassStorage() {
        return isAllowMassStorage;
    }

    void setAllowMassStorage(boolean allowMassStorage) {
        isAllowMassStorage = allowMassStorage;
    }
}
