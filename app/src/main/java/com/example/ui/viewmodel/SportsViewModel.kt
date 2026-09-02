package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.SportsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SportsViewModel(private val repository: SportsRepository) : ViewModel() {

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private val _selectedDate = MutableStateFlow(sdf.format(Date()))
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Date Tabs for Today, Yesterday, Tomorrow
    val datesList: List<Pair<String, String>> = List(5) { i ->
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, i - 2) // yesterday-yesterday, yesterday, today, tomorrow, tomorrow-tomorrow
        val dateVal = sdf.format(cal.time)
        val label = when (i) {
            0 -> "۲ روز قبل"
            1 -> "دیروز"
            2 -> "امروز"
            3 -> "فردا"
            4 -> "۲ روز بعد"
            else -> ""
        }
        Pair(label, dateVal)
    }

    // All Matches (for search)
    val allMatches: StateFlow<List<MatchEntity>> = repository.allMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Matches for Selected Date
    val matchesForSelectedDate: StateFlow<List<MatchEntity>> = _selectedDate
        .flatMapLatest { date -> repository.getMatchesByDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live Matches
    val liveMatches: StateFlow<List<MatchEntity>> = repository.liveMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Leagues
    val allLeagues: StateFlow<List<LeagueEntity>> = repository.allLeagues
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // News
    val newsFeed: StateFlow<List<NewsEntity>> = repository.newsFeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isRefreshingNews = MutableStateFlow(false)
    val isRefreshingNews: StateFlow<Boolean> = _isRefreshingNews.asStateFlow()

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

    // Favorites
    val favoriteMatches: StateFlow<List<MatchEntity>> = repository.favoriteMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteTeams: StateFlow<List<TeamEntity>> = repository.favoriteTeams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteLeagues: StateFlow<List<LeagueEntity>> = repository.favoriteLeagues
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDate(date: String) {
        _selectedDate.value = date
    }

    // Toggles
    fun toggleMatchFavorite(id: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.setMatchFavorite(id, !isFavorite)
        }
    }

    fun toggleTeamFavorite(id: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.setTeamFavorite(id, !isFavorite)
        }
    }

    fun toggleLeagueFavorite(id: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.setLeagueFavorite(id, !isFavorite)
        }
    }

    // Details Getters
    fun getMatchFlow(id: String): Flow<MatchEntity?> = repository.getMatchByIdFlow(id)
    fun getTeamFlow(id: String): Flow<TeamEntity?> = repository.getTeamByIdFlow(id)
    fun getLeagueFlow(id: String): Flow<LeagueEntity?> = repository.getLeagueByIdFlow(id)

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
