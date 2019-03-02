package cn.zfs.fileselectorexample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.snail.fileselector.FileSelector
import com.snail.fileselector.Language
import com.snail.fileselector.OnFileSelectListener
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
                .setFilenameFilter(FilenameFilter { _, name ->
                    name != null && !name.startsWith(".")
                })
        //设置根目录，如果不设置，默认列出所有存储路径作为根目录
        selector!!.setRoot(null)
        btnSelectMultiFile.setOnClickListener {
            selector!!.setMultiSelectionEnabled(true)
            selector!!.setSelectionMode(FileSelector.FILES_ONLY)
            selector!!.select(this)
        }
        btnSelectSingleFile.setOnClickListener {
            selector!!.setMultiSelectionEnabled(false)
            selector!!.setLanguage(Language.ENGLISH)
            selector!!.setSelectionMode(FileSelector.FILES_ONLY)
            selector!!.select(this)
        }
        btnSelectSingleDir.setOnClickListener {
            selector!!.setSelectionMode(FileSelector.DIRECTORIES_ONLY)
            selector!!.setLanguage(Language.TRADITIONAL_CHINESE)
            selector!!.setMultiSelectionEnabled(false)
            selector!!.select(this)
        }
        btnSelectMultiDir.setOnClickListener {
            selector!!.setMultiSelectionEnabled(true)
            selector!!.setSelectionMode(FileSelector.DIRECTORIES_ONLY)
            selector!!.select(this)
        }
        btnSelectMultiFileDir.setOnClickListener {
            selector!!.setMultiSelectionEnabled(true)
            selector!!.setSelectionMode(FileSelector.FILES_AND_DIRECTORIES)
            selector!!.select(this)
        }
        btnSelectSingleFileDir.setOnClickListener {
            selector!!.setLanguage(Language.SIMPLIFIED_CHINESE)
            selector!!.setMultiSelectionEnabled(false)
            selector!!.setSelectionMode(FileSelector.FILES_AND_DIRECTORIES)
            selector!!.select(this)
        }
        selector!!.setTitle("文件选择器")
        selector!!.setOnFileSelectListener(object : OnFileSelectListener {
            override fun onFileSelect(paths: List<String>) {
                tvResult.text = ""
                paths.forEach {
                    tvResult.append("$it\n")
                }
            }
        })
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
