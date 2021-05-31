package com.arnigor.incomeexpenses.presentation.di

import com.arnigor.incomeexpenses.di.ActivityScope
import com.arnigor.incomeexpenses.presentation.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivitiesModule {
    @ActivityScope
    @ContributesAndroidInjector(modules = [HomeFragmentModule::class, DetailsFragmentModule::class])
    abstract fun bindMainActivity(): MainActivity
}
