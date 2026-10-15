package com.kturker.multiplatform.core.auth.di

import com.kturker.multiplatform.core.auth.AuthService
import com.kturker.multiplatform.core.auth.BackendImageLoaderCustomizer
import com.kturker.multiplatform.core.auth.FirebaseAuthService
import com.kturker.multiplatform.core.auth.FirebaseAvailability
import com.kturker.multiplatform.core.auth.SessionTokens
import com.kturker.multiplatform.core.auth.SocialSignIn
import com.kturker.multiplatform.core.auth.authServiceFor
import com.kturker.multiplatform.core.auth.platformFirebaseAvailability
import com.kturker.multiplatform.core.auth.platformSocialSignIn
import com.kturker.multiplatform.core.domain.BuildEnvironment
import com.kturker.multiplatform.core.image.ImageLoaderCustomizer
import com.kturker.multiplatform.core.network.BACKEND_CLIENT
import com.kturker.multiplatform.core.network.BackendConfig
import com.kturker.multiplatform.core.network.TokenSource
import com.kturker.multiplatform.core.network.buildBackendClient
import com.kturker.multiplatform.core.network.forgetToken
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.kturker.multiplatform.core.auth")
class AuthModule {

    // One instance is both the AuthService and core:network's TokenSource (the backend's bearer token).
    // Without a Firebase config it is the Unavailable service, and Firebase.auth is never called.
    @Single(binds = [TokenSource::class])
    fun provideAuthService(
        social: SocialSignIn,
        firebase: FirebaseAvailability
    ): AuthService = authServiceFor(firebase) {
        FirebaseAuthService(
            auth = Firebase.auth,
            social = social,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        )
    }

    @Single
    fun provideSocialSignIn(): SocialSignIn = platformSocialSignIn()

    @Single
    fun provideFirebaseAvailability(): FirebaseAvailability = platformFirebaseAvailability()

    // The backend's HttpClient (the server module): the signed-in user's ID token goes to the backend's host
    // only. The app's other HttpClient (core:network, unnamed) stays without a token.
    @Single
    @Named(BACKEND_CLIENT)
    fun provideBackendClient(
        @Provided engine: HttpClientEngine,
        @Provided environment: BuildEnvironment,
        tokens: TokenSource
    ): HttpClient = buildBackendClient(
        engine = engine,
        baseUrl = BackendConfig.baseUrl(environment),
        tokens = tokens,
        isDebug = environment.isDebug
    )

    // core:image's ImageLoader loads network images through the backend client (its bearer token, its host only)
    @Single
    fun provideBackendImageLoaderCustomizer(
        @Named(BACKEND_CLIENT) client: HttpClient
    ): ImageLoaderCustomizer = BackendImageLoaderCustomizer(backendClient = client)

    // Whoever signs the user out (account settings, a 401 in any feature) drops the backend client's cached token
    @Single
    fun provideSessionTokens(@Named(BACKEND_CLIENT) client: HttpClient): SessionTokens =
        SessionTokens { client.forgetToken() }
}
