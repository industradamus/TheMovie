package com.wispapp.themovie.ui.moviedetails.fragments

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import com.wispapp.themovie.R
import com.wispapp.themovie.core.model.database.models.TrailerModel
import com.wispapp.themovie.ui.base.BaseFragment
import com.wispapp.themovie.ui.moviedetails.interfaces.PlaybackYouTubeListener
import com.wispapp.themovie.ui.viewmodel.MoviesViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class TrailerFragment(private val playbackListener: PlaybackYouTubeListener) :
    BaseFragment(R.layout.fragment_trailer) {

    private val moviesViewModel: MoviesViewModel by sharedViewModel()
    private val youtube : YouTubePlayerSupportFragment by lazy{ childFragmentManager
        .findFragmentById(R.id.youtube_player) as YouTubePlayerSupportFragment}

    override fun onResume() {
        super.onResume()
        getTrailerData()
        youtube.onResume()
    }

    override fun onPause() {
        super.onPause()
        youtube.onPause()
    }

    override fun initViewModel() {
    }

    override fun initView(view: View) {
    }

    override fun dataLoadingObserve() {
    }

    override fun exceptionObserve() {
        moviesViewModel.exception.observe(this, Observer { error ->
            if (error != null && error.errorMessage.isNotEmpty())
                showError(error.errorMessage, error.func)
        })
    }

    private fun getTrailerData() {
        val trailer = moviesViewModel.trailersLiveData.value?.find { it.id == getMovieId() }
        trailer?.let {
            startTrailer(it)
        } ?: run { showError(getString(R.string.video_upload_error), null) }
    }

    private fun startTrailer(trailer: TrailerModel) {
        @Suppress("CAST_NEVER_SUCCEEDS")
        youtube.initialize(KEY, object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(
                provider: YouTubePlayer.Provider?,
                player: YouTubePlayer?,
                wasRestored: Boolean
            ) {
                player?.loadVideo(trailer.key)
                player?.setPlaybackEventListener(object : YouTubePlayer.PlaybackEventListener {
                    override fun onSeekTo(p0: Int) {}

                    override fun onBuffering(p0: Boolean) {
                        playbackListener.onStopPlayback()
                    }

                    override fun onPlaying() {
                        playbackListener.onStartPlay()
                    }

                    override fun onStopped() {
                        playbackListener.onStopPlayback()
                    }

                    override fun onPaused() {
                        playbackListener.onStopPlayback()
                    }
                })
            }

            override fun onInitializationFailure(
                provider: YouTubePlayer.Provider?,
                result: YouTubeInitializationResult?
            ) {
                showError(getString(R.string.video_upload_error), null)
            }
        })
    }

    private fun getMovieId() =
        arguments?.getString(MOVIE_ID) ?: run {
            showError(getString(R.string.video_upload_error), null)
            ""
        }

    companion object {

        private const val KEY = "AIzaSyACvygu6rqrc3l2KT7eu8Zygqfr_pT_Qo8"
        private const val MOVIE_ID = "movie_id"

        fun newInstance(
            listener: PlaybackYouTubeListener,
            trailerId: String
        ): TrailerFragment {
            val fragment = TrailerFragment(listener)
            val bundle = Bundle()
            bundle.putString(MOVIE_ID, trailerId)
            fragment.arguments = bundle
            return fragment
        }
    }
}
