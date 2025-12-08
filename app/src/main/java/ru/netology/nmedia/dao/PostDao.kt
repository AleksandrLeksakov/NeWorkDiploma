package ru.netology.nmedia.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM posts ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, PostEntity>

    @Query("SELECT * FROM posts WHERE id = :id")
    suspend fun getById(id: Long): PostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("DELETE FROM posts WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM posts")
    suspend fun removeAll()

    @Query("UPDATE posts SET liked_by_me = :likedByMe, like_owner_ids = :likeOwnerIds WHERE id = :id")
    suspend fun updateLikeById(id: Long, likedByMe: Boolean, likeOwnerIds: List<Long>)

}