package com.commit451.gitlab.model.api

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
data class ProjectNamespace(
    @field:Json(name = "id")
    var id: Long = 0,
    @field:Json(name = "name")
    var name: String? = null,
    @field:Json(name = "path")
    var path: String? = null,
    @field:Json(name = "owner_id")
    var ownerId: Long = 0,
    @field:Json(name = "created_at")
    var createdAt: Date? = null,
    @field:Json(name = "updated_at")
    var updatedAt: Date? = null,
    @field:Json(name = "description")
    var description: String? = null,
    @field:Json(name = "avatar")
    var avatar: Avatar? = null,
    @field:Json(name = "public")
    var isPublic: Boolean = false
) : Parcelable {

    @Parcelize
    data class Avatar(
        @field:Json(name = "url")
        var url: String? = null
    ) : Parcelable
}
