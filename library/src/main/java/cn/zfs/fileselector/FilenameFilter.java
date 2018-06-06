package cn.zfs.fileselector;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.File;

/**
 * 描述: 过滤器
 * 时间: 2018/6/2 14:45
 * 作者: zengfansheng
 */
public class FilenameFilter implements Parcelable {
    public boolean accept(File dir, String name) {
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public FilenameFilter() {
    }

    protected FilenameFilter(Parcel in) {
    }

    public static final Parcelable.Creator<FilenameFilter> CREATOR = new Parcelable.Creator<FilenameFilter>() {
        @Override
        public FilenameFilter createFromParcel(Parcel source) {
            return new FilenameFilter(source);
        }

        @Override
        public FilenameFilter[] newArray(int size) {
            return new FilenameFilter[size];
        }
    };
}
