package com.natijeh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.natijeh.data.mapper.SportsMapper
import com.natijeh.data.model.LeagueEntity
import com.natijeh.data.model.MatchEntity
import com.natijeh.data.model.NewsEntity
import com.natijeh.data.model.TeamEntity
import com.natijeh.data.repository.SportsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class SportsViewModel(private val repository: SportsRepository) : ViewModel() {

    val datesList: List<Pair<String, Int>> = listOf(
        "۲ روز قبل" to -2,
        "دیروز" to -1,
        "امروز" to 0,
        "فردا" to 1,
        "۲ روز بعد" to 2
    )

    private val _selectedOffset = MutableStateFlow(0)
    val selectedOffset: StateFlow<Int> = _selectedOffset.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    val allMatches: StateFlow<List<MatchEntity>> = repository.allMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val matchesForSelectedDate: StateFlow<List<MatchEntity>> = _selectedOffset
        .flatMapLatest { offset -> repository.getMatchesByOffset(offset) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val liveMatches: StateFlow<List<MatchEntity>> = repository.liveMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allLeagues: StateFlow<List<LeagueEntity>> = repository.allLeagues
        .map { list ->
            list.sortedWith(
                compareBy({ SportsMapper.leaguePriority(it.id, it.name) }, { it.name })
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val allTeams: StateFlow<List<TeamEntity>> = repository.allTeams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val newsFeed: StateFlow<List<NewsEntity>> = repository.newsFeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isRefreshingNews = MutableStateFlow(false)
    val isRefreshingNews: StateFlow<Boolean> = _isRefreshingNews.asStateFlow()

    val favoriteMatches: StateFlow<List<MatchEntity>> = repository.favoriteMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteTeams: StateFlow<List<TeamEntity>> = repository.favoriteTeams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteLeagues: StateFlow<List<LeagueEntity>> = repository.favoriteLeagues
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        repository.startLiveUpdates()
        refresh(forceAll = true)
    }

    fun selectDate(offset: Int) {
        _selectedOffset.value = offset
        viewModelScope.launch {
            try {
                repository.refreshLiveScore(offset)
            } catch (e: Exception) {
                _errorMessage.value = "به‌روزرسانی این روز ناموفق بود"
            }
        }
    }

    fun refresh(forceAll: Boolean = false) {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                if (forceAll) {
                    repository.refreshAll(includeNews = true)
                } else {
                    repository.refreshLiveScore(_selectedOffset.value)
                    if (_selectedOffset.value != 0) {
                        repository.refreshLiveScore(0)
                    }
                }
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "اتصال به داده زنده برقرار نشد. بعداً دوباره تلاش کنید."
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refreshNews() {
        viewModelScope.launch {
            _isRefreshingNews.value = true
            try {
                repository.fetchRssNews()
            } catch (e: Exception) {
                android.util.Log.e("SportsViewModel", "Error refreshing news", e)
            } finally {
                _isRefreshingNews.value = false
            }
        }
    }

    fun toggleMatchFavorite(id: String, isFavorite: Boolean) {
        viewModelScope.launch { repository.setMatchFavorite(id, !isFavorite) }
    }

    fun toggleTeamFavorite(id: String, isFavorite: Boolean) {
        viewModelScope.launch { repository.setTeamFavorite(id, !isFavorite) }
    }

    fun toggleLeagueFavorite(id: String, isFavorite: Boolean) {
        viewModelScope.launch { repository.setLeagueFavorite(id, !isFavorite) }
    }

    fun getMatchFlow(id: String) = repository.getMatchByIdFlow(id)
    fun getTeamFlow(id: String) = repository.getTeamByIdFlow(id)
    fun getLeagueFlow(id: String) = repository.getLeagueByIdFlow(id)

    fun loadMatchDetails(id: String) {
        viewModelScope.launch {
            try {
                repository.loadMatchDetails(id)
            } catch (e: Exception) {
                android.util.Log.e("SportsViewModel", "Match details failed", e)
            }
        }
    }

    fun loadTeamDetails(id: String) {
        viewModelScope.launch {
            try {
                repository.loadTeamDetails(id)
            } catch (e: Exception) {
                android.util.Log.e("SportsViewModel", "Team details failed", e)
            }
        }
    }

    fun loadLeagueDetails(id: String) {
        viewModelScope.launch {
            try {
                repository.loadLeagueDetails(id)
            } catch (e: Exception) {
                android.util.Log.e("SportsViewModel", "League details failed", e)
            }
        }
    }

    override fun onCleared() {
        repository.stopLiveUpdates()
        super.onCleared()
    }

    class Factory(private val repository: SportsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SportsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SportsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
