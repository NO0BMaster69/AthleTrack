package estga.dadm.athletrack.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.google.gson.Gson
import estga.dadm.athletrack.ui.theme.*
import estga.dadm.athletrack.api.User
import estga.dadm.athletrack.viewmodels.CalendarViewModel
import java.net.URLEncoder
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import estga.dadm.athletrack.ui.theme.*

/**
 *
 */
@Composable
fun CalendarScreen(
    user: User,
    navController: NavHostController,
    viewModel: CalendarViewModel = viewModel()
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val eventos by viewModel.eventos.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()

    LaunchedEffect(currentMonth) {
        viewModel.carregarEventosParaMes(user.idSocio)
    }

    val eventosFiltrados = eventos.filter { evento ->
        val dataEvento = LocalDate.parse(evento.data)
        dataEvento == selectedDate
    }
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7
    val monthLabel = currentMonth.month.getDisplayName(TextStyle.FULL, Locale("pt", "BR"))
        .replaceFirstChar { it.uppercase() }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    navController.popBackStack()
                }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Voltar",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        },
        containerColor = colorScheme.surface
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Próximos Eventos",
                style = typography.displayLarge,
                color = colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                colors = CardDefaults.cardColors(containerColor = colorScheme.primaryContainer)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onClick = {
                            viewModel.irParaMesAnterior()
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Mês anterior",
                                tint = colorScheme.primary
                            )
                        }
                        Text(
                            "$monthLabel ${currentMonth.year}",
                            color = colorScheme.primary
                        )
                        IconButton(onClick = {
                            viewModel.irParaMesSeguinte()
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Próximo mês",
                                tint = colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                            Text(it, style = Typography.labelSmall, color = colorScheme.secondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val totalSlots = firstDayOfWeek + daysInMonth
                    val weeks = (totalSlots + 6) / 7
                    for (week in 0 until weeks) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            for (dayOfWeek in 0..6) {
                                val dayIndex = week * 7 + dayOfWeek
                                val day = dayIndex - firstDayOfWeek + 1

                                if (day in 1..daysInMonth) {
                                    val date = currentMonth.atDay(day)
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (date == selectedDate) colorScheme.secondary else colorScheme.tertiary
                                            )
                                            .clickable {
                                                viewModel.selecionarData(date)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$day",
                                            style = Typography.labelMedium,
                                            color = colorScheme.primary
                                        )
                                    }
                                } else {
                                    Spacer(modifier = Modifier.size(36.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Exibir botão apenas se o tipo for "Professor"
                    if (user.tipo.lowercase() == "professor") {
                        Button(
                            onClick = {
                                val userJson = URLEncoder.encode(Gson().toJson(user), "UTF-8")
                                navController.navigate("adicionarEvento/$userJson/${selectedDate.toString()}")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.primary,
                                contentColor = colorScheme.background
                            )
                        ) {
                            Text(
                                text = "Adicionar Evento",
                                style = typography.labelMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(eventosFiltrados) { evento ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .background(colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(evento.localEvento, color = colorScheme.primary)
                            Text(evento.hora, style = Typography.labelSmall, color = colorScheme.secondary)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${selectedDate.dayOfMonth}",
                                style = Typography.titleMedium,
                                color = colorScheme.primary
                            )
                            Text(
                                selectedDate.month.getDisplayName(TextStyle.SHORT, Locale("pt", "BR"))
                                    .replaceFirstChar { it.uppercase() },
                                style = Typography.labelSmall,
                                color = colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}
// Composable de pré-visualização para desenvolvimento no Android Studio
//
//@Preview(showBackground = false)
//@Composable
//fun CalendarScreenPreview() {
//    CalendarScreen(userName = "João")
//}

