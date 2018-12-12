## 代码托管
[![](https://jitpack.io/v/wandersnail/fileselector.svg)](https://jitpack.io/#wandersnail/fileselector)
[![Download](https://api.bintray.com/packages/wandersnail/android/fileselector/images/download.svg) ](https://bintray.com/wandersnail/android/fileselector/_latestVersion)

# 使用方法

1. module的build.gradle中的添加依赖，自行修改为最新版本，同步后通常就可以用了：
```
dependencies {
	...
	implementation 'com.github.wandersnail:fileselector:1.0.0'
}
```

2. 如果从jcenter下载失败。在project的build.gradle里的repositories添加内容，最好两个都加上，有时jitpack会抽风，同步不下来。添加完再次同步即可。
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
		maven { url 'https://dl.bintray.com/wandersnail/android/' }
	}
}
```
	

    class MainActivity : CheckPermissionsActivity() {
	    private var selector: FileSelector? = null
    
	    override fun onCreate(savedInstanceState: Bundle?) {
	        super.onCreate(savedInstanceState)
	        setContentView(R.layout.activity_main)
	        selector = FileSelector().setScreenOrientation(false)
	                .setFilenameFilter(object : FilenameFilter() {
	            override fun accept(dir: File?, name: String?): Boolean {
	                return name != null && !name.startsWith(".")
	            }
	        })
	        //设置根目录，如果不设置，默认列出所有存储路径作为根目录
	//        selector!!.setRoot(Environment.getExternalStorageDirectory())
	        btnSelectMultiFile.setOnClickListener {
	            selector!!.setMultiSelect(true)
	            selector!!.setSelectFile(true)
	            selector!!.select(this)
	        }
	        btnSelectSingleFile.setOnClickListener {
	            selector!!.setMultiSelect(false)
	            selector!!.setSelectFile(true)
	            selector!!.select(this)
	        }
	        btnSelectSingleDir.setOnClickListener {
	            selector!!.setSelectFile(false)
	            selector!!.setMultiSelect(false)
	            selector!!.select(this)
	        }
	        btnSelectMultiDir.setOnClickListener {
	            selector!!.setMultiSelect(true)
	            selector!!.setSelectFile(false)
	            selector!!.select(this)
	        }
	        selector!!.setOnFileSelectListener {
	            tvResult.text = ""
	            it.forEach {
	                tvResult.append("$it\n")
	            }
	        }
	    }
	
	    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
	        super.onActivityResult(requestCode, resultCode, data)
	        selector?.onActivityResult(requestCode, resultCode, data)
	    }
	}

## 示例效果
![image](https://github.com/wandersnail/fileselector/blob/master/screenshot/device-2018-05-27-165915.png)
![image](https://github.com/wandersnail/fileselector/blob/master/screenshot/device-2018-05-27-170008.png)
![image](https://github.com/wandersnail/fileselector/blob/master/screenshot/device-2018-05-27-170035.png)
![image](https://github.com/wandersnail/fileselector/blob/master/screenshot/device-2018-05-27-162627.png)
