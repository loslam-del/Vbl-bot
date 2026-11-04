package com.vblbot

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private enum class VblPage { HOME, BALL, STYLES, STATS, SETTINGS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                VblApp(onOpenAccessibility = {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                })
            }
        }
    }
}

@Composable
private fun VblApp(onOpenAccessibility: () -> Unit) {
    var page by remember { mutableStateOf(VblPage.HOME) }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("VBL-BOT", style = MaterialTheme.typography.headlineLarge)
                    Text("Тренировочный помощник")
                }
                Text("BETA", style = MaterialTheme.typography.labelLarge)
            }
            when (page) {
                VblPage.HOME -> HomeMenu(
                    onBall = { page = VblPage.BALL },
                    onStyles = { page = VblPage.STYLES },
                    onStats = { page = VblPage.STATS },
                    onSettings = { page = VblPage.SETTINGS }
                )
                VblPage.BALL -> BallScreen(onBack = { page = VblPage.HOME })
                VblPage.STYLES -> StylesScreen(onBack = { page = VblPage.HOME })
                VblPage.STATS -> InfoScreen(
                    title = "Статистика",
                    description = "Статистика появится после подключения анализа видеокадров. Сейчас приложение ещё не распознаёт мяч автоматически и не собирает игровые данные.",
                    onBack = { page = VblPage.HOME }
                )
                VblPage.SETTINGS -> SettingsScreen(
                    onBack = { page = VblPage.HOME },
                    onOpenAccessibility = onOpenAccessibility
                )
            }
            Text(
                "VBL-BOT не управляет персонажем и не нажимает игровые кнопки.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun HomeMenu(
    onBall: () -> Unit,
    onStyles: () -> Unit,
    onStats: () -> Unit,
    onSettings: () -> Unit
) {
    Text("Главное меню", style = MaterialTheme.typography.titleLarge)
    MenuCard("Распознавание мяча", "Захват экрана и поиск мяча — функция ещё разрабатывается.", "Открыть") { onBall() }
    MenuCard("Прогноз траектории", "Тестовый расчёт по наблюдениям. Автоматическое отслеживание пока не подключено.", "Открыть") { onBall() }
    MenuCard("Профили стилей", "Общий, быстрый, кручёный, подача и особый профиль.", "Настроить") { onStyles() }
    MenuCard("Статистика", "Пока нет собранных данных для статистики.", "Открыть") { onStats() }
    MenuCard("Настройки", "Разрешения и информация о приложении.", "Открыть") { onSettings() }
}

@Composable
private fun MenuCard(title: String, description: String, action: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(action) }
        }
    }
}

@Composable
private fun BallScreen(onBack: () -> Unit) {
    Text("Мяч и траектория", style = MaterialTheme.typography.titleLarge)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Автоматическое распознавание ещё не подключено.", style = MaterialTheme.typography.titleMedium)
            Text("Следующий технический этап — добавить системный захват экрана Android с твоего разрешения и обнаружение мяча на кадрах. Пока этот экран показывает статус функции, а не живой анализ.")
            Text("Ручной ввод координат временно убран с главного экрана, чтобы не заставлять тебя угадывать положение мяча.")
        }
    }
    BackButton(onBack)
}

@Composable
private fun StylesScreen(onBack: () -> Unit) {
    Text("Профили стилей", style = MaterialTheme.typography.titleLarge)
    listOf(
        "Общий" to "Базовый профиль движения.",
        "Быстрый" to "Для более резкого перемещения мяча.",
        "Кручёный" to "Экспериментальный профиль.",
        "Подача" to "Профиль для подачи.",
        "Особый" to "Дополнительный экспериментальный профиль."
    ).forEach { (name, description) ->
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
    Text("Профили пока демонстрационные: реальная настройка по игровым стилям ещё не реализована.")
    BackButton(onBack)
}

@Composable
private fun SettingsScreen(onBack: () -> Unit, onOpenAccessibility: () -> Unit) {
    Text("Настройки", style = MaterialTheme.typography.titleLarge)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Специальные возможности Android", style = MaterialTheme.typography.titleMedium)
            Text("Служба приложения не нажимает кнопки и не управляет Roblox.")
            Button(onClick = onOpenAccessibility, modifier = Modifier.fillMaxWidth()) {
                Text("Открыть настройки Android")
            }
        }
    }
    BackButton(onBack)
}

@Composable
private fun InfoScreen(title: String, description: String, onBack: () -> Unit) {
    Text(title, style = MaterialTheme.typography.titleLarge)
    Card(modifier = Modifier.fillMaxWidth()) {
        Text(description, modifier = Modifier.padding(16.dp))
    }
    BackButton(onBack)
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    Spacer(Modifier.height(4.dp))
    OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
        Text("← Назад в меню")
    }
}
