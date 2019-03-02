package com.snail.fileselector

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.media.ThumbnailUtils
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.util.TypedValue
import androidx.core.content.ContextCompat
import androidx.core.os.EnvironmentCompat
import java.io.File
import java.lang.reflect.Method
import java.util.*

/**
 * Created by zeng on 2017/3/1.
 */

internal object Utils {
    private val videoSuffixs = arrayOf(".avi", ".wmv", ".wmp", ".wm", ".asf", ".mpg", ".mpeg", ".mpe", ".m1v", ".m2v", ".mpv2", ".mp2v", ".ts", ".tp", ".tpr", ".trp", ".vob", ".ifo", ".ogm", "ogv", ".mp4", ".m4v", ".m4p", ".m4b", ".3gp", ".3gpp", ".3g2", ".3gp2", ".mkv", ".rm", ".ram", "rmvb", ".rpm", ".flv", ".swf", ".mov", ".qt", ".amr", ".nsv", ".dpg", ".m2ts", ".m2t", ".mts", "dvr-ms", ".k3g", ".skm", ".evo", ".nsr", ".amv", ".divx", ".webm", ".wtv", ".f4v")
    private val imageSuffixs = arrayOf(".bmp", ".jpg", ".jpeg", ".png", ".gif")
    private val audioSuffixs = arrayOf(".mp3", ".mp2", ".wma", ".wav", ".ape", ".flac", ".ogg", ".m4a", ".m4r", ".aac", ".mid", ".ra")
    private val pptSuffixs = arrayOf(".ppt", ".pot", ".pps", ".pptx", ".pptm", ".ppsx", ".ppsm", ".potx", ".dps", ".potm")
    private val wordSuffixs = arrayOf(".doc", ".docx", ".docm", ".dot", ".dotx", ".dotm", ".rtf", ".tar", ".ace", ".wpt", ".wps")
    private val excelSuffixs = arrayOf(".et", ".csv", ".xl", ".xls", ".xlt", ".xlsx", ".xlsm", ".xlsb", ".xltx", ".xltm", ".xla", ".xlm", ".xlw")
    private val zipSuffixs = arrayOf(".rar", ".zip", ".7z", ".gz", ".arj", ".cab", ".jar", ".tar", ".ace")
    private val psSuffixs = arrayOf(".psd", ".pdd", ".eps", ".psb")
    private val htmlSuffixs = arrayOf(".htm", ".html", ".mht", ".mhtml")
    private val developerSuffixs = arrayOf(".db", ".db-journal", ".db3", ".sqlite", ".xml", ".wdb", ".mdf", ".dbf", ".properties", ".cfg", ".ini", ".sys")

    /**
     * 获取显示屏幕宽度，不包含状态栏和导航栏
     */
    fun getDisplayScreenWidth(context: Context): Int {
        return context.resources.displayMetrics.widthPixels
    }

    /**
     * 获取显示屏幕高度
     */
    fun getDisplayScreenHeight(context: Context): Int {
        return context.resources.displayMetrics.heightPixels
    }

    /**
     * 根据视频生成缩略图
     * @param path 视频的路径
     * @param width 要生成的图片宽度
     * @param height 要生成的图片高度
     */
    fun getVideoThumbnail(path: String, width: Int, height: Int): Bitmap {
        val bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Video.Thumbnails.MINI_KIND)
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象  
        return ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT)
    }

    /**
     * 根据宽高获取缩略图
     * @param source 原图片
     * @param width 宽度
     * @param height 高度
     */
    fun extractThumbnail(source: Bitmap, width: Int, height: Int): Bitmap {
        return ThumbnailUtils.extractThumbnail(source, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT)
    }

    /**
     * 获取Apk文件的图标
     */
    fun getApkThumbnail(context: Context, path: String): Drawable? {
        val pm = context.applicationContext.packageManager
        val packageInfo = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES)
        if (packageInfo != null) {
            val appInfo = packageInfo.applicationInfo
            //获取apk的图标
            appInfo.sourceDir = path
            appInfo.publicSourceDir = path
            return appInfo.loadIcon(pm)
        }
        return null
    }

    /**
     * Drawable转Bitmap
     */
    fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }
        // 取 drawable 的长宽
        val w = drawable.intrinsicWidth
        val h = drawable.intrinsicHeight
        // 取 drawable 的颜色格式
        val config = if (drawable.opacity != PixelFormat.OPAQUE)
            Bitmap.Config.ARGB_8888
        else
            Bitmap.Config.RGB_565
        //建立对应的Bitmap
        val bitmap = Bitmap.createBitmap(w, h, config)
        // 建立对应 bitmap 的画布
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, w, h)
        // 把 drawable 内容画到画布中
        drawable.draw(canvas)
        return bitmap
    }

    private fun isWhat(suffixs: Array<String>, path: String): Boolean {
        for (suffix in suffixs) {
            if (path.toLowerCase().endsWith(suffix)) {
                return true
            }
        }
        return false
    }

    fun getStatusBarHeight(context: Context): Int {
        var result = 0
        val resourceId = context.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = context.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }

    /**
     * 获取存储卡剩余大小
     */
    fun getStorageFreeSpace(path: String?): Long {
        if (path != null) {
            val file = File(path)
            if (file.exists()) {
                val stat = StatFs(path)
                return stat.availableBlocksLong * stat.blockSizeLong
            }
        }
        return 0
    }

    /**
     * 存储卡总容量
     */
    fun getStorageTotalSpace(path: String?): Long {
        if (path != null) {
            val file = File(path)
            if (file.exists()) {
                val stat = StatFs(path)
                return stat.blockSizeLong * stat.blockCountLong
            }
        }
        return 0
    }

    fun getPrimaryColor(context: Context, defaultColor: Int): Int {
        val typedValue = TypedValue()
        var found = false
        try {
            val resId = Class.forName(context.packageName + ".R\$attr").getField("colorPrimary").getInt(null)
            if (resId > 0) {
                found = context.theme.resolveAttribute(resId, typedValue, true)
            }
        } catch (ignored: Exception) {
        }

        return if (found) typedValue.data else defaultColor
    }

    fun getPrimaryDarkColor(context: Context, defaultColor: Int): Int {
        val typedValue = TypedValue()
        var found = false
        try {
            val resId = Class.forName(context.packageName + ".R\$attr").getField("colorPrimaryDark").getInt(null)
            if (resId > 0) {
                found = context.theme.resolveAttribute(resId, typedValue, true)
            }
        } catch (ignored: Exception) {
        }

        return if (found) typedValue.data else defaultColor
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    fun dp2px(context: Context, dpValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun getShape(context: Context, color: Int, strokeWidth: Int, strokeColor: Int): Drawable {
        val drawable = GradientDrawable()
        drawable.setColor(color)
        drawable.cornerRadius = dp2px(context, 30f).toFloat()
        drawable.setStroke(strokeWidth, strokeColor)
        return drawable
    }

    fun getFillBlueBg(context: Context, colors: IntArray): StateListDrawable {
        val pressed = getShape(context, colors[1], 0, colors[1])
        val normal = getShape(context, colors[0], 0, colors[0])
        val disable = getShape(context, ContextCompat.getColor(context, R.color.fsEditHint), 0, ContextCompat.getColor(context, R.color.fsEditHint))
        return createBg(normal, pressed, disable)
    }

    fun getFillGrayBg(context: Context): StateListDrawable {
        val pressed = getShape(context, ContextCompat.getColor(context, R.color.fsEditHintDark), 0, ContextCompat.getColor(context, R.color.fsEditHintDark))
        val normal = getShape(context, ContextCompat.getColor(context, R.color.fsEditHint), 0, ContextCompat.getColor(context, R.color.fsEditHint))
        val disable = getShape(context, ContextCompat.getColor(context, R.color.fsEditHint), 0, ContextCompat.getColor(context, R.color.fsEditHint))
        return createBg(normal, pressed, disable)
    }

    fun getFrameBlueBg(context: Context, color: Int): StateListDrawable {
        val pressed = getShape(context, color, 0, color)
        val normal = getShape(context, ContextCompat.getColor(context, R.color.fsTransparent), dp2px(context, 1f), color)
        return createBg(normal, pressed, null)
    }

    fun createBg(normal: Drawable, pressed: Drawable, disable: Drawable?): StateListDrawable {
        val drawable = StateListDrawable()
        if (disable != null) {
            drawable.addState(intArrayOf(-android.R.attr.state_enabled), disable)
        }
        drawable.addState(intArrayOf(android.R.attr.state_pressed), pressed)
        drawable.addState(intArrayOf(), normal)//normal一定要最后
        return drawable
    }

    /**
     * @param normal  正常时的颜色
     * @param pressed 按压时的颜色
     */
    fun createColorStateList(normal: Int, pressed: Int): ColorStateList {
        //normal一定要最后
        val states = arrayOf(intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled), intArrayOf())
        return ColorStateList(states, intArrayOf(pressed, normal))
    }

    fun getStorages(context: Context): ArrayList<Storage>? {
        try {
            val storageManager = context.applicationContext.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            //得到StorageManager中的getVolumeList()方法的对象
            val getVolumeList = storageManager.javaClass.getMethod("getVolumeList")
            //得到StorageVolume类的对象
            val storageValumeClazz = Class.forName("android.os.storage.StorageVolume")
            //获得StorageVolume中的一些方法
            val getPath = storageValumeClazz.getMethod("getPath")
            val isRemovable = storageValumeClazz.getMethod("isRemovable")
            val allowMassStorage = storageValumeClazz.getMethod("allowMassStorage")
            val primary = storageValumeClazz.getMethod("isPrimary")
            val description = storageValumeClazz.getMethod("getDescription", Context::class.java)

            var mGetState: Method? = null
            //getState 方法是在4.4_r1之后的版本加的，之前版本（含4.4_r1）没有
            // （http://grepcode.com/file/repository.grepcode.com/java/ext/com.google.android/android/4.4_r1/android/os/Environment.java/）
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                try {
                    mGetState = storageValumeClazz.getMethod("getState")
                } catch (e: NoSuchMethodException) {
                    e.printStackTrace()
                }
            }

            //调用getVolumeList方法，参数为：“谁”中调用这个方法
            val invokeVolumeList = getVolumeList.invoke(storageManager)
            val length = java.lang.reflect.Array.getLength(invokeVolumeList)
            val list = ArrayList<Storage>()
            for (i in 0 until length) {
                val storageValume = java.lang.reflect.Array.get(invokeVolumeList, i)//得到StorageVolume对象
                val path = getPath.invoke(storageValume) as? String ?: ""
                val removable = isRemovable.invoke(storageValume) as? Boolean ?: false
                val isAllowMassStorage = allowMassStorage.invoke(storageValume) as? Boolean ?: false
                val isPrimary = primary.invoke(storageValume) as? Boolean ?: false
                val desc = description.invoke(storageValume, context) as? String ?: ""
                val state = if (mGetState != null) {
                    mGetState.invoke(storageValume) as? String
                } else {
                    Environment.getStorageState(File(path))
                }
                var totalSize: Long = 0
                var availaleSize: Long = 0
                if (Environment.MEDIA_MOUNTED == state) {
                    totalSize = getStorageTotalSpace(path)
                    availaleSize = getStorageFreeSpace(path)
                }
                val storage = Storage()
                storage.availaleSize = availaleSize
                storage.totalSize = totalSize
                storage.state = state ?: EnvironmentCompat.MEDIA_UNKNOWN
                storage.path = path
                storage.isRemovable = removable
                storage.description = desc
                storage.isAllowMassStorage = isAllowMassStorage
                storage.isPrimary = isPrimary
                storage.isUsb = desc.toLowerCase(Locale.ENGLISH).contains("usb")
                list.add(storage)
            }
            return list
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun isVideo(path: String): Boolean {
        return isWhat(videoSuffixs, path)
    }

    fun isImage(path: String): Boolean {
        return isWhat(imageSuffixs, path)
    }

    fun isApk(path: String): Boolean {
        return path.toLowerCase().endsWith(".apk")
    }

    fun isAudio(path: String): Boolean {
        return isWhat(audioSuffixs, path)
    }

    fun isText(path: String): Boolean {
        return path.toLowerCase().endsWith(".txt")
    }

    fun isPdf(path: String): Boolean {
        return path.toLowerCase().endsWith(".pdf")
    }

    fun isZip(path: String): Boolean {
        return isWhat(zipSuffixs, path)
    }

    fun isFlash(path: String): Boolean {
        return path.toLowerCase().endsWith(".swf") || path.toLowerCase().endsWith(".fla")
    }

    fun isHtml(path: String): Boolean {
        return isWhat(htmlSuffixs, path)
    }

    fun isPs(path: String): Boolean {
        return isWhat(psSuffixs, path)
    }

    fun isWord(path: String): Boolean {
        return isWhat(wordSuffixs, path)
    }

    fun isExcel(path: String): Boolean {
        return isWhat(excelSuffixs, path)
    }

    fun isPPT(path: String): Boolean {
        return isWhat(pptSuffixs, path)
    }

    fun isDeveloper(path: String): Boolean {
        return isWhat(developerSuffixs, path)
    }
}
