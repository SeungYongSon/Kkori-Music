package com.tails.data.repository

import com.tails.data.model.SearchResultEntity
import com.tails.data.model.SearchResultMapper
import com.tails.data.source.search.SearchRemoteDataSource
import com.tails.domain.entity.SearchResult
import com.tails.domain.entity.VideoMeta
import com.tails.domain.repository.SearchRepository
import io.reactivex.Single
import okhttp3.ResponseBody
import java.io.UnsupportedEncodingException
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.regex.Pattern
import javax.inject.Inject

class SearchRepositoryImp @Inject constructor(
    private val searchRemoteDataSource: SearchRemoteDataSource,
    private val searchResultMapper: SearchResultMapper
) : SearchRepository {

    override fun searchList(keyword: String): Single<SearchResult> =
        searchRemoteDataSource.searchList(keyword).map {
            val token =
                if (it.nextPageToken != null) it.nextPageToken
                else ""

            val result = ArrayList<String>().apply {
                it.items.forEach { items ->
                    this.add(items.id.videoId)
                }
            }
            searchResultMapper.mapToDomain(SearchResultEntity(result, token))
        }

    override fun searchList(keyword: String, nextPageToken: String): Single<SearchResult> =
        searchRemoteDataSource.searchList(keyword, nextPageToken).map {
            val token =
                if (it.nextPageToken != null) it.nextPageToken
                else ""

            val result = ArrayList<String>().apply {
                it.items.forEach { items ->
                    this.add(items.id.videoId)
                }
            }
            searchResultMapper.mapToDomain(SearchResultEntity(result, token))
        }

    override fun searchResultParse(videoId: String): Single<VideoMeta> =
        searchRemoteDataSource.searchResultParse(videoId)
            .map {
                val info = resBodyToStream(it.body()!!)
                parseVideoMeta(info, videoId)
            }

    companion object {
        private val patTitle = Pattern.compile("title=(.*?)(&|\\z)")
        private val patAuthor = Pattern.compile("author=(.+?)(&|\\z)")
        private val patChannelId = Pattern.compile("ucid=(.+?)(&|\\z)")
        private val patLength = Pattern.compile("length_seconds=(\\d+?)(&|\\z)")
        private val patViewCount = Pattern.compile("view_count=(\\d+?)(&|\\z)")
        private val patHlsvp = Pattern.compile("hlsv p=(.+?)(&|\\z)")
    }

    @Throws(UnsupportedEncodingException::class)
    private fun parseVideoMeta(getVideoInfo: String, videoID: String): VideoMeta {
        var isLiveStream = false
        var title = ""
        var author = ""
        var channelId = ""
        var viewCount: Long = 0
        var length: Long = 0
        var mat = patTitle.matcher(getVideoInfo)

        if (mat.find()) {
            title = URLDecoder.decode(mat.group(1), "UTF-8")
        }

        mat = patHlsvp.matcher(getVideoInfo)
        if (mat.find()) {
            isLiveStream = true
        }

        mat = patAuthor.matcher(getVideoInfo)
        if (mat.find()) {
            author = URLDecoder.decode(mat.group(1), "UTF-8")
        }
        mat = patChannelId.matcher(getVideoInfo)
        if (mat.find()) {
            channelId = mat.group(1)
        }
        mat = patLength.matcher(getVideoInfo)
        if (mat.find()) {
            length = java.lang.Long.parseLong(mat.group(1))
        }
        mat = patViewCount.matcher(getVideoInfo)
        if (mat.find()) {
            viewCount = java.lang.Long.parseLong(mat.group(1))
        }

        return VideoMeta(videoID, title, author, channelId, length, viewCount, isLiveStream, getVideoInfo)
    }

    private fun resBodyToStream(resBody: ResponseBody): String {
        val source = resBody.source().apply {
            request(Long.MAX_VALUE)
        }
        val buffer = source.buffer()
        val charset = Charset.forName("UTF-8")

        return buffer.readString(charset)
    }
}