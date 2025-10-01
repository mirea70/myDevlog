package com.mydevlog.mydevlog.api.post

import com.mydevlog.mydevlog.MyDevlogApplication
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.util.*

@SpringBootTest(classes = [MyDevlogApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PostsControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Test
    fun admin_create_then_get_list_update_delete() {
        val authorId = UUID.randomUUID()
        val createBody = """
            {
              "title": "Hello World",
              "content": "This is my first post with some words to read.",
              "summary": "Intro",
              "coverImageUrl": null,
              "tags": ["intro","hello"],
              "category": "general",
              "authorId": "$authorId",
              "featured": true
            }
        """.trimIndent()
        val createRes = mockMvc.post("/api/admin/posts") {
            header("X-Admin", "true")
            contentType = MediaType.APPLICATION_JSON
            content = createBody
        }.andExpect { status { isCreated() } }.andReturn()
        val slug = Regex("\"slug\":\"([^\"]+)\"").find(createRes.response.contentAsString)!!.groupValues[1]
        val id = Regex("\"id\":\"([a-f0-9-]+)\"").find(createRes.response.contentAsString)!!.groupValues[1]

        // get by slug
        mockMvc.get("/api/posts/{slug}", slug)
            .andExpect { status { isOk() } }

        // list and featured
        mockMvc.get("/api/posts")
            .andExpect { status { isOk() } }
        mockMvc.get("/api/posts/featured")
            .andExpect { status { isOk() } }

        // update
        val updateBody = """
            {
              "title": "Hello World Updated",
              "content": "This is my first post updated content.",
              "summary": "Intro2",
              "coverImageUrl": null,
              "tags": ["intro"],
              "category": "general",
              "featured": false
            }
        """.trimIndent()
        mockMvc.put("/api/admin/posts/{id}", UUID.fromString(id)) {
            header("X-Admin", "true")
            contentType = MediaType.APPLICATION_JSON
            content = updateBody
        }.andExpect { status { isOk() } }

        // delete
        mockMvc.delete("/api/admin/posts/{id}", UUID.fromString(id)) {
            header("X-Admin", "true")
        }.andExpect { status { isNoContent() } }
    }
}
