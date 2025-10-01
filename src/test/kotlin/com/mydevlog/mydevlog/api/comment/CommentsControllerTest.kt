package com.mydevlog.mydevlog.api.comment

import com.mydevlog.mydevlog.MyDevlogApplication
import com.mydevlog.mydevlog.domain.post.Post
import com.mydevlog.mydevlog.infrastructure.post.InMemoryPostRepository
import org.junit.jupiter.api.BeforeEach
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
import java.time.Instant
import java.util.*

@SpringBootTest(classes = [MyDevlogApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CommentsControllerTest {

    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var postRepo: InMemoryPostRepository

    lateinit var post: Post

    @BeforeEach
    fun setup() {
        val now = Instant.now()
        post = Post(
            id = UUID.randomUUID(),
            slug = "hello-world",
            title = "Hello",
            content = "content words",
            summary = null,
            coverImageUrl = null,
            tags = listOf("kotlin"),
            category = "dev",
            updatedAt = now,
            createdAt = now,
            authorId = UUID.randomUUID(),
            featured = false,
            readingMinutes = 1,
        )
        postRepo.save(post)
    }

    @Test
    fun create_list_update_delete_comment_flow() {
        // create
        val createBody = """{"content":"Nice post!","parentId":null}"""
        val createRes = mockMvc.post("/api/posts/{slug}/comments", post.slug) {
            header("X-User-Id", UUID.randomUUID().toString())
            contentType = MediaType.APPLICATION_JSON
            content = createBody
        }.andExpect { status { isCreated() } }.andReturn()
        val id = Regex("\"id\":\"([a-f0-9-]+)\"").find(createRes.response.contentAsString)!!.groupValues[1]

        // list
        mockMvc.get("/api/posts/{slug}/comments", post.slug)
            .andExpect { status { isOk() } }
            .andExpect { content { string(org.hamcrest.Matchers.containsString(id)) } }

        // update by author
        val newContent = """{"content":"Edited!"}"""
        mockMvc.put("/api/comments/{id}", UUID.fromString(id)) {
            header("X-User-Id", extractAuthorId(createRes.response.contentAsString))
            contentType = MediaType.APPLICATION_JSON
            content = newContent
        }.andExpect { status { isOk() } }

        // delete by admin
        mockMvc.delete("/api/comments/{id}", UUID.fromString(id)) {
            header("X-Admin", "true")
        }.andExpect { status { isNoContent() } }
    }

    private fun extractAuthorId(json: String): String {
        val m = Regex("\"authorId\":\"([a-f0-9-]+)\"").find(json)
        return m?.groupValues?.get(1) ?: UUID.randomUUID().toString()
    }
}
