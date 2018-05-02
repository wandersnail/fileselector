package com.zfs.fileselector;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import java.text.DecimalFormat;

/**
 * Created by zeng on 2017/3/1.
 */

public class Utils {
	private static final String[] videoSuffixs = {".avi", ".wmv", ".wmp", ".wm", ".asf", ".mpg", ".mpeg",
			".mpe", ".m1v", ".m2v", ".mpv2", ".mp2v", ".ts", ".tp", ".tpr", ".trp", ".vob", ".ifo", ".ogm",
			"ogv", ".mp4", ".m4v", ".m4p", ".m4b", ".3gp", ".3gpp", ".3g2", ".3gp2", ".mkv", ".rm", ".ram",
			"rmvb", ".rpm", ".flv", ".swf", ".mov", ".qt", ".amr", ".nsv", ".dpg", ".m2ts", ".m2t", ".mts",
			"dvr-ms", ".k3g", ".skm", ".evo", ".nsr", ".amv", ".divx", ".webm", ".wtv", ".f4v"};
	private static final String[] imageSuffixs = {".bmp", ".jpg", ".jpeg", ".png", ".gif"};
    private static final String[] audioSuffixs = {".mp3", ".mp2", ".wma", ".wav", ".ape", ".flac", ".ogg", ".m4a", ".m4r", ".aac", ".mid", ".ra"};
    private static final String[] pptSuffixs = {".ppt", ".pot", ".pps", ".pptx", ".pptm", ".ppsx", ".ppsm", ".potx", ".dps", ".potm"};
    private static final String[] wordSuffixs = {".doc", ".docx", ".docm", ".dot", ".dotx", ".dotm", ".rtf", ".tar", ".ace", ".wpt", ".wps"};
    private static final String[] excelSuffixs = {".et", ".csv", ".xl", ".xls", ".xlt", ".xlsx", ".xlsm", ".xlsb", ".xltx", ".xltm", ".xla", ".xlm", ".xlw"};
    private static final String[] zipSuffixs = {".rar", ".zip", ".7z", ".gz", ".arj", ".cab", ".jar", ".tar", ".ace"};
    private static final String[] psSuffixs = {".psd", ".pdd", ".eps", ".psb"};
    private static final String[] htmlSuffixs = {".htm", ".html", ".mht", ".mhtml"};
    private static final String[] developerSuffixs = {".db", ".db3", ".sqlite", ".xml", ".wdb", ".mdf", ".dbf", ".properties", ".cfg", ".ini", ".sys"};
	
    /**
     * 根据视频生成缩略图
     * @param path 视频的路径
     * @param width 要生成的图片宽度
     * @param height 要生成的图片高度
     */
    public static Bitmap getVideoThumbnail(String path, int width, int height) {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND);
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象  
        return ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
    }

	/**
	 * 根据宽高获取缩略图	
	 * @param source 原图片
	 * @param width 宽度
	 * @param height 高度
	 */
	public static Bitmap extractThumbnail(Bitmap source, int width, int height){
		return ThumbnailUtils.extractThumbnail(source, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
	}
	
    /**
     * 获取Apk文件的图标
     */
    public static Drawable getApkThumbnail(Context context, String path){
        PackageManager pm = context.getApplicationContext().getPackageManager();
        PackageInfo packageInfo = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            /**获取apk的图标 */
            appInfo.sourceDir = path;
            appInfo.publicSourceDir = path;
            return appInfo.loadIcon(pm);
        }
        return null;
    }

	/**
	 * Drawable转Bitmap
	 */
	public static Bitmap drawableToBitmap(Drawable drawable){
        if (drawable == null) {
            return null;
        }
		// 取 drawable 的长宽
		int w = drawable.getIntrinsicWidth();
		int h = drawable.getIntrinsicHeight();
		// 取 drawable 的颜色格式
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565;
		//建立对应的Bitmap
		Bitmap bitmap = Bitmap.createBitmap(w, h, config);
		// 建立对应 bitmap 的画布
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, w, h);
		// 把 drawable 内容画到画布中
		drawable.draw(canvas);
		return bitmap;
	}

	/**
	 * 格式化文件大小，根据文件大小不同使用不同单位
	 * @param size 文件大小
	 * @return 字符串形式的大小，包含单位(B,KB,MB,GB,TB,PB)
	 */
	public static String formatFileSize(long size) {
		DecimalFormat formater = new DecimalFormat("####.00");
		if (size < 1024L) {
			return size + " B";
		} else if (size < 1048576L) {
			return formater.format(size / 1024f) + " KB";
		} else if (size < 1073741824L) {
			return formater.format(size / 1048576f) + " MB";
		} else if (size < 1099511627776L) {
			return formater.format(size / 1073741824f) + " GB";
		} else if (size < 1125899906842624L) {
			return formater.format(size / 1099511627776f) + " TB";
		} else if (size < 1152921504606846976L) {
			return formater.format(size / 1125899906842624f) + " PB";
		}
		return "size: out of range";
	}

    private static boolean isWhat(String[] suffixs, String path) {
        for (String suffix : suffixs) {
            if (path.toLowerCase().endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }
    
	public static boolean isVideo(String path) {
		return isWhat(videoSuffixs, path);
	}

	public static boolean isImage(String path) {
		return isWhat(imageSuffixs, path);
	}

    public static boolean isApk(String path) {
        return path.toLowerCase().endsWith(".apk");
    }
    
	public static boolean isAudio(String path) {
        return isWhat(audioSuffixs, path);
    }
    
    public static boolean isText(String path) {
        return path.toLowerCase().endsWith(".txt");
    }

    public static boolean isPdf(String path) {
        return path.toLowerCase().endsWith(".pdf");
    }

    public static boolean isZip(String path) {
        return isWhat(zipSuffixs, path);
    }
    
    public static boolean isFlash(String path) {
	    return path.toLowerCase().endsWith(".swf") || path.toLowerCase().endsWith(".fla");
    }

    public static boolean isHtml(String path) {
	    return isWhat(htmlSuffixs, path);
    }

    public static boolean isPs(String path) {
        return isWhat(psSuffixs, path);
    }
    
    public static boolean isWord(String path) {
        return isWhat(wordSuffixs, path);
    }

    public static boolean isExcel(String path) {
        return isWhat(excelSuffixs, path);
    }

    public static boolean isPPT(String path) {
        return isWhat(pptSuffixs, path);
    }
    
    public static boolean isDeveloper(String path) {
	    return isWhat(developerSuffixs, path);
    }
}
