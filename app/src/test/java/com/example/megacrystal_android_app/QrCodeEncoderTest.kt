package com.example.megacrystal_android_app

import com.example.megacrystal_android_app.util.QrCodeEncoder
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import org.junit.Assert.assertEquals
import org.junit.Test

class QrCodeEncoderTest {
    @Test
    fun encodedQrDecodesBackToThePaymentPayload() {
        val payload = "megacrystal://payments/confirm?token=" + "a".repeat(64)

        val matrix = QrCodeEncoder.encode(payload)
        val pixels = IntArray(matrix.width * matrix.height)
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                pixels[y * matrix.width + x] =
                    if (matrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
            }
        }

        val source = RGBLuminanceSource(matrix.width, matrix.height, pixels)
        val decoded = MultiFormatReader()
            .decode(BinaryBitmap(HybridBinarizer(source)))
            .text

        assertEquals(payload, decoded)
    }
}
