package com.zen.alchan.ui.social

import androidx.lifecycle.ViewModel
import com.zen.alchan.R
import com.zen.alchan.data.repository.MediaRepository
import com.zen.alchan.data.repository.OtherUserRepository
import com.zen.alchan.data.repository.SocialRepository
import com.zen.alchan.data.repository.UserRepository
import com.zen.alchan.data.response.User
import com.zen.alchan.data.response.UserAvatar
import com.zen.alchan.helper.pojo.ActivityItem
import com.zen.alchan.helper.pojo.ActivityReply
import com.zen.alchan.helper.pojo.BestFriend
import type.ActivityType

class SocialViewModel(private val mediaRepository: MediaRepository,
                      private val userRepository: UserRepository,
                      private val socialRepository: SocialRepository
) : ViewModel() {

    val TEXT_ACTIVITY = "TextActivity"
    val LIST_ACTIVITY = "ListActivity"
    val MESSAGE_ACTIVITY = "MessageActivity"

    var isInit = false

    var selectedActivityType: ArrayList<ActivityType>? = null
    var selectedBestFriend: BestFriend? = null

    var bestFriends = ArrayList<BestFriend>()

    val activityTypeList = arrayListOf(
        null, arrayListOf(ActivityType.TEXT), arrayListOf(ActivityType.ANIME_LIST, ActivityType.MANGA_LIST)
    )

    val activityTypeArray = arrayOf(
        R.string.all, R.string.text_status, R.string.list_status
    )

    val savedBestFriends: List<BestFriend>?
        get() = userRepository.bestFriends

    val activityList = ArrayList<ActivityItem>()

    val mostTrendingAnimeBannerLiveData by lazy {
        mediaRepository.mostTrendingAnimeBannerLivaData
    }

    val bestFriendChangedNotifier by lazy {
        userRepository.bestFriendChangedNotifier
    }

    val friendsActivityResponse by lazy {
        socialRepository.friendsActivityResponse
    }

    fun initData() {
        if (!isInit) {
            isInit = true
            reinitBestFriends()
            socialRepository.getFriendsActivity(selectedActivityType, if (selectedBestFriend != null) listOf(selectedBestFriend?.id!!) else null)
        }
    }

    fun retrieveFriendsActivity() {
        socialRepository.getFriendsActivity(selectedActivityType, if (selectedBestFriend != null) listOf(selectedBestFriend?.id!!) else null)
    }

    fun reinitBestFriends() {
        bestFriends.clear()
        bestFriends.add(BestFriend(null, null, null))
        savedBestFriends?.forEach {
            bestFriends.add(it)
        }
    }

    fun getReplies(activityType: String?, item: ActivityQuery.Activity.Fragments): List<ActivityReply> {
        val list = ArrayList<ActivityReply>()

        when (activityType) {
            TEXT_ACTIVITY -> {
                item.onTextActivity?.replies?.forEach { reply ->
                    val likeUser = User(
                        id = reply?.user?.id!!,
                        name = reply.user.name,
                        avatar = UserAvatar(null, reply.user.avatar?.medium)
                    )

                    val likes = ArrayList<User>()
                    reply.likes?.forEach { like ->
                        likes.add(User(
                            id = like?.id!!,
                            name = like.name,
                            avatar = UserAvatar(null, like.avatar?.medium)
                        ))
                    }

                    list.add(ActivityReply(
                        reply.id,
                        reply.userId,
                        reply.activityId,
                        reply.text,
                        reply.likeCount,
                        reply.isLiked,
                        reply.createdAt,
                        likeUser,
                        likes
                    ))
                }
            }
            LIST_ACTIVITY -> {
                item.onListActivity?.replies?.forEach { reply ->
                    val likeUser = User(
                        id = reply?.user?.id!!,
                        name = reply.user.name,
                        avatar = UserAvatar(null, reply.user.avatar?.medium)
                    )

                    val likes = ArrayList<User>()
                    reply.likes?.forEach { like ->
                        likes.add(User(
                            id = like?.id!!,
                            name = like.name,
                            avatar = UserAvatar(null, like.avatar?.medium)
                        ))
                    }

                    list.add(ActivityReply(
                        reply.id,
                        reply.userId,
                        reply.activityId,
                        reply.text,
                        reply.likeCount,
                        reply.isLiked,
                        reply.createdAt,
                        likeUser,
                        likes
                    ))
                }
            }
            MESSAGE_ACTIVITY -> {
                item.onMessageActivity?.replies?.forEach { reply ->
                    val likeUser = User(
                        id = reply?.user?.id!!,
                        name = reply.user.name,
                        avatar = UserAvatar(null, reply.user.avatar?.medium)
                    )

                    val likes = ArrayList<User>()
                    reply.likes?.forEach { like ->
                        likes.add(User(
                            id = like?.id!!,
                            name = like.name,
                            avatar = UserAvatar(null, like.avatar?.medium)
                        ))
                    }

                    list.add(ActivityReply(
                        reply.id,
                        reply.userId,
                        reply.activityId,
                        reply.text,
                        reply.likeCount,
                        reply.isLiked,
                        reply.createdAt,
                        likeUser,
                        likes
                    ))
                }
            }
        }

        return list
    }

    fun getLikes(activityType: String?, item: ActivityQuery.Activity.Fragments): List<User> {
        val list = ArrayList<User>()

        when (activityType) {
            TEXT_ACTIVITY -> {
                item.onTextActivity?.likes?.forEach { like ->
                    list.add(User(
                        id = like?.id!!,
                        name = like.name,
                        avatar = UserAvatar(null, like.avatar?.medium)
                    ))
                }
            }
            LIST_ACTIVITY -> {
                item.onListActivity?.likes?.forEach { like ->
                    list.add(User(
                        id = like?.id!!,
                        name = like.name,
                        avatar = UserAvatar(null, like.avatar?.medium)
                    ))
                }
            }
            MESSAGE_ACTIVITY -> {
                item.onMessageActivity?.likes?.forEach { like ->
                    list.add(User(
                        id = like?.id!!,
                        name = like.name,
                        avatar = UserAvatar(null, like.avatar?.medium)
                    ))
                }
            }
        }

        return list
    }
}