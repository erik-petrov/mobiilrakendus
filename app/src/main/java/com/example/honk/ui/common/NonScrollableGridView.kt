package com.example.honk.ui.common

import android.content.Context
import android.util.AttributeSet
import android.widget.GridView

class NonScrollableGridView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0
) : GridView(context, attrs, defStyle) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Expand to show all rows; let parent (NestedScrollView) handle scrolling
        val expandSpec = MeasureSpec.makeMeasureSpec(
            Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST
        )
        super.onMeasure(widthMeasureSpec, expandSpec)
        layoutParams.height = measuredHeight
    }
}
