package com.snail.fileselector

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fs_activity_select_file.*
import java.io.File
import java.io.FilenameFilter
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by zeng on 2017/3/1.
 */

class SelectFileActivity : Activity(), AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {
    private var selectionMode = FileSelector.FILES_ONLY
    private var isMultiSelect: Boolean = false
    private val posList = ArrayList<IntArray>()
    private val itemList = ArrayList<Item>()
    private val selectItemList = ArrayList<Item>()
    private var isSelectedAll: Boolean = false
    private var rootFile: File? = null
    private var adapter: FileListAdapter? = null
    private var selectedItemDialog: SelectedItemDialog? = null
    private var currentPath: String? = null//当前路径
    private val rootFiles = ArrayList<File>()
    private var title: String? = null//标题
    var themeColors: IntArray? = null
        private set
    internal val textHolder = TextHolder()
    private var showHiddenFiles = false//设置是否显示隐藏文件和文件夹

    private val comparator = Comparator<Item> { o1, o2 ->
        if (o1 == null) {
            return@Comparator -1
        }
        if (o2 == null) {
            1
        } else o1.file!!.name.compareTo(o2.file!!.name, ignoreCase = true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //沉浸状态栏
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.requestFeature(Window.FEATURE_NO_TITLE)
        getDataFromIntent()
        setContentView(R.layout.fs_activity_select_file)
        //先检查写权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), PERMISSON_REQUESTCODE)
        } else {
            initViews()
            initEvents()
        }
    }

    //检测是否所有的权限都已经授权
    private fun verifyPermissions(grantResults: IntArray): Boolean {
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, paramArrayOfInt: IntArray) {
        if (requestCode == PERMISSON_REQUESTCODE) {
            if (verifyPermissions(paramArrayOfInt)) {
                initViews()
            } else {
                finish()
            }
        }
    }

    override fun onDestroy() {
        filenameFilter = null
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (!posList.isEmpty()) {
            val child = fsDirContainer.getChildAt(fsDirContainer.childCount - 1)
            val tv = child.findViewById<TextView>(R.id.fsTv)
            val cell = tv.tag as DirCell
            fsDirContainer.removeView(child)
            if (rootFiles.isEmpty()) {
                loadFiles(cell.location.parentFile)
            } else {
                var isSecondLast = false
                for (file in rootFiles) {
                    if (cell.location == file) {
                        isSecondLast = true
                        loadFiles(null)
                        break
                    }
                }
                if (!isSecondLast) {
                    loadFiles(cell.location.parentFile)
                }
            }
            val ints = posList.removeAt(posList.size - 1)
            fsLv.setSelectionFromTop(ints[0], ints[1])
        } else if (rootFile == null && currentPath != null) {
            loadFiles(null)
        } else {
            super.onBackPressed()
        }
    }

    private fun addDir(file: File?) {
        val view = layoutInflater.inflate(R.layout.fs_dir_view, null)
        val tv = view.findViewById<TextView>(R.id.fsTv)        
        val childCount = fsDirContainer.childCount
        tv.tag = DirCell(childCount, file!!)
        tv.text = file.name
        tv.setOnClickListener {
            val cell = it.tag as DirCell
            //把当前之后的移除
            val count = fsDirContainer.childCount
            if (cell.index < count - 1) {
                fsDirContainer.removeViews(cell.index + 1, count - cell.index - 1)
            }
            //清除之后的位置记录
            var ints = IntArray(2)
            for (i in posList.indices.reversed()) {
                if (cell.index < i) {
                    ints = posList.removeAt(i)
                } else {
                    break
                }
            }
            //更新列表
            loadFiles(cell.location)
            fsLv.setSelectionFromTop(ints[0], ints[1])
        }
        fsDirContainer.addView(view)
    }

    private fun initViews() {
        fsIvAll.setColorFilter(ContextCompat.getColor(this, R.color.fsDisable))
        fsTvRoot.text = textHolder.getText(TextHolder.ROOT)
        fsTvCancel.text = textHolder.getText(TextHolder.CANCEL)
        fsTvOk.text = textHolder.getText(TextHolder.OK)
        
        fsStatusBar.setBackgroundColor(themeColors!![0])
        fsLayoutTitle.setBackgroundColor(themeColors!![0])
        val params = fsStatusBar.layoutParams
        params.height = Utils.getStatusBarHeight(this)
        fsStatusBar.layoutParams = params
        if (title == null) {
            fsTvTitle.text = textHolder.getText(TextHolder.ALL_FILES)
        } else {
            fsTvTitle.text = title
        }
        fsTvSelected.background = Utils.getFrameBlueBg(this, themeColors!![0])
        fsTvSelected.setTextColor(Utils.createColorStateList(themeColors!![0], Color.WHITE))
        fsTvOk.background = Utils.getFillBlueBg(this, themeColors!!)
        fsTvCancel.background = Utils.getFillGrayBg(this)
        fsTvOk.isEnabled = false
        updateSelectedText()
        selectedItemDialog = SelectedItemDialog(this)
        adapter = FileListAdapter()
        fsLv.adapter = adapter
        fsLv.onItemClickListener = this
        fsLv.onItemLongClickListener = this
        loadFiles(rootFile)
    }
    
    private fun initEvents() {
        fsIvBack.setOnClickListener {
            onBackPressed()
        }
        fsIvMore.setOnClickListener {
            showPopupWindow()
        }
        fsIvAll.setOnClickListener {
            switchSelectAll(!isSelectedAll)
        }
        fsTvSelected.setOnClickListener {
            selectedItemDialog!!.updateList(selectItemList)
            selectedItemDialog!!.show()
        }
        fsTvRoot.setOnClickListener {
            if (rootFile != null) {
                if (fsDirContainer.childCount > 0) {
                    loadFiles(rootFile)
                    val ints = posList.removeAt(0)
                    fsLv.setSelectionFromTop(ints[0], ints[1])
                    posList.clear()
                    fsDirContainer.removeAllViews()
                }
            } else {
                loadFiles(null)
                posList.clear()
                fsDirContainer.removeAllViews()
            }
        }
        fsTvCancel.setOnClickListener {
            finish()
        }
        fsTvOk.setOnClickListener {
            val intent = Intent()
            val pathList = ArrayList<String>()
            if (isMultiSelect) {
                for (item in selectItemList) {
                    pathList.add(item.file!!.absolutePath)
                }
                if (pathList.isEmpty() && currentPath != null) {
                    pathList.add(currentPath!!)
                }
            } else {
                if (!selectItemList.isEmpty()) {
                    pathList.add(selectItemList[0].file!!.absolutePath)
                } else if (currentPath != null) {
                    pathList.add(currentPath!!)
                }
            }
            intent.putExtra(EXTRA_SELECTED_FILE_PATH_LIST, pathList)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun getDataFromIntent() {
        val language = intent.getIntExtra(EXTRA_LANGUAGE, -1)
        if (language != -1) {
            val lan = Language.values().firstOrNull { it.value == language }
            if (lan != null) {
                textHolder.language = lan
            }
        }
        selectionMode = intent.getIntExtra(EXTRA_SELECTION_MODE, FileSelector.FILES_ONLY)
        isMultiSelect = intent.getBooleanExtra(EXTRA_IS_MULTI_SELECT, false)
        requestedOrientation = if (intent.getBooleanExtra(EXTRA_IS_LANDSCAPE, false)) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
        rootFile = intent.getSerializableExtra(EXTRA_ROOT) as? File
        if (rootFile == null) {
            if (ShellUtils.hasRootPermission()) {
                rootFile = File("/")
            }
        }
        title = intent.getStringExtra(EXTRA_TITLE)
        val themeColors = intent.getIntArrayExtra(EXTRA_THEME_COLORS)
        if (themeColors == null) {
            this.themeColors = intArrayOf(Utils.getPrimaryColor(this, ContextCompat.getColor(this, R.color.fsColorPrimary)), Utils.getPrimaryColor(this, ContextCompat.getColor(this, R.color.fsColorPrimaryDark)))
        } else {
            this.themeColors = themeColors
        }
        showHiddenFiles = intent.getBooleanExtra(EXTRA_SHOW_HIDDEN_FILES, false)
    }

    private fun loadFiles(dir: File?) {
        currentPath = dir?.absolutePath
        itemList.clear()
        val dirList = ArrayList<Item>()
        val fList = ArrayList<Item>()
        if (dir == null) {
            rootFiles.clear()
            val list = Utils.getStorages(this)
            if (list != null) {
                for (storage in list) {
                    val file = File(storage.path)
                    rootFiles.add(file)
                    dirList.add(Item(file, isSelectedItem(file), storage.description))
                }
            }
        } else {
            dir.listFiles()?.forEach { file ->
                if (showHiddenFiles && file.name.startsWith(".") && ((file.isDirectory && selectionMode != FileSelector.FILES_ONLY) ||
                                (file.isFile && selectionMode != FileSelector.DIRECTORIES_ONLY))) {
                    handleFileList(file, dirList, fList)
                } else if (!file.name.startsWith(".") && (filenameFilter == null || filenameFilter!!.accept(file, file.name))) {
                    handleFileList(file, dirList, fList)
                }
            }
        }
        Collections.sort(dirList, comparator)
        Collections.sort(fList, comparator)
        itemList.addAll(dirList)
        itemList.addAll(fList)

        val ivAllLp = fsIvAll.layoutParams as RelativeLayout.LayoutParams
        if (dir == null) {
            fsIvMore.visibility = View.GONE
            ivAllLp.addRule(RelativeLayout.ALIGN_PARENT_END)
        } else {
            fsIvMore.visibility = View.VISIBLE
            ivAllLp.removeRule(RelativeLayout.ALIGN_PARENT_END)
        }
        fsIvAll.layoutParams = ivAllLp
        //只有多选，并且当选择文件时，文件列表不为空，当选择文件夹时，文件夹列表不为空
        fsIvAll.visibility = if (isMultiSelect && (selectionMode == FileSelector.FILES_ONLY && fList.isNotEmpty() || 
                        (selectionMode == FileSelector.DIRECTORIES_ONLY && dirList.isNotEmpty()) || 
                        (selectionMode == FileSelector.FILES_AND_DIRECTORIES && (fList.isNotEmpty() || dirList.isNotEmpty())))) {
            View.VISIBLE
        } else {
            View.INVISIBLE
        }
        if (fsIvAll.visibility == View.VISIBLE && selectItemList.containsAll(itemList)) {
            switchSelectAllState(true)
        }
        val file = File(if (currentPath == null) "" else currentPath)
        fsTvOk.isEnabled = !selectItemList.isEmpty() || selectionMode != FileSelector.FILES_ONLY && file.exists()

        adapter!!.notifyDataSetChanged()
        fsScrollView.post { fsScrollView.fullScroll(ScrollView.FOCUS_RIGHT) }
    }

    private fun handleFileList(file: File, dirList: ArrayList<Item>, fList: ArrayList<Item>) {
        if (selectionMode == FileSelector.DIRECTORIES_ONLY) {
            if (file.isDirectory) {
                dirList.add(Item(file, isSelectedItem(file)))
            }
        } else {
            if (file.isDirectory) {
                dirList.add(Item(file, isSelectedItem(file)))
            } else {
                fList.add(Item(file, isSelectedItem(file)))
            }
        }
    }

    //是否已被选
    private fun isSelectedItem(file: File): Boolean {
        for (item in selectItemList) {
            if (item.file == file) {
                return true
            }
        }
        return false
    }

    //改变全选或全不选
    private fun switchSelectAll(enable: Boolean) {
        switchSelectAllState(enable)
        for (item in itemList) {
            //只全选指定类型
            if (selectionMode == FileSelector.FILES_AND_DIRECTORIES || (selectionMode == FileSelector.DIRECTORIES_ONLY && 
                    item.file!!.isDirectory) || (selectionMode == FileSelector.FILES_ONLY && item.file!!.isFile) || 
                    (currentPath == null && selectionMode != FileSelector.FILES_ONLY)) {
                item.checked = enable
                updateSelectedFileList(item, false)
            }
        }
        updateViews()
    }

    //清除全选状态，更新标题栏按钮文本
    private fun switchSelectAllState(selectAll: Boolean) {
        isSelectedAll = selectAll
        fsIvAll.visibility = if (isMultiSelect) View.VISIBLE else View.INVISIBLE
        if (isSelectedAll) {
            fsIvAll.clearColorFilter()
        } else {
            fsIvAll.setColorFilter(ContextCompat.getColor(this, R.color.fsDisable))
        }
    }

    private fun updateSelectedText() {
        fsTvSelected.text = String.format(textHolder.getText(TextHolder.SELECTED_PATTERN), selectItemList.size)
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val item = itemList[position]
        if (item.file!!.isDirectory || currentPath == null) {
            switchSelectAllState(false)
            loadFiles(item.file)
            //记录点击条目所在文件夹的文件列表滚动到的位置
            posList.add(intArrayOf(fsLv.firstVisiblePosition, fsLv.getChildAt(0).top))
            //添加导航文件夹
            addDir(item.file)
            //进入的时候重置回到顶端位置
            fsLv.setSelectionFromTop(0, 0)
        }
    }

    override fun onItemLongClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long): Boolean {
        if (currentPath != null) {
            val item = itemList[position]
            ActionDialog(this, arrayListOf(textHolder.getText(TextHolder.RENAME), textHolder.getText(TextHolder.DELETE))) {
                when (it) {
                    0 -> showInputDialog(textHolder.getText(TextHolder.RENAME), item.file!!.name, null) { filename ->
                        val file = File(currentPath, filename)
                        if (!file.exists()) {
                            val b = item.file!!.renameTo(file)
                            if (b) {
                                Toast.makeText(this, textHolder.getText(TextHolder.RENAME_SUCCESS), Toast.LENGTH_SHORT).show()
                                loadFiles(File(currentPath!!))
                            } else {
                                Toast.makeText(this, textHolder.getText(TextHolder.RENAME_FAILED), Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this, textHolder.getText(TextHolder.RENAME_FAILED), Toast.LENGTH_SHORT).show()
                        }
                    }
                    1 -> AlertDialog.Builder(this)
                            .setMessage(textHolder.getText(TextHolder.ENSURE_DELETE_PROMPT))
                            .setNegativeButton(textHolder.getText(TextHolder.CANCEL), null)
                            .setPositiveButton(textHolder.getText(TextHolder.OK)) { _, _ ->
                                if (item.file!!.isFile) {
                                    item.file?.delete()
                                } else {
                                    FileUtils.deleteDir(item.file!!, true)
                                }
                                loadFiles(File(currentPath!!))
                            }.show()
                }
            }.show()
        }
        return true
    }
    
    private fun showInputDialog(title: String, fill: String?, hint: String?, callback: (String) -> Unit) {
        val layout = FrameLayout(this)
        val et = EditText(this)
        if (!TextUtils.isEmpty(fill)) {
            et.setText(fill)
            et.setSelection(fill!!.length)
        }
        if (!TextUtils.isEmpty(hint)) {
            et.hint = hint
        }
        val padding = Utils.dp2px(this, 8f)
        layout.setPadding(padding, 0, padding, 0)
        layout.addView(et)
        AlertDialog.Builder(this)
                .setTitle(title)
                .setView(layout)
                .setNegativeButton(textHolder.getText(TextHolder.CANCEL), null)
                .setPositiveButton(textHolder.getText(TextHolder.OK)) { _, _ ->
                    if (et.text != null && !et.text.toString().trim { it <= ' ' }.isEmpty()) {
                        callback.invoke(et.text.toString().trim { it <= ' ' })
                    }
                }.show()
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
                view = View.inflate(this@SelectFileActivity, R.layout.fs_file_item_view, null)
                holder = ViewHolder()
                view!!.tag = holder
                holder.tvName = view.findViewById(R.id.fsTvName)
                holder.tvDesc = view.findViewById(R.id.fsTvDesc)
                holder.iv = view.findViewById(R.id.iv)
                holder.ivSelect = view.findViewById(R.id.fsIvSelect)
                view.findViewById<View>(R.id.fsivDel).visibility = View.INVISIBLE
                holder.chkView = view.findViewById(R.id.fsChkView)
                holder.chkView!!.tag = holder
                holder.chkView!!.setOnClickListener { v ->
                    val h = v.tag as ViewHolder
                    val item = getItem(h.position)
                    item.checked = !item.checked
                    updateSelectedFileList(item, true)
                }
            } else {
                holder = view.tag as ViewHolder
            }
            val item = getItem(position)
            holder.position = position
            if (currentPath == null) {
                holder.tvName!!.text = item.desc
            } else {
                holder.tvName!!.text = item.file!!.name
            }
            val path = item.file!!.absolutePath
            if (item.file!!.isDirectory || currentPath == null) {
                //选择文件时，不可点击，不显示选框
                holder.chkView!!.isClickable = selectionMode != FileSelector.FILES_ONLY
                holder.ivSelect!!.visibility = if (selectionMode == FileSelector.FILES_ONLY) View.INVISIBLE else View.VISIBLE
                var num = 0
                //如果是选择文件夹，文件不计数                
                item.file?.listFiles()?.forEach {
                    if (showHiddenFiles && it.name.startsWith(".") && ((it.isDirectory && selectionMode != FileSelector.FILES_ONLY) ||
                                    (it.isFile && selectionMode != FileSelector.DIRECTORIES_ONLY))) {
                        num++
                    } else if (!it.name.startsWith(".") && (filenameFilter == null || filenameFilter!!.accept(it, it.name))) {
                        num++
                    }
                }
                holder.tvDesc!!.text = String.format(textHolder.getText(if (num > 1) TextHolder.MULTI_ITEM_PATTERN else TextHolder.SINGLE_ITEM_PATTERN), num)
                ImageLoader.instance.loadImage(R.drawable.fs_folder, holder.iv!!)
            } else {
                holder.chkView!!.isClickable = selectionMode != FileSelector.DIRECTORIES_ONLY
                holder.ivSelect!!.visibility = if (selectionMode != FileSelector.DIRECTORIES_ONLY) View.VISIBLE else View.INVISIBLE
                holder.tvDesc!!.text = FileUtils.formatFileSize(item.file!!.length())
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
                holder.ivSelect!!.setColorFilter(themeColors!![0])
            } else {
                holder.ivSelect!!.setColorFilter(Color.LTGRAY)
            }
            return view
        }
    }

    fun clearSelectedFileList() {
        for (item in selectItemList) {
            item.checked = false
        }
        selectItemList.clear()
        switchSelectAllState(false)
        updateViews()
    }

    private fun updateViews() {
        val file = File(if (currentPath == null) "" else currentPath)
        fsTvOk.isEnabled = selectItemList.isNotEmpty() || (selectionMode != FileSelector.FILES_ONLY && file.exists())
        updateSelectedText()
        adapter!!.notifyDataSetChanged()
        selectedItemDialog!!.updateList(selectItemList)
    }

    internal fun updateSelectedFileList(item: Item, needNotify: Boolean) {
        if (item.checked) {
            if (!selectItemList.contains(item)) {
                //如果是单选，把已选的删除
                if (!isMultiSelect && !selectItemList.isEmpty()) {
                    val removeItem = selectItemList.removeAt(0)
                    for (i in itemList) {
                        if (i == removeItem) {
                            i.checked = false
                            break
                        }
                    }
                }
                selectItemList.add(item)
            }
        } else {
            selectItemList.remove(item)
        }
        if (needNotify) {
            updateViews()
            var b = true
            itemList.forEach { 
                if ((selectionMode == FileSelector.FILES_AND_DIRECTORIES || (selectionMode == FileSelector.FILES_ONLY && it.file!!.isFile) ||
                                (selectionMode == FileSelector.DIRECTORIES_ONLY && it.file!!.isDirectory)) && !selectItemList.contains(it)) {
                    b = false
                }
            }
            switchSelectAllState(b)
        }
    }

    private fun showPopupWindow() {
        val lv = View.inflate(this, R.layout.fs_listview, null) as ListView
        val items = ArrayList<String>()
        items.add(textHolder.getText(TextHolder.NEW_FOLDER))
        items.add(textHolder.getText(if (showHiddenFiles) TextHolder.DONOT_SHOW_HIDDEN_FILES else TextHolder.SHOW_HIDDEN_FILES))
        lv.adapter = PopupMenuAdapter(this, items)
        val height = items.size * Utils.dp2px(this, 50f) + items.size
        val popupWindow = PopupWindow(lv, Utils.getDisplayScreenWidth(this), height)
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.fs_popun_menu_bg))
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.setOnDismissListener {
            //pop消失，去掉蒙层
            fsMaskView.clearAnimation()
            fsMaskView.visibility = View.GONE
        }
        lv.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            popupWindow.dismiss()
            when (position) {
                0 -> {
                    showInputDialog(textHolder.getText(TextHolder.NEW_FOLDER), null, null) { dirName ->
                        val file = File(currentPath, dirName)
                        //不存在才新建
                        if (!file.exists()) {
                            if (file.mkdir()) {
                                Toast.makeText(this, textHolder.getText(TextHolder.FOLDER_CREATE_SUCCESS), Toast.LENGTH_SHORT).show()
                                loadFiles(File(currentPath!!))
                            } else {
                                Toast.makeText(this, textHolder.getText(TextHolder.FOLDER_CREATE_FAILED), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                1 -> {
                    showHiddenFiles = !showHiddenFiles
                    loadFiles(if (currentPath == null) null else File(currentPath))
                }
            }
        }
        //显示蒙层
        fsMaskView.visibility = View.VISIBLE
        val alphaAnimation = AlphaAnimation(0f, 1f)
        alphaAnimation.duration = 300
        alphaAnimation.fillAfter = true
        fsMaskView.startAnimation(alphaAnimation)
        popupWindow.showAsDropDown(fsLayoutTitle)
    }

    private inner class PopupMenuAdapter internal constructor(context: Context, data: MutableList<String>) : BaseListAdapter<String>(context, data) {

        override fun getHolder(position: Int): BaseHolder<String> {
            return object : BaseHolder<String>() {
                private var tv: TextView? = null

                override fun setData(data: String, position: Int) {                    
                    if (position == 1) {
                        tv!!.text = textHolder.getText(if (showHiddenFiles) TextHolder.DONOT_SHOW_HIDDEN_FILES else TextHolder.SHOW_HIDDEN_FILES)
                    } else {
                        tv!!.text = data
                    }
                }

                override fun createConvertView(): View {
                    val view = View.inflate(context, R.layout.fs_item_popup_menu, null)
                    tv = view.findViewById(R.id.fsTv)
                    return view
                }
            }
        }
    }

    companion object {
        private const val PERMISSON_REQUESTCODE = 10

        internal const val EXTRA_SELECTION_MODE = "SELECTION_MODE"
        internal const val EXTRA_IS_MULTI_SELECT = "IS_MULTI_SELECT"
        internal const val EXTRA_IS_LANDSCAPE = "SCREEN_ORIENTATION"
        internal const val EXTRA_ROOT = "ROOT"
        internal const val EXTRA_SELECTED_FILE_PATH_LIST = "SELECTED_FILE_LIST"
        internal const val EXTRA_TITLE = "TITLE"
        internal const val EXTRA_THEME_COLORS = "THEME_COLORS"
        internal const val EXTRA_LANGUAGE = "LANGUAGE"
        internal const val EXTRA_SHOW_HIDDEN_FILES = "SHOW_HIDDEN_FILES"
        internal var filenameFilter: FilenameFilter? = null
    }
}
