package com.arnigor.incomeexpenses.di

import com.arnigor.incomeexpenses.IncomeExpensesApp
import com.arnigor.incomeexpenses.data.DataModule
import com.arnigor.incomeexpenses.presentation.di.UiModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        AppModule::class,
        UiModule::class,
        DataModule::class,
    ]
)
interface AppComponent : AndroidInjector<IncomeExpensesApp> {
    override fun inject(application: IncomeExpensesApp)

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: IncomeExpensesApp): Builder

        fun build(): AppComponent
    }
}
