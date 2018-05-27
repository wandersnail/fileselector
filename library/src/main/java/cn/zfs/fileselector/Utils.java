package cn.zfs.fileselector;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.provider.MediaStore;
import android.support.v4.os.EnvironmentCompat;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by zeng on 2017/3/1.
 */

class Utils {
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
    private static final String[] developerSuffixs = {".db", ".db-journal", ".db3", ".sqlite", ".xml", ".wdb", ".mdf", ".dbf", ".properties", ".cfg", ".ini", ".sys"};
	
    /**
     * 根据视频生成缩略图
     * @param path 视频的路径
     * @param width 要生成的图片宽度
     * @param height 要生成的图片高度
     */
    static Bitmap getVideoThumbnail(String path, int width, int height) {
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
	static Bitmap extractThumbnail(Bitmap source, int width, int height){
		return ThumbnailUtils.extractThumbnail(source, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
	}
	
    /**
     * 获取Apk文件的图标
     */
    static Drawable getApkThumbnail(Context context, String path){
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

	/**
	 * Drawable转Bitmap
	 */
	static Bitmap drawableToBitmap(Drawable drawable){
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
	static String formatFileSize(long size) {
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

    static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 获取存储卡剩余大小
     */
    static long getStorageFreeSpace(String path) {
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                StatFs stat = new StatFs(path);
                return stat.getAvailableBlocksLong() * stat.getBlockSizeLong();
            }
        }
        return 0;
    }

    /**
     * 存储卡总容量
     */
    static long getStorageTotalSpace(String path) {
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                StatFs stat = new StatFs(path);
                return stat.getBlockSizeLong() * stat.getBlockCountLong();
            }
        }
        return 0;
    }
    
    static ArrayList<Storage> getStorages(Context context) {
        StorageManager storageManager = (StorageManager) context.getApplicationContext().getSystemService(Context.STORAGE_SERVICE);
        try {
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
            //getState 方法是在4.4_r1之后的版本加的，之前版本（含4.4_r1）没有
            // （http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/4.4_r1/android/os/Environment.java/）
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                try {
                    mGetState = storageValumeClazz.getMethod("getState");
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }

            //调用getVolumeList方法，参数为：“谁”中调用这个方法
            Object invokeVolumeList = getVolumeList.invoke(storageManager);
            int length = Array.getLength(invokeVolumeList);
            ArrayList<Storage> list = new ArrayList<>();
            for (int i = 0; i < length; i++) {
                Object storageValume = Array.get(invokeVolumeList, i);//得到StorageVolume对象
                String path = (String) getPath.invoke(storageValume);
                boolean removable = (boolean) isRemovable.invoke(storageValume);
                boolean isAllowMassStorage = (boolean) allowMassStorage.invoke(storageValume);
                boolean isPrimary = (boolean) primary.invoke(storageValume);
                String desc = (String) description.invoke(storageValume, context);
                String state;
                if (mGetState != null) {
                    state = (String) mGetState.invoke(storageValume);
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        state = Environment.getStorageState(new File(path));
                    } else {
                        if (removable) {
                            state = EnvironmentCompat.getStorageState(new File(path));
                        } else {
                            //不能移除的存储介质，一直是mounted
                            state = Environment.MEDIA_MOUNTED;
                        }
                    }
                }
                long totalSize = 0;
                long availaleSize = 0;
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    totalSize = getStorageTotalSpace(path);
                    availaleSize = getStorageFreeSpace(path);
                }
                Storage storage = new Storage();
                storage.availaleSize = availaleSize;
                storage.totalSize = totalSize;
                storage.state = state;
                storage.path = path;
                storage.isRemovable = removable;
                storage.description = desc;
                storage.isAllowMassStorage = isAllowMassStorage;
                storage.isPrimary = isPrimary;
                storage.isUsb = desc != null && desc.toLowerCase().contains("usb");
                list.add(storage);
            }
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
