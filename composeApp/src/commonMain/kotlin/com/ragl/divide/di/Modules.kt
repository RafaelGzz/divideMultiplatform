package com.ragl.divide.di

import com.ragl.divide.data.repositories.FriendsRepositoryImpl
import com.ragl.divide.data.repositories.GroupRepositoryImpl
import com.ragl.divide.data.repositories.PreferencesRepositoryImpl
import com.ragl.divide.data.repositories.UserRepositoryImpl
import com.ragl.divide.data.services.AnalyticsServiceImpl
import com.ragl.divide.data.services.AppInitializationServiceImpl
import com.ragl.divide.data.services.AppStateServiceImpl
import com.ragl.divide.data.services.GroupExpenseServiceImpl
import com.ragl.divide.data.services.UserServiceImpl
import com.ragl.divide.data.stateHolders.UserStateHolderImpl
import com.ragl.divide.domain.repositories.FriendsRepository
import com.ragl.divide.domain.repositories.GroupRepository
import com.ragl.divide.domain.repositories.PreferencesRepository
import com.ragl.divide.domain.repositories.UserRepository
import com.ragl.divide.domain.services.AnalyticsService
import com.ragl.divide.domain.services.AppInitializationService
import com.ragl.divide.domain.services.AppStateService
import com.ragl.divide.domain.services.GroupExpenseService
import com.ragl.divide.domain.services.UserService
import com.ragl.divide.domain.stateHolders.UserStateHolder
import com.ragl.divide.domain.usecases.auth.CheckEmailVerificationUseCase
import com.ragl.divide.domain.usecases.auth.SendEmailVerificationUseCase
import com.ragl.divide.domain.usecases.auth.SignInWithEmailUseCase
import com.ragl.divide.domain.usecases.auth.SignInWithGoogleUseCase
import com.ragl.divide.domain.usecases.auth.SignOutUseCase
import com.ragl.divide.domain.usecases.auth.SignUpWithEmailUseCase
import com.ragl.divide.presentation.screens.addFriends.AddFriendsViewModel
import com.ragl.divide.presentation.screens.event.EventViewModel
import com.ragl.divide.presentation.screens.eventProperties.EventPropertiesViewModel
import com.ragl.divide.presentation.screens.expense.ExpenseViewModel
import com.ragl.divide.presentation.screens.expenseProperties.ExpensePropertiesViewModel
import com.ragl.divide.presentation.screens.group.GroupViewModel
import com.ragl.divide.presentation.screens.groupExpense.GroupExpenseViewModel
import com.ragl.divide.presentation.screens.groupExpenseProperties.GroupExpensePropertiesViewModel
import com.ragl.divide.presentation.screens.groupPayment.GroupPaymentViewModel
import com.ragl.divide.presentation.screens.groupPaymentProperties.GroupPaymentPropertiesViewModel
import com.ragl.divide.presentation.screens.groupProperties.GroupPropertiesViewModel
import com.ragl.divide.presentation.screens.onboarding.OnboardingViewModel
import com.ragl.divide.presentation.screens.signIn.AuthViewModel
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
    singleOf(::PreferencesRepositoryImpl).bind<PreferencesRepository>()

    factoryOf(::AuthViewModel)
    factoryOf(::OnboardingViewModel)
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

    singleOf(::GroupExpenseServiceImpl).bind<GroupExpenseService>()
    singleOf(::AnalyticsServiceImpl).bind<AnalyticsService>()
    singleOf(::AppStateServiceImpl).bind<AppStateService>()
    singleOf(::AppInitializationServiceImpl).bind<AppInitializationService>()
    singleOf(::UserServiceImpl).bind<UserService>()

    singleOf(::UserStateHolderImpl).bind<UserStateHolder>()

    // USE CASES
    factoryOf(::SignInWithEmailUseCase)
    factoryOf(::SignUpWithEmailUseCase)
    factoryOf(::SignInWithGoogleUseCase)
    factoryOf(::SignOutUseCase)
    factoryOf(::SendEmailVerificationUseCase)
    factoryOf(::CheckEmailVerificationUseCase)

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