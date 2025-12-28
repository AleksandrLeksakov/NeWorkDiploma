package ru.netology.nework.repository.remotemediator.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nework.api.services.EventApiService
import ru.netology.nework.api.services.PostApiService
import ru.netology.nework.api.services.UserApiService
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
        postApiService: PostApiService,
        appDb: AppDb,
        postDao: PostDao,
        postRemoteKeyDao: PostRemoteKeyDao
    ): PostRemoteMediator = PostRemoteMediator(
        postApiService,
        appDb,
        postDao,
        postRemoteKeyDao
    )

    @Singleton
    @Provides
    fun provideEventRemoteMediator(
        eventApiService: EventApiService,
        appDb: AppDb,
        eventDao: EventDao,
        eventRemoteKeyDao: EventRemoteKeyDao
    ): EventRemoteMediator = EventRemoteMediator(
        eventApiService,
        appDb,
        eventDao,
        eventRemoteKeyDao
    )

    @Singleton
    @Provides
    fun provideUserRemoteMediator(
        userApiService: UserApiService,
        userDao: UserDao
    ): UserRemoteMediator = UserRemoteMediator(
        userApiService,
        userDao
    )
}