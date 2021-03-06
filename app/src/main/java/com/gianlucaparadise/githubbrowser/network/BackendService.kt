package com.gianlucaparadise.githubbrowser.network

import android.util.Log
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.Input
import com.apollographql.apollo.coroutines.toDeferred
import com.apollographql.apollo.exception.ApolloException
import com.gianlucaparadise.githubbrowser.*
import com.gianlucaparadise.githubbrowser.type.AddStarInput
import com.gianlucaparadise.githubbrowser.type.RemoveStarInput
import com.gianlucaparadise.githubbrowser.util.SharedPreferencesManager
import com.gianlucaparadise.githubbrowser.vo.*
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

private const val GITHUB_API_ENDPOINT = "https://api.github.com/graphql"
private const val GITHUB_WEBSITE_ENDPOINT = "https://github.com"

@Singleton
class BackendService @Inject constructor(val sharedPreferences: SharedPreferencesManager) {

    /**
     * This is the client used to interact with Github's GraphQL Backend
     */
    private val graphQlClient: ApolloClient

    private val restClient: GithubService

    init {

        val okHttpClient = OkHttpClient.Builder()
            .authenticator(object : Authenticator {
                override fun authenticate(route: Route?, response: Response): Request? {
                    val authHeader = "Bearer ${sharedPreferences.accessToken}"
                    return response.request.newBuilder().header("Authorization", authHeader)
                        .build()
                }
            })
            .build()

        graphQlClient = ApolloClient.builder()
            .okHttpClient(okHttpClient)
            .serverUrl(GITHUB_API_ENDPOINT)
            .build()

        val retrofitClient = Retrofit.Builder()
            .baseUrl(GITHUB_WEBSITE_ENDPOINT)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        restClient = retrofitClient.create(GithubService::class.java)
    }

    suspend fun retrieveAuthenticatedUser(): User? {
        try {
            val user = graphQlClient
                .query(AuthenticatedUserQuery())
                .toDeferred()
                .await()

            val userFragment = user.data?.viewer?.fragments?.userFragment
                ?: throw Exception("Empty Response")

            return User.fromUserFragment(userFragment)

        } catch (apolloEx: ApolloException) {
            throw Exception("Error while retrieving Authenticated User")
        }
    }

    suspend fun retrieveAuthenticatedUserRepos(
        first: Int,
        startCursor: String? = null
    ): PaginatedResponse<Repo> {
        try {
            val repos = graphQlClient
                .query(
                    AuthenticatedUserReposQuery(
                        Input.fromNullable(first),
                        Input.optional(startCursor)
                    )
                )
                .toDeferred()
                .await()

            val reposResponse = repos.data?.viewer?.repositories
                ?: throw Exception("Empty Response")

            return Repo.fromReposResponse(reposResponse)

        } catch (apolloEx: ApolloException) {
            throw Exception("Error while retrieving Authenticated User's Repos")
        }
    }

    suspend fun searchUsers(
        query: String,
        first: Int,
        startCursor: String? = null
    ): PaginatedResponse<User> {
        try {
            val users = graphQlClient
                .query(
                    SearchUsersQuery(
                        query,
                        Input.fromNullable(first),
                        Input.optional(startCursor)
                    )
                )
                .toDeferred()
                .await()

            val usersResponse = users.data?.search
                ?: throw Exception("Empty Response")

            return User.fromSearchUsersResponse(usersResponse)

        } catch (apolloEx: ApolloException) {
            throw Exception("Error while searching for Users")
        }
    }

    suspend fun searchRepos(
        query: String,
        first: Int,
        startCursor: String? = null
    ): PaginatedResponse<Repo> {
        try {
            val repos = graphQlClient
                .query(
                    SearchReposQuery(
                        query,
                        Input.fromNullable(first),
                        Input.optional(startCursor)
                    )
                )
                .toDeferred()
                .await()

            val reposResponse = repos.data?.search
                ?: throw Exception("Empty Response")

            return Repo.fromSearchReposResponse(reposResponse)

        } catch (apolloEx: ApolloException) {
            throw Exception("Error while searching for Repos")
        }
    }

    /**
     * Add or remove a star to the input repo
     * @return new Stargazers Count
     */
    suspend fun toggleStar(repo: Repo): Starrable? {
        return if (repo.viewerHasStarred) {
            removeStar(repo)
        } else {
            addStar(repo)
        }
    }

    /**
     * Add a star to the input repo
     * @return new Stargazers Count
     */
    private suspend fun addStar(repo: Repo): Starrable? {
        try {
            val input = AddStarInput(repo.id)
            val response = graphQlClient
                .mutate(AddStarMutation(input))
                .toDeferred()
                .await()

            Log.d("Backend", "AddStar response: $response")

            val starrableFragment = response.data?.addStar?.starrable?.fragments?.starrableFragment
                ?: throw Exception("Empty Response")

            return Starrable.fromStarrableFragment(starrableFragment)

        } catch (apolloEx: ApolloException) {
            throw Exception("Error while adding star to Repo")
        }
    }

    /**
     * Remove a star from the input repo
     * @return new Stargazers Count
     */
    private suspend fun removeStar(repo: Repo): Starrable? {
        try {
            val input = RemoveStarInput(repo.id)
            val response = graphQlClient
                .mutate(RemoveStarMutation(input))
                .toDeferred()
                .await()

            Log.d("Backend", "RemoveStar response: $response")

            val starrableFragment =
                response.data?.removeStar?.starrable?.fragments?.starrableFragment
                    ?: throw Exception("Empty Response")

            return Starrable.fromStarrableFragment(starrableFragment)

        } catch (apolloEx: ApolloException) {
            throw Exception("Error while removing star from Repo")
        }
    }

    suspend fun retrieveAccessToken(code: String, state: String): AccessTokenModel {
        val clientId = BuildConfig.CLIENT_ID
        val clientSecret = BuildConfig.CLIENT_SECRET
        return this.restClient.postAccessToken(clientId, clientSecret, code, state)
    }
}