package com.arnigor.incomeexpenses.presentation.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.arnigor.incomeexpenses.data.repository.prefs.PreferencesDataSource
import com.arnigor.incomeexpenses.data.repository.sheets.SheetsRepository
import com.arnigor.incomeexpenses.di.FragmentScope
import com.arnigor.incomeexpenses.di.ViewModelKey
import com.arnigor.incomeexpenses.presentation.details.DetailsFragment
import com.arnigor.incomeexpenses.presentation.details.DetailsViewModel
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap

@Module(
    includes = [
        DetailsFragmentModule.ProvideDetailsViewModel::class
    ]
)
interface DetailsFragmentModule {
    @FragmentScope
    @ContributesAndroidInjector(modules = [InjectDetailsViewModel::class])
    fun contributeFragmentInjector(): DetailsFragment

    @Module
    class ProvideDetailsViewModel {

        @Provides
        @IntoMap
        @ViewModelKey(DetailsViewModel::class)
        fun provideDetailsViewModel(
            sheetsRepository: SheetsRepository,
            preferencesDataSource: PreferencesDataSource
        ): ViewModel = DetailsViewModel(sheetsRepository, preferencesDataSource)
    }

    @Module
    class InjectDetailsViewModel {

        @Provides
        fun providesDetailsViewModel(
            factory: ViewModelProvider.Factory,
            target: DetailsFragment
        ) = ViewModelProvider(target, factory).get(DetailsViewModel::class.java)
    }
}