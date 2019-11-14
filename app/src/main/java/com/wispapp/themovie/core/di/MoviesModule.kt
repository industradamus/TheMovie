@file:Suppress("RemoveExplicitTypeArguments")

package com.wispapp.themovie.core.di

import com.wispapp.themovie.core.model.database.models.MovieDetailsModel
import com.wispapp.themovie.core.model.database.models.MovieOverviewModel
import com.wispapp.themovie.core.model.datasource.RemoteDataSourceImpl
import com.wispapp.themovie.core.model.network.ApiInterface
import com.wispapp.themovie.core.model.network.MoviesDetailsProvider
import com.wispapp.themovie.core.model.network.PopularMoviesRemoteProvider
import com.wispapp.themovie.core.model.network.mappers.*
import com.wispapp.themovie.core.viewmodel.MoviesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val MAPPER_MOVIES_OVERVIEW = "mapper_movies_overview"
private const val MAPPER_MOVIES_DETAILS = "mapper_movies_details"
private const val MAPPER_MOVIES_GENRES = "mapper_movies_genres"
private const val MAPPER_MOVIES_COMPANIES = "mapper_movies_companies"
private const val MAPPER_MOVIES_COUNTRIES = "mapper_movies_countries"
private const val MAPPER_MOVIES_LANGUAGE = "mapper_movies_language"

private const val NETWORK_PROVIDER_POPULAR_MOVIES = "network_provider_popular_movies"
private const val NETWORK_PROVIDER_MOVIE_DETAILS = "network_provider_movie_details"

private const val DATA_SOURCE_MOVIES_OVERVIEW = "data_source_movies_overview"
private const val DATA_SOURCE_MOVIE_DETAILS = "data_source_movie_details"

val moviesModule = module {

    single(named(MAPPER_MOVIES_OVERVIEW)) { MoviesOverviewMapper() }
    single(named(MAPPER_MOVIES_LANGUAGE)) { SpokenLanguagesMapper() }
    single(named(MAPPER_MOVIES_COUNTRIES)) { ProductionCountriesMapper() }
    single(named(MAPPER_MOVIES_COMPANIES)) { ProductionCompaniesMapper() }
    single(named(MAPPER_MOVIES_GENRES)) { GenresMapper() }
    single(named(MAPPER_MOVIES_DETAILS)) {
        MoviesDetailsMapper(
            get(named(MAPPER_MOVIES_GENRES)),
            get(named(MAPPER_MOVIES_COMPANIES)),
            get(named(MAPPER_MOVIES_COUNTRIES)),
            get(named(MAPPER_MOVIES_LANGUAGE))
        )
    }

    factory(named(NETWORK_PROVIDER_POPULAR_MOVIES)) {
        PopularMoviesRemoteProvider(
            get(named(MAPPER_MOVIES_OVERVIEW)),
            get<ApiInterface>()
        )
    }
    factory(named(NETWORK_PROVIDER_MOVIE_DETAILS)) {
        MoviesDetailsProvider(
            get(named(MAPPER_MOVIES_DETAILS)),
            get<ApiInterface>()
        )
    }

    factory(named(DATA_SOURCE_MOVIES_OVERVIEW)) {
        RemoteDataSourceImpl<MovieOverviewModel>(get(named(NETWORK_PROVIDER_POPULAR_MOVIES)))
    }

    factory(named(DATA_SOURCE_MOVIE_DETAILS)) {
        RemoteDataSourceImpl<MovieDetailsModel>(get(named(NETWORK_PROVIDER_MOVIE_DETAILS)))
    }

    viewModel {
        MoviesViewModel(
            get(named(DATA_SOURCE_MOVIES_OVERVIEW)),
            get(named(DATA_SOURCE_MOVIE_DETAILS))
        )
    }
}