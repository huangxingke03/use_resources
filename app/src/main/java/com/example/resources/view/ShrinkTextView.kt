package com.example.resources.view

import android.content.Context
import android.text.DynamicLayout
import android.text.Layout
import android.util.AttributeSet
import android.util.Log
import androidx.appcompat.widget.AppCompatTextView
import com.blankj.utilcode.util.SizeUtils

class ShrinkTextView : AppCompatTextView {
    private val TAG = "ShrinkTextView"
    private var textViewWidth = resources.displayMetrics.widthPixels - SizeUtils.dp2px(87.78f + 21.82f)
    private lateinit var mDynamicLayout: DynamicLayout
    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
            context!!,
        attrs,
        defStyleAttr
    )

    fun shrinkText(content: String) {
        //用来计算内容的大小
        mDynamicLayout = DynamicLayout(
            content,
            paint,
            textViewWidth,
            Layout.Alignment.ALIGN_NORMAL,
            1f,
            0.0f,
            true
        )
        val index = if (maxLines > mDynamicLayout.lineCount) 0 else 1
        val endPosition = mDynamicLayout.getLineEnd(index)
        val startPosition = mDynamicLayout.getLineStart(index)//指定行文字开始的pos（如果有上一行，这里的startPosition就是顺呈上一行的文字个数）
        var lineWidth = mDynamicLayout.getLineWidth(index)//指定行的文字宽度
        var moreTxtWidth = paint.measureText("... 更多")
        val showTxt = if (textViewWidth - lineWidth >= moreTxtWidth){
            "$content..."
        }else{
            //最后一行需要添加的文字的个数
            val position = ((textViewWidth - moreTxtWidth) * (endPosition - startPosition) / lineWidth).toInt()
            if (index == 0){//只有一行
                content.substring(0, position) + "..."
            }else{
                if (textViewWidth - lineWidth <= moreTxtWidth){//避免和“... 更多”文案靠得太紧
                    content.substring(0, startPosition + position - 1) + "..."
                }else{
                    content.substring(0, startPosition + position) + "..."
                }
            }
        }
        Log.v(TAG, "showTxt: $showTxt")
        text = showTxt
    }

    fun expandText(content: String){
        text = content
    }
}