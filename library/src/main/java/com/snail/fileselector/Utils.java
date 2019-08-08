package com.snail.fileselector;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.util.TypedValue;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.os.EnvironmentCompat;

/**
 * date: 2019/8/8 13:57
 * author: zengfansheng
 */
final class Utils {
    private static final String[] videoSuffixs = {".avi", ".wmv", ".wmp", ".wm", ".asf", ".mpg", ".mpeg", ".mpe", ".m1v", ".m2v", ".mpv2", ".mp2v", ".ts", ".tp", ".tpr", ".trp", ".vob", ".ifo", ".ogm", "ogv", ".mp4", ".m4v", ".m4p", ".m4b", ".3gp", ".3gpp", ".3g2", ".3gp2", ".mkv", ".rm", ".ram", "rmvb", ".rpm", ".flv", ".swf", ".mov", ".qt", ".amr", ".nsv", ".dpg", ".m2ts", ".m2t", ".mts", "dvr-ms", ".k3g", ".skm", ".evo", ".nsr", ".amv", ".divx", ".webm", ".wtv", ".f4v"};
    private static final String[] imageSuffixs = {".bmp", ".jpg", ".jpeg", ".png", ".gif"};
    private static final String[] audioSuffixs = {".mp3", ".mp2", ".wma", ".wav", ".ape", ".flac", ".ogg", ".m4a", ".m4r", ".aac", ".mid", ".ra"};
    private static final String[] pptSuffixs = {".ppt", ".pot", ".pps", ".pptx", ".pptm", ".ppsx", ".ppsm", ".potx", ".dps", ".potm"};
    private static final String[] wordSuffixs = {".doc", ".docx", ".docm", ".dot", ".dotx", ".dotm", ".rtf", ".tar", ".ace", ".wpt", ".wps"};
    private static final String[] excelSuffixs = {".et", ".csv", ".xl", ".xls", ".xlt", ".xlsx", ".xlsm", ".xlsb", ".xltx", ".xltm", ".xla", ".xlm", ".xlw"};
    private static final String[] zipSuffixs = {".rar", ".zip", ".7z", ".gz", ".arj", ".cab", ".jar", ".tar", ".ace"};
    private static final String[] psSuffixs = {".psd", ".pdd", ".eps", ".psb"};
    private static final String[] htmlSuffixs = {".htm", ".html", ".mht", ".mhtml"};
    private static final String[] developerSuffixs = {".db", ".db-journal", ".db3", ".sqlite", ".xml", ".wdb", ".mdf", ".dbf", ".properties", ".cfg", ".ini", ".sys"};

    /**
     * 获取显示屏幕宽度，不包含状态栏和导航栏
     */
    static int getDisplayScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 获取显示屏幕高度，不包含状态栏和导航栏
     */
    static int getDisplayScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    /**
     * 获取Apk文件的图标
     */
    static Drawable getApkThumbnail(Context context, String path) {
        PackageManager pm = context.getApplicationContext().getPackageManager();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            //获取apk的图标
            appInfo.sourceDir = path;
            appInfo.publicSourceDir = path;
            return appInfo.loadIcon(pm);
        }
        return null;
    }

    private static boolean isWhat(String[] suffixs, String path) {
        for (String suffix : suffixs) {
            if (path.toLowerCase().endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    static int getStatusBarHeight(Context context) {
        int result = 0;
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获取存储设备剩余大小
     */
    public static long getStorageFreeSpace(@NonNull String path) {
        if (new File(path).exists()) {
            StatFs stat = new StatFs(path);
            return stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
        }
        return 0;
    }

    /**
     * 存储设备总容量
     */
    public static long getStorageTotalSpace(@NonNull String path) {
        if (new File(path).exists()) {
            StatFs stat = new StatFs(path);
            return stat.getBlockSizeLong() * stat.getBlockCountLong();
        }
        return 0;
    }

    static int getPrimaryColor(Context context, int defaultColor) {
        TypedValue typedValue = new TypedValue();
        boolean found = false;
        try {
            int resId = context.getResources().getIdentifier("colorPrimary", "attr", context.getPackageName());
            if (resId > 0) {
                found = context.getTheme().resolveAttribute(resId, typedValue, true);
            }
        } catch (Exception ignore) {
        }
        return found ? typedValue.data : defaultColor;
    }

    static int getPrimaryDarkColor(Context context, int defaultColor) {
        TypedValue typedValue = new TypedValue();
        boolean found = false;
        try {
            int resId = context.getResources().getIdentifier("colorPrimaryDark", "attr", context.getPackageName());
            if (resId > 0) {
                found = context.getTheme().resolveAttribute(resId, typedValue, true);
            }
        } catch (Exception ignore) {
        }
        return found ? typedValue.data : defaultColor;
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    static int dp2px(Context context, float value) {
        return (int) (value * context.getResources().getDisplayMetrics().density + 0.5f);
    }

    static Drawable getShape(Context context, int color, int strokeWidth, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp2px(context, 30f));
        drawable.setStroke(strokeWidth, strokeColor);
        return drawable;
    }

    static StateListDrawable getFillBlueBg(Context context, int[] colors) {
        Drawable pressed = getShape(context, colors[1], 0, colors[1]);
        Drawable normal = getShape(context, colors[0], 0, colors[0]);
        Drawable disable = getShape(context, ContextCompat.getColor(context, R.color.fsEditHint), 0, ContextCompat.getColor(context, R.color.fsEditHint));
        return createBg(normal, pressed, disable);
    }

    static StateListDrawable getFillGrayBg(Context context) {
        Drawable pressed = getShape(context, ContextCompat.getColor(context, R.color.fsEditHintDark), 0, ContextCompat.getColor(context, R.color.fsEditHintDark));
        Drawable normal = getShape(context, ContextCompat.getColor(context, R.color.fsEditHint), 0, ContextCompat.getColor(context, R.color.fsEditHint));
        Drawable disable = getShape(context, ContextCompat.getColor(context, R.color.fsEditHint), 0, ContextCompat.getColor(context, R.color.fsEditHint));
        return createBg(normal, pressed, disable);
    }

    static StateListDrawable getFrameBlueBg(Context context, int color) {
        Drawable pressed = getShape(context, color, 0, color);
        Drawable normal = getShape(context, ContextCompat.getColor(context, R.color.fsTransparent), dp2px(context, 1f), color);
        return createBg(normal, pressed, null);
    }

    static StateListDrawable createBg(Drawable normal, Drawable pressed, Drawable disable) {
        StateListDrawable drawable = new StateListDrawable();
        if (disable != null) {
            drawable.addState(new int[]{-android.R.attr.state_enabled}, disable);
        }
        drawable.addState(new int[]{android.R.attr.state_pressed}, pressed);
        drawable.addState(new int[0], normal);//normal一定要最后
        return drawable;
    }

    /**
     * @param normal  正常时的颜色
     * @param pressed 按压时的颜色
     */
    static ColorStateList createColorStateList(int normal, int pressed) {
        //normal一定要最后
        int[][] states = new int[][]{new int[]{android.R.attr.state_pressed, android.R.attr.state_enabled}, new int[0]};
        return new ColorStateList(states, new int[]{pressed, normal});
    }

    static List<Storage> getStorages(@NonNull Context context) {
        try {
            StorageManager storageManager = (StorageManager) context.getApplicationContext().getSystemService(Context.STORAGE_SERVICE);
            Objects.requireNonNull(storageManager);
            //得到StorageManager中的getVolumeList()方法的对象
            Method getVolumeList = storageManager.getClass().getMethod("getVolumeList");
            //得到StorageVolume类的对象
            Class<?> storageValumeClazz = Class.forName("android.os.storage.StorageVolume");
            //获得StorageVolume中的一些方法
            Method getPath = storageValumeClazz.getMethod("getPath");
            Method isRemovable = storageValumeClazz.getMethod("isRemovable");
            Method allowMassStorage = storageValumeClazz.getMethod("allowMassStorage");
            Method primary = storageValumeClazz.getMethod("isPrimary");
            Method description = storageValumeClazz.getMethod("getDescription", Context.class);

            Method mGetState = null;
            try {
                mGetState = storageValumeClazz.getMethod("getState");
            } catch (NoSuchMethodException ignore) {
            }

            //调用getVolumeList方法，参数为：“谁”中调用这个方法
            Object invokeVolumeList = getVolumeList.invoke(storageManager);
            int length = java.lang.reflect.Array.getLength(invokeVolumeList);
            List<Storage> list = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                Object storageValume = java.lang.reflect.Array.get(invokeVolumeList, i);//得到StorageVolume对象
                Object invokePath = getPath.invoke(storageValume);
                String path = invokePath == null ? "" : (String) invokePath;
                Object invokeRemovable = isRemovable.invoke(storageValume);
                boolean removable = invokeRemovable != null && (boolean) invokeRemovable;
                Object invokeAllowMass = allowMassStorage.invoke(storageValume);
                boolean isAllowMassStorage = invokeAllowMass != null && (boolean) invokeAllowMass;
                Object invokePrimary = primary.invoke(storageValume);
                boolean isPrimary = invokePrimary != null && (boolean) invokePrimary;
                Object invokeValume = description.invoke(storageValume, context);
                String desc = invokeValume == null ? "" : (String) invokeValume;
                String state;
                if (mGetState != null) {
                    state = (String) mGetState.invoke(storageValume);
                } else {
                    state = Environment.getStorageState(new File(path));
                }
                long totalSize = 0;
                long availaleSize = 0;
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    totalSize = getStorageTotalSpace(path);
                    availaleSize = getStorageFreeSpace(path);
                }
                Storage storage = new Storage();
                storage.setAvailaleSize(availaleSize);
                storage.setTotalSize(totalSize);
                storage.setState(state == null ? EnvironmentCompat.MEDIA_UNKNOWN : state);
                storage.setPath(path);
                storage.setRemovable(removable);
                storage.setDescription(desc);
                storage.setAllowMassStorage(isAllowMassStorage);
                storage.setPrimary(isPrimary);
                storage.setUsb(desc.toLowerCase(Locale.ENGLISH).contains("usb"));
                list.add(storage);
            }
            return list;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    static boolean isVideo(String path) {
        return isWhat(videoSuffixs, path);
    }

    static boolean isImage(String path) {
        return isWhat(imageSuffixs, path);
    }

    static boolean isApk(String path) {
        return path.toLowerCase().endsWith(".apk");
    }

    static boolean isAudio(String path) {
        return isWhat(audioSuffixs, path);
    }

    static boolean isText(String path) {
        return path.toLowerCase().endsWith(".txt");
    }

    static boolean isPdf(String path) {
        return path.toLowerCase().endsWith(".pdf");
    }

    static boolean isZip(String path) {
        return isWhat(zipSuffixs, path);
    }

    static boolean isFlash(String path) {
        return path.toLowerCase().endsWith(".swf") || path.toLowerCase().endsWith(".fla");
    }

    static boolean isHtml(String path) {
        return isWhat(htmlSuffixs, path);
    }

    static boolean isPs(String path) {
        return isWhat(psSuffixs, path);
    }

    static boolean isWord(String path) {
        return isWhat(wordSuffixs, path);
    }

    static boolean isExcel(String path) {
        return isWhat(excelSuffixs, path);
    }

    static boolean isPPT(String path) {
        return isWhat(pptSuffixs, path);
    }

    static boolean isDeveloper(String path) {
        return isWhat(developerSuffixs, path);
    }
}
