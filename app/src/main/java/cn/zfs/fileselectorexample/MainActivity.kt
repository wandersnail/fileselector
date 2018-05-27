package cn.zfs.fileselectorexample

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Environment
import cn.zfs.fileselector.SelectFileActivity
import kotlinx.android.synthetic.main.activity_main.*

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
                    null, false, false, { dir, name ->
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
