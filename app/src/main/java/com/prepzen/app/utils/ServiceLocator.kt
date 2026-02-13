package com.prepzen.app.utils

import android.content.Context
import com.prepzen.app.data.ContentRepository
import com.prepzen.app.data.UserPrefsRepository

object ServiceLocator {
    @Volatile
    private var contentRepository: ContentRepository? = null

    @Volatile
    private var userPrefsRepository: UserPrefsRepository? = null

    fun contentRepository(context: Context): ContentRepository {
        return contentRepository ?: synchronized(this) {
            contentRepository ?: ContentRepository(context.applicationContext).also {
                contentRepository = it
            }
        }
    }

    fun userPrefsRepository(context: Context): UserPrefsRepository {
        return userPrefsRepository ?: synchronized(this) {
            userPrefsRepository ?: UserPrefsRepository(context.applicationContext).also {
                userPrefsRepository = it
            }
        }
    }
}
