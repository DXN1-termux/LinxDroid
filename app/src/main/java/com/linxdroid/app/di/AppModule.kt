package com.linxdroid.app.di

import android.content.Context
import com.linxdroid.app.PRootManager
import com.linxdroid.app.utils.DownloadManager
import com.linxdroid.app.utils.PreferencesManager
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
    fun provideDownloadManager(): DownloadManager = DownloadManager()

    @Provides
    @Singleton
    fun provideRootFSManager(@ApplicationContext context: Context): RootFSManager =
        RootFSManager(context)

    @Provides
    @Singleton
    fun providePreferencesManager(@ApplicationContext context: Context): PreferencesManager =
        PreferencesManager(context)

    @Provides
    @Singleton
    fun provideVNCClient(): VNCClient = VNCClient()
}
