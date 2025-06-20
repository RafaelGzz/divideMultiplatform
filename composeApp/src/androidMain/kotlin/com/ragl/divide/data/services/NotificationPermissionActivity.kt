package com.ragl.divide.data.services

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.edit

class NotificationPermissionActivity : ComponentActivity() {
    
    companion object {
        private const val PREFS_NAME = "notification_permissions"
        private const val KEY_PERMISSION_REJECTED = "permission_rejected_permanently"
        
        fun wasPermissionRejectedPermanently(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_PERMISSION_REJECTED, false)
        }
        
        fun clearPermissionRejectedFlag(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit { remove(KEY_PERMISSION_REJECTED) }
        }
    }
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            // Si el permiso fue rechazado, verificar si fue rechazado permanentemente
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    // El usuario marcó "No volver a preguntar" o rechazó permanentemente
                    // Guardar flag para que el ViewModel pueda mostrar el diálogo apropiado
                    savePermissionRejectedFlag()
                }
            }
        } else {
            // Si el permiso fue concedido, limpiar cualquier flag anterior
            clearPermissionRejectedFlag(this)
        }
        finish()
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // El permiso ya está concedido, limpiar flag
                    clearPermissionRejectedFlag(this)
                    finish()
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // El usuario rechazó antes pero puede volver a preguntar
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    // Primera vez pidiendo permiso o fue rechazado permanentemente
                    // Intentar mostrar el diálogo primero
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // En versiones anteriores a Android 13, no se necesita este permiso
            finish()
        }
    }
    
    private fun savePermissionRejectedFlag() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit { putBoolean(KEY_PERMISSION_REJECTED, true) }
    }
    
    private fun openNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
        }
        startActivity(intent)
    }
} 