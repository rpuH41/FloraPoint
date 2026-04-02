package com.liulkovich.florapoint.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.liulkovich.florapoint.data.AppDatabase
import com.liulkovich.florapoint.presentation.ui.theme.FloraPointTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FloraPointTheme {
                check()
            }
        }
    }
}

@Composable
fun check(){
    val context: Context = LocalContext.current
    LaunchedEffect(Unit) {
        val db = AppDatabase.getDatabase(context)
        val mushroomList = db.referenceDao().getByCategory("Грибы")
        Log.d("DB_TEST", "Записей в базе: ${mushroomList.size}")
    }
}

