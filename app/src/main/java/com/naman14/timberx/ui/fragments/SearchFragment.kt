/*
 * Copyright (c) 2019 Naman Dwivedi.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
package com.naman14.timberx.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.naman14.timberx.R
import com.naman14.timberx.databinding.FragmentSearchBinding
import com.naman14.timberx.ui.adapters.AlbumAdapter
import com.naman14.timberx.ui.adapters.ArtistAdapter
import com.naman14.timberx.ui.adapters.SongsAdapter
import com.naman14.timberx.ui.fragments.base.BaseNowPlayingFragment
import com.naman14.timberx.ui.viewmodels.SearchViewModel
import com.naman14.timberx.util.AutoClearedValue
import com.naman14.timberx.util.InjectorUtils
import com.naman14.timberx.extensions.addOnItemClick
import com.naman14.timberx.extensions.getExtraBundle
import com.naman14.timberx.extensions.inflateWithBinding
import com.naman14.timberx.extensions.safeActivity
import com.naman14.timberx.extensions.toSongIds
import kotlinx.android.synthetic.main.fragment_search.btnBack
import kotlinx.android.synthetic.main.fragment_search.etSearch
import kotlinx.android.synthetic.main.fragment_search.rvAlbums
import kotlinx.android.synthetic.main.fragment_search.rvArtist
import kotlinx.android.synthetic.main.fragment_search.rvSongs

class SearchFragment : BaseNowPlayingFragment() {
    private lateinit var searchViewModel: SearchViewModel
    private lateinit var songAdapter: SongsAdapter
    private lateinit var albumAdapter: AlbumAdapter
    private lateinit var artistAdapter: ArtistAdapter

    var binding by AutoClearedValue<FragmentSearchBinding>(this)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = inflater.inflateWithBinding(R.layout.fragment_search, container)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        searchViewModel = ViewModelProviders
                .of(safeActivity, InjectorUtils.provideSearchViewModel(safeActivity))
                .get(SearchViewModel::class.java)

        songAdapter = SongsAdapter().apply {
            popupMenuListener = mainViewModel.popupMenuListener
        }
        rvSongs.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = songAdapter
        }

        albumAdapter = AlbumAdapter()
        rvAlbums.apply {
            layoutManager = GridLayoutManager(safeActivity, 3)
            adapter = albumAdapter
            addOnItemClick { position: Int, _: View ->
                mainViewModel.mediaItemClicked(albumAdapter.albums[position], null)
            }
        }

        artistAdapter = ArtistAdapter()
        rvArtist.apply {
            layoutManager = GridLayoutManager(safeActivity, 3)
            adapter = artistAdapter
            addOnItemClick { position: Int, _: View ->
                mainViewModel.mediaItemClicked(artistAdapter.artists[position], null)
            }
        }

        rvSongs.addOnItemClick { position: Int, _: View ->
            songAdapter.getSongForPosition(position)?.let { song ->
                val extras = getExtraBundle(songAdapter.songs.toSongIds(), "All songs")
                mainViewModel.mediaItemClicked(song, extras)
            }
        }
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                searchViewModel.search(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                songAdapter.updateData(emptyList())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        })
        btnBack.setOnClickListener { safeActivity.onBackPressed() }

        searchViewModel.searchLiveData.observe(this, Observer {
            songAdapter.updateData(it.songs)
            albumAdapter.updateData(it.albums)
            artistAdapter.updateData(it.artists)
        })

        binding.let {
            it.viewModel = searchViewModel
            it.setLifecycleOwner(this)
        }
    }
}
