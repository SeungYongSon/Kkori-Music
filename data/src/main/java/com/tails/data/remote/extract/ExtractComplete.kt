package com.tails.data.remote.extract

import com.tails.domain.entity.VideoMeta
import com.tails.domain.entity.YtFile

interface ExtractComplete {
    fun onExtractComplete(ytFile: YtFile?, videoMeta: VideoMeta?)
}