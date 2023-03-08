package com.yun.mysimplenavi

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MySimpleNaviApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}