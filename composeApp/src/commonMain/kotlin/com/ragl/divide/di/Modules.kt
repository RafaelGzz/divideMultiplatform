package com.ragl.divide.di

import com.ragl.divide.createDataStore
import com.ragl.divide.data.repositories.FriendsRepository
import com.ragl.divide.data.repositories.FriendsRepositoryImpl
import com.ragl.divide.data.repositories.GroupRepository
import com.ragl.divide.data.repositories.GroupRepositoryImpl
import com.ragl.divide.data.repositories.PreferencesRepository
import com.ragl.divide.data.repositories.UserRepository
import com.ragl.divide.data.repositories.UserRepositoryImpl
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.expense.ExpenseViewModel
import com.ragl.divide.ui.screens.expenseProperties.ExpensePropertiesViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.database.FirebaseDatabase
import dev.gitlive.firebase.database.database
import dev.gitlive.firebase.storage.FirebaseStorage
import dev.gitlive.firebase.storage.storage
import org.koin.core.module.Module
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformModule: Module

val sharedModule = module {

    singleOf(::UserRepositoryImpl).bind<UserRepository>()
    singleOf(::FriendsRepositoryImpl).bind<FriendsRepository>()
    singleOf(::GroupRepositoryImpl).bind<GroupRepository>()
    singleOf(::PreferencesRepository)

    singleOf(::UserViewModel)
    factoryOf(::ExpensePropertiesViewModel)
    factoryOf(::ExpenseViewModel)

    // FIREBASE DEPENDENCIES
    single {
        Firebase.auth
    }.bind<FirebaseAuth>()

    single {
        Firebase.storage
    }.bind<FirebaseStorage>()

    single {
        Firebase.database
    }.bind<FirebaseDatabase>()
}