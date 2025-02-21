package org.chtijbug.drools.common.date

import java.text.SimpleDateFormat
import java.util.*

class DateHelper {
    companion object {
        var sFormat: String = "yyyy-MM-dd"
        fun getDate(sDate: String?): Date {
            val sdf = SimpleDateFormat(sFormat)
            return sdf.parse(sDate)
        }
        fun getDate(sDate: String?, anotherFormat: String): Date {
            val sdf = SimpleDateFormat(anotherFormat)
            return sdf.parse(sDate)
        }

        fun getDate(date: Date?): String {
            val sdf = SimpleDateFormat(sFormat)
            val formatedDate = sdf.format(date)
            return formatedDate
        }

        fun getDate(date: Date?, anotherFormat: String): String {
            val sdf = SimpleDateFormat(anotherFormat)
            val formatedDate = sdf.format(date)
            return formatedDate
        }
    }

}