package ru.netology.nmedia.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nmedia.entity.PostRemoteKeyEntity

@Dao
interface PostRemoteKeyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(key: PostRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(keys: List<PostRemoteKeyEntity>)

    @Query("SELECT * FROM posts_remote_keys WHERE type = :type")
    suspend fun getByType(type: String): PostRemoteKeyEntity?

    @Query("DELETE FROM posts_remote_keys")
    suspend fun removeAll()

    // Добавьте эти методы
    @Query("SELECT MAX(id) FROM posts_remote_keys WHERE type = 'AFTER'")
    suspend fun max(): Long?

    @Query("SELECT MIN(id) FROM posts_remote_keys WHERE type = 'BEFORE'")
    suspend fun min(): Long?

    @Query("DELETE FROM posts_remote_keys")
    suspend fun clear()
}