package com.sakura.anime.presentation.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakura.anime.domain.model.History
import com.sakura.anime.domain.repository.RoomRepository
import com.sakura.anime.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val roomRepository: RoomRepository
) : ViewModel() {
    private val _historyList: MutableStateFlow<Resource<List<History>>> =
        MutableStateFlow(value = Resource.Loading)

    val historyList: StateFlow<Resource<List<History>>>
        get() = _historyList

    init {
        getAllHistories()
    }

    private fun getAllHistories() {
        viewModelScope.launch {
            roomRepository.getHistories().collect {
                _historyList.value = Resource.Success(it)
            }
        }
    }

    fun updateHistoryDate(detailUrl: String) {
        viewModelScope.launch {
            roomRepository.updateHistoryDate(detailUrl)
        }
    }

    fun deleteHistory(detailUrl: String) {
        viewModelScope.launch {
            roomRepository.deleteHistory(detailUrl)
        }
    }
}