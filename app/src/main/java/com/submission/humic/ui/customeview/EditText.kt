package com.submission.humic.ui.customeview

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.submission.humic.R

class EditText: AppCompatEditText {

    constructor(context: Context) : super(context) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setup()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        setup()
    }

    private fun setup() {
        setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent))
        val shapeDrawable = GradientDrawable()
        shapeDrawable.cornerRadius = resources.getDimensionPixelSize(R.dimen.corner_radius).toFloat()
        shapeDrawable.setColor(ContextCompat.getColor(context, R.color.white))
        shapeDrawable.setStroke(5, ContextCompat.getColor(context, R.color.grey))
        background = shapeDrawable

        setPadding(
            resources.getDimensionPixelSize(R.dimen.edittext_padding_horizontal),
            resources.getDimensionPixelSize(R.dimen.edittext_padding_vertical),
            resources.getDimensionPixelSize(R.dimen.edittext_padding_horizontal),
            resources.getDimensionPixelSize(R.dimen.edittext_padding_vertical)
        )

    }
}