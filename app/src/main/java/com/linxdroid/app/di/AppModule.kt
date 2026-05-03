package com.linxdroid.app.di

import android.content.Context
import com.linxdroid.app.utils.RootFSManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRootFSManager(@ApplicationContext context: Context): RootFSManager {
        return RootFSManager(context)
    }
}
