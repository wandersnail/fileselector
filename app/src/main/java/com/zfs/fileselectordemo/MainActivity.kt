package com.zfs.fileselectordemo

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import com.zfs.fileselector.SelectFileActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : CheckPermissionsActivity() {
    companion object {
        private const val REQUEST_SELECT_FILE_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnSelect.setOnClickListener {
            SelectFileActivity.startForResult(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, REQUEST_SELECT_FILE_CODE,
                    Environment.getExternalStorageDirectory(), true, false, { dir, name ->
                !name.startsWith(".") && (File(dir, name).isDirectory || name.endsWith(".bin", true))
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SELECT_FILE_CODE && resultCode == Activity.RESULT_OK) {
            val f = data?.getSerializableExtra(SelectFileActivity.EXTRA_SELECTED_FILE) as File
            Toast.makeText(this, f.absolutePath, Toast.LENGTH_SHORT).show()
        }
    }
}
