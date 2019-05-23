package com.tails.domain.entity

class Format{
    enum class VCodec {
        H263, H264, MPEG4, VP8, VP9, NONE
    }

    enum class ACodec {
        MP3, AAC, VORBIS, OPUS, NONE
    }

    private var iTag: Int = 0
    var ext: String? = null
    private var height: Int = 0
    private var fps: Int = 0
    private val vCodec: VCodec? = null
    private val aCodec: ACodec? = null
    private var audioBitrate: Int = 0
    private var isDashContainer: Boolean = false
    private var isHlsContent: Boolean = false

    constructor(itag: Int, ext: String, height: Int, vCodec: VCodec, aCodec: ACodec, isDashContainer: Boolean){
        this.iTag = itag
        this.ext = ext
        this.height = height
        this.fps = 30
        this.audioBitrate = -1
        this.isDashContainer = isDashContainer
        this.isHlsContent = false
    }

    constructor(itag: Int, ext: String, vCodec: VCodec, aCodec: ACodec, audioBitrate: Int,
                isDashContainer: Boolean){
        this.iTag = itag
        this.ext = ext
        this.height = -1
        this.fps = 30
        this.audioBitrate = audioBitrate
        this.isDashContainer = isDashContainer
        this.isHlsContent = false
    }

    constructor(iTag: Int, ext: String, height: Int, vCodec: VCodec, aCodec: ACodec, audioBitrate: Int,
                isDashContainer: Boolean) {
        this.iTag = iTag
        this.ext = ext
        this.height = height
        this.fps = 30
        this.audioBitrate = audioBitrate
        this.isDashContainer = isDashContainer
        this.isHlsContent = false
    }

    constructor(iTag: Int, ext: String, height: Int, vCodec: VCodec, aCodec: ACodec, audioBitrate: Int,
                isDashContainer: Boolean, isHlsContent: Boolean) {
        this.iTag = iTag
        this.ext = ext
        this.height = height
        this.fps = 30
        this.audioBitrate = audioBitrate
        this.isDashContainer = isDashContainer
        this.isHlsContent = isHlsContent
    }

    constructor(iTag: Int, ext: String, height: Int, vCodec: VCodec, fps: Int, aCodec: ACodec,
                isDashContainer: Boolean) {
        this.iTag = iTag
        this.ext = ext
        this.height = height
        this.audioBitrate = -1
        this.fps = fps
        this.isDashContainer = isDashContainer
        this.isHlsContent = false
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val format = other as Format?

        if (iTag != format!!.iTag) return false
        if (height != format.height) return false
        if (fps != format.fps) return false
        if (audioBitrate != format.audioBitrate) return false
        if (isDashContainer != format.isDashContainer) return false
        if (isHlsContent != format.isHlsContent) return false
        if (if (ext != null) ext != format.ext else format.ext != null) return false
        return if (vCodec != format.vCodec) false else aCodec == format.aCodec

    }

    override fun hashCode(): Int {
        var result = iTag
        result = 31 * result + if (ext != null) ext.hashCode() else 0
        result = 31 * result + height
        result = 31 * result + fps
        result = 31 * result + (vCodec?.hashCode() ?: 0)
        result = 31 * result + (aCodec?.hashCode() ?: 0)
        result = 31 * result + audioBitrate
        result = 31 * result + if (isDashContainer) 1 else 0
        result = 31 * result + if (isHlsContent) 1 else 0
        return result
    }

    override fun toString(): String
        = "Format{" +
                "itag=" + iTag +
                ", ext='" + ext + '\''.toString() +
                ", height=" + height +
                ", fps=" + fps +
                ", vCodec=" + vCodec +
                ", aCodec=" + aCodec +
                ", audioBitrate=" + audioBitrate +
                ", isDashContainer=" + isDashContainer +
                ", isHlsContent=" + isHlsContent +
                '}'.toString()
}