package com.snail.fileselector

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import java.util.*

/**
 * Created by zeng on 2017/3/2.
 */

internal class SelectedItemDialog(private val activity: SelectFileActivity) : Dialog(activity) {
    private var tvTitle: TextView? = null
    private var adapter: FileListAdapter? = null
    private var itemList: List<Item> = ArrayList()

    init {
        window?.requestFeature(Window.FEATURE_NO_TITLE)
        //沉浸状态栏
        window?.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window?.decorView?.setPadding(0, 0, 0, 0)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setWindowAnimations(R.style.FsDialogAnimFromBottom)
        val lp = window?.attributes
        lp?.width = WindowManager.LayoutParams.MATCH_PARENT
        lp?.height = WindowManager.LayoutParams.MATCH_PARENT
        window?.attributes = lp
        setContentView(inflateView(activity))
    }

    private fun inflateView(context: Context): View {
        val view = View.inflate(context, R.layout.fs_dialog_selected_item, null)
        val layoutTitle = view.findViewById<View>(R.id.fsLayoutTitle)
        layoutTitle.setBackgroundColor(activity.themeColors!![0])
        tvTitle = view.findViewById(R.id.fsTvTitle)
        tvTitle?.text = String.format(activity.textHolder.getText(TextHolder.SELECTED_ITEM_PATTERN), itemList.size)
        val statusBar = view.findViewById<View>(R.id.fsStatusBar)
        statusBar.setBackgroundColor(activity.themeColors!![0])
        val params = statusBar.layoutParams
        params.height = Utils.getStatusBarHeight(context)
        statusBar.layoutParams = params
        val tvClose = view.findViewById<TextView>(R.id.fsTvClose)
        tvClose?.text = activity.textHolder.getText(TextHolder.CLOSE)
        tvClose?.setOnClickListener { dismiss() }
        val tvClear = view.findViewById<TextView>(R.id.fsTvClear)
        tvClear?.text = activity.textHolder.getText(TextHolder.CLEAR)
        tvClear?.setOnClickListener {
            activity.clearSelectedFileList()
        }
        adapter = FileListAdapter()
        val lv = view.findViewById<ListView>(R.id.fsLv)
        lv.adapter = adapter
        return view
    }

    fun updateList(selectItemList: List<Item>) {
        itemList = selectItemList
        tvTitle!!.text = String.format(activity.textHolder.getText(TextHolder.SELECTED_ITEM_PATTERN), itemList.size)
        adapter!!.notifyDataSetChanged()
    }

    private inner class FileListAdapter internal constructor() : BaseAdapter() {

        init {
            ImageLoader.instance.setDefautImageResoure(R.drawable.fs_file)
            ImageLoader.instance.setLoadErrorImageResoure(R.drawable.fs_file)
        }

        override fun getCount(): Int {
            return itemList.size
        }

        override fun getItem(position: Int): Item {
            return itemList[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            val holder: ViewHolder
            if (view == null) {
                view = View.inflate(context, R.layout.fs_file_item_view, null)
                holder = ViewHolder()
                view!!.tag = holder
                holder.tvName = view.findViewById(R.id.fsTvName)
                holder.tvDesc = view.findViewById(R.id.fsTvDesc)
                holder.iv = view.findViewById(R.id.iv)
                holder.ivSelect = view.findViewById(R.id.fsIvSelect)
                holder.ivSelect?.visibility = View.INVISIBLE
                holder.chkView = view.findViewById(R.id.fsChkView)
                holder.chkView!!.tag = holder
                holder.chkView!!.setOnClickListener { v ->
                    val h = v.tag as ViewHolder
                    val item = getItem(h.position)
                    item.checked = false
                    activity.updateSelectedFileList(item, true)
                }
            } else {
                holder = view.tag as ViewHolder
            }
            val item = getItem(position)
            holder.position = position
            holder.tvName!!.text = item.file!!.name
            val path = item.file!!.absolutePath
            if (item.file!!.isDirectory) {
                holder.tvDesc!!.visibility = View.GONE
                ImageLoader.instance.loadImage(R.drawable.fs_folder, holder.iv!!)
            } else {
                holder.tvDesc!!.text = item.file!!.parentFile.absolutePath
                if (Utils.isApk(path) || Utils.isImage(path) || Utils.isVideo(path)) {
                    ImageLoader.instance.loadImage(path, holder.iv!!)
                } else if (Utils.isAudio(path)) {
                    ImageLoader.instance.loadImage(R.drawable.fs_audio, holder.iv!!)
                } else if (Utils.isText(path)) {
                    ImageLoader.instance.loadImage(R.drawable.fs_text, holder.iv!!)
                } else if (Utils.isPdf(path)) {
                    ImageLoader.instance.loadImage(R.drawable.fs_pdf, holder.iv!!)
                } else if (Utils.isExcel(path)) {
                    ImageLoader.instance.loadImage(R.drawable.fs_excel, holder.iv!!)
                } else if (Utils.isWord(path)) {
                    ImageLoader.instance.loadImage(R.drawable.fs_word, holder.iv!!)
                } else if (Utils.isPPT(path)) {
                    ImageLoader.instance.loadImage(R.drawable.fs_ppt, holder.iv!!)
                } else if (Utils.isZip(path)) {
                    ImageLoader.instance.loadImage(R.drawable.fs_zip, holder.iv!!)
                } else if (Utils.isFlash(path)) {
                    ImageLoader.instance.loadImage(R.drawable.fs_flash, holder.iv!!)
                } else if (Utils.isPs(path)) {
                    ImageLoader.instance.loadImage(R.drawable.fs_ps, holder.iv!!)
                } else if (Utils.isHtml(path)) {
                    ImageLoader.instance.loadImage(R.drawable.fs_html, holder.iv!!)
                } else if (Utils.isDeveloper(path)) {
                    ImageLoader.instance.loadImage(R.drawable.fs_developer, holder.iv!!)
                } else {
                    ImageLoader.instance.loadImage(R.drawable.fs_file, holder.iv!!)
                }
            }
            holder.ivSelect!!.isSelected = item.checked
            if (item.checked) {
                holder.ivSelect!!.setColorFilter(activity.themeColors!![0])
            } else {
                holder.ivSelect!!.setColorFilter(Color.LTGRAY)
            }
            return view
        }
    }
}
