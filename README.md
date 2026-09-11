## 推荐一款蓝牙调试APP【蓝牙工坊】

> 面向硬件工程师、嵌入式开发者、电子爱好者的蓝牙调试工具，同时支持BLE低功耗蓝牙与SPP串口蓝牙，一站式完成蓝牙设备测试。各大应用市场搜索【蓝牙工坊】安装即可。

<div align="center">
    <img src="https://s41.ax1x.com/2026/09/11/pneuhmd.png" width=150>
    <img src="https://s41.ax1x.com/2026/09/11/pneuWOH.png" width=150>
    <img src="https://s41.ax1x.com/2026/09/11/pneu5TI.png" width=150>
    <img src="https://s41.ax1x.com/2026/09/11/pneu40A.png" width=150>
</div>

✅ 实时日志打印，数据秒级展示，支持日志持久化本地保存，调试过程数据不丢失；

✅ 支持多蓝牙设备同时连接，并行收发数据；

✅ 自定义快捷按键，预设指令，一键下发写入数据，省去重复输入；

✅ 日志一键导出、分享，快速保存调试记录，方便复盘、联调沟通；

适合蓝牙模块、单片机、传感器、智能硬件开发调试，无需电脑，手机即可快速完成蓝牙收发调试。

点击下方按钮【蓝牙工坊】

[![](https://img.shields.io/badge/下载-%E8%93%9D%E7%89%99%E5%B7%A5%E5%9D%8A-blue.svg)](https://www.pgyer.com/lanyagongfang)

----------------------------------------------

# 文件选择器使用说明

## 代码托管
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/cn.wandersnail/file-selector/badge.svg)](https://maven-badges.herokuapp.com/maven-central/cn.wandersnail/file-selector)
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
