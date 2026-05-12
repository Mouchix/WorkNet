package com.example.worknet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.worknet.navigation.AppNavigation
import com.example.worknet.ui.theme.WorkNetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WorkNetTheme {
                AppNavigation()
            }
        }
    }
}
