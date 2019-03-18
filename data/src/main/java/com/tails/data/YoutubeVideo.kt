package com.tails.data

class YoutubeVideo{
    var id = ""
    var title = ""
    var thumbnailURL = ""
    var duration = ""
    var viewCount = ""

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