package estga.dadm.backend.dto.treino

data class TreinoProfResponseDTO(
    val idTreino: Int,
    val nomeModalidade: String,
    val diaSemana: String,
    val hora: String,
    val qrCode: String
)