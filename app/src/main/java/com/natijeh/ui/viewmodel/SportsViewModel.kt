package com.natijeh.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.natijeh.BuildConfig
import com.natijeh.data.mapper.SportsMapper
import com.natijeh.data.model.LeagueEntity
import com.natijeh.data.model.MatchEntity
import com.natijeh.data.model.NewsEntity
import com.natijeh.data.model.PlayerEntity
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

    data class MaintenanceState(
        val checkingUpdate: Boolean = false,
        val updateMessage: String? = null,
        val updateUrl: String? = null,
        val updateVersion: String? = null,
        val updateDownloading: Boolean = false,
        val updateProgress: Int? = null,
        val updateApkUri: String? = null,
        val downloading: Boolean = false,
        val downloadMessage: String? = null,
        val downloadProgress: Int? = null
    )

    private val _maintenance = MutableStateFlow(MaintenanceState())
    val maintenance: StateFlow<MaintenanceState> = _maintenance.asStateFlow()

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

    val allPlayers: StateFlow<List<PlayerEntity>> = repository.allPlayers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val newsFeed: StateFlow<List<NewsEntity>> = repository.newsFeed
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val notificationHistory = repository.notificationHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _isRefreshingNews = MutableStateFlow(false)
    val isRefreshingNews: StateFlow<Boolean> = _isRefreshingNews.asStateFlow()

    val favoriteMatches: StateFlow<List<MatchEntity>> = repository.favoriteMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteTeams: StateFlow<List<TeamEntity>> = repository.favoriteTeams
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoriteLeagues: StateFlow<List<LeagueEntity>> = repository.favoriteLeagues
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val favoritePlayers: StateFlow<List<PlayerEntity>> = repository.favoritePlayers
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

    fun checkForUpdate() {
        viewModelScope.launch {
            _maintenance.value = _maintenance.value.copy(checkingUpdate = true, updateMessage = null)
            try {
                val release = repository.latestRelease()
                val newer = compareVersions(release.version, BuildConfig.VERSION_NAME) > 0
                _maintenance.value = _maintenance.value.copy(
                    checkingUpdate = false,
                    updateMessage = if (newer) "نسخه ${release.version} آماده دانلود است" else "آخرین نسخه نصب است",
                    updateUrl = if (newer) release.apkUrl else null,
                    updateVersion = if (newer) release.version else null,
                    updateProgress = null,
                    updateApkUri = null
                )
            } catch (_: Exception) {
                _maintenance.value = _maintenance.value.copy(checkingUpdate = false, updateMessage = "بررسی نسخه ناموفق بود")
            }
        }
    }

    fun downloadUpdate() {
        val url = _maintenance.value.updateUrl ?: return
        val version = _maintenance.value.updateVersion ?: return
        viewModelScope.launch {
            _maintenance.value = _maintenance.value.copy(updateDownloading = true, updateProgress = 0, updateMessage = "در حال دانلود نسخه $version")
            try {
                val uri = repository.downloadUpdateApk(version, url) { progress ->
                    _maintenance.value = _maintenance.value.copy(updateProgress = progress)
                }
                _maintenance.value = _maintenance.value.copy(updateDownloading = false, updateProgress = 100, updateApkUri = uri.toString(), updateMessage = "دانلود کامل شد؛ برای نصب بزنید")
            } catch (_: Exception) {
                _maintenance.value = _maintenance.value.copy(updateDownloading = false, updateProgress = null, updateMessage = "دانلود نسخه ناموفق بود")
            }
        }
    }

    fun downloadOfflineData() {
        viewModelScope.launch {
            _maintenance.value = _maintenance.value.copy(downloading = true, downloadMessage = "در حال دریافت داده و تصاویر…", downloadProgress = 0)
            try {
                val count = repository.downloadOfflineData { progress ->
                    _maintenance.value = _maintenance.value.copy(downloadProgress = progress)
                }
                _maintenance.value = _maintenance.value.copy(downloading = false, downloadProgress = 100, downloadMessage = "$count تصویر و تازه‌ترین داده‌ها ذخیره شد")
            } catch (_: Exception) {
                _maintenance.value = _maintenance.value.copy(downloading = false, downloadProgress = null, downloadMessage = "دانلود کامل نشد؛ اتصال اینترنت را بررسی کنید")
            }
        }
    }

    fun clearOfflineImages() {
        viewModelScope.launch {
            runCatching { repository.clearOfflineImages() }
                .onSuccess {
                    _maintenance.value = _maintenance.value.copy(downloadMessage = "حافظه تصاویر آفلاین پاک شد")
                }
                .onFailure {
                    _maintenance.value = _maintenance.value.copy(downloadMessage = "پاک‌کردن حافظه تصاویر ناموفق بود")
                }
        }
    }

    private fun compareVersions(left: String, right: String): Int {
        val a = left.split('.').map { it.toIntOrNull() ?: 0 }
        val b = right.split('.').map { it.toIntOrNull() ?: 0 }
        repeat(maxOf(a.size, b.size)) { index ->
            val diff = a.getOrElse(index) { 0 }.compareTo(b.getOrElse(index) { 0 })
            if (diff != 0) return diff
        }
        return 0
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

    fun togglePlayerFavorite(id: String, isFavorite: Boolean) {
        viewModelScope.launch { repository.setPlayerFavorite(id, !isFavorite) }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch { repository.markNotificationRead(id) }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch { repository.markAllNotificationsRead() }
    }

    fun getMatchFlow(id: String) = repository.getMatchByIdFlow(id)
    fun getTeamFlow(id: String) = repository.getTeamByIdFlow(id)
    fun getLeagueFlow(id: String) = repository.getLeagueByIdFlow(id)
    fun getPlayerFlow(id: String) = repository.getPlayerByIdFlow(id)

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

    fun loadPlayer(id: String) {
        viewModelScope.launch {
            try {
                repository.loadPlayer(id)
            } catch (e: Exception) {
                android.util.Log.e("SportsViewModel", "Player details failed", e)
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
