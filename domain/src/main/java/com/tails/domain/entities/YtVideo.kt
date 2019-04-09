package com.tails.domain.entities

data class YtVideo(
    var id : String? = null,
    var title : String? = null,
    var thumbnailURL : String? = null,
    var duration : String? = null,
    var viewCount : String? = null
){
    override fun toString(): String {
        return """
                id : $id
                title : $title
                thumbnailURL : $thumbnailURL
                duration : $duration
                viewCount : $viewCount
                """
    }
}