package com.sakura.anime.data.remote.dto

import com.sakura.anime.domain.model.Video

data class VideoBean(
    val title: String,
    val url: String,            /* 视频播放地址 */
    val episodeName: String,    /* 当前播放的剧集数名 */
    val episodes: List<EpisodeBean>,
    val headers: Map<String, String> = emptyMap()
) {
    fun toVideo(): Video {
        val index = episodes.indexOfFirst { it.name == episodeName }
        val episodeUrl = episodes[index].url
        return Video(
            title = title,
            url = url,
            episodeName = episodeName,
            episodeUrl = episodeUrl,
            currentEpisodeIndex = index,
            episodes = episodes.map { it.toEpisode() },
            headers = headers,
        )
    }
}
