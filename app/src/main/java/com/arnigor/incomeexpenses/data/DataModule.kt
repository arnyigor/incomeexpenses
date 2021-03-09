package com.arnigor.incomeexpenses.data

import com.arnigor.incomeexpenses.data.repository.prefs.PreferencesDataSource
import com.arnigor.incomeexpenses.data.repository.prefs.PreferencesDataSourceImpl
import com.arnigor.incomeexpenses.data.repository.sheets.SheetsDataSource
import com.arnigor.incomeexpenses.data.repository.sheets.SheetsDataSourceImpl
import com.arnigor.incomeexpenses.data.repository.sheets.SheetsRepository
import com.arnigor.incomeexpenses.data.repository.sheets.SheetsRepositoryImpl
import dagger.Binds
import dagger.Module
import javax.inject.Singleton

@Module
interface DataModule {
    @Binds
    @Singleton
    fun bindsSheetsRepository(repository: SheetsRepositoryImpl): SheetsRepository

    @Binds
    @Singleton
    fun bindsSheetsDataSource(repository: SheetsDataSourceImpl): SheetsDataSource

    @Binds
    @Singleton
    fun bindsPreferencesSource(prefs: PreferencesDataSourceImpl): PreferencesDataSource
}
