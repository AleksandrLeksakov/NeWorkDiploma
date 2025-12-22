package ru.netology.nework.db

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DbModule {
    @Singleton
    @Provides
    fun provideDb(@ApplicationContext context: Context): AppDb {
        return AppDb.getInstance(context)
    }

    @Provides
    fun providePostDao(db: AppDb) = db.postDao()

    @Provides
    fun providePostRemoteKeyDao(db: AppDb) = db.postRemoteKeyDao()
}