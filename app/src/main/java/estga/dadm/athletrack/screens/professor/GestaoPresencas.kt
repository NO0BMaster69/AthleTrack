package estga.dadm.athletrack.screens.professor

import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import estga.dadm.athletrack.ui.theme.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import estga.dadm.athletrack.api.User
import estga.dadm.athletrack.other.LoadingScreen
import estga.dadm.athletrack.viewmodels.GestaoPresencasViewModel

/**
 * Tela de gestão de presenças que permite visualizar e atualizar a lista de presenças dos atletas em um treino específico.
 *
 * @param user Objeto do usuário logado, contendo informações como ID de sócio.
 * @param qrCode Código QR associado ao treino para carregar as presenças.
 * @param navController Controlador de navegação para gerenciar rotas entre telas.
 * @param viewModel ViewModel responsável por gerenciar os dados e ações da tela.
 */
@Composable
fun GestaoPresencas(
    user: User,
    qrCode: String,
    navController: NavHostController,
    viewModel: GestaoPresencasViewModel = viewModel()
) {
    // Estado que armazena as informações do treino.
    val treinoInfo by viewModel.treinoInfo.collectAsState()
    // Estado que armazena a lista de atletas.
    val atletas by viewModel.alunos.collectAsState()

    // Define se a tela está carregando com base nos estados.
    val isLoading = treinoInfo == null && atletas.isEmpty()

    // Carrega as informações do treino e a lista de presenças ao iniciar a tela.
    LaunchedEffect(qrCode) {
        viewModel.carregarPresencas(qrCode, user.idSocio)
    }

    // Exibe a tela principal com as informações do treino e a lista de presenças.
    LoadingScreen(isLoading = isLoading) {
        Scaffold(
            topBar = {
                // Barra superior com título e botão de voltar.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.statusBars)
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Gestão de Presenças",
                        style = Typography.displayLarge,
                        color = colorScheme.primary
                    )
                }
            },
            containerColor = colorScheme.surface
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Exibe as informações do treino.
                treinoInfo?.let { treino ->
                    Text(
                        text = "${treino.nomeModalidade} - ${treino.diaSemana}",
                        style = Typography.displayLarge,
                        color = colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Hora: ${treino.hora}",
                        style = Typography.titleMedium,
                        color = colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Lista de atletas com estilização e controle de presença.
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(atletas) { atleta ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .background(
                                    colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = atleta.nome,
                                    style = Typography.bodyLarge,
                                    color = colorScheme.primary
                                )
                                Text(
                                    text = "ID: ${atleta.id}",
                                    style = Typography.labelSmall,
                                    color = colorScheme.secondary
                                )
                            }
                            Checkbox(
                                checked = atleta.estado,
                                onCheckedChange = {
                                    if (!atleta.qrCode) {
                                        viewModel.atualizarPresenca(atleta.id, it)
                                    }
                                },
                                enabled = !atleta.qrCode // Desativa se a presença foi registrada por QR Code.
                            )
                        }
                    }
                }

                // Botão para salvar as presenças.
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        viewModel.salvarPresencas(qrCode)
                        navController.popBackStack() // Volta para a página anterior.
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Salvar Presenças",
                        color = colorScheme.inversePrimary
                    )
                }
            }
        }
    }
}