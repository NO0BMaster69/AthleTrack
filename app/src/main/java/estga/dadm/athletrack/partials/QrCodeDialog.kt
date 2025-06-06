package estga.dadm.athletrack.partials

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import estga.dadm.athletrack.api.User
import java.net.URLEncoder

/**
 * Componente que exibe um diálogo contendo um código QR e opções de interação.
 *
 * @param qrCode Código QR a ser exibido.
 * @param onDismiss Função chamada ao fechar o diálogo.
 * @param user Objeto do usuário utilizado para navegação.
 * @param navController Controlador de navegação para redirecionar o usuário.
 */
@Composable
fun QrCodeDialog(
    qrCode: String,
    onDismiss: () -> Unit,
    user: User,
    navController: NavHostController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background.copy(alpha = 0.5f))
            .clickable(onClick = onDismiss)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
                .width(360.dp)
                .background(colorScheme.primary, shape = RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 24.dp, horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "Código QR",
                    style = typography.displayLarge,
                    color = colorScheme.inversePrimary,
                )

                QrCodeGenerator(data = qrCode)

                Button(
                    onClick = {
                        val userJson = URLEncoder.encode(Gson().toJson(user), "UTF-8")
                        val encodedQrCode = URLEncoder.encode(qrCode, "UTF-8")
                        navController.navigate("gestaoPresencas/$userJson/$encodedQrCode")
                    },
                    modifier = Modifier
                        .width(240.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "Gerir Presenças",
                        style = typography.labelMedium,
                        textAlign = TextAlign.Center,
                        color = colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Componente que gera e exibe um código QR com base nos dados fornecidos.
 *
 * @param data Dados a serem codificados no código QR.
 * @param size Tamanho do código QR gerado (padrão: 768).
 */
@Composable
fun QrCodeGenerator(data: String, size: Int = 768) {
    val bitmap = remember(data) {
        generateQrCodeBitmap(data, size)
    }

    bitmap?.let {
        Image(bitmap = it.asImageBitmap(), contentDescription = "QR Code")
    }
}

/**
 * Função que gera um bitmap de código QR com base nos dados fornecidos.
 *
 * @param data Dados a serem codificados no código QR.
 * @param size Tamanho do código QR gerado.
 * @return Bitmap contendo o código QR ou `null` em caso de erro.
 */
fun generateQrCodeBitmap(data: String, size: Int): Bitmap? {
    return try {
        val bitMatrix = QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, size, size)
        val bmp = createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp[x, y] = if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE
            }
        }
        bmp
    } catch (e: Exception) {
        null
    }
}