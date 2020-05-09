package com.example.coroutinesexamples

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

abstract class BaseActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getLayout())
    }

    protected abstract fun getLayout(): Int

    fun ShowShortOrLongToast(messageValue: String, isShort: Boolean = false) {
        if (isShort) {
            Toast.makeText(this, messageValue, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, messageValue, Toast.LENGTH_SHORT).show();
        }
    }



}
