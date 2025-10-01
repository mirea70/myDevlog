package com.mydevlog.mydevlog.api.media

import com.mydevlog.mydevlog.MyDevlogApplication
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.multipart
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

@SpringBootTest(classes = [MyDevlogApplication::class])
@AutoConfigureMockMvc
class AdminMediaControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    private fun pngBytes(width: Int = 10, height: Int = 10): ByteArray {
        val img = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g = img.createGraphics()
        g.color = Color.RED
        g.fillRect(0, 0, width, height)
        g.dispose()
        val baos = ByteArrayOutputStream()
        ImageIO.write(img, "png", baos)
        return baos.toByteArray()
    }

    @Test
    fun successful_upload_returns_201_and_metadata() {
        val file = MockMultipartFile("file", "test.png", "image/png", pngBytes())
        val mvcResult = mockMvc.multipart("/api/admin/media") {
            file(file)
            header("X-Admin", "true")
        }.andExpect { status { isCreated() } }.andReturn()
        val body = mvcResult.response.contentAsString
        assert(body.contains("publicUrl"))
        assert(body.contains("\"mime\":\"image/png\""))
    }

    @Test
    fun reject_oversized_file_with_413() {
        val big = ByteArray(6 * 1024 * 1024) { 0 }
        val file = MockMultipartFile("file", "big.png", "image/png", big)
        mockMvc.multipart("/api/admin/media") {
            file(file)
            header("X-Admin", "true")
        }.andExpect { status { isPayloadTooLarge() } }
    }

    @Test
    fun reject_disallowed_mime_with_415() {
        val file = MockMultipartFile("file", "file.png", "image/png", "not an image".toByteArray())
        mockMvc.multipart("/api/admin/media") {
            file(file)
            header("X-Admin", "true")
        }.andExpect { status { isUnsupportedMediaType() } }
    }

    @Test
    fun reject_extension_mismatch_with_415() {
        val file = MockMultipartFile("file", "file.jpg", "image/jpeg", pngBytes())
        mockMvc.multipart("/api/admin/media") {
            file(file)
            header("X-Admin", "true")
        }.andExpect { status { isUnsupportedMediaType() } }
    }

    @Test
    fun delete_returns_204() {
        val file = MockMultipartFile("file", "test.png", "image/png", pngBytes())
        val mvcResult = mockMvc.multipart("/api/admin/media") {
            file(file)
            header("X-Admin", "true")
        }.andExpect { status { isCreated() } }.andReturn()
        val id = Regex("\"id\":\"([a-f0-9-]+)\"").find(mvcResult.response.contentAsString)!!.groupValues[1]
        mockMvc.delete("/api/admin/media/{id}", UUID.fromString(id)) {
            header("X-Admin", "true")
        }.andExpect { status { isNoContent() } }
    }
}
