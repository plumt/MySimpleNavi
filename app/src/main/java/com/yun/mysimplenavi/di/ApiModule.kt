package com.yun.mysimplenavi.di

import com.yun.mysimplenavi.data.Api
import com.yun.mysimplenavi.data.repository.ApiRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)

object ApiModule {

    @Singleton
    @Provides
    @Named("kakao")
    fun providerKakaoApi(@Named("kakao") retrofit: Retrofit): Api {
        return retrofit.create(Api::class.java)
    }

    @Singleton
    @Provides
    @Named("kakao")
    fun providerKakaoRepository(@Named("kakao") api: Api) = ApiRepository(api)
}