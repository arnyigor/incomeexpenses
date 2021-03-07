package com.arnigor.incomeexpenses

import com.arnigor.incomeexpenses.di.DaggerAppComponent
import dagger.android.DaggerApplication

class IncomeExpensesApp : DaggerApplication() {
    private val applicationInjector = DaggerAppComponent.builder()
        .application(this)
        .build()

    override fun applicationInjector() = applicationInjector
}
