package com.snail.fileselector

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.fs_listview.*

/**
 *
 *
 * date: 2019/3/3 14:58
 * author: zengfansheng
 */
internal class ActionDialog(activity: SelectFileActivity, items: ArrayList<String>, listener: (Int) -> Unit) : Dialog(activity) {
    
    init {
        window?.requestFeature(Window.FEATURE_NO_TITLE)
        window?.decorView?.setPadding(0, 0, 0, 0)
        window?.setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.fs_action_dialog_bg))
        window?.setWindowAnimations(R.style.FsDialogAnimFromBottom)
        window?.setGravity(Gravity.BOTTOM)
        val lp = window?.attributes
        lp?.width = WindowManager.LayoutParams.MATCH_PARENT
        lp?.height = items.size * Utils.dp2px(activity, 50f) + items.size
        window?.attributes = lp
        setContentView(R.layout.fs_listview)
        lv.adapter = ActionAdapter(activity, items)
        lv.setOnItemClickListener { _, _, position, _ -> 
            listener.invoke(position)
            dismiss()
        }
    }
    
    private class ActionAdapter(context: Context, list: MutableList<String>) : BaseListAdapter<String>(context, list) {
        override fun getHolder(position: Int): BaseHolder<String> {
            return object : BaseHolder<String>() {
                private var tv: TextView? = null
                
                override fun setData(data: String, position: Int) {
                    tv?.text = data
                }

                override fun createConvertView(): View {
                    val view = View.inflate(context, R.layout.fs_item_popup_menu, null)
                    tv = view.findViewById(R.id.fsTv)
                    return view
                }
            }
        }
    }
}