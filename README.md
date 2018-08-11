# 使用方法
	
	dependencies {
		implementation 'com.github.fszeng2011:fileselector:1.1.8'
	}

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
	
## 代码托管
[![](https://jitpack.io/v/fszeng2011/fileselector.svg)](https://jitpack.io/#fszeng2011/fileselector)
[![Download](https://api.bintray.com/packages/fszeng2017/maven/fileselector/images/download.svg) ](https://bintray.com/fszeng2017/maven/fileselector/_latestVersion)
[![JCenter](https://img.shields.io/badge/JCenter-1.1.5-green.svg?style=flat)](http://jcenter.bintray.com/com/github/fszeng2011/fileselector/1.1.5/)

## 示例效果
![image](https://github.com/fszeng2011/fileselector/blob/master/screenshot/device-2018-05-27-165915.png)
![image](https://github.com/fszeng2011/fileselector/blob/master/screenshot/device-2018-05-27-170008.png)
![image](https://github.com/fszeng2011/fileselector/blob/master/screenshot/device-2018-05-27-170035.png)
![image](https://github.com/fszeng2011/fileselector/blob/master/screenshot/device-2018-05-27-162627.png)
