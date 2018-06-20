package cn.zfs.fileselectorexample

import android.content.Intent
import android.os.Bundle
import cn.zfs.fileselector.FileSelector
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : CheckPermissionsActivity() {
    private var selector: FileSelector? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        selector = FileSelector().setScreenOrientation(false)
                .setFilenameFilter { _, name ->
                    name != null && !name.startsWith(".")
                }
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
        selector!!.setTitle("test")
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
