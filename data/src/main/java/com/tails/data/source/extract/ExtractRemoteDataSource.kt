package com.tails.data.source.extract

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.util.SparseArray
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YouTubeExtractor
import at.huber.youtubeExtractor.YtFile
import io.reactivex.Observable
import io.reactivex.subjects.AsyncSubject
import javax.inject.Inject

class ExtractRemoteDataSource @Inject constructor(val context: Context) {

    private val YOUTUBE_ITAG_251 = 251    // webm - stereo, 48 KHz 160 Kbps (opus)
    private val YOUTUBE_ITAG_250 = 250    // webm - stereo, 48 KHz 64 Kbps (opus)
    private val YOUTUBE_ITAG_249 = 249    // webm - stereo, 48 KHz 48 Kbps (opus)
    private val YOUTUBE_ITAG_171 = 171    // webm - stereo, 48 KHz 128 Kbps (vortis)
    private val YOUTUBE_ITAG_141 = 141    // mp4a - stereo, 44.1 KHz 256 Kbps (aac)
    private val YOUTUBE_ITAG_140 = 140    // mp4a - stereo, 44.1 KHz 128 Kbps (aac)
    private val YOUTUBE_ITAG_43 = 43      // webm - stereo, 44.1 KHz 128 Kbps (vortis)
    private val YOUTUBE_ITAG_22 = 22      // mp4 - stereo, 44.1 KHz 192 Kbps (aac)
    private val YOUTUBE_ITAG_18 = 18      // mp4 - stereo, 44.1 KHz 96 Kbps (aac)
    private val YOUTUBE_ITAG_36 = 36      // mp4 - stereo, 44.1 KHz 32 Kbps (aac)
    private val YOUTUBE_ITAG_17 = 17      // mp4 - stereo, 44.1 KHz 24 Kbps (aac)

    fun extract(videoId: String): Observable<String> {
        val streamUrlSubject = AsyncSubject.create<String>()

        object : YouTubeExtractor(context) {
            override fun onExtractionComplete(ytFiles: SparseArray<YtFile>?, videoMeta: VideoMeta?) {
                if (ytFiles != null) {
                    streamUrlSubject.onNext(getBestStream(ytFiles).url!!)
                    streamUrlSubject.onComplete()
                }
            }
        }.extract(videoId, true, false)

        return streamUrlSubject
    }

    private fun getBestStream(ytFiles: SparseArray<YtFile>): YtFile {
        when {
            ytFiles.get(YOUTUBE_ITAG_141) != null -> {
                Log.e(TAG, " gets YOUTUBE_ITAG_141")
                return ytFiles.get(YOUTUBE_ITAG_141)
            }
            ytFiles.get(YOUTUBE_ITAG_140) != null -> {
                Log.e(TAG, " gets YOUTUBE_ITAG_140")
                return ytFiles.get(YOUTUBE_ITAG_140)
            }
            ytFiles.get(YOUTUBE_ITAG_251) != null -> {
                Log.e(TAG, " gets YOUTUBE_ITAG_251")
                return ytFiles.get(YOUTUBE_ITAG_251)
            }
            ytFiles.get(YOUTUBE_ITAG_250) != null -> {
                Log.e(TAG, " gets YOUTUBE_ITAG_250")
                return ytFiles.get(YOUTUBE_ITAG_250)
            }
            ytFiles.get(YOUTUBE_ITAG_249) != null -> {
                Log.e(TAG, " gets YOUTUBE_ITAG_249")
                return ytFiles.get(YOUTUBE_ITAG_249)
            }
            ytFiles.get(YOUTUBE_ITAG_171) != null -> {
                Log.e(TAG, " gets YOUTUBE_ITAG_171")
                return ytFiles.get(YOUTUBE_ITAG_171)
            }
            ytFiles.get(YOUTUBE_ITAG_18) != null -> {
                Log.e(TAG, " gets YOUTUBE_ITAG_18")
                return ytFiles.get(YOUTUBE_ITAG_18)
            }
            ytFiles.get(YOUTUBE_ITAG_22) != null -> {
                Log.e(TAG, " gets YOUTUBE_ITAG_22")
                return ytFiles.get(YOUTUBE_ITAG_22)
            }
            ytFiles.get(YOUTUBE_ITAG_43) != null -> {
                Log.e(TAG, " gets YOUTUBE_ITAG_43")
                return ytFiles.get(YOUTUBE_ITAG_43)
            }
            ytFiles.get(YOUTUBE_ITAG_36) != null -> {
                Log.e(TAG, " gets YOUTUBE_ITAG_36")
                return ytFiles.get(YOUTUBE_ITAG_36)
            }
            else -> {
                Log.e(TAG, " gets YOUTUBE_ITAG_17")
                return ytFiles.get(YOUTUBE_ITAG_17)
            }
        }
    }
}