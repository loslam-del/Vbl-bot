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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                VblHome(onOpenAccessibility = {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                })
            }
        }
    }
}

@Composable
private fun VblHome(onOpenAccessibility: () -> Unit) {
    var xText by remember { mutableStateOf("500") }
    var yText by remember { mutableStateOf("300") }
    var profile by remember { mutableStateOf(ShotProfile.GENERAL) }
    var result by remember { mutableStateOf("Добавь минимум три наблюдения координат мяча.") }
    val observations = remember { mutableStateListOf<BallPoint>() }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("VBL-BOT", style = MaterialTheme.typography.headlineLarge)
            Text("Помощник для тренировок Volleyball Legends")

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Служба специальных возможностей", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(6.dp))
                    Text(if (VblServiceState.connected) "Подключена" else "Не включена")
                    Spacer(Modifier.height(10.dp))
                    Button(onClick = onOpenAccessibility, modifier = Modifier.fillMaxWidth()) {
                        Text("Открыть настройки Android")
                    }
                    Text("Служба не нажимает кнопки и не управляет Roblox.", style = MaterialTheme.typography.bodySmall)
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Тест прогнозирования траектории", style = MaterialTheme.typography.titleMedium)
                    Text("Введи координаты мяча с трёх последовательных кадров. Пока это ручной тест алгоритма.")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = xText, onValueChange = { xText = it }, label = { Text("X") }, modifier = Modifier.weight(1f), singleLine = true)
                        OutlinedTextField(value = yText, onValueChange = { yText = it }, label = { Text("Y") }, modifier = Modifier.weight(1f), singleLine = true)
                    }
                    Text("Профиль удара: \${profile.label}")
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(onClick = { profile = ShotProfile.GENERAL }) { Text("Общий") }
                        Button(onClick = { profile = ShotProfile.FAST }) { Text("Быстрый") }
                        Button(onClick = { profile = ShotProfile.FLOAT }) { Text("Кручёный") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(onClick = { profile = ShotProfile.SERVE }) { Text("Подача") }
                        Button(onClick = { profile = ShotProfile.SPECIAL }) { Text("Особый") }
                    }
                    Button(onClick = {
                        val x = xText.toFloatOrNull()
                        val y = yText.toFloatOrNull()
                        if (x == null || y == null) {
                            result = "Введи числовые координаты X и Y."
                        } else {
                            observations.add(BallPoint(x, y, System.currentTimeMillis()))
                            if (observations.size > 8) observations.removeAt(0)
                            val prediction = TrajectoryPredictor.predict(observations, profile)
                            result = if (prediction == null) {
                                "Наблюдение добавлено: \${observations.size}. Нужно минимум 3."
                            } else {
                                "Прогноз на экране: X=\${prediction.x.toInt()}, Y=\${prediction.y.toInt()}\\n" +
                                    "Условная уверенность: \${(prediction.confidence * 100).toInt()}%\\n" + prediction.note
                            }
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text("Добавить координату и пересчитать") }
                    Button(onClick = {
                        observations.clear()
                        result = "Наблюдения очищены."
                    }, modifier = Modifier.fillMaxWidth()) { Text("Сбросить наблюдения") }
                    Text("Точек записано: \${observations.size}")
                    Text(result, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Следующий этап", style = MaterialTheme.typography.titleMedium)
                    Text("Захват экрана с явным разрешением Android и автоматическое обнаружение мяча. Сейчас координаты вводятся вручную; живое распознавание ещё не подключено.")
                }
            }
        }
    }
}
