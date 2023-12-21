## 推荐一款工具箱【蜗牛工具箱】

> 涵盖广，功能丰富。生活实用、效率办公、图片处理等等，还有隐藏的VIP功能，总之很多惊喜的功能。各大应用市场搜索【蜗牛工具箱】安装即可。

<div align="center">
    <img src="https://tucdn.wpon.cn/2023/12/21/3cea99987b074.png" width=150>
    <img src="https://tucdn.wpon.cn/2023/12/21/d46d124878a87.png" width=150>
    <img src="https://tucdn.wpon.cn/2023/12/21/191d4b5dca4d3.png" width=150>
    <img src="https://tucdn.wpon.cn/2023/12/21/cad80aeb12184.png" width=150>
</div>

**部分功能介绍（持续更新中...）**

- 【滚动字幕】超实用应援打call神器，输入文字内容使文字在屏幕中滚动显示；
- 【振动器】可自定义振动频率、时长，达到各种有意思的效果；
- 【测量仪器】手机当直尺、水平仪、指南针、分贝仪；
- 【文件加解密】可加密任意文件，可用于私密文件分享；
- 【金额转大写】将阿拉伯数字类型的金额转成中文大写；
- 【通信调试】BLE/SPP蓝牙、USB、TCP/UDP/MQTT通信调试；
- 【二维码】调用相机扫描或扫描图片识别二维码，支持解析WiFi二维码获取密码，输入文字生成相应的二维码；
- 【图片模糊处理】将图片进行高斯模糊处理，毛玻璃效果；
- 【黑白图片上色】黑白图片变彩色；
- 【成语词典】查询成语拼音、释义、出处、例句；
- 【图片拼接】支持长图、4宫格、9宫格拼接；
- 【自动点击】自动连点器，解放双手；
- 【图片加水印】图片上添加自定义水印；
- 【网页定时刷新】设定刷新后自动定时刷新网页；
- 【应用管理】查看本机安装的应用详细信息，并可提取安装包分享。

点击下方按钮或扫码下载【蜗牛工具箱】

[![](https://img.shields.io/badge/下载-%E8%9C%97%E7%89%9B%E5%B7%A5%E5%85%B7%E7%AE%B1-red.svg)](https://mobile.baidu.com/item?pid=2067914)

<img src="https://tucdn.wpon.cn/2023/12/21/31d6480011cd8.png" width=150>

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
