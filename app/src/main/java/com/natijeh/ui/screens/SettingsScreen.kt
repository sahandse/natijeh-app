package com.natijeh.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.natijeh.BuildConfig
import com.natijeh.data.settings.ThemeMode
import com.natijeh.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val colors = MaterialTheme.colorScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "بازگشت")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colors.background,
                    titleContentColor = colors.onBackground,
                    navigationIconContentColor = colors.onBackground
                )
            )
        },
        containerColor = colors.background
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            ScoreboardHero()

            SectionLabel("ظاهر")
            ThemePicker(
                selected = settings.themeMode,
                onSelect = viewModel::setThemeMode
            )

            SectionLabel("اعلان و نمایش")
            SettingsCard {
                SettingToggle(
                    icon = Icons.Outlined.Notifications,
                    title = "اعلان گل و کارت",
                    subtitle = "برای تیم‌ها و لیگ‌های محبوب",
                    checked = settings.goalNotifications,
                    onCheckedChange = viewModel::setGoalNotifications
                )
                Hairline()
                SettingToggle(
                    icon = Icons.Outlined.WbSunny,
                    title = "روشن ماندن صفحه",
                    subtitle = "وقتی جزئیات بازی زنده باز است",
                    checked = settings.keepScreenOnLive,
                    onCheckedChange = viewModel::setKeepScreenOnLive
                )
                Hairline()
                SettingToggle(
                    icon = Icons.Outlined.SportsSoccer,
                    title = "شروع روی تب زنده",
                    subtitle = "اگر مسابقه‌ای در جریان باشد",
                    checked = settings.openLiveTab,
                    onCheckedChange = viewModel::setOpenLiveTab
                )
            }

            SectionLabel("درباره")
            SettingsCard {
                AboutRow("نسخه", BuildConfig.VERSION_NAME)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ScoreboardHero() {
    val colors = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(colors.primary.copy(alpha = 0.18f), colors.surface)
                )
            )
            .padding(vertical = 22.dp, horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("نتیجه", color = colors.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("۲", color = colors.onSurface, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(colors.primary)
                )
                Text("۱", color = colors.onSurface, style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
            }
            Text(
                "مینیمال · زنده · فارسی",
                color = colors.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun ThemePicker(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        ThemePreviewCard(
            modifier = Modifier.weight(1f),
            title = "سیاه",
            selected = selected == ThemeMode.DARK,
            icon = Icons.Outlined.DarkMode,
            canvas = Color(0xFF07090C),
            card = Color(0xFF12161C),
            accent = Color(0xFF1DB954),
            onClick = { onSelect(ThemeMode.DARK) }
        )
        ThemePreviewCard(
            modifier = Modifier.weight(1f),
            title = "سفید",
            selected = selected == ThemeMode.LIGHT,
            icon = Icons.Outlined.LightMode,
            canvas = Color(0xFFF7F4EE),
            card = Color.White,
            accent = Color(0xFF128A3E),
            onClick = { onSelect(ThemeMode.LIGHT) }
        )
        ThemePreviewCard(
            modifier = Modifier.weight(1f),
            title = "سیستم",
            selected = selected == ThemeMode.SYSTEM,
            icon = Icons.Outlined.PhoneAndroid,
            canvas = Color(0xFF07090C),
            card = Color.White,
            accent = Color(0xFF1DB954),
            split = true,
            onClick = { onSelect(ThemeMode.SYSTEM) }
        )
    }
}

@Composable
private fun ThemePreviewCard(
    modifier: Modifier,
    title: String,
    selected: Boolean,
    icon: ImageVector,
    canvas: Color,
    card: Color,
    accent: Color,
    split: Boolean = false,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(20.dp)
    val border = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    Column(
        modifier = modifier
            .clip(shape)
            .border(if (selected) 2.dp else 1.dp, border, shape)
            .clickable(onClick = onClick)
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (split) Brush.horizontalGradient(listOf(Color(0xFF07090C), Color(0xFFF7F4EE)))
                    else Brush.linearGradient(listOf(canvas, canvas))
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 44.dp, height = 28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (split) Color.Transparent else card)
                    .then(
                        if (split) Modifier.background(Brush.horizontalGradient(listOf(Color(0xFF12161C), Color.White)))
                        else Modifier
                    )
            )
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(accent)
            )
        }
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(18.dp))
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 4.dp)
    ) {
        content()
    }
}

@Composable
private fun SettingToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary
            )
        )
    }
}

@Composable
private fun AboutRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun Hairline() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
        thickness = 0.6.dp
    )
}
