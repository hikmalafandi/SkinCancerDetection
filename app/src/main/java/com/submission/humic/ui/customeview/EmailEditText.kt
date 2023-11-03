package com.submission.humic.ui.customeview

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.submission.humic.R

class EmailEditText: AppCompatEditText {

    private var isEmailValid = true

    constructor(context: Context) : super(context) {
        setup()
        init()
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        setup()
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr) {
        setup()
        init()
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

    private fun init() {
        addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(s.toString().trim()).matches()

                if (!isEmailValid) {
                    setError("Format email salah")
                } else {
                    setError(null)
                }
            }

        })
    }




}