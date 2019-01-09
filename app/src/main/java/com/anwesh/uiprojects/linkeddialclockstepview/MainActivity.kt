package com.anwesh.uiprojects.linkeddialclockstepview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.dialclockstepview.DialClockStepView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DialClockStepView.create(this)
    }
}
