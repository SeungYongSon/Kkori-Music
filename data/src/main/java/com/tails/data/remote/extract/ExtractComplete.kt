package com.tails.data.remote.extract

import com.tails.domain.entities.VideoMeta
import com.tails.domain.entities.YtFile

interface ExtractComplete {
    fun onExtractComplete(ytFile: YtFile?, videoMeta: VideoMeta?)
}