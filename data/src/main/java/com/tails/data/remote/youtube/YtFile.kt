package com.tails.data.remote.youtube

data class YtFile(
    var format : Format?,
    var url : String?
){
    @Deprecated("", ReplaceWith("format"))
    fun getMeta(): Format? = format

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        val ytFile = other as YtFile?

        if (if (format != null) format!! != ytFile!!.format else ytFile!!.format != null) return false
        return if (url != null) url == ytFile.url else ytFile.url == null
    }

    override fun hashCode(): Int {
        var result = if (format != null) format.hashCode() else 0
        result = 31 * result + if (url != null) url.hashCode() else 0
        return result
    }

    override fun toString(): String
        = "YtFile{" +
                "format=" + format +
                ", url='" + url + '\''.toString() +
                '}'.toString()
}