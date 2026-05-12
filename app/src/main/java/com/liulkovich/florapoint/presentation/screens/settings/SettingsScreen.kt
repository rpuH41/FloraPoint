package com.liulkovich.florapoint.presentation.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.liulkovich.florapoint.BuildConfig
import com.liulkovich.florapoint.R


@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateToNotifications: () -> Unit
) {
    val context = LocalContext.current
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.handleImport(context, it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SettingsSection(title = stringResource(R.string.notifications)) {
            SettingsItem(
                icon = Icons.Default.Notifications,
                title = stringResource(R.string.seasonal_notifications),
                subtitle = stringResource(R.string.start_peak_and_end_of_season),
                onClick = onNavigateToNotifications
            )
        }

        Spacer(Modifier.height(12.dp))

        SettingsSection(title = stringResource(R.string.my_details)) {
            SettingsItem(
                icon = Icons.Default.Backup,
                title = stringResource(R.string.backup),
                subtitle = stringResource(R.string.save_all_points_to_a_file),
                onClick = { showBackupDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
            SettingsItem(
                icon = Icons.Default.Restore,
                title = stringResource(R.string.restore),
                subtitle = stringResource(R.string.load_points_from_a_file),
                onClick = { showRestoreDialog = true }
            )
        }

        Spacer(Modifier.height(12.dp))

        SettingsSection(title = stringResource(R.string.about_the_app)) {
            SettingsItem(
                icon = Icons.Default.Lock,
                title = stringResource(R.string.privacy),
                subtitle = stringResource(R.string.how_we_handle_your_data),
                onClick = { showPrivacyDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
            val feedback = stringResource(R.string.feedback_subject)
            val letter = stringResource(R.string.write_letter)
            SettingsItem(
                icon = Icons.Default.Mail,
                title = stringResource(R.string.contact_developer),
                subtitle = stringResource(R.string.questions_and_suggestions),
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("your@email.com")) //не забыть поменять почту
                        putExtra(Intent.EXTRA_SUBJECT, feedback )
                    }
                    context.startActivity(Intent.createChooser(intent,
                        letter))
                }
            )
            HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
            SettingsItem(
                icon = Icons.Default.Star,
                title = stringResource(R.string.rate_the_app),
                subtitle = stringResource(R.string.help_us_get_better),
                onClick = {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        "market://details?id=${context.packageName}".toUri()
                    )
                    runCatching { context.startActivity(intent) }
                }
            )
            HorizontalDivider(modifier = Modifier.padding(start = 52.dp))
            SettingsItem(
                icon = Icons.Default.Info,
                title = stringResource(R.string.app_version),
                subtitle = BuildConfig.VERSION_NAME,
                onClick = {},
                showArrow = false
            )
        }
    }

    if (showPrivacyDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(stringResource(R.string.privacy)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.what_we_store),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(stringResource(R.string.your_map_points))
                    Text(stringResource(R.string.notification_settings))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.what_we_dont),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(stringResource(R.string.we_dont_share_data_with_third_parties))
                    Text(stringResource(R.string.we_dont_sell_your_data))
                    Text(stringResource(R.string.we_dont_use_data_for_advertising))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.all_data_is_stored_only_on_your_device),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showPrivacyDialog = false }
                ) { Text(stringResource(R.string.got_it)) }
            }
        )
    }

    if (showBackupDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text(stringResource(R.string.backup)) },
            text = { Text(stringResource(R.string.save_all_your_points_to_a_file)) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showBackupDialog = false
                    viewModel.exportPoints(context) { uri ->
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/gpx+xml"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        context.startActivity(
                            Intent.createChooser(intent, "Сохранить резервную копию")
                        )
                    }
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showBackupDialog = false }
                ) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (showRestoreDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text(stringResource(R.string.restore_points)) },
            text = { Text(stringResource(R.string.select_backup_file)) },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showRestoreDialog = false
                    importLauncher.launch("*/*")
                }) { Text(stringResource(R.string.choose_file)) }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = { showRestoreDialog = false }
                ) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}


@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showArrow: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (showArrow) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}