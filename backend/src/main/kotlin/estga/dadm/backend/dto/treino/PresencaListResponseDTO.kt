package estga.dadm.backend.dto.treino

/**
 * DTO de resposta para listagem de presenças em um treino.
 *
 * Contém informações do sócio, seu estado de presença e se a presença foi registrada via QR Code.
 *
 * @property id Identificador único do sócio.
 * @property nome Nome do sócio.
 * @property estado Estado da presença (true = presente, false = ausente).
 * @property qrCode Indica se a presença foi registrada via QR Code.
 */
data class PresencaListResponseDTO(
    val id: Int,            // ID do sócio
    val nome: String,       // Nome do sócio
    var estado: Boolean,    // Estado da presença (true = presente)
    var qrCode: Boolean     // Indica se a presença foi registrada via QR Code
)