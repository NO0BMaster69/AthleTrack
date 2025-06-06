package estga.dadm.athletrack.screens.calendar

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.compose.ui.platform.LocalContext
import estga.dadm.athletrack.ui.theme.*
import kotlinx.coroutines.launch
import estga.dadm.athletrack.api.User
import estga.dadm.athletrack.viewmodels.AdicionarEventoViewModel
import java.time.LocalDate
import java.time.LocalTime
import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import com.google.gson.Gson
import estga.dadm.athletrack.api.Modalidade
import estga.dadm.athletrack.other.FloatingPopupToast
import java.net.URLEncoder

import java.util.*

/**
 * Tela para adicionar um novo evento ao calendário.
 *
 * @param user Objeto do usuário contendo informações como ID de sócio.
 * @param navController Controlador de navegação para redirecionar o usuário entre telas.
 * @param selectedDate Data inicial selecionada para o evento.
 * @param viewModel ViewModel responsável por gerenciar os dados e ações da tela.
 */
@Composable
fun AdicionarEventoScreen(
    user: User,
    navController: NavHostController,
    selectedDate: LocalDate,
    viewModel: AdicionarEventoViewModel = viewModel()
) {
    // Estado para armazenar os valores do evento.
    var data by remember { mutableStateOf(selectedDate) }
    var hora by remember { mutableStateOf(LocalTime.now()) }
    var local by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    val modalidades by viewModel.modalidades.collectAsState()
    val modalidadesSelecionadas = remember { mutableStateListOf<Modalidade>() }
    var isLoading by remember { mutableStateOf(true) }
    var isDropdownExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var showToast by remember { mutableStateOf(false) }
    var toastMessage by remember { mutableStateOf("") }
    var isToastSuccess by remember { mutableStateOf(true) }

    // Carrega as modalidades ao iniciar a tela.
    LaunchedEffect(Unit) {
        isLoading = true
        try {
            viewModel.carregarModalidades()
        } catch (e: Exception) {
            println("Erro ao carregar modalidades: ${e.message}")
        }
        isLoading = false
    }

    // Estrutura principal da tela.
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
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        },
        containerColor = colorScheme.surface,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->

        if (isLoading) {
            // Exibe um indicador de carregamento enquanto as modalidades são carregadas.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = colorScheme.primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Adicionar Evento",
                    style = typography.displayLarge,
                    color = colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botão para selecionar a data do evento.
                OutlinedButton(
                    onClick = {
                        val datePicker = DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                data = LocalDate.of(year, month + 1, dayOfMonth)
                            },
                            data.year,
                            data.monthValue - 1,
                            data.dayOfMonth
                        )
                        datePicker.show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, colorScheme.onPrimary),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.secondary
                    )
                ) {
                    Text("Data: ${data.toString()}")
                }

                // Botão para selecionar a hora do evento.
                OutlinedButton(
                    onClick = {
                        val timePicker = TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                hora = LocalTime.of(hourOfDay, minute)
                            },
                            hora.hour,
                            hora.minute,
                            true
                        )
                        timePicker.show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, colorScheme.onPrimary),
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = colorScheme.primaryContainer,
                        contentColor = colorScheme.secondary
                    )
                ) {
                    Text("Hora: ${hora.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"))}")
                }

                // Campo de texto para inserir o local do evento.
                OutlinedTextField(
                    value = local,
                    onValueChange = { local = it },
                    label = { Text("Local") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.secondary,
                        focusedLabelColor = colorScheme.primary,
                        unfocusedLabelColor = colorScheme.secondary,
                        cursorColor = colorScheme.primary
                    )
                )

                // Campo de texto para inserir a descrição do evento.
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = { Text("Descrição") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorScheme.primary,
                        unfocusedBorderColor = colorScheme.secondary,
                        focusedLabelColor = colorScheme.primary,
                        unfocusedLabelColor = colorScheme.secondary,
                        cursorColor = colorScheme.primary
                    ),
                    maxLines = 5
                )

                // MultiSelect para selecionar modalidades.
                Text("Modalidades", color = colorScheme.primary)
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = { isDropdownExpanded = !isDropdownExpanded },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = colorScheme.primaryContainer,
                            contentColor = colorScheme.secondary
                        ),
                        border = BorderStroke(1.dp, colorScheme.onPrimary),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (modalidadesSelecionadas.isEmpty()) "Selecionar Modalidades" else modalidadesSelecionadas.joinToString { it.nomeModalidade },
                            color = colorScheme.primary
                        )
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        tonalElevation = 0.dp,
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .background(colorScheme.primaryContainer)
                    ) {
                        modalidades.forEach { modalidade ->
                            DropdownMenuItem(
                                onClick = {
                                    if (modalidade in modalidadesSelecionadas) {
                                        modalidadesSelecionadas.remove(modalidade)
                                    } else {
                                        modalidadesSelecionadas.add(modalidade)
                                    }
                                },
                                modifier = Modifier.fillMaxSize(),
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Checkbox(
                                            checked = modalidade in modalidadesSelecionadas,
                                            onCheckedChange = null,
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = colorScheme.primary,
                                                uncheckedColor = colorScheme.secondary
                                            )
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(modalidade.nomeModalidade, color = colorScheme.primary)
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botão para salvar o evento.
                Button(
                    onClick = {
                        if (local.isBlank() || descricao.isBlank() || modalidadesSelecionadas.isEmpty()) {
                            toastMessage = "Preencha todos os campos obrigatórios"
                            isToastSuccess = false
                            showToast = true
                        } else {
                            coroutineScope.launch {
                                try {
                                    viewModel.adicionarEvento(
                                        idSocio = user.idSocio,
                                        data = data.toString(),
                                        hora = hora.toString(),
                                        local = local,
                                        descricao = descricao,
                                        modalidades = modalidadesSelecionadas.map { it.id },
                                        onSuccess = {
                                            toastMessage = "Evento criado com sucesso"
                                            isToastSuccess = true
                                            showToast = true

                                            // Navegação após sucesso.
                                            val userJson =
                                                URLEncoder.encode(Gson().toJson(user), "UTF-8")
                                            navController.navigate("calendar/$userJson") {
                                                popUpTo("calendar/$userJson") { inclusive = true }
                                            }
                                        },
                                        onError = { errorMessage ->
                                            coroutineScope.launch {
                                                toastMessage = "Erro ao criar evento: $errorMessage"
                                                isToastSuccess = false
                                                showToast = true
                                            }
                                        }
                                    )
                                } catch (e: Exception) {
                                    coroutineScope.launch {
                                        toastMessage =
                                            "Erro ao criar evento: ${e.message ?: "Erro desconhecido"}"
                                        isToastSuccess = false
                                        showToast = true
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary)
                ) {
                    Text("Guardar", color = colorScheme.background)
                }
            }
            if (showToast) {
                FloatingPopupToast(
                    message = toastMessage,
                    icon = if (isToastSuccess) Icons.Default.Check else Icons.Default.Warning,
                    color = if (isToastSuccess) GreenSuccess else MaterialTheme.colorScheme.error
                ) {
                    showToast = false
                }
            }
        }
    }
}