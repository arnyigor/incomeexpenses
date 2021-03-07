package com.arnigor.incomeexpenses.ui.di

import com.arnigor.incomeexpenses.di.ActivityScope
import com.arnigor.incomeexpenses.ui.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivitiesModule {
    @ActivityScope
    @ContributesAndroidInjector(modules = [HomeFragmentModule::class])
    abstract fun bindMainActivity(): MainActivity
}
