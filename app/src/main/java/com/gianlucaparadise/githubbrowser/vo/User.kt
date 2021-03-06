package com.gianlucaparadise.githubbrowser.vo

import com.gianlucaparadise.githubbrowser.SearchUsersQuery
import com.gianlucaparadise.githubbrowser.fragment.UserFragment
import java.io.Serializable

data class User(
    val id: String,
    /**
     * The username used to login.
     */
    val login: String,
    /**
     * The user's public profile bio.
     */
    val bio: String?,
    /**
     * The user's public profile name.
     */
    val name: String?,
    /**
     * A URL pointing to the user's public avatar.
     */
    val avatarUrl: String,
    /**
     * The total count of users the given user is followed by.
     */
    val followersCount: Int,
    /**
     * The total count of users the given user is following.
     */
    val followingCount: Int
) : Serializable {
    val displayName: String
        get() = name ?: login

    companion object {
        fun fromUserFragment(userFragment: UserFragment?): User? {
            if (userFragment == null) return null

            return User(
                id = userFragment.id,
                login = userFragment.login,
                bio = userFragment.bio,
                name = userFragment.name,
                avatarUrl = userFragment.avatarUrl.toString(),
                followersCount = userFragment.followers.totalCount,
                followingCount = userFragment.following.totalCount
            )
        }

        private fun fromSearchUsersNodeList(searchUserNodes: List<SearchUsersQuery.Node?>?): List<User> {
            if (searchUserNodes == null) return emptyList()

            val users = mutableListOf<User>()

            searchUserNodes.forEach { node ->
                val user =
                    fromUserFragment(node?.fragments?.userFragment)
                if (user != null) {
                    users.add(user)
                }
            }

            return users
        }

        fun fromSearchUsersResponse(users: SearchUsersQuery.Search): PaginatedResponse<User> {
            return PaginatedResponse(
                endCursor = users.pageInfo.endCursor,
                hasNextPage = users.pageInfo.hasNextPage,
                totalCount = users.userCount,
                nodes = fromSearchUsersNodeList(
                    users.nodes
                )
            )
        }
    }
}