package com.wispapp.themovie.ui.main.viewholders

import android.view.View
import com.wispapp.themovie.core.common.ApiConfigLinkProvider
import com.wispapp.themovie.core.common.ConfigsHolder
import com.wispapp.themovie.core.common.ImageLoader
import com.wispapp.themovie.core.model.database.models.MovieModel
import com.wispapp.themovie.ui.recycler.BaseViewHolder
import com.wispapp.themovie.ui.recycler.GenericAdapter
import kotlinx.android.synthetic.main.item_movie.view.poster_image
import kotlinx.android.synthetic.main.item_movie.view.title_text
import kotlinx.android.synthetic.main.item_search_movie.view.*
import org.koin.core.KoinComponent
import org.koin.core.inject

class SearchMovieVH(private val rootView: View) : BaseViewHolder<MovieModel>(rootView),
    GenericAdapter.Binder<MovieModel>, KoinComponent {

    private val imageLoader: ImageLoader by inject()
    private val configs = ConfigsHolder.getConfig()

    override fun bind(data: MovieModel, listener: GenericAdapter.OnItemClickListener<MovieModel>?) {
        setPoster(data)
        rootView.apply {
            title_text.text = data.title
            overview_text.text = data.overview
            date_text.text = data.releaseDate
        }
        rootView.setOnClickListener { listener?.onClickItem(data) }
    }

    private fun setPoster(data: MovieModel) {
        val linkProvider = ApiConfigLinkProvider(data.posterPath, configs)
        imageLoader.loadPoster(linkProvider, rootView.poster_image)
    }
}