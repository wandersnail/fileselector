## 代码托管
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/cn.wandersnail/file-selector/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cn.wandersnail/file-selector)
[![Download](https://api.bintray.com/packages/wandersnail/androidx/file-selector/images/download.svg) ](https://bintray.com/wandersnail/androidx/file-selector/_latestVersion)
[![](https://img.shields.io/badge/源码-github-blue.svg)](https://github.com/wandersnail/fileselector)
[![](https://img.shields.io/badge/源码-码云-blue.svg)](https://gitee.com/fszeng/fileselector)
# 使用方法

1. module的build.gradle中的添加依赖，自行修改为最新版本，同步后通常就可以用了：
```
dependencies {
	...
	implementation 'cn.wandersnail:file-selector:latestVersion'
	implementation 'com.github.bumptech.glide:glide:latestVersion'
	implementation 'cn.wandersnail:common-base:latestVersion'
	implementation 'cn.wandersnail:common-utils:latestVersion'
}
```

2. 如果从jcenter下载失败。在project的build.gradle里的repositories添加内容，最好两个都加上，添加完再次同步即可。
```
allprojects {
	repositories {
		...
		mavenCentral()
		maven { url 'https://dl.bintray.com/wandersnail/androidx/' }
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
![image](https://s2.ax1x.com/2020/02/29/3sonAI.png)
![image](https://s2.ax1x.com/2020/02/29/3soeHA.png)
![image](https://s2.ax1x.com/2020/02/29/3soZBd.png)
![image](https://s2.ax1x.com/2020/02/29/3soVnH.png)
