package com.example.smarthospitalqueue

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.smarthospitalqueue.ui.navigation.AppNavigation
import com.example.smarthospitalqueue.ui.theme.SmartHospitalQueueTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {

            SmartHospitalQueueTheme {

                Surface(
                    color = MaterialTheme.colorScheme.background
                ) {

                    AppNavigation()

                }
            }
        }
    }
}