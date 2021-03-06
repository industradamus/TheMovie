package com.wispapp.themovie.ui.main

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.wispapp.themovie.R
import com.wispapp.themovie.core.application.showKeyboard
import com.wispapp.themovie.core.model.database.models.MovieModel
import com.wispapp.themovie.ui.base.BaseFragment
import com.wispapp.themovie.ui.moviedetails.fragments.MOVIE_ID
import com.wispapp.themovie.ui.recycler.GenericAdapter
import com.wispapp.themovie.ui.viewmodel.MoviesViewModel
import com.wispapp.themovie.ui.viewmodel.SearchViewModel
import kotlinx.android.synthetic.main.bottom_sheet_main_fragment.*
import kotlinx.android.synthetic.main.content_main_fragment.*
import kotlinx.android.synthetic.main.toolbar_main.*
import kotlinx.android.synthetic.main.view_empty_search_result.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*


private const val DATE_FORMAT = "EEEE, MMMM d"

class MainFragment : BaseFragment(R.layout.fragment_main),
    GenericAdapter.OnItemClickListener<MovieModel> {

    private val moviesViewModel: MoviesViewModel by sharedViewModel()
    private val searchViewModel: SearchViewModel by viewModel()

    private val nowPlayingMoviesAdapter by lazy { getMovieAdapter() }
    private val popularMoviesAdapter by lazy { getMovieAdapter() }
    private val topRatedMoviesAdapter by lazy { getMovieAdapter() }
    private val upcomingMoviesAdapter by lazy { getMovieAdapter() }
    private val searchMovieAdapter by lazy { getSearchAdapter() }

    private var actionClose: MenuItem? = null
    private var actionSearch: MenuItem? = null
    private var isFirstLaunch = true

    private var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>? = null

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
        actionClose = menu.findItem(R.id.action_close)
        actionSearch = menu.findItem(R.id.action_search)

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_search -> showSearch()
            R.id.action_close -> closeSearch()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun initViewModel() {
        //todo Перенести позже в какой-нить сплеш экран
        moviesObserveLiveData()
    }

    override fun initView(view: View, savedInstanceState: Bundle?) {
        current_date_text.text = getCurrentDate()
        initRecycler()
        initToolbar(view)
        initBottomSheet()
    }

    override fun dataLoadingObserve() {
        moviesViewModel.isDataLoading.observe(this, Observer { isDataLoaded ->
            if (isDataLoaded)
                showLoading()
            else
                hideLoading()
        })
        searchViewModel.isDataLoading.observe(this, Observer { isDataLoaded ->
            if (isDataLoaded)
                showLoading()
            else
                hideLoading()
        })
    }

    override fun exceptionObserve() {
        moviesViewModel.exception.observe(this, Observer { error ->
            if (error != null && error.errorMessage.isNotEmpty())
                showError(error.errorMessage, error.func)
        })
        searchViewModel.exception.observe(this, Observer { error ->
            if (error != null && error.errorMessage.isNotEmpty())
                showError(error.errorMessage, error.func)
        })
    }

    override fun onClickItem(data: MovieModel) {
        val args = Bundle()
        args.putInt(MOVIE_ID, data.id)
        navigateTo(R.id.action_main_to_details, args)

        search_field.setText("")
    }

    override fun onBackPressed() {
        super.onBackPressed()
        requireActivity().finish()
    }

    private fun moviesObserveLiveData() {
        moviesViewModel.apply {
            nowPlayingMoviesLiveData.observe(this@MainFragment, Observer {
                nowPlayingMoviesAdapter.update(it)
                expandMovieList()
            })
            popularMoviesLiveData.observe(this@MainFragment, Observer {
                popularMoviesAdapter.update(it)
            })
            topRatedMovieLiveData.observe(this@MainFragment, Observer {
                topRatedMoviesAdapter.update(it)
            })
            upcomingMoviesLiveData.observe(this@MainFragment, Observer {
                upcomingMoviesAdapter.update(it)
            })
        }
        searchViewModel.searchMovieLiveData.observe(this, Observer {
            showSearchResult(it)
        })
    }

    private fun expandMovieList() {
        if (isFirstLaunch) {
            main_motion_layout.transitionToEnd()
            isFirstLaunch = false
        } else {
            main_motion_layout.setTransition(R.id.end, R.id.end)
        }
    }

    private fun showSearchResult(searchResult: MutableList<MovieModel>) {
        if (searchResult.isNotEmpty()) {
            empty_search_result_screen.visibility = View.GONE
            searchMovieAdapter.update(searchResult)
        } else
            empty_search_result_screen.visibility = View.VISIBLE
    }

    private fun initRecycler() {
        now_playing_recycler.adapter = nowPlayingMoviesAdapter
        popular_recycler.adapter = popularMoviesAdapter
        top_rated_recycler.adapter = topRatedMoviesAdapter
        upcoming_recycler.adapter = upcomingMoviesAdapter

        searchMovieAdapter.setDiffUtil(getMovieDiffUtilCallback())
        search_movie_recycler.adapter = searchMovieAdapter
    }

    private fun initToolbar(view: View) {
        val toolbar: Toolbar = view.findViewById(R.id.toolbar)
        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
    }

    private fun initBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottom_sheet)
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH)
        return dateFormat.format(Date())
    }

    private fun getMovieAdapter() = object : GenericAdapter<MovieModel>(this) {
        override fun getLayoutId(position: Int, obj: MovieModel): Int = R.layout.item_movie
    }

    private fun getSearchAdapter() = object : GenericAdapter<MovieModel>(this) {
        override fun getLayoutId(position: Int, obj: MovieModel): Int = R.layout.item_search_movie
    }

    private fun showSearch() {
        search_field.apply {
            visibility = View.VISIBLE
            requestFocus()
            showKeyboard(true)
            addTextChangedListener(getMovieSearchTextWatcher())
            setOnEditorActionListener(getOnEditorActionListener())
        }

        actionClose?.isVisible = true
        actionSearch?.isVisible = false
    }

    private fun closeSearch() {
        search_field.apply {
            showKeyboard(false)
            search_field.setText("")
            visibility = View.GONE
        }

        Handler().postDelayed({
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED
        }, 100)

        actionClose?.isVisible = false
        actionSearch?.isVisible = true
    }

    private fun getMovieSearchTextWatcher() = object : TextWatcher {
        override fun afterTextChanged(s: Editable) {
            if (s.length >= 4 && s.length % 2 == 0) {
                searchViewModel.searchMovie(s.toString())
                expandBottomSheet()
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    private fun getOnEditorActionListener() =
        TextView.OnEditorActionListener { view, actionID, _ ->
            if (actionID == EditorInfo.IME_ACTION_DONE) {
                searchViewModel.searchMovie(view.text.toString())
                search_field.showKeyboard(false)
                expandBottomSheet()
                true
            } else
                false
        }

    private fun expandBottomSheet() {
        if (bottomSheetBehavior?.state == BottomSheetBehavior.STATE_COLLAPSED)
            bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun getMovieDiffUtilCallback() = object : GenericAdapter.GenericItemDiff<MovieModel> {
        override fun isSame(
            oldItems: List<MovieModel>,
            newItems: List<MovieModel>,
            oldItemPosition: Int,
            newItemPosition: Int
        ): Boolean {
            val old = oldItems[oldItemPosition]
            val new = newItems[newItemPosition]
            return old.id == new.id
        }

        override fun isSameContent(
            oldItems: List<MovieModel>,
            newItems: List<MovieModel>,
            oldItemPosition: Int,
            newItemPosition: Int
        ): Boolean {
            val old = oldItems[oldItemPosition]
            val new = newItems[newItemPosition]
            return old.id == new.id &&
                    old.title == new.title &&
                    old.originalTitle == new.originalTitle &&
                    old.overview == new.overview &&
                    old.popularity == new.popularity &&
                    old.originalLanguage == new.originalLanguage &&
                    old.hasVideo == new.hasVideo &&
                    old.genreIds == new.genreIds &&
                    old.posterPath == new.posterPath &&
                    old.backdropPath == new.backdropPath &&
                    old.releaseDate == new.releaseDate &&
                    old.isAdult == new.isAdult &&
                    old.voteAverage == new.voteAverage &&
                    old.voteCount == new.voteCount &&
                    old.category == new.category
        }
    }
}
