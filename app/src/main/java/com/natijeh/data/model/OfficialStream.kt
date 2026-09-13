package com.natijeh.data.model

import android.net.Uri

data class OfficialStream(
    val id: String,
    val title: String,
    val subtitle: String,
    val url: String
)

object OfficialStreamResolver {
    fun forMatch(match: MatchEntity): List<OfficialStream> {
        if (match.status != "LIVE") return emptyList()
        val query = Uri.encode("${match.homeTeamName} ${match.awayTeamName} پخش زنده")
        val providers = mutableListOf(
            OfficialStream("aparat", "آپارات", "جستجوی پخش رسمی مسابقه", "https://www.aparat.com/result/$query"),
            OfficialStream("football360", "فوتبال ۳۶۰", "بررسی صفحه پخش مسابقه", "https://football360.ir/search?q=$query"),
            OfficialStream("varzesh3", "ورزش ۳", "پخش زنده و گزارش مسابقه", "https://www.varzesh3.com/livescore")
        )
        if (isIranian(match)) {
            providers += OfficialStream("tv3", "شبکه سه", "پخش رسمی از تلوبیون", "https://telewebion.net/live/tv3")
        }
        return providers
    }

    private fun isIranian(match: MatchEntity): Boolean {
        val text = "${match.leagueName} ${match.homeTeamName} ${match.awayTeamName}"
        return listOf("ایران", "برتر", "آزادگان", "استقلال", "پرسپولیس", "سپاهان", "تراکتور", "ملوان", "فولاد")
            .any(text::contains)
    }
}
