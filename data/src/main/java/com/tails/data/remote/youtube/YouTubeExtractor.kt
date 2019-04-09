package com.tails.data.remote.youtube

import android.content.Context
import android.os.AsyncTask
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.SparseArray
import com.evgenii.jsevaluator.JsEvaluator
import com.evgenii.jsevaluator.interfaces.JsCallback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.*
import java.lang.ref.WeakReference
import java.net.URLDecoder
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import java.util.regex.Matcher
import java.util.regex.Pattern

abstract class YouTubeExtractor(context: Context) : AsyncTask<String, Void, SparseArray<YtFile>>() {

    companion object {
        private const val LOG_TAG = "YouTubeExtractor"
        private const val CACHE_FILE_NAME = "decipher_js_funct"
        private const val DASH_PARSE_RETRIES = 5

        private const val USER_AGENT =
            "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.36"
        private const val STREAM_MAP_STRING = "url_encoded_fmt_stream_map"

        private val patDashManifest1 = Pattern.compile("dashmpd=(.+?)(&|\\z)")
        private val patDashManifest2 = Pattern.compile("\"dashmpd\":\"(.+?)\"")
        private val patDashManifestEncSig = Pattern.compile("/s/([0-9A-F|.]{10,}?)(/|\\z)")

        private val patTitle = Pattern.compile("title=(.*?)(&|\\z)")
        private val patAuthor = Pattern.compile("author=(.+?)(&|\\z)")
        private val patChannelId = Pattern.compile("ucid=(.+?)(&|\\z)")
        private val patLength = Pattern.compile("length_seconds=(\\d+?)(&|\\z)")
        private val patViewCount = Pattern.compile("view_count=(\\d+?)(&|\\z)")
        private val patStatusOk = Pattern.compile("status=ok(&|,|\\z)")

        private val patHlsvp = Pattern.compile("hlsvp=(.+?)(&|\\z)")
        private val patHlsItag = Pattern.compile("/itag/(\\d+?)/")

        private val patItag = Pattern.compile("itag=([0-9]+?)([&,])")
        private val patEncSig = Pattern.compile("s=([0-9A-F|.]{10,}?)([&,\"])")
        private val patIsSigEnc = Pattern.compile("s%3D([0-9A-F|.]{10,}?)(%26|%2C)")
        private val patUrl = Pattern.compile("url=(.+?)([&,])")

        private val patVariableFunction =
            Pattern.compile("([{; =])([a-zA-Z$][a-zA-Z0-9$]{0,2})\\.([a-zA-Z$][a-zA-Z0-9$]{0,2})\\(")
        private val patFunction = Pattern.compile("([{; =])([a-zA-Z\$_][a-zA-Z0-9$]{0,2})\\(")

        private val patDecryptionJsFile = Pattern.compile("jsbin\\\\/(player(_ias)?-(.+?).js)")
        private val patSignatureDecFunction =
            Pattern.compile("([\\w$]+)\\s*=\\s*function\\(([\\w$]+)\\).\\s*\\2=\\s*\\2\\.split\\(\"\"\\)\\s*;")

        private const val YOUTUBE_ITAG_251 = 251    // webm - stereo, 48 KHz 160 Kbps (opus)
        private const val YOUTUBE_ITAG_250 = 250    // webm - stereo, 48 KHz 64 Kbps (opus)
        private const val YOUTUBE_ITAG_249 = 249    // webm - stereo, 48 KHz 48 Kbps (opus)
        private const val YOUTUBE_ITAG_171 = 171    // webm - stereo, 48 KHz 128 Kbps (vortis)
        private const val YOUTUBE_ITAG_141 = 141    // mp4a - stereo, 44.1 KHz 256 Kbps (aac)
        private const val YOUTUBE_ITAG_140 = 140    // mp4a - stereo, 44.1 KHz 128 Kbps (aac)
        private const val YOUTUBE_ITAG_43 = 43      // webm - stereo, 44.1 KHz 128 Kbps (vortis)
        private const val YOUTUBE_ITAG_22 = 22      // mp4 - stereo, 44.1 KHz 192 Kbps (aac)
        private const val YOUTUBE_ITAG_18 = 18      // mp4 - stereo, 44.1 KHz 96 Kbps (aac)
        private const val YOUTUBE_ITAG_36 = 36      // mp4 - stereo, 44.1 KHz 32 Kbps (aac)
        private const val YOUTUBE_ITAG_17 = 17      // mp4 - stereo, 44.1 KHz 24 Kbps (aac)

        private val FORMAT_MAP = SparseArray<Format>().apply {
            // Video and Audio
            // http://en.wikipedia.org/wiki/YouTube#Quality_and_formats
            put(5, Format(5, "flv", 240, Format.VCodec.H263, Format.ACodec.MP3, 64, false))
            put(6, Format(6, "flv", 270, Format.VCodec.H263, Format.ACodec.MP3, 64, false))
            put(17, Format(17, "3gp", 144, Format.VCodec.MPEG4, Format.ACodec.AAC, 24, false))
            put(18, Format(18, "mp4", 360, Format.VCodec.H264, Format.ACodec.AAC, 96, false))
            put(22, Format(22, "mp4", 720, Format.VCodec.H264, Format.ACodec.AAC, 192, false))
            put(34, Format(36, "flv", 360, Format.VCodec.MPEG4, Format.ACodec.AAC, 128, false))
            put(35, Format(36, "flv", 480, Format.VCodec.MPEG4, Format.ACodec.AAC, 128, false))
            put(36, Format(36, "3gp", 240, Format.VCodec.MPEG4, Format.ACodec.AAC, 32, false))
            put(37, Format(37, "mp4", 1080, Format.VCodec.H264, Format.ACodec.AAC, 192, false))
            put(38, Format(38, "mp4", 3072, Format.VCodec.H264, Format.ACodec.AAC, 192, false))
            put(43, Format(43, "webm", 360, Format.VCodec.VP8, Format.ACodec.VORBIS, 128, false))
            put(44, Format(44, "webm", 480, Format.VCodec.VP8, Format.ACodec.VORBIS, 128, false))
            put(45, Format(45, "webm", 720, Format.VCodec.VP8, Format.ACodec.VORBIS, 192, false))
            put(46, Format(46, "webm", 1080, Format.VCodec.VP8, Format.ACodec.VORBIS, 192, false))
            put(59, Format(59, "mp4", 480, Format.VCodec.H264, Format.ACodec.AAC, 128, false))
            put(78, Format(78, "mp4", 480, Format.VCodec.H264, Format.ACodec.AAC, 128, false))

            // Dash Video
            put(133, Format(133, "mp4", 240, Format.VCodec.H264, Format.ACodec.NONE, true))
            put(134, Format(134, "mp4", 360, Format.VCodec.H264, Format.ACodec.NONE, true))
            put(135, Format(135, "mp4", 480, Format.VCodec.H264, Format.ACodec.NONE, true))
            put(136, Format(136, "mp4", 720, Format.VCodec.H264, Format.ACodec.NONE, true))
            put(137, Format(137, "mp4", 1080, Format.VCodec.H264, Format.ACodec.NONE, true))
            //itag 138 videos are either 3840x2160 or 7680x4320 (sLprVF6d7Ug)
            put(138, Format(138, "mp4", 4320, Format.VCodec.H264, Format.ACodec.NONE, true))
            put(160, Format(160, "mp4", 144, Format.VCodec.H264, Format.ACodec.NONE, true))
            put(160, Format(160, "mp4", 144, Format.VCodec.H264, Format.ACodec.NONE, true))
            put(264, Format(264, "mp4", 1440, Format.VCodec.H264, Format.ACodec.NONE, true))
            put(266, Format(266, "mp4", 2160, Format.VCodec.H264, Format.ACodec.NONE, true))

            put(298, Format(298, "mp4", 720, Format.VCodec.H264, 60, Format.ACodec.NONE, true))
            put(299, Format(299, "mp4", 1080, Format.VCodec.H264, 60, Format.ACodec.NONE, true))

            // Dash Audio
            put(139, Format(139, "m4a", Format.VCodec.NONE, Format.ACodec.AAC, 48, true))
            put(140, Format(140, "m4a", Format.VCodec.NONE, Format.ACodec.AAC, 128, true))
            put(141, Format(141, "m4a", Format.VCodec.NONE, Format.ACodec.AAC, 256, true))

            // WEBM Dash Video
            put(167, Format(167, "webm", 360, Format.VCodec.VP9, Format.ACodec.NONE, true))
            put(168, Format(168, "webm", 480, Format.VCodec.VP9, Format.ACodec.NONE, true))
            put(169, Format(169, "webm", 720, Format.VCodec.VP9, Format.ACodec.NONE, true))
            put(170, Format(170, "webm", 1080, Format.VCodec.VP9, Format.ACodec.NONE, true))
            put(218, Format(218, "webm", 480, Format.VCodec.VP9, Format.ACodec.NONE, true))
            put(219, Format(219, "webm", 480, Format.VCodec.VP9, Format.ACodec.NONE, true))
            put(278, Format(278, "webm", 144, Format.VCodec.VP9, Format.ACodec.NONE, true))
            put(242, Format(242, "webm", 240, Format.VCodec.VP9, Format.ACodec.NONE, true))
            put(243, Format(243, "webm", 360, Format.VCodec.VP9, Format.ACodec.NONE, true))
            put(244, Format(244, "webm", 480, Format.VCodec.VP9, Format.ACodec.NONE, true))
            put(247, Format(247, "webm", 720, Format.VCodec.VP9, Format.ACodec.NONE, true))
            put(248, Format(248, "webm", 1080, Format.VCodec.VP9, Format.ACodec.NONE, true))
            put(271, Format(271, "webm", 1440, Format.VCodec.VP9, Format.ACodec.NONE, true))
            //itag 272 videos are either 3840x2160 (e.g. RtoitU2A-3E) or 7680x4320 (sLprVF6d7Ug)
            put(272, Format(272, "webm", 2160, Format.VCodec.VP9, Format.ACodec.NONE, true))
            put(313, Format(313, "webm", 2160, Format.VCodec.VP9, Format.ACodec.NONE, true))

            put(302, Format(302, "webm", 720, Format.VCodec.VP9, 60, Format.ACodec.NONE, true))
            put(303, Format(303, "webm", 1080, Format.VCodec.VP9, 60, Format.ACodec.NONE, true))
            put(308, Format(308, "webm", 1440, Format.VCodec.VP9, 60, Format.ACodec.NONE, true))
            put(315, Format(315, "webm", 2160, Format.VCodec.VP9, 60, Format.ACodec.NONE, true))

            // WEBM Dash Audio
            put(171, Format(171, "webm", Format.VCodec.NONE, Format.ACodec.VORBIS, 128, true))
            put(172, Format(172, "webm", Format.VCodec.NONE, Format.ACodec.VORBIS, 128, true))

            // WEBM Dash audio with opus inside
            put(249, Format(249, "webm", Format.VCodec.NONE, Format.ACodec.OPUS, 50, true))
            put(250, Format(250, "webm", Format.VCodec.NONE, Format.ACodec.OPUS, 70, true))
            put(251, Format(251, "webm", Format.VCodec.NONE, Format.ACodec.OPUS, 160, true))

            // HLS Live Stream
            put(91, Format(91, "mp4", 144, Format.VCodec.H264, Format.ACodec.AAC, 48, false, true))
            put(92, Format(92, "mp4", 240, Format.VCodec.H264, Format.ACodec.AAC, 48, false, true))
            put(93, Format(93, "mp4", 360, Format.VCodec.H264, Format.ACodec.AAC, 128, false, true))
            put(94, Format(94, "mp4", 480, Format.VCodec.H264, Format.ACodec.AAC, 128, false, true))
            put(95, Format(95, "mp4", 720, Format.VCodec.H264, Format.ACodec.AAC, 256, false, true))
            put(96, Format(96, "mp4", 1080, Format.VCodec.H264, Format.ACodec.AAC, 256, false, true))
            put(132, Format(132, "mp4", 240, Format.VCodec.H264, Format.ACodec.AAC, 48, false, true))
            put(151, Format(151, "mp4", 72, Format.VCodec.H264, Format.ACodec.AAC, 24, false, true))
        }
    }

    private val interceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        //.addInterceptor(interceptor)
        .build()

    private var parseDashManifest = false
    private var includeWebM = true
    private lateinit var videoID: String
    private lateinit var videoMeta: VideoMeta

    private val refContext = WeakReference<Context>(context)
    private val cacheDirPath = context.cacheDir.absolutePath

    @Volatile
    private var decipheredSignature: String? = null

    private var decipherJsFileName: String? = null
    private var decipherFunctions: String? = null
    private var decipherFunctionName: String? = null

    private val lock = ReentrantLock()
    private val jsExecuting = lock.newCondition()

    fun extract(videoID: String, parseDashManifest: Boolean, includeWebM: Boolean) {
        this.parseDashManifest = parseDashManifest
        this.includeWebM = includeWebM
        this.execute(videoID)
    }

    override fun onPostExecute(ytFiles: SparseArray<YtFile>?) {
        onExtractionComplete(getBestStream(ytFiles), videoMeta)
    }

    protected abstract fun onExtractionComplete(ytFile: YtFile?, videoMeta: VideoMeta?)

    override fun doInBackground(vararg params: String): SparseArray<YtFile>? {
        videoID = params[0]

        if (videoID.isNotEmpty()) {
            try {
                return getStreamUrls()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        Log.e(LOG_TAG, "videoID = null")
        return null
    }

    @Throws(IOException::class, InterruptedException::class)
    private fun getStreamUrls(): SparseArray<YtFile>? {
        var dashMpdUrl: String? = null

        val reqVideoInfo = Request.Builder()
            .url("https://www.youtube.com/get_video_info?video_id=$videoID&eurl=https://youtube.googleapis.com/v/$videoID")
            .addHeader("User-Agent", USER_AGENT)
            .build()

        val resBody = client.newCall(reqVideoInfo).execute().body()!!
        var streamMap = resBodyToStream(resBody)

        parseVideoMeta(streamMap)

        var mat: Matcher
        var curJsFileName: String? = null
        val streams: Array<String>
        var encSignatures: SparseArray<String>? = null

        if (videoMeta.isLiveStream) {
            mat = patHlsvp.matcher(streamMap)
            if (mat.find()) {
                val hlsvp = URLDecoder.decode(mat.group(1), "UTF-8")
                val ytFiles = SparseArray<YtFile>()

                val reqHlsvp = Request.Builder()
                    .url(hlsvp)
                    .addHeader("User-Agent", USER_AGENT)
                    .build()

                val hlsvpBody = client.newCall(reqHlsvp).execute().body()!!
                val bodyInputStream = hlsvpBody.source().inputStream()
                val reader = BufferedReader(InputStreamReader(bodyInputStream))
                var line: String
                reader.use {
                    while (true) {
                        line = it.readLine()
                        if (line.startsWith("https://") || line.startsWith("http://")) {
                            mat = patHlsItag.matcher(line)
                            if (mat.find()) {
                                val itag = Integer.parseInt(mat.group(1))
                                val newFile = YtFile(FORMAT_MAP.get(itag), line)
                                ytFiles.put(itag, newFile)
                            }
                        }
                    }
                }
                if (ytFiles.size() == 0) {
                    return null
                }
                return ytFiles
            }
            return null
        }

        var sigEnc = true
        var statusFail = false
        if (streamMap.contains(STREAM_MAP_STRING)) {
            val streamMapSub = streamMap.substring(streamMap.indexOf(STREAM_MAP_STRING))
            mat = patIsSigEnc.matcher(streamMapSub)
            if (!mat.find()) {
                sigEnc = false

                if (!patStatusOk.matcher(streamMap).find())
                    statusFail = true
            }
        }

        if (sigEnc || statusFail) {
            if (decipherJsFileName == null || decipherFunctions == null || decipherFunctionName == null)
                readDecipherFunctFromCache()

            val reqYouTube = Request.Builder()
                .url("https://youtube.com/watch?v=$videoID")
                .addHeader("User-Agent", USER_AGENT)
                .build()

            val youTubeBody = client.newCall(reqYouTube).execute().body()!!
            val bodyInputStream = youTubeBody.source().inputStream()
            val reader = BufferedReader(InputStreamReader(bodyInputStream))
            var line: String
            reader.use {
                while (true) {
                    line = it.readLine()
                    if (line.contains(STREAM_MAP_STRING)) {
                        streamMap = line.replace("\\u0026", "&")
                        break
                    }
                }
                reader.close()
            }

            encSignatures = SparseArray()

            mat = patDecryptionJsFile.matcher(streamMap)
            if (mat.find()) {
                curJsFileName = mat.group(1).replace("\\/", "/")
                if (mat.group(2) != null)
                    curJsFileName.replace(mat.group(2), "")
                if (decipherJsFileName == null || decipherJsFileName != curJsFileName) {
                    decipherFunctions = null
                    decipherFunctionName = null
                }
                decipherJsFileName = curJsFileName
            }

            if (parseDashManifest) {
                mat = patDashManifest2.matcher(streamMap)
                if (mat.find()) {
                    dashMpdUrl = mat.group(1).replace("\\/", "/")
                    mat = patDashManifestEncSig.matcher(dashMpdUrl)
                    if (mat.find()) {
                        encSignatures.append(0, mat.group(1))
                    } else {
                        dashMpdUrl = null
                    }
                }
            }
        } else {
            if (parseDashManifest) {
                mat = patDashManifest1.matcher(streamMap)
                if (mat.find()) {
                    dashMpdUrl = URLDecoder.decode(mat.group(1), "UTF-8")
                }
            }
            streamMap = URLDecoder.decode(streamMap, "UTF-8")
        }

        streams = streamMap.split(",|$STREAM_MAP_STRING|&adaptive_fmts=".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        val ytFiles = SparseArray<YtFile>()
        for (it in streams) {
            val encStream = "$it,"
            if (!encStream.contains("itag%3D")) {
                continue
            }
            val stream: String
            stream = URLDecoder.decode(encStream, "UTF-8")

            mat = patItag.matcher(stream)
            val itag: Int
            if (mat.find()) {
                itag = Integer.parseInt(mat.group(1))
                Log.d(LOG_TAG, "Itag found:$itag")
                if (FORMAT_MAP.get(itag) == null) {
                    Log.d(LOG_TAG, "Itag not in list:$itag")
                    continue
                } else if (!includeWebM && FORMAT_MAP.get(itag).ext.equals("webm")) {
                    continue
                }
            } else {
                continue
            }

            if (curJsFileName != null) {
                mat = patEncSig.matcher(stream)
                if (mat.find()) {
                    encSignatures?.append(itag, mat.group(1))
                }
            }

            mat = patUrl.matcher(encStream)
            var url: String? = null
            if (mat.find()) {
                url = mat.group(1)
            }

            if (url != null) {
                val format = FORMAT_MAP.get(itag)
                val finalUrl = URLDecoder.decode(url, "UTF-8")
                val newVideo = YtFile(format, finalUrl)
                ytFiles.put(itag, newVideo)
            }
        }

        if (encSignatures != null) {
            Log.d(LOG_TAG, "Decipher signatures: " + encSignatures.size() + ", videos: " + ytFiles.size())
            decipheredSignature = null
            if (decipherSignature(encSignatures)) {
                lock.lock()
                try {
                    jsExecuting.await(7, TimeUnit.SECONDS)
                } finally {
                    lock.unlock()
                }
            }
            val signature = decipheredSignature
            if (signature == null) {
                return null
            } else {
                val sigs = signature.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                var i = 0
                while (i < encSignatures.size() && i < sigs.size) {
                    val key = encSignatures.keyAt(i)
                    if (key == 0) {
                        if (dashMpdUrl != null) {
                            dashMpdUrl = dashMpdUrl.replace("/s/" + encSignatures.get(key), "/signature/" + sigs[i])
                        }
                    } else {
                        var url = ytFiles.get(key).url
                        url += "&signature=" + sigs[i]
                        val newFile = YtFile(FORMAT_MAP.get(key), url)
                        ytFiles.put(key, newFile)
                    }
                    i++
                }
            }
            if (parseDashManifest && dashMpdUrl != null) {
                for (i in 0 until DASH_PARSE_RETRIES) {
                    try {
                        // It sometimes fails to connect for no apparent reason. We just retry.
                        parseDashManifest(dashMpdUrl, ytFiles)
                        break
                    } catch (io: IOException) {
                        Thread.sleep(5)
                        Log.d(LOG_TAG, "Failed to parse dash manifest " + (i + 1))
                    }

                }
            }

            if (ytFiles.size() == 0) {
                Log.d(LOG_TAG, streamMap)
                return null
            }
        }
        return ytFiles
    }

    private fun resBodyToStream(resBody: ResponseBody): String {
        val source = resBody.source().apply {
            request(Long.MAX_VALUE)
        }
        val buffer = source.buffer()
        val charset = Charset.forName("UTF-8")

        return buffer.readString(charset)
    }

    @Throws(IOException::class)
    private fun decipherSignature(encSignatures: SparseArray<String>): Boolean {
        if (decipherFunctionName == null || decipherFunctions == null) {
            val reqDecipher = Request.Builder()
                .url("https://s.ytimg.com/yts/jsbin/$decipherJsFileName")
                .header("User-Agent", USER_AGENT)
                .build()

            val decipherBody = client.newCall(reqDecipher).execute().body()!!
            val bodyInputStream = decipherBody.source().inputStream()
            val reader = BufferedReader(InputStreamReader(bodyInputStream))
            var javascriptFile = ""
            var line: String?
            reader.use {
                val sb = StringBuilder()
                while (true) {
                    line = it.readLine()
                    if (line != null) {
                        sb.append(line)
                        sb.append(" ")
                    } else {
                        break
                    }
                }
                javascriptFile = sb.toString()
                reader.close()
            }
            Log.d(LOG_TAG, "Decipher FunctURL: https://s.ytimg.com/yts/jsbin/$decipherJsFileName")

            var mat = patSignatureDecFunction.matcher(javascriptFile)
            if (mat.find()) {
                decipherFunctionName = mat.group(1)
                Log.d(LOG_TAG, "Decipher Functname: $decipherFunctionName")

                val patMainVariable = Pattern.compile(
                    "(var |\\s|,|;)${decipherFunctionName?.replace("$", "\\$")}(=function\\((.{1,3})\\)\\{)"
                )

                var mainDecipherFunct: String?
                mat = patMainVariable.matcher(javascriptFile)
                if (mat.find()) {
                    mainDecipherFunct = "var $decipherFunctionName${mat.group(2)}"
                } else {
                    val patMainFunction = Pattern.compile(
                        "function ${decipherFunctionName?.replace("$", "\\$")}(\\((.{1,3})\\)\\{)"
                    )
                    mat = patMainFunction.matcher(javascriptFile)
                    if (!mat.find())
                        return false
                    mainDecipherFunct = "function $decipherFunctionName${mat.group(2)}"
                }

                var startIndex = mat.end()
                var braces = 1
                var i = startIndex
                while (i < javascriptFile.length) {
                    if (braces == 0 && startIndex + 5 < i) {
                        mainDecipherFunct += javascriptFile.substring(startIndex, i) + ";"
                        break
                    }
                    if (javascriptFile[i] == '{')
                        braces++
                    else if (javascriptFile[i] == '}')
                        braces--
                    i++
                }
                decipherFunctions = mainDecipherFunct

                mat = patVariableFunction.matcher(mainDecipherFunct)
                while (mat.find()) {
                    val variableDef = "var " + mat.group(2) + "={"
                    if (decipherFunctions!!.contains(variableDef)) {
                        continue
                    }
                    startIndex = javascriptFile.indexOf(variableDef) + variableDef.length
                    var braces = 1
                    var i = startIndex
                    while (i < javascriptFile.length) {
                        if (braces == 0) {
                            decipherFunctions += variableDef + javascriptFile.substring(startIndex, i) + ";"
                            break
                        }
                        if (javascriptFile[i] == '{')
                            braces++
                        else if (javascriptFile[i] == '}')
                            braces--
                        i++
                    }
                }
                // Search for functions
                mat = patFunction.matcher(mainDecipherFunct)
                while (mat.find()) {
                    val functionDef = "function " + mat.group(2) + "("
                    if (decipherFunctions!!.contains(functionDef)) {
                        continue
                    }
                    startIndex = javascriptFile.indexOf(functionDef) + functionDef.length
                    var braces = 0
                    var i = startIndex
                    while (i < javascriptFile.length) {
                        if (braces == 0 && startIndex + 5 < i) {
                            decipherFunctions += functionDef + javascriptFile.substring(startIndex, i) + ";"
                            break
                        }
                        if (javascriptFile[i] == '{')
                            braces++
                        else if (javascriptFile[i] == '}')
                            braces--
                        i++
                    }
                }
                Log.d(LOG_TAG, "Decipher Function: $decipherFunctions")
                decipherViaWebView(encSignatures)
                writeDecipherFunctionToCache()
            } else {
                return false
            }
        } else {
            decipherViaWebView(encSignatures)
        }
        return true
    }


    @Throws(IOException::class)
    private fun parseDashManifest(dashMpdUrl: String, ytFiles: SparseArray<YtFile>) {
        val patBaseUrl = Pattern.compile("<\\s*BaseURL(.*?)>(.+?)<\\s*/BaseURL\\s*>")
        val patDashItag = Pattern.compile("itag/([0-9]+?)/")
        var dashManifest: String? = null

        val reqDashMp = Request.Builder()
            .url(dashMpdUrl)
            .header("User-Agent", USER_AGENT)
            .build()

        val dashMpBody = client.newCall(reqDashMp).execute().body()!!
        val bodyInputStream = dashMpBody.source().inputStream()
        val reader = BufferedReader(InputStreamReader(bodyInputStream))

        reader.use {
            it.readLine()
            dashManifest = it.readLine()
        }

        if (dashManifest != null) {
            val mat = patBaseUrl.matcher(dashManifest)
            while (mat.find()) {
                val itag: Int
                val url = mat.group(2)
                val mat2 = patDashItag.matcher(url)
                if (mat2.find()) {
                    itag = Integer.parseInt(mat2.group(1))
                    if (FORMAT_MAP.get(itag) == null)
                        continue
                    if (!includeWebM && FORMAT_MAP.get(itag).ext.equals("webm"))
                        continue
                } else {
                    continue
                }
                val yf = YtFile(FORMAT_MAP.get(itag), url)
                ytFiles.append(itag, yf)
            }
        }
    }

    @Throws(UnsupportedEncodingException::class)
    private fun parseVideoMeta(getVideoInfo: String) {
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
        videoMeta = VideoMeta(videoID, title, author, channelId, length, viewCount, isLiveStream)
    }

    private fun readDecipherFunctFromCache() {
        val cacheFile = File("$cacheDirPath/$CACHE_FILE_NAME")
        // The cached functions are valid for 2 weeks
        if (cacheFile.exists() && System.currentTimeMillis() - cacheFile.lastModified() < 1209600000) {
            var reader: BufferedReader? = null
            try {
                reader = BufferedReader(InputStreamReader(FileInputStream(cacheFile), "UTF-8"))
                decipherJsFileName = reader.readLine()
                decipherFunctionName = reader.readLine()
                decipherFunctions = reader.readLine()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (reader != null) {
                    try {
                        reader.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }
        }
    }

    private fun decipherViaWebView(encSignatures: SparseArray<String>) {
        val context = refContext.get() ?: return

        val stb = StringBuilder("$decipherFunctions function decipher(")
        stb.append("){return ")
        for (i in 0 until encSignatures.size()) {
            val key = encSignatures.keyAt(i)
            if (i < encSignatures.size() - 1)
                stb.append(decipherFunctionName).append("('").append(encSignatures.get(key)).append("')+\"\\n\"+")
            else
                stb.append(decipherFunctionName).append("('").append(encSignatures.get(key)).append("')")
        }
        stb.append("};decipher();")

        Handler(Looper.getMainLooper()).post {
            JsEvaluator(context).evaluate(stb.toString(), object : JsCallback {
                override fun onResult(result: String?) {
                    lock.lock()
                    try {
                        decipheredSignature = result
                        jsExecuting.signal()
                    } finally {
                        lock.unlock()
                    }
                }

                override fun onError(errorMessage: String?) {
                    lock.lock()
                    try {
                        Log.e(LOG_TAG, errorMessage)
                        jsExecuting.signal()
                    } finally {
                        lock.unlock()
                    }
                }
            })
        }
    }

    private fun writeDecipherFunctionToCache() {
        val cacheFile = File("$cacheDirPath/$CACHE_FILE_NAME")
        var writer: BufferedWriter? = null
        try {
            writer = BufferedWriter(OutputStreamWriter(FileOutputStream(cacheFile), "UTF-8"))
            writer.write(decipherJsFileName + "\n")
            writer.write(decipherFunctionName + "\n")
            writer.write(decipherFunctions)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            if (writer != null) {
                try {
                    writer.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getBestStream(ytFiles: SparseArray<YtFile>?): YtFile? {
        Log.e(LOG_TAG, "ytFiles: $ytFiles")
        if (ytFiles != null) {
            when {
                ytFiles.get(YOUTUBE_ITAG_141) != null -> {
                    Log.e(LOG_TAG, " gets YOUTUBE_ITAG_141")
                    return ytFiles.get(YOUTUBE_ITAG_141)
                }
                ytFiles.get(YOUTUBE_ITAG_140) != null -> {
                    Log.e(LOG_TAG, " gets YOUTUBE_ITAG_140")
                    return ytFiles.get(YOUTUBE_ITAG_140)
                }
                ytFiles.get(YOUTUBE_ITAG_251) != null -> {
                    Log.e(LOG_TAG, " gets YOUTUBE_ITAG_251")
                    return ytFiles.get(YOUTUBE_ITAG_251)
                }
                ytFiles.get(YOUTUBE_ITAG_250) != null -> {
                    Log.e(LOG_TAG, " gets YOUTUBE_ITAG_250")
                    return ytFiles.get(YOUTUBE_ITAG_250)
                }
                ytFiles.get(YOUTUBE_ITAG_249) != null -> {
                    Log.e(LOG_TAG, " gets YOUTUBE_ITAG_249")
                    return ytFiles.get(YOUTUBE_ITAG_249)
                }
                ytFiles.get(YOUTUBE_ITAG_171) != null -> {
                    Log.e(LOG_TAG, " gets YOUTUBE_ITAG_171")
                    return ytFiles.get(YOUTUBE_ITAG_171)
                }
                ytFiles.get(YOUTUBE_ITAG_18) != null -> {
                    Log.e(LOG_TAG, " gets YOUTUBE_ITAG_18")
                    return ytFiles.get(YOUTUBE_ITAG_18)
                }
                ytFiles.get(YOUTUBE_ITAG_22) != null -> {
                    Log.e(LOG_TAG, " gets YOUTUBE_ITAG_22")
                    return ytFiles.get(YOUTUBE_ITAG_22)
                }
                ytFiles.get(YOUTUBE_ITAG_43) != null -> {
                    Log.e(LOG_TAG, " gets YOUTUBE_ITAG_43")
                    return ytFiles.get(YOUTUBE_ITAG_43)
                }
                ytFiles.get(YOUTUBE_ITAG_36) != null -> {
                    Log.e(LOG_TAG, " gets YOUTUBE_ITAG_36")
                    return ytFiles.get(YOUTUBE_ITAG_36)
                }
                else -> {
                    Log.e(LOG_TAG, " gets YOUTUBE_ITAG_17")
                    return ytFiles.get(YOUTUBE_ITAG_17)
                }
            }
        } else return null
    }
}