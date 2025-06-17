package com.ragl.divide.di

import com.ragl.divide.data.repositories.FriendsRepository
import com.ragl.divide.data.repositories.FriendsRepositoryImpl
import com.ragl.divide.data.repositories.GroupRepository
import com.ragl.divide.data.repositories.GroupRepositoryImpl
import com.ragl.divide.data.repositories.PreferencesRepository
import com.ragl.divide.data.repositories.UserRepository
import com.ragl.divide.data.repositories.UserRepositoryImpl
import com.ragl.divide.data.services.AnalyticsService
import com.ragl.divide.data.services.GroupExpenseService
import com.ragl.divide.ui.screens.UserViewModel
import com.ragl.divide.ui.screens.addFriends.AddFriendsViewModel
import com.ragl.divide.ui.screens.event.EventViewModel
import com.ragl.divide.ui.screens.eventProperties.EventPropertiesViewModel
import com.ragl.divide.ui.screens.expense.ExpenseViewModel
import com.ragl.divide.ui.screens.expenseProperties.ExpensePropertiesViewModel
import com.ragl.divide.ui.screens.group.GroupViewModel
import com.ragl.divide.ui.screens.groupExpense.GroupExpenseViewModel
import com.ragl.divide.ui.screens.groupExpenseProperties.GroupExpensePropertiesViewModel
import com.ragl.divide.ui.screens.groupPayment.GroupPaymentViewModel
import com.ragl.divide.ui.screens.groupPaymentProperties.GroupPaymentPropertiesViewModel
import com.ragl.divide.ui.screens.groupProperties.GroupPropertiesViewModel
import com.ragl.divide.ui.screens.signIn.LogInViewModel
import com.ragl.divide.ui.screens.signIn.SignUpViewModel
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.analytics.FirebaseAnalytics
import dev.gitlive.firebase.analytics.analytics
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.crashlytics.FirebaseCrashlytics
import dev.gitlive.firebase.crashlytics.crashlytics
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
    factoryOf(::LogInViewModel)
    factoryOf(::SignUpViewModel)
    factoryOf(::ExpensePropertiesViewModel)
    factoryOf(::ExpenseViewModel)
    factoryOf(::GroupViewModel)
    factoryOf(::GroupPropertiesViewModel)
    factoryOf(::GroupExpensePropertiesViewModel)
    factoryOf(::GroupExpenseViewModel)
    factoryOf(::GroupPaymentPropertiesViewModel)
    factoryOf(::GroupPaymentViewModel)
    factoryOf(::AddFriendsViewModel)
    factoryOf(::EventViewModel)
    factoryOf(::EventPropertiesViewModel)

    singleOf(::GroupExpenseService)
    singleOf(::AnalyticsService)

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

    single{
        Firebase.crashlytics
    }.bind<FirebaseCrashlytics>()

    single{
        Firebase.analytics
    }.bind<FirebaseAnalytics>()
}