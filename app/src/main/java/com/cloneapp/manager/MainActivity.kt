package com.cloneapp.manager

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
import android.os.Bundle
import android.os.UserManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CloneAppScreen() }
    }
}

private data class InstalledApp(val label: String, val packageName: String)

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
private fun CloneAppScreen() {
    val context = LocalContext.current
    val userManager = context.getSystemService(UserManager::class.java)
    val isManagedProfile = userManager?.isManagedProfile == true
    var refresh by remember { mutableStateOf(0) }
    var showProfileInfo by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Ready") }

    val apkPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val installIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(installIntent)
                status = "APK sent to Android Package Installer"
            } catch (error: Exception) {
                status = "Could not open installer: ${error.localizedMessage ?: "unknown error"}"
            }
        }
    }

    val apps = remember(refresh) {
        runCatching {
            context.packageManager.getInstalledApplications(0)
                .filter { it.packageName != context.packageName }
                .map {
                    InstalledApp(
                        label = context.packageManager.getApplicationLabel(it).toString(),
                        packageName = it.packageName
                    )
                }
                .sortedBy { it.label.lowercase() }
        }.getOrDefault(emptyList())
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Clone App", fontWeight = FontWeight.Bold)
                            Text("App & profile manager", style = MaterialTheme.typography.labelSmall)
                        }
                    },
                    actions = {
                        androidx.compose.material3.IconButton(onClick = { refresh++ }) {
                            Icon(Icons.Default.Settings, contentDescription = "Refresh apps")
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Layers, contentDescription = null)
                                Text("  Current Android space", fontWeight = FontWeight.SemiBold)
                            }
                            Text(
                                if (isManagedProfile) "Managed / work profile"
                                else "Personal Android profile",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "This app runs inside the Android profile where it was installed. " +
                                    "A separate Android work profile has its own app data and sign-in sessions."
                            )
                            OutlinedButton(onClick = { showProfileInfo = true }) {
                                Text("How profile isolation works")
                            }
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { apkPicker.launch(arrayOf("application/vnd.android.package-archive")) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Text("  Import APK")
                        }
                        OutlinedButton(
                            onClick = { refresh++ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Apps, contentDescription = null)
                            Text("  Refresh apps")
                        }
                    }
                }
                item {
                    Text(status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary)
                    Text("Installed apps in this profile (${apps.size})", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                if (apps.isEmpty()) {
                    item { Text("No installed apps were found.") }
                } else {
                    items(apps, key = { it.packageName }) { app ->
                        Card(Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(Icons.Default.Android, contentDescription = null)
                                Column(Modifier.weight(1f)) {
                                    Text(app.label, fontWeight = FontWeight.SemiBold)
                                    Text(app.packageName, style = MaterialTheme.typography.labelSmall)
                                }
                                OutlinedButton(onClick = {
                                    val launch = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                    if (launch != null) context.startActivity(launch)
                                    else status = "This app does not expose a launch activity."
                                }) { Text("Open") }
                            }
                        }
                    }
                }
                item {
                    Divider()
                    Text(
                        "Important: Android does not let an ordinary app create arbitrary virtual Android phones or rewrite protected device identifiers. " +
                            "This build uses real Android profile boundaries where the operating system or an administrator has provisioned them. " +
                            "It does not spoof hardware IDs or bypass third-party identity checks.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.padding(bottom = 12.dp))
                }
            }
        }
    }

    if (showProfileInfo) {
        AlertDialog(
            onDismissRequest = { showProfileInfo = false },
            icon = { Icon(Icons.Default.Info, contentDescription = null) },
            title = { Text("About Android profile isolation") },
            text = {
                Text(
                    "A work profile is provisioned and controlled by Android's device-policy system. " +
                        "This app can detect the profile it is running in and manage apps visible in that profile, " +
                        "but a normal installed app cannot silently create a new work profile. " +
                        "On supported devices, use Android Settings or your organization's official work-profile enrollment flow. " +
                        "Install Clone App in each profile where you want to manage that profile's apps."
                )
            },
            confirmButton = {
                TextButton(onClick = { showProfileInfo = false }) { Text("Got it") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showProfileInfo = false
                    context.startActivity(Intent(Settings.ACTION_SETTINGS))
                }) { Text("Open Settings") }
            }
        )
    }
}
