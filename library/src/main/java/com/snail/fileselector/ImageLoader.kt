package com.snail.fileselector

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.LruCache
import android.widget.ImageView
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Semaphore

/**
 * Created by zeng on 2017/3/1.
 */

internal class ImageLoader private constructor() {
    //图片缓存的核心对象
    private var mLruCache: LruCache<String, Bitmap>? = null
    //线程池
    private var mThreadPool: ExecutorService? = null
    //任务队列
    private var mTaskQueue: LinkedList<Runnable>? = null
    private var mPoolThreadHandler: MyHandler? = null
    //UI线程中的Handler
    private var mUIHandler: Handler? = null

    private val mSemaphorePoolThreadHandler = Semaphore(0)
    private var mSemaphoreThreadPool: Semaphore? = null
    //默认的图片
    private var defautBitmap: Bitmap? = null
    private var defautBitmapResId: Int = 0
    //加载错误显示的图片
    private var loadErrorBitmap: Bitmap? = null
    private var loadErrorBitmapResId: Int = 0

    init {
        mUIHandler = Handler()
        initBackThread()
        // 获取我们应用的最大可用内存
        val maxMemory = Runtime.getRuntime().maxMemory().toInt()
        mLruCache = object : LruCache<String, Bitmap>(maxMemory / 8) {
            override fun sizeOf(key: String, value: Bitmap): Int {
                return value.rowBytes * value.height
            }

        }

        // 创建线程池
        mThreadPool = Executors.newFixedThreadPool(DEAFULT_THREAD_COUNT)
        mTaskQueue = LinkedList()
        mSemaphoreThreadPool = Semaphore(DEAFULT_THREAD_COUNT)
    }

    //初始化后台轮询线程
    private fun initBackThread() {
        // 后台轮询线程
        object : Thread() {
            override fun run() {
                Looper.prepare()
                mPoolThreadHandler = MyHandler(this@ImageLoader)
                // 释放一个信号量
                mSemaphorePoolThreadHandler.release()
                Looper.loop()
            }
        }.start()
    }

    private class MyHandler internal constructor(loader: ImageLoader) : Handler() {
        private val weakReference: WeakReference<ImageLoader> = WeakReference(loader)

        override fun handleMessage(msg: Message) {
            val loader = weakReference.get() ?: return
            // 线程池去取出一个任务进行执行
            loader.mThreadPool!!.execute(loader.mTaskQueue!!.removeLast())
            try {
                loader.mSemaphoreThreadPool!!.acquire()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

        }
    }

    fun setDefautImageBitmap(bitmap: Bitmap) {
        defautBitmap = bitmap
        defautBitmapResId = 0
    }

    fun setDefautImageResoure(resid: Int) {
        defautBitmapResId = resid
        defautBitmap = null
    }

    fun setLoadErrorImageBitmap(bitmap: Bitmap) {
        loadErrorBitmap = bitmap
        loadErrorBitmapResId = 0
    }

    fun setLoadErrorImageResoure(resid: Int) {
        loadErrorBitmapResId = resid
        loadErrorBitmap = null
    }

    private fun setErrorImageBitmap(iv: ImageView?) {
        if (iv != null) {
            when {
                loadErrorBitmap != null -> iv.setImageBitmap(loadErrorBitmap)
                loadErrorBitmapResId != 0 -> iv.setImageResource(loadErrorBitmapResId)
                defautBitmap != null -> iv.setImageBitmap(defautBitmap)
                defautBitmapResId != 0 -> iv.setImageResource(defautBitmapResId)
                else -> iv.setImageBitmap(null)
            }
        }
    }

    /**
     * 根据path为imageview设置图片
     *
     * @param path      图片路径
     * @param imageView 显示图片的ImageView
     */
    fun loadImage(path: String, imageView: ImageView) {
        if (TextUtils.isEmpty(path)) {
            if (loadErrorBitmap != null) {
                imageView.setImageBitmap(loadErrorBitmap)
            } else if (loadErrorBitmapResId != 0) {
                imageView.setImageResource(loadErrorBitmapResId)
            }
        } else {
            if (defautBitmap != null) {
                imageView.setImageBitmap(defautBitmap)
            } else if (defautBitmapResId != 0) {
                imageView.setImageResource(defautBitmapResId)
            }
            // 根据path在缓存中获取bitmap
            val bm = getBitmapFromLruCache(path)
            if (bm != null) {
                imageView.setImageBitmap(bm)
            } else {
                addTask(buildTask(imageView, path))
            }
        }
    }

    fun loadImage(resid: Int, imageView: ImageView) {
        imageView.tag = null
        imageView.setImageResource(resid)
    }

    //根据path在缓存中获取bitmap
    private fun getBitmapFromLruCache(key: String): Bitmap? {
        return mLruCache!!.get(key)
    }

    //根据传入的参数，新建一个任务
    private fun buildTask(imageView: ImageView, path: String): Runnable {
        return MyRunnable(imageView, path)
    }

    private inner class MyRunnable internal constructor(iv: ImageView, private val path: String) : Runnable {
        private val ivRef: WeakReference<ImageView>

        init {
            iv.tag = path
            ivRef = WeakReference(iv)
        }

        override fun run() {
            val imageView = ivRef.get()
            if (imageView != null) {
                val bm = loadImageFromLocal(path, imageView)
                // 3、把图片加入到缓存
                addBitmapToLruCache(path, bm)
                refreashBitmap(path, imageView, bm)
                mSemaphoreThreadPool!!.release()
            }
        }
    }

    private fun refreashBitmap(path: String, imageView: ImageView, bm: Bitmap?) {
        mUIHandler!!.post {
            val tag = imageView.tag
            if (tag != null && tag.toString() == path) {
                if (bm != null) {
                    imageView.setImageBitmap(bm)
                } else {
                    setErrorImageBitmap(imageView)
                }
            }
        }
    }

    private fun loadImageFromLocal(path: String, imageView: ImageView): Bitmap? {
        val imageSize = getImageViewSize(imageView)
        return if (Utils.isVideo(path)) {
            Utils.getVideoThumbnail(path, imageSize.width, imageSize.height)
        } else if (Utils.isApk(path)) {
            val b = Utils.drawableToBitmap(Utils.getApkThumbnail(imageView.context, path))
            if (b == null) null else Utils.extractThumbnail(b, imageSize.width, imageSize.height)
        } else {
            decodeSampledBitmapFromPath(path, imageSize.width, imageSize.height)
        }
    }

    //将图片加入LruCache
    private fun addBitmapToLruCache(path: String, bm: Bitmap?) {
        if (getBitmapFromLruCache(path) == null) {
            if (bm != null)
                mLruCache!!.put(path, bm)
        }
    }

    //根据图片需要显示的宽和高对图片进行压缩
    private fun decodeSampledBitmapFromPath(path: String, width: Int, height: Int): Bitmap {
        // 获得图片的宽和高，并不把图片加载到内存中
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(path, options)
        options.inSampleSize = caculateInSampleSize(options, width, height)

        // 使用获得到的InSampleSize再次解析图片
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(path, options)
    }

    @Synchronized
    private fun addTask(runnable: Runnable) {
        mTaskQueue!!.add(runnable)
        try {
            if (mPoolThreadHandler == null)
                mSemaphorePoolThreadHandler.acquire()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        mPoolThreadHandler!!.sendEmptyMessage(0x110)
    }

    private class ImageSize {
        internal var width: Int = 0
        internal var height: Int = 0
    }

    //根据需求的宽和高以及图片实际的宽和高计算SampleSize
    private fun caculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val width = options.outWidth
        val height = options.outHeight
        var inSampleSize = 1
        if (width > reqWidth || height > reqHeight) {
            val widthRadio = Math.round(width * 1.0f / reqWidth)
            val heightRadio = Math.round(height * 1.0f / reqHeight)
            inSampleSize = Math.max(widthRadio, heightRadio)
        }
        return inSampleSize
    }

    internal object Holder {
        val imageLoader = ImageLoader()
    }
    
    companion object {
        private const val DEAFULT_THREAD_COUNT = 1

        val instance: ImageLoader
            get() = Holder.imageLoader

        //通过反射获取imageview的某个属性值
        private fun getImageViewFieldValue(`object`: Any, fieldName: String): Int {
            var value = 0
            try {
                val field = ImageView::class.java.getDeclaredField(fieldName)
                field.isAccessible = true
                val fieldValue = field.getInt(`object`)
                if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
                    value = fieldValue
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return value
        }

        //根据ImageView获适当的压缩的宽和高
        private fun getImageViewSize(imageView: ImageView): ImageSize {
            val imageSize = ImageSize()
            val displayMetrics = imageView.context.resources.displayMetrics
            val lp = imageView.layoutParams
            var width = imageView.width// 获取imageview的实际宽度
            if (width <= 0) {
                width = lp.width// 获取imageview在layout中声明的宽度
            }
            if (width <= 0) {
                width = getImageViewFieldValue(imageView, "mMaxWidth")
            }
            if (width <= 0) {
                width = displayMetrics.widthPixels
            }

            var height = imageView.height// 获取imageview的实际高度
            if (height <= 0) {
                height = lp.height// 获取imageview在layout中声明的宽度
            }
            if (height <= 0) {
                height = getImageViewFieldValue(imageView, "mMaxHeight")// 检查最大值
            }
            if (height <= 0) {
                height = displayMetrics.heightPixels
            }
            imageSize.width = width
            imageSize.height = height
            return imageSize
        }
    }
}
