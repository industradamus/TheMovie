package com.wispapp.themovie.core.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.wispapp.themovie.core.common.ImageLoader
import com.wispapp.themovie.core.model.database.models.CATEGORY
import com.wispapp.themovie.core.model.database.models.CATEGORY.*
import com.wispapp.themovie.core.model.database.models.ConfigModel
import com.wispapp.themovie.core.model.database.models.MovieDetailsModel
import com.wispapp.themovie.core.model.database.models.MovieModel
import com.wispapp.themovie.core.model.datasource.DataSource
import com.wispapp.themovie.core.model.datasource.Result
import com.wispapp.themovie.core.model.network.MovieIdArgs
import com.wispapp.themovie.core.model.network.SearchQueryArgs
import com.wispapp.themovie.core.model.network.models.NetworkException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "MoviesViewModel"

class MoviesViewModel(
    private val movieDataSource: DataSource<List<MovieModel>>,
    private val movieDetailsDataSource: DataSource<MovieDetailsModel>,
    private val configDataSource: DataSource<ConfigModel>,
    private val searchMovieDataSource: DataSource<List<MovieModel>>,
    private val imageLoader: ImageLoader
) : BaseViewModel() {

    val nowPlayingMoviesLiveData = MutableLiveData<MutableList<MovieModel>>()
    val popularMoviesLiveData = MutableLiveData<MutableList<MovieModel>>()
    val topRatedMovieLiveData = MutableLiveData<MutableList<MovieModel>>()
    val upcomingMoviesLiveData = MutableLiveData<MutableList<MovieModel>>()
    val movieDetailsLiveData = MutableLiveData<MutableList<MovieDetailsModel>>()
    val searchMovieLiveData = MutableLiveData<MutableList<MovieModel>>()

    fun getMovies() {
        showLoader()
        backgroundScope.launch {
            withContext(Dispatchers.IO) { getConfigs() }
            withContext(Dispatchers.IO) { getAllMovies() }

            hideLoader()
        }
    }

    fun getMovieDetails(id: Int) {
        showLoader()
        backgroundScope.launch {
            when (val result = movieDetailsDataSource.get(MovieIdArgs(id))) {
                is Result.Success -> movieDetailsLiveData.postValue(mutableListOf(result.data))
                is Result.Error -> handleError(result.exception) { getMovieDetails(id) }
            }
            hideLoader()
        }
    }

    fun searchMovie(query: String) {
        backgroundScope.launch {
            when (val result = searchMovieDataSource.get((SearchQueryArgs(query)))) {
                is Result.Success -> searchMovieLiveData.postValue(result.data.toMutableList())
                is Result.Error -> handleError(result.exception) { searchMovie(query) }
            }
        }
    }

    private suspend fun getConfigs() {
        when (val result = configDataSource.get()) {
            is Result.Success -> imageLoader.setConfigs(result.data.imagesConfig)
            is Result.Error -> handleError(result.exception) { getMovies() }
        }
    }

    private suspend fun getAllMovies() {
        when (val result = movieDataSource.get()) {
            is Result.Success -> updateUi(result)
            is Result.Error -> handleError(result.exception) { getMovies() }
        }
    }

    private fun updateUi(result: Result.Success<List<MovieModel>>) {
        val nowPlaying = filterByCategory(result, NOW_PLAYING)
        val popular = filterByCategory(result, POPULAR)
        val topRated = filterByCategory(result, TOP_RATED)
        val upcoming = filterByCategory(result, UPCOMING)

        nowPlayingMoviesLiveData.postValue(nowPlaying)
        popularMoviesLiveData.postValue(popular)
        topRatedMovieLiveData.postValue(topRated)
        upcomingMoviesLiveData.postValue(upcoming)
    }

    private fun filterByCategory(result: Result.Success<List<MovieModel>>, category: CATEGORY) =
        result.data.filter { it.category.contains(category) }.toMutableList()

    private fun handleError(error: Exception, func: () -> Unit) {
        if (error is NetworkException) {
            showError(error.statusMessage, func)
            Log.d(TAG, error.statusMessage)
        } else
            showError(error.message ?: "Something was wrong", func)
    }
}