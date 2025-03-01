package com.ragl.divide.di

import com.ragl.divide.data.repositories.UserRepository
import com.ragl.divide.data.repositories.UserRepositoryImpl
import com.ragl.divide.ui.screens.UserViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.database.FirebaseDatabase
import dev.gitlive.firebase.database.database
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

expect val platformModule: Module

val sharedModule = module {
    singleOf(::UserRepositoryImpl).bind<UserRepository>()
    factory { UserViewModel(get()) }
    single {
        Firebase.auth
    }.bind<FirebaseAuth>()
    single {
        Firebase.database
    }.bind<FirebaseDatabase>()
}