package com.liulkovich.florapoint.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.liulkovich.florapoint.presentation.navigation.NavGraph
import com.liulkovich.florapoint.presentation.ui.theme.FloraPointTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FloraPointTheme {
                NavGraph()
                //MapScreen()
            }
        }
    }
}

