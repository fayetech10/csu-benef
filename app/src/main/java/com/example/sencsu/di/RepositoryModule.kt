package com.example.sencsu.di

import com.example.sencsu.data.repository.*
import com.example.sencsu.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        implementation: AuthRepository
    ): IAuthRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(
        implementation: DashboardRepository
    ): IDashboardRepository

    @Binds
    @Singleton
    abstract fun bindAdherentRepository(
        implementation: AdherentRepository
    ): IAdherentRepository

    @Binds
    @Singleton
    abstract fun bindPaiementRepository(
        implementation: PaiementRepository
    ): IPaiementRepository

    @Binds
    @Singleton
    abstract fun bindFileRepository(
        implementation: FileRepository
    ): IFileRepository

    @Binds
    @Singleton
    abstract fun bindCotisationRepository(
        implementation: CotisationRepository
    ): ICotisationRepository
}
