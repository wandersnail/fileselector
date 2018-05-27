# 使用方法

    class MainActivity : CheckPermissionsActivity() {
	    companion object {
	        private const val REQUEST_SELECT_FILE_CODE = 100
	    }
	
	    override fun onCreate(savedInstanceState: Bundle?) {
	        super.onCreate(savedInstanceState)
	        setContentView(R.layout.activity_main)
	        btnSelectMultiFile.setOnClickListener {
	            SelectFileActivity.startForResult(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, REQUEST_SELECT_FILE_CODE,
	                    null, true, true, { dir, name ->
	                !name.startsWith(".")
	            })
	        }
	        btnSelectSingleFile.setOnClickListener {
	            SelectFileActivity.startForResult(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, REQUEST_SELECT_FILE_CODE,
	                    Environment.getExternalStorageDirectory(), true, false, { dir, name ->
	                !name.startsWith(".")
	            })
	        }
	        btnSelectSingleDir.setOnClickListener {
	            SelectFileActivity.startForResult(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, REQUEST_SELECT_FILE_CODE,
	                    null, false, true, { dir, name ->
	                !name.startsWith(".")
	            })
	        }
	        btnSelectMultiDir.setOnClickListener {
	            SelectFileActivity.startForResult(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, REQUEST_SELECT_FILE_CODE,
	                    null, false, true, { dir, name ->
	                !name.startsWith(".")
	            })
	        }
	    }
	
	    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
	        super.onActivityResult(requestCode, resultCode, data)
	        if (requestCode == REQUEST_SELECT_FILE_CODE && resultCode == Activity.RESULT_OK) {
	            val filePaths = data?.getCharSequenceArrayListExtra(SelectFileActivity.EXTRA_SELECTED_FILE_PATH_LIST)
	            tvResult.text = ""
	            filePaths!!.forEach { 
	                tvResult.append("$it\n")
	            }
	        }
	    }
	}
	
## 代码托管
[![JitPack](https://img.shields.io/badge/JitPack-fileselector-green.svg?style=flat)](https://jitpack.io/#fszeng2011/fileselector)
[![Download](https://api.bintray.com/packages/fszeng2017/maven/fileselector/images/download.svg) ](https://bintray.com/fszeng2017/maven/fileselector/_latestVersion)
[![JCenter](https://img.shields.io/badge/JCenter-1.1.1-green.svg?style=flat)](http://jcenter.bintray.com/com/github/fszeng2011/fileselector/1.1.1/)

## 示例效果
![image](https://github.com/fszeng2011/fileselector/blob/master/screenshot/device-2018-05-27-165915.png)
![image](https://github.com/fszeng2011/fileselector/blob/master/screenshot/device-2018-05-27-170008.png)
![image](https://github.com/fszeng2011/fileselector/blob/master/screenshot/device-2018-05-27-170035.png)
![image](https://github.com/fszeng2011/fileselector/blob/master/screenshot/device-2018-05-27-162627.png)
