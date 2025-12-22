package ru.netology.nework.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.netology.nework.dao.PostDao
import ru.netology.nework.dao.PostRemoteKeyDao
import ru.netology.nework.entity.PostEntity
import ru.netology.nework.entity.PostRemoteKeyEntity

@Database(
    entities = [
        PostEntity::class,
        PostRemoteKeyEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDb : RoomDatabase() {
    abstract fun postDao(): PostDao
    abstract fun postRemoteKeyDao(): PostRemoteKeyDao

    companion object {
        @Volatile
        private var instance: AppDb? = null

        fun getInstance(context: Context): AppDb {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDb {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDb::class.java,
                "app.db"
            )
                .fallbackToDestructiveMigrationOnDowngrade(true)
                .build()
        }
    }
}