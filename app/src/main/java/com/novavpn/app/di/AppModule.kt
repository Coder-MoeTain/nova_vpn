package com.novavpn.app.di

import android.content.Context
import com.wireguard.android.backend.Backend
import com.wireguard.android.backend.GoBackend
import com.novavpn.app.vpn.NovaTunnel
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
    fun provideBackend(@ApplicationContext context: Context): Backend = GoBackend(context)

    @Provides
    @Singleton
    fun provideNovaTunnel(): NovaTunnel = NovaTunnel()
}
