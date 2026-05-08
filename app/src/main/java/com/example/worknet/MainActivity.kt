package com.example.worknet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.worknet.navigation.AppNavigation
import com.example.worknet.ui.theme.WorkNetTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.serialization.Serializable


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseAuth.getInstance().signInAnonymously()
            .addOnSuccessListener {
                setContent {
                    WorkNetTheme {
                        AppNavigation()
                    }
                }
            }
            .addOnFailureListener {
                setContent {
                    WorkNetTheme {
                        androidx.compose.material3.Text("Errore login anonimo")
                    }
                }
            }
    }
}
