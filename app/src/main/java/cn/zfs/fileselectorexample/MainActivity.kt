package cn.zfs.fileselectorexample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import cn.wandersnail.fileselector.FileSelector
import cn.wandersnail.fileselector.Language
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FilenameFilter

class MainActivity : CheckPermissionsActivity() {
    private var selector: FileSelector? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppThemeGreen)
        setContentView(R.layout.activity_main)
        selector = FileSelector().setScreenOrientation(false)
                .setThemeColor(getColorByAttrId(this, R.attr.colorPrimary), getColorByAttrId(this, R.attr.colorPrimaryDark))
                .showHiddenFiles(false)
                .setFilenameFilter(FilenameFilter { dir, name -> !name.endsWith(".txt") })
        //设置根目录，如果不设置，默认为内部存储，设置null列出所有存储路径作为根目录
//        selector!!.setRoot(null)
        btnSelectMultiFile.setOnClickListener {
            selector!!.setMultiSelectionEnabled(true)
            selector!!.setSelectionMode(FileSelector.FILES_ONLY)
            selector!!.select(this, 1)
        }
        btnSelectSingleFile.setOnClickListener {
            selector!!.setMultiSelectionEnabled(false)
            selector!!.setLanguage(Language.ENGLISH)
            selector!!.setSelectionMode(FileSelector.FILES_ONLY)
            selector!!.select(this, 2)
        }
        btnSelectSingleDir.setOnClickListener {
            selector!!.setSelectionMode(FileSelector.DIRECTORIES_ONLY)
//            selector!!.setLanguage(Language.TRADITIONAL_CHINESE)
            selector!!.setScreenOrientation(true)
            selector!!.setRoot(null)
            selector!!.setMultiSelectionEnabled(false)
            selector!!.select(this, 3)
        }
        btnSelectMultiDir.setOnClickListener {
            selector!!.setMultiSelectionEnabled(true)
            selector!!.setSelectionMode(FileSelector.DIRECTORIES_ONLY)
            selector!!.select(this, 4)
        }
        btnSelectMultiFileDir.setOnClickListener {
            selector!!.setMultiSelectionEnabled(true)
            selector!!.setSelectionMode(FileSelector.FILES_AND_DIRECTORIES)
            selector!!.select(this, 5)
        }
        btnSelectSingleFileDir.setOnClickListener {
            selector!!.setLanguage(Language.SIMPLIFIED_CHINESE)
            selector!!.setMultiSelectionEnabled(false)
            selector!!.setSelectionMode(FileSelector.FILES_AND_DIRECTORIES)
            selector!!.select(this, 6)
        }
        selector!!.setTitle("文件选择器")
        selector!!.setOnFileSelectListener { requestCode, paths ->
            Log.d("Main", "requestCode: $requestCode")
            tvResult.text = ""
            paths.forEach {
                tvResult.append("$it\n")
            }
        }
    }

    fun getColorByAttrId(context: Context, attr: Int): Int {
        val typedArray = context.obtainStyledAttributes(intArrayOf(attr))
        val color = typedArray.getColor(0, -0x1000000)
        typedArray.recycle()
        return color
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        selector?.onActivityResult(requestCode, resultCode, data)
    }
}
