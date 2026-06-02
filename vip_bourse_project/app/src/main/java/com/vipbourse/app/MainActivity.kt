package com.vipbourse.app

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.vipbourse.app.ui.TradingMainView
import com.vipbourse.app.ui.TradingViewModel
import com.vipbourse.app.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    
    private val viewModel: TradingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Main responsive screen root with injected safe inner padding from Scaffold
                    TradingMainView(
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}
