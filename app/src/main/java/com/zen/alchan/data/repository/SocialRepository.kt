package com.zen.alchan.data.repository

import androidx.lifecycle.LiveData
import com.zen.alchan.data.network.Resource
import com.zen.alchan.data.response.User
import com.zen.alchan.helper.pojo.ActivityItem
import com.zen.alchan.helper.pojo.KeyValueItem
import type.ActivityType
import type.LikeableType

interface SocialRepository {

    val textActivityText: String
    val listActivityText: String
    val messageActivityText: String

    val notifyFriendsActivity: LiveData<Boolean>

    val friendsActivityResponse: LiveData<Resource<ActivityQuery.Data>>
    val toggleLikeResponse: LiveData<Resource<ActivityItem>>
    val toggleActivitySubscriptionResponse: LiveData<Resource<ActivityItem>>
    val deleteActivityResponse: LiveData<Resource<Boolean>>

    val activityDetailResponse: LiveData<Resource<ActivityDetailQuery.Data>>
    val toggleLikeDetailResponse: LiveData<Resource<ActivityItem>>
    val toggleActivitySubscriptionDetailResponse: LiveData<Resource<ActivityItem>>
    val deleteActivityDetailResponse: LiveData<Resource<Boolean>>
    val deleteActivityReplyResponse: LiveData<Resource<Int>>

    fun getFriendsActivity(typeIn: List<ActivityType>?, userId: Int?)
    fun getActivityDetail(id: Int)

    fun toggleLike(id: Int, type: LikeableType, fromDetail: Boolean = false)
    fun toggleActivitySubscription(activityId: Int, subscribe: Boolean, fromDetail: Boolean = false)
    fun deleteActivity(id: Int, fromDetail: Boolean = false)
    fun deleteActivityReply(id: Int)
}