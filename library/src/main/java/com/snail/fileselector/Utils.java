package com.snail.fileselector;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;

import com.snail.commons.util.UiUtils;

import androidx.core.content.ContextCompat;

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

    private static Drawable getShape(int color, int strokeWidth, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(UiUtils.dp2px(30f));
        drawable.setStroke(strokeWidth, strokeColor);
        return drawable;
    }

    static StateListDrawable getFillBlueBg(Context context, int[] colors) {
        Drawable pressed = getShape(colors[1], 0, colors[1]);
        Drawable normal = getShape(colors[0], 0, colors[0]);
        Drawable disable = getShape(ContextCompat.getColor(context, R.color.fsEditHint), 0, ContextCompat.getColor(context, R.color.fsEditHint));
        return createBg(normal, pressed, disable);
    }

    static StateListDrawable getFillGrayBg(Context context) {
        Drawable pressed = getShape(ContextCompat.getColor(context, R.color.fsEditHintDark), 0, ContextCompat.getColor(context, R.color.fsEditHintDark));
        Drawable normal = getShape(ContextCompat.getColor(context, R.color.fsEditHint), 0, ContextCompat.getColor(context, R.color.fsEditHint));
        Drawable disable = getShape(ContextCompat.getColor(context, R.color.fsEditHint), 0, ContextCompat.getColor(context, R.color.fsEditHint));
        return createBg(normal, pressed, disable);
    }

    static StateListDrawable getFrameBlueBg(Context context, int color) {
        Drawable pressed = getShape(color, 0, color);
        Drawable normal = getShape(ContextCompat.getColor(context, R.color.fsTransparent), UiUtils.dp2px(1f), color);
        return createBg(normal, pressed, null);
    }

    private static StateListDrawable createBg(Drawable normal, Drawable pressed, Drawable disable) {
        StateListDrawable drawable = new StateListDrawable();
        if (disable != null) {
            drawable.addState(new int[]{-android.R.attr.state_enabled}, disable);
        }
        drawable.addState(new int[]{android.R.attr.state_pressed}, pressed);
        drawable.addState(new int[0], normal);//normal一定要最后
        return drawable;
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
