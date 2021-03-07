package com.arnigor.incomeexpenses.di

import android.content.Context
import com.arnigor.incomeexpenses.IncomeExpensesApp
import dagger.Binds
import dagger.Module

@Module
internal abstract class AppModule {
    @Binds
    abstract fun provideContext(application: IncomeExpensesApp): Context
}
