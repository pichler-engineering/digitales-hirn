package at.pichler.digitaleshirn

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import at.pichler.digitaleshirn.ui.DigitalesHirnApp
import at.pichler.digitaleshirn.ui.theme.DigitalesHirnTheme
import at.pichler.digitaleshirn.vm.MainViewModel
import at.pichler.digitaleshirn.vm.MainViewModelFactory

class MainActivity : ComponentActivity() {
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        askNotificationPermission()

        val app = application as DigitalesHirnApplication
        setContent {
            DigitalesHirnTheme {
                val vm: MainViewModel = viewModel(
                    factory = MainViewModelFactory(app.repository, app.reminderScheduler)
                )
                DigitalesHirnApp(vm)
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT < 33) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        ) return
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}
