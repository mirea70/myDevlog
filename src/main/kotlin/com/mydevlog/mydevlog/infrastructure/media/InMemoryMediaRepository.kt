package com.mydevlog.mydevlog.infrastructure.media

import com.mydevlog.mydevlog.domain.media.Media
import org.springframework.stereotype.Repository
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryMediaRepository {
    private val store = ConcurrentHashMap<UUID, Media>()

    fun save(media: Media): Media {
        store[media.id] = media
        return media
    }

    fun findById(id: UUID): Media? = store[id]

    fun deleteById(id: UUID) { store.remove(id) }

    fun clear() { store.clear() }
}
