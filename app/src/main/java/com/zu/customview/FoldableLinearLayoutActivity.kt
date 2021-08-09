package com.zu.customview

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.zu.customview.view.FoldableLinearLayout

class FoldableLinearLayoutActivity : AppCompatActivity() {

    var expanded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_foldable_linear_layout)
        val changeBtn:Button = findViewById(R.id.btn_change)
        val innerLayout:FoldableLinearLayout = findViewById(R.id.inner)
        changeBtn.setOnClickListener(View.OnClickListener {
            if(expanded)
            {
                expanded = false
                innerLayout.shrink()
            }else{
                expanded = true
                innerLayout.expand()
            }
        })
    }
}
