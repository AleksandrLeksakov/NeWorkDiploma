package ru.netology.nework.dao.post

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nework.entity.post.PostRemoteKeyEntity

@Dao
interface PostRemoteKeyDao {

    @Query("SELECT max(id) FROM postKey")
    suspend fun max(): Long?

    @Query("SELECT min(id) FROM postKey")
    suspend fun min(): Long?

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(postRemoteKeyEntity: PostRemoteKeyEntity)

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(postRemoteKeyEntity: List<PostRemoteKeyEntity>)

    @Query("DELETE FROM postKey")
    suspend fun clear()
}