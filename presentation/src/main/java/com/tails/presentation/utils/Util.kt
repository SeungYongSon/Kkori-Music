package com.tails.presentation.utils

import com.google.api.services.youtube.model.SearchResult

object Util {

    fun convertISO8601DurationToNormalTime(isoTime: String): String {
        val icH = isoTime.contains("H")
        val icM = isoTime.contains("M")
        val icS = isoTime.contains("S")

        val hours : String
        val minutes : String
        val seconds : String

        if (icH && icM && icS) {
            hours = isoTime.substring(isoTime.indexOf("T") + 1, isoTime.indexOf("H"))
            minutes = isoTime.substring(isoTime.indexOf("H") + 1, isoTime.indexOf("M"))
            seconds = isoTime.substring(isoTime.indexOf("M") + 1, isoTime.indexOf("S"))
            return hours + ":" + formatTo2Digits(minutes) + ":" + formatTo2Digits(seconds)
        } else if (!icH && icM && icS) {
            minutes = isoTime.substring(isoTime.indexOf("T") + 1, isoTime.indexOf("M"))
            seconds = isoTime.substring(isoTime.indexOf("M") + 1, isoTime.indexOf("S"))
            return minutes + ":" + formatTo2Digits(seconds)
        } else if (icH && !icM && icS) {
            hours = isoTime.substring(isoTime.indexOf("T") + 1, isoTime.indexOf("H"))
            seconds = isoTime.substring(isoTime.indexOf("H") + 1, isoTime.indexOf("S"))
            return hours + ":00:" + formatTo2Digits(seconds)
        } else if (icH && icM && !icS) {
            hours = isoTime.substring(isoTime.indexOf("T") + 1, isoTime.indexOf("H"))
            minutes = isoTime.substring(isoTime.indexOf("H") + 1, isoTime.indexOf("M"))
            return hours + ":" + formatTo2Digits(minutes) + ":00"
        } else if (!icH && !icM && icS) {
            seconds = isoTime.substring(isoTime.indexOf("T") + 1, isoTime.indexOf("S"))
            return "0:" + formatTo2Digits(seconds)
        } else if (!icH && icM && !icS) {
            minutes = isoTime.substring(isoTime.indexOf("T") + 1, isoTime.indexOf("M"))
            return "$minutes:00"
        } else if (icH && !icM && !icS) {
            hours = isoTime.substring(isoTime.indexOf("T") + 1, isoTime.indexOf("H"))
            return "$hours:00:00"
        }
        return ""
    }

    private fun formatTo2Digits(str: String): String = if(str.length < 2) "0$str" else str

    fun concatenateIDs(searchResults: List<SearchResult>): String? {
        val contentDetails = StringBuilder()
        for (result in searchResults) {
            if (result.id == null) continue
            val id = result.id.videoId
            if (id != null) {
                contentDetails.append(id)
                contentDetails.append(",")
            }
        }

        if (contentDetails.isEmpty()) return null

        if (contentDetails.toString().endsWith(",")) {
            contentDetails.setLength(contentDetails.length - 1) // remove last
        }

        return contentDetails.toString()
    }
}