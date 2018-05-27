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

![image](https://github.com/fszeng2011/fileselector/blob/master/screenshot/device-2018-05-27-165915.png)
![image](https://github.com/fszeng2011/fileselector/blob/master/screenshot/device-2018-05-27-170008.png)
![image](https://github.com/fszeng2011/fileselector/blob/master/screenshot/device-2018-05-27-170035.png)
![image](https://github.com/fszeng2011/fileselector/blob/master/screenshot/device-2018-05-27-162627.png)
