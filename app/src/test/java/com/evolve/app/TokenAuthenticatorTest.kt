package com.evolve.app

import com.evolve.app.data.TokenStore
import com.evolve.app.data.api.AuthInterceptor
import com.evolve.app.data.api.AuthSession
import com.evolve.app.data.api.TokenAuthenticator
import kotlinx.coroutines.flow.MutableStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TokenAuthenticatorTest {

    private lateinit var server: MockWebServer
    private lateinit var tokens: FakeTokens
    private var refreshCalls = 0

    private val client: OkHttpClient
        get() = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokens))
            .authenticator(
                TokenAuthenticator(tokens) {
                    refreshCalls++
                    AuthSession("new-access", "refresh-2")
                }
            )
            .build()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        tokens = FakeTokens("old-access", "refresh-1")
        refreshCalls = 0
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun call(path: String) =
        client.newCall(Request.Builder().url(server.url(path)).build()).execute()

    @Test
    fun `401 refreshes once and retries with the new token`() {
        server.enqueue(MockResponse().setResponseCode(401))
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))

        val response = call("/plans")

        assertEquals(200, response.code)
        response.close()
        assertEquals(1, refreshCalls)
        assertEquals("Bearer old-access", server.takeRequest().getHeader("Authorization"))
        assertEquals("Bearer new-access", server.takeRequest().getHeader("Authorization"))
        assertEquals("new-access", tokens.accessToken)
        assertEquals("refresh-2", tokens.refreshToken)
    }

    @Test
    fun `failed refresh clears the session and returns 401`() {
        server.enqueue(MockResponse().setResponseCode(401))
        val failing = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokens))
            .authenticator(TokenAuthenticator(tokens) { null })
            .build()

        val response = failing
            .newCall(Request.Builder().url(server.url("/plans")).build())
            .execute()

        assertEquals(401, response.code)
        response.close()
        assertTrue(tokens.cleared)
        assertNull(tokens.accessToken)
        assertNull(tokens.refreshToken)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `non 401 errors do not refresh`() {
        server.enqueue(MockResponse().setResponseCode(500))

        val response = call("/plans")

        assertEquals(500, response.code)
        response.close()
        assertEquals(0, refreshCalls)
        assertEquals(1, server.requestCount)
    }

    private class FakeTokens(access: String?, refresh: String?) : TokenStore {
        override var accessToken: String? = access
        override var refreshToken: String? = refresh
        override val hasSession = MutableStateFlow(refresh != null)
        var cleared = false

        override fun save(accessToken: String, refreshToken: String) {
            this.accessToken = accessToken
            this.refreshToken = refreshToken
            hasSession.value = true
        }

        override fun clear() {
            accessToken = null
            refreshToken = null
            cleared = true
            hasSession.value = false
        }
    }
}
