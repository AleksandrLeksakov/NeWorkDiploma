package ru.netology.nework.repository.remotemediator.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nework.api.ApiService
import ru.netology.nework.dao.event.EventDao
import ru.netology.nework.dao.event.EventRemoteKeyDao
import ru.netology.nework.dao.post.PostDao
import ru.netology.nework.dao.post.PostRemoteKeyDao
import ru.netology.nework.dao.user.UserDao
import ru.netology.nework.db.AppDb
import ru.netology.nework.repository.remotemediator.EventRemoteMediator
import ru.netology.nework.repository.remotemediator.PostRemoteMediator
import ru.netology.nework.repository.remotemediator.UserRemoteMediator
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class RemoteMediatorModule {

    @Singleton
    @Provides
    fun providePostRemoteMediator(
        apiService: ApiService,
        appDb: AppDb,
        postDao: PostDao,
        postRemoteKeyDao: PostRemoteKeyDao
    ): PostRemoteMediator = PostRemoteMediator(apiService, appDb, postDao, postRemoteKeyDao)

    @Singleton
    @Provides
    fun provideEventRemoteMediator(
        apiService: ApiService,
        appDb: AppDb,
        eventDao: EventDao,
        eventRemoteKeyDao: EventRemoteKeyDao
    ): EventRemoteMediator = EventRemoteMediator(apiService, appDb, eventDao, eventRemoteKeyDao)

    @Singleton
    @Provides
    fun provideUserRemoteMediator(
        apiService: ApiService,
        userDao: UserDao
    ): UserRemoteMediator = UserRemoteMediator(apiService, userDao)

}