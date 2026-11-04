package com.vblbot

import android.content.Context
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private enum class VblPage { HOME, BALL, TRAINING, STYLES, GUIDE, STATS, SETTINGS }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                VblApp(
                    onOpenAccessibility = { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
                    preferences = getSharedPreferences("vbl_stats", Context.MODE_PRIVATE)
                )
            }
        }
    }
}

@Composable
private fun VblApp(
    onOpenAccessibility: () -> Unit,
    preferences: android.content.SharedPreferences
) {
    var page by remember { mutableStateOf(VblPage.HOME) }
    var selectedProfile by remember { mutableStateOf(preferences.getString("profile", "Общий") ?: "Общий") }
    var receives by remember { mutableIntStateOf(preferences.getInt("receives", 0)) }
    var sets by remember { mutableIntStateOf(preferences.getInt("sets", 0)) }
    var serves by remember { mutableIntStateOf(preferences.getInt("serves", 0)) }
    var sessionNotes by remember { mutableStateOf(preferences.getString("notes", "") ?: "") }

    fun saveCount(key: String, value: Int) {
        preferences.edit().putInt(key, value).apply()
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("VBL-BOT", style = MaterialTheme.typography.headlineLarge)
                    Text("Тренировочный центр · BETA")
                }
                Text("🏐", style = MaterialTheme.typography.headlineMedium)
            }
            when (page) {
                VblPage.HOME -> HomeMenu(
                    onBall = { page = VblPage.BALL },
                    onTraining = { page = VblPage.TRAINING },
                    onStyles = { page = VblPage.STYLES },
                    onGuide = { page = VblPage.GUIDE },
                    onStats = { page = VblPage.STATS },
                    onSettings = { page = VblPage.SETTINGS }
                )
                VblPage.BALL -> BallScreen(onBack = { page = VblPage.HOME })
                VblPage.TRAINING -> TrainingScreen(
                    receives = receives,
                    sets = sets,
                    serves = serves,
                    onReceive = { receives += 1; saveCount("receives", receives) },
                    onSet = { sets += 1; saveCount("sets", sets) },
                    onServe = { serves += 1; saveCount("serves", serves) },
                    onBack = { page = VblPage.HOME }
                )
                VblPage.STYLES -> StylesScreen(
                    selected = selectedProfile,
                    onSelect = {
                        selectedProfile = it
                        preferences.edit().putString("profile", it).apply()
                    },
                    onBack = { page = VblPage.HOME }
                )
                VblPage.GUIDE -> GuideScreen(onBack = { page = VblPage.HOME })
                VblPage.STATS -> StatsScreen(
                    receives = receives, sets = sets, serves = serves,
                    onReset = {
                        receives = 0; sets = 0; serves = 0
                        preferences.edit().putInt("receives", 0).putInt("sets", 0).putInt("serves", 0).apply()
                    },
                    onBack = { page = VblPage.HOME }
                )
                VblPage.SETTINGS -> SettingsScreen(
                    selectedProfile = selectedProfile,
                    onOpenAccessibility = onOpenAccessibility,
                    onBack = { page = VblPage.HOME }
                )
            }
            Text(
                "VBL-BOT помогает анализировать тренировки. Он не нажимает игровые кнопки и не управляет персонажем.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun HomeMenu(
    onBall: () -> Unit, onTraining: () -> Unit, onStyles: () -> Unit,
    onGuide: () -> Unit, onStats: () -> Unit, onSettings: () -> Unit
) {
    Text("Главное меню", style = MaterialTheme.typography.titleLarge)
    MenuCard("🎯  Анализ мяча", "Захват экрана и распознавание мяча — следующий этап разработки.", "Открыть") { onBall() }
    MenuCard("🏋️  Тренировки", "Счётчик приёмов, передач и подач с сохранением на телефоне.", "Тренироваться") { onTraining() }
    MenuCard("⚙️  Профили стилей", "Выбери профиль для тестирования прогнозной модели.", "Настроить") { onStyles() }
    MenuCard("📚  Советы и гайды", "Подсказки по позиционированию, приёму, сетам и тренировкам.", "Читать") { onGuide() }
    MenuCard("📊  Статистика", "Твои записанные тренировочные действия.", "Открыть") { onStats() }
    MenuCard("🔧  Настройки", "Разрешения и информация о приложении.", "Открыть") { onSettings() }
}

@Composable
private fun MenuCard(title: String, description: String, action: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium)
            OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(action) }
        }
    }
}

@Composable
private fun BallScreen(onBack: () -> Unit) {
    Text("Анализ мяча", style = MaterialTheme.typography.titleLarge)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Статус: разработка", style = MaterialTheme.typography.titleMedium)
            Text("Планируемые инструменты: системный захват экрана Android с твоего разрешения, поиск мяча на кадрах, отслеживание движения, линия примерной траектории и отметка предполагаемой точки приземления.")
            Text("Важно: сейчас автоматическое распознавание ещё не работает. Предсказание будет приблизительным и потребует проверки на разных игровых ситуациях.")
            Text("Запись экрана должна включаться и останавливаться тобой через системное окно Android.")
        }
    }
    BackButton(onBack)
}

@Composable
private fun TrainingScreen(
    receives: Int, sets: Int, serves: Int,
    onReceive: () -> Unit, onSet: () -> Unit, onServe: () -> Unit, onBack: () -> Unit
) {
    Text("Тренировочная сессия", style = MaterialTheme.typography.titleLarge)
    Text("Нажимай после каждого выполненного упражнения. Счётчики сохраняются на телефоне.")
    CounterCard("Приём мяча", receives, "Записать приём", onReceive)
    CounterCard("Передача / сет", sets, "Записать передачу", onSet)
    CounterCard("Подача", serves, "Записать подачу", onServe)
    BackButton(onBack)
}

@Composable
private fun CounterCard(title: String, count: Int, action: String, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(count.toString(), style = MaterialTheme.typography.headlineSmall)
            }
            Button(onClick = onClick, modifier = Modifier.fillMaxWidth()) { Text(action) }
        }
    }
}

@Composable
private fun StylesScreen(selected: String, onSelect: (String) -> Unit, onBack: () -> Unit) {
    Text("Профили стилей", style = MaterialTheme.typography.titleLarge)
    val profiles = listOf(
        "Общий" to "Универсальный базовый профиль.",
        "Быстрый" to "Экспериментальный профиль для быстрых атак.",
        "Кручёный" to "Профиль для нестандартной траектории.",
        "Подача" to "Отдельный профиль для подачи.",
        "Особый" to "Условный профиль для специальных приёмов."
    )
    profiles.forEach { (name, description) ->
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
            containerColor = if (selected == name) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
        )) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(name, style = MaterialTheme.typography.titleMedium)
                Text(description)
                OutlinedButton(onClick = { onSelect(name) }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (selected == name) "Выбран" else "Выбрать")
                }
            }
        }
    }
    Text("Профили пока переключают только настройку приложения; точная игровая физика ещё не откалибрована.")
    BackButton(onBack)
}

@Composable
private fun GuideScreen(onBack: () -> Unit) {
    Text("Советы и гайды", style = MaterialTheme.typography.titleLarge)
    val tips = listOf(
        "Приём" to "Следи за направлением мяча и старайся заранее занимать удобную позицию.",
        "Передача" to "Тренируй стабильную высоту и направление передач, а не только скорость.",
        "Подача" to "Повторяй один тип подачи сериями и отмечай, какие попытки получаются точнее.",
        "Чтение игры" to "Наблюдай за моментом удара и изменением направления мяча.",
        "Разбор повтора" to "Если доступна запись матча, просматривай эпизоды на обычной скорости и замедленно."
    )
    tips.forEach { (title, text) ->
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(text)
            }
        }
    }
    BackButton(onBack)
}

@Composable
private fun StatsScreen(receives: Int, sets: Int, serves: Int, onReset: () -> Unit, onBack: () -> Unit) {
    Text("Статистика", style = MaterialTheme.typography.titleLarge)
    CounterCard("Записано приёмов", receives, "—") {}
    CounterCard("Записано передач", sets, "—") {}
    CounterCard("Записано подач", serves, "—") {}
    Text("Это ручные тренировочные отметки, не автоматическая статистика Roblox.")
    OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth()) { Text("Сбросить статистику") }
    BackButton(onBack)
}

@Composable
private fun SettingsScreen(selectedProfile: String, onOpenAccessibility: () -> Unit, onBack: () -> Unit) {
    Text("Настройки", style = MaterialTheme.typography.titleLarge)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Текущий профиль: $selectedProfile", style = MaterialTheme.typography.titleMedium)
            Text("Специальные возможности Android")
            Text("Служба приложения не нажимает кнопки и не управляет Roblox. Для будущего анализа видео будет отдельное системное разрешение на захват экрана.")
            Button(onClick = onOpenAccessibility, modifier = Modifier.fillMaxWidth()) { Text("Открыть настройки Android") }
        }
    }
    BackButton(onBack)
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    Spacer(Modifier.height(4.dp))
    OutlinedButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("← Назад в меню") }
}
