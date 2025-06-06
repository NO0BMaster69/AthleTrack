package estga.dadm.backend.controller

import estga.dadm.backend.dto.IdRequestDTO
import estga.dadm.backend.dto.treino.*
import estga.dadm.backend.keys.PresencaId
import estga.dadm.backend.model.Presenca
import estga.dadm.backend.repository.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * Controlador REST para operações relacionadas a presenças em treinos.
 *
 * Fornece endpoints para registar presença via QR code, registar presenças manualmente
 * e listar presenças de um treino específico.
 */
@RestController
@RequestMapping("/api/presencas")
class PresencaController(
    private val userRepository: UserRepository,
    private val treinoRepository: TreinoRepository,
    private val presencaRepository: PresencaRepository,
    private val socioModalidadeRepository: SocioModalidadeRepository
) {

    /**
     * Regista a presença de um sócio num treino através do QR code.
     *
     * @param request Dados da presença, incluindo o QR code e o ID do sócio.
     * @return ResponseEntity com o resultado do registo da presença.
     */
    @PostMapping("/registar")
    fun registarPresencaQr(@RequestBody request: PresencaRequestDTO): ResponseEntity<PresencaResponseDTO> {
        val treino = treinoRepository.findByQrCode(request.qrCode)
            ?: return ResponseEntity.badRequest()
                .body(PresencaResponseDTO(sucesso = false, mensagem = "QR Code inválido."))

        val aluno = userRepository.findById(request.idSocio).orElse(null)
            ?: return ResponseEntity.badRequest()
                .body(PresencaResponseDTO(sucesso = false, mensagem = "Sócio não encontrado."))

        val modalidadesDoAluno = socioModalidadeRepository.findBySocioId(request.idSocio)
            .map { it.modalidade.id }

        if (treino.modalidade.id !in modalidadesDoAluno) {
            return ResponseEntity.status(403)
                .body(PresencaResponseDTO(sucesso = false, mensagem = "Aluno não inscrito nesta modalidade."))
        }

        val presencaId = PresencaId(
            socio = request.idSocio,
            treino = treino.id
        )

        if (presencaRepository.existsById(presencaId)) {
            return ResponseEntity.status(208)
                .body(PresencaResponseDTO(sucesso = false, mensagem = "Presença já confirmada anteriormente."))
        }

        val novaPresenca = Presenca(
            socio = aluno,
            treino = treino,
            estado = true,
            qrCode = true
        )

        presencaRepository.save(novaPresenca)

        return ResponseEntity.ok(PresencaResponseDTO(sucesso = true, mensagem = "Presença registada com sucesso."))
    }

    /**
     * Regista presenças manualmente para uma lista de sócios e treinos.
     *
     * @param requests Lista de dados de presença a serem registados.
     * @return ResponseEntity indicando sucesso ou falha do registo.
     */
    @PostMapping("/registarmanual")
    fun registarPresencasManuais(@RequestBody requests: List<PresencaRequestDTO>): ResponseEntity<Boolean> {
        return try {
            requests.forEach { request ->
                if (request.qrCode.isBlank()) return@forEach

                val treino = treinoRepository.findByQrCode(request.qrCode) ?: return@forEach
                val aluno = userRepository.findById(request.idSocio).orElse(null) ?: return@forEach
                val presencaId = PresencaId(aluno.id, treino.id)
                val presencaExistente = presencaRepository.findById(presencaId).orElse(null)

                if (presencaExistente != null) {
                    presencaExistente.estado = request.estado
                    presencaRepository.save(presencaExistente)
                } else {
                    val nova = Presenca(
                        socio = aluno,
                        treino = treino,
                        estado = request.estado,
                        qrCode = false
                    )
                    presencaRepository.save(nova)
                }
            }

            ResponseEntity.ok(true)
        } catch (e: Exception) {
            println("Erro ao guardar presenças manuais: ${e.message}")
            ResponseEntity.internalServerError().body(false)
        }
    }

    /**
     * Lista as presenças de um treino específico, indicando o estado de presença de cada sócio.
     *
     * @param request Objeto contendo o ‘ID’ do treino.
     * @return Lista de presenças dos sócios para o treino especificado.
     */
    @PostMapping("/listar")
    fun listarPresencas(@RequestBody request: IdRequestDTO): List<PresencaListResponseDTO> {
        val treino = treinoRepository.findById(request.id).orElse(null)
        if (treino == null) {
            return emptyList()
        }

        val modalidade = treino.modalidade

        // Obtém todos os sócios da modalidade, excluindo os do tipo "professor"
        val alunos = socioModalidadeRepository.findByModalidadeId(modalidade.id)
            .map { it.socio }
            .filter { it.tipo.lowercase() != "professor" }

        val presencasSimuladas = alunos.map { socio ->
            PresencaListResponseDTO(
                id = socio.id,
                nome = socio.nome,
                estado = false,
                qrCode = false
            )
        }

        presencasSimuladas.forEach { presenca ->
            val presencaReal = presencaRepository.findBySocioIdAndTreinoId(
                socioId = presenca.id,
                treinoId = request.id
            )

            if (presencaReal?.estado == true) {
                if (presencaReal.qrCode) {
                    presenca.estado = true
                    presenca.qrCode = true
                } else if (!presencaReal.qrCode) {
                    presenca.estado = true
                    presenca.qrCode = false
                }
            } else {
                presenca.estado = false
                presenca.qrCode = false
            }
        }

        return presencasSimuladas
    }
}