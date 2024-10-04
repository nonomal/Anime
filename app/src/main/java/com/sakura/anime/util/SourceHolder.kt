package com.sakura.anime.util

import com.sakura.anime.application.AnimeApplication
import com.sakura.anime.data.remote.parse.AgedmSource
import com.sakura.anime.data.remote.parse.AnfunsSource
import com.sakura.anime.data.remote.parse.AnimeSource
import com.sakura.anime.data.remote.parse.GirigiriSource
import com.sakura.anime.data.remote.parse.MxdmSource
import com.sakura.anime.data.remote.parse.NyafunSource
import com.sakura.anime.data.remote.parse.SilisiliSource
import com.sakura.anime.data.remote.parse.YhdmSource

object SourceHolder {
    private lateinit var _currentSource: AnimeSource
    private lateinit var _currentSourceMode: SourceMode

    /**
     * 默认动漫源
     */
    val DEFAULT_ANIME_SOURCE = SourceMode.Mxdm

    val currentSource: AnimeSource
        get() = _currentSource

    val currentSourceMode: SourceMode
        get() = _currentSourceMode

    var isSourceChanged = false

    init {
        val preferences = AnimeApplication.getInstance().preferences
        initDefaultSource(preferences.getEnum(KEY_SOURCE_MODE, DEFAULT_ANIME_SOURCE))
    }

    /**
     * 初始化加载默认的数据源，切换数据源请用方法[SourceHolder].switchSource()
     */
    private fun initDefaultSource(mode: SourceMode) {
        _currentSource = getSource(mode)
        _currentSourceMode = mode
        _currentSource.onEnter()
    }

    /**
     *切换数据源
     */
    fun switchSource(mode: SourceMode) {
        _currentSource.onExit()

        _currentSource = getSource(mode)
        _currentSourceMode = mode

        _currentSource.onEnter()
    }

    /**
     * 根据[SourceMode]获取对应的[AnimeSource]数据源
     * */
    fun getSource(mode: SourceMode): AnimeSource {
        return when (mode) {
            SourceMode.Yhdm -> YhdmSource
            SourceMode.Silisili -> SilisiliSource
            SourceMode.Mxdm -> MxdmSource
            SourceMode.Agedm -> AgedmSource
            SourceMode.Anfuns -> AnfunsSource
            SourceMode.Girigiri -> GirigiriSource
            SourceMode.Nyafun -> NyafunSource
        }
    }
}