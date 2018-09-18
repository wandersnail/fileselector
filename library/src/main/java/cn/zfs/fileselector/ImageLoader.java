package cn.zfs.fileselector;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * Created by zeng on 2017/3/1.
 */

class ImageLoader {
	private static ImageLoader mInstance;
	//图片缓存的核心对象
	private LruCache<String, Bitmap> mLruCache;
	//线程池
	private ExecutorService mThreadPool;
	private static final int DEAFULT_THREAD_COUNT = 1;
	//任务队列
	private LinkedList<Runnable> mTaskQueue;
	private MyHandler mPoolThreadHandler;
	//UI线程中的Handler
	private Handler mUIHandler;

	private Semaphore mSemaphorePoolThreadHandler = new Semaphore(0);
	private Semaphore mSemaphoreThreadPool;
    //默认的图片
    private Bitmap defautBitmap;
    private int defautBitmapResId;
    //加载错误显示的图片
    private Bitmap loadErrorBitmap;
    private int loadErrorBitmapResId;

	private ImageLoader(int threadCount) {
		init(threadCount);
	}

	private void init(int threadCount) {
		mUIHandler = new Handler();
		initBackThread();
		// 获取我们应用的最大可用内存
		int maxMemory = (int) Runtime.getRuntime().maxMemory();
		mLruCache = new LruCache<String, Bitmap>(maxMemory / 8) {
			@Override
			protected int sizeOf(String key, Bitmap value) {
				return value.getRowBytes() * value.getHeight();
			}

		};

		// 创建线程池
		mThreadPool = Executors.newFixedThreadPool(threadCount);
		mTaskQueue = new LinkedList<>();
		mSemaphoreThreadPool = new Semaphore(threadCount);
	}

	//初始化后台轮询线程
	private void initBackThread() {
		// 后台轮询线程
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				mPoolThreadHandler = new MyHandler(ImageLoader.this);
				// 释放一个信号量
				mSemaphorePoolThreadHandler.release();
				Looper.loop();
			}
		}.start();
	}

	private static class MyHandler extends Handler {
		private WeakReference<ImageLoader> weakReference;

		MyHandler(ImageLoader loader) {
			weakReference = new WeakReference<>(loader);
		}

		@Override
		public void handleMessage(Message msg) {
			ImageLoader loader = weakReference.get();
			if (loader == null) {
				return;
			}
			// 线程池去取出一个任务进行执行
			loader.mThreadPool.execute(loader.mTaskQueue.removeLast());
			try {
				loader.mSemaphoreThreadPool.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	static ImageLoader getInstance() {
		if (mInstance == null) {
			synchronized (ImageLoader.class) {
				if (mInstance == null) {
					mInstance = new ImageLoader(DEAFULT_THREAD_COUNT);
				}
			}
		}
		return mInstance;
	}

    void setDefautImageBitmap(Bitmap bitmap) {
        defautBitmap = bitmap;
        defautBitmapResId = 0;
    }

    void setDefautImageResoure(int resid) {
        defautBitmapResId = resid;
        defautBitmap = null;
    }

    void setLoadErrorImageBitmap(Bitmap bitmap) {
        loadErrorBitmap = bitmap;
        loadErrorBitmapResId = 0;
    }

    void setLoadErrorImageResoure(int resid) {
        loadErrorBitmapResId = resid;
        loadErrorBitmap = null;
    }
    
    private void setErrorImageBitmap(ImageView iv) {
        if (iv != null) {
            if (loadErrorBitmap != null) {
                iv.setImageBitmap(loadErrorBitmap);
            } else if (loadErrorBitmapResId != 0) {
                iv.setImageResource(loadErrorBitmapResId);
            } else if (defautBitmap != null) {
                iv.setImageBitmap(defautBitmap);
            } else if (defautBitmapResId != 0) {
                iv.setImageResource(defautBitmapResId);
            } else {
                iv.setImageBitmap(null);
            }
        }
    }
    
	/**
	 * 根据path为imageview设置图片
	 *
	 * @param path      图片路径
	 * @param imageView 显示图片的ImageView
	 */
	void loadImage(String path, ImageView imageView) {
        if (TextUtils.isEmpty(path)) {
            if (loadErrorBitmap != null) {
                imageView.setImageBitmap(loadErrorBitmap);
            } else if (loadErrorBitmapResId != 0) {
                imageView.setImageResource(loadErrorBitmapResId);
            }
        } else {
            if (defautBitmap != null) {
                imageView.setImageBitmap(defautBitmap);
            } else if (defautBitmapResId != 0) {
                imageView.setImageResource(defautBitmapResId);
            }
            // 根据path在缓存中获取bitmap
            Bitmap bm = getBitmapFromLruCache(path);
            if (bm != null) {
                imageView.setImageBitmap(bm);
            } else {
                addTask(buildTask(imageView, path));
            }
        }
	}
	
	void loadImage(int resid, ImageView imageView) {
	    imageView.setTag(null);
	    imageView.setImageResource(resid);
    }

	//根据path在缓存中获取bitmap
	private Bitmap getBitmapFromLruCache(String key) {
		return mLruCache.get(key);
	}

	//根据传入的参数，新建一个任务
	private Runnable buildTask(final ImageView imageView, final String path) {
		return new MyRunnable(imageView, path);
	}

	private class MyRunnable implements Runnable {
		private WeakReference<ImageView> ivRef;
		private String path;

		MyRunnable(ImageView iv, String path) {
			iv.setTag(path);
			ivRef = new WeakReference<>(iv);
			this.path = path;
		}

		@Override
		public void run() {
			ImageView imageView = ivRef.get();
			if (imageView != null) {
				Bitmap bm = loadImageFromLocal(path, imageView);
				// 3、把图片加入到缓存
				addBitmapToLruCache(path, bm);
				refreashBitmap(path, imageView, bm);
				mSemaphoreThreadPool.release();
			}
		}
	}

	private void refreashBitmap(final String path, final ImageView imageView, final Bitmap bm) {
		mUIHandler.post(new Runnable() {
			@Override
			public void run() {
				Object tag = imageView.getTag();
			    if (tag != null && tag.toString().equals(path)) {
					if (bm != null) {
						imageView.setImageBitmap(bm);
					} else {
                        setErrorImageBitmap(imageView);
                    }
				}
			}
		});
	}

	private Bitmap loadImageFromLocal(final String path, final ImageView imageView) {
		Bitmap bm;
		ImageSize imageSize = getImageViewSize(imageView);
		if (Utils.isVideo(path)) {
			bm = Utils.getVideoThumbnail(path, imageSize.width, imageSize.height);
		} else if (Utils.isApk(path)) {
            Bitmap b = Utils.drawableToBitmap(Utils.getApkThumbnail(imageView.getContext(), path));
			bm = b == null ? null : Utils.extractThumbnail(b, imageSize.width, imageSize.height);
		} else {
			bm = decodeSampledBitmapFromPath(path, imageSize.width, imageSize.height);
		}
		return bm;
	}

	//将图片加入LruCache
	private void addBitmapToLruCache(String path, Bitmap bm) {
		if (getBitmapFromLruCache(path) == null) {
			if (bm != null)
				mLruCache.put(path, bm);
		}
	}

	//根据图片需要显示的宽和高对图片进行压缩
	private Bitmap decodeSampledBitmapFromPath(String path, int width, int height) {
		// 获得图片的宽和高，并不把图片加载到内存中
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		options.inSampleSize = caculateInSampleSize(options, width, height);

		// 使用获得到的InSampleSize再次解析图片
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

	private synchronized void addTask(Runnable runnable) {
		mTaskQueue.add(runnable);
		try {
			if (mPoolThreadHandler == null)
				mSemaphorePoolThreadHandler.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		mPoolThreadHandler.sendEmptyMessage(0x110);
	}

	//通过反射获取imageview的某个属性值
	private static int getImageViewFieldValue(Object object, String fieldName) {
		int value = 0;
		try {
			Field field = ImageView.class.getDeclaredField(fieldName);
			field.setAccessible(true);
			int fieldValue = field.getInt(object);
			if (fieldValue > 0 && fieldValue < Integer.MAX_VALUE) {
				value = fieldValue;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return value;
	}

	//根据ImageView获适当的压缩的宽和高
	private static ImageSize getImageViewSize(ImageView imageView) {
		ImageSize imageSize = new ImageSize();
		DisplayMetrics displayMetrics = imageView.getContext().getResources().getDisplayMetrics();
		ViewGroup.LayoutParams lp = imageView.getLayoutParams();
		int width = imageView.getWidth();// 获取imageview的实际宽度
		if (width <= 0) {
			width = lp.width;// 获取imageview在layout中声明的宽度
		}
		if (width <= 0) {
			width = getImageViewFieldValue(imageView, "mMaxWidth");
		}
		if (width <= 0) {
			width = displayMetrics.widthPixels;
		}

		int height = imageView.getHeight();// 获取imageview的实际高度
		if (height <= 0) {
			height = lp.height;// 获取imageview在layout中声明的宽度
		}
		if (height <= 0) {
			height = getImageViewFieldValue(imageView, "mMaxHeight");// 检查最大值
		}
		if (height <= 0) {
			height = displayMetrics.heightPixels;
		}
		imageSize.width = width;
		imageSize.height = height;
		return imageSize;
	}

	private static class ImageSize {
		int width;
		int height;
	}

	//根据需求的宽和高以及图片实际的宽和高计算SampleSize
	private int caculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		int width = options.outWidth;
		int height = options.outHeight;
		int inSampleSize = 1;
		if (width > reqWidth || height > reqHeight) {
			int widthRadio = Math.round(width * 1.0f / reqWidth);
			int heightRadio = Math.round(height * 1.0f / reqHeight);
			inSampleSize = Math.max(widthRadio, heightRadio);
		}
		return inSampleSize;
	}
}
