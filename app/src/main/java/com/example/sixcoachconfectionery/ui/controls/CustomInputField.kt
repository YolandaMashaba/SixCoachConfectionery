package com.example.sixcoachconfectionery.ui.controls

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.example.sixcoachconfectionery.R

class CustomInputField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val tvLabel: TextView
    val editText: EditText
    private val tvError: TextView

    init {
        orientation = VERTICAL
        val view = LayoutInflater.from(context).inflate(R.layout.view_custom_input_field, this, true)

        tvLabel = view.findViewById(R.id.tvInputLabel)
        editText = view.findViewById(R.id.etInputField)
        tvError = view.findViewById(R.id.tvInputError)

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                clearError()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    fun setLabel(label: String) {
        tvLabel.text = label
    }

    fun setHint(hint: String) {
        editText.hint = hint
    }

    fun setInputType(inputType: Int) {
        editText.inputType = inputType
    }

    fun setText(text: String) {
        editText.setText(text)
    }

    fun getText(): String {
        return editText.text.toString().trim()
    }

    fun setError(errorMsg: String) {
        tvError.text = errorMsg
        tvError.visibility = View.VISIBLE
    }

    fun clearError() {
        tvError.visibility = View.GONE
    }
}
