package com.yun.mysimplenavi.ui.search.keyword

import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.yun.mysimplenavi.R
import com.yun.mysimplenavi.BR
import com.yun.mysimplenavi.base.BaseFragment
import com.yun.mysimplenavi.base.BaseRecyclerAdapter
import com.yun.mysimplenavi.data.model.KeywordSearchModel
import com.yun.mysimplenavi.databinding.FragmentKeywordSearchBinding
import com.yun.mysimplenavi.databinding.ItemKeywordSearchBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class KeywordSearchFragment : BaseFragment<FragmentKeywordSearchBinding, KeywordSearchViewModel>() {
    override val viewModel: KeywordSearchViewModel by viewModels()
    override fun setVariable(): Int = BR.keyword
    override fun getResourceId(): Int = R.layout.fragment_keyword_search
    override fun isOnBackEvent(): Boolean = true
    override fun onBackEvent() {
        if (viewModel.isFocus.value!!) {
            clearFocus()
        } else {
            findNavController().popBackStack()
        }
    }

    /**
     * 현재 위치
     */
    private var locationManager: LocationManager? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        viewModel.isLoading.observe(viewLifecycleOwner) {
            sharedViewModel.isLoading.value = it
        }

        viewModel.isFocus.observe(viewLifecycleOwner) {
            if (!it) {
                val imm =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }

        binding.run {

            layoutParent.setOnClickListener {
                clearFocus()
            }

            btnSearch.setOnClickListener {
                if (viewModel.locationKeyword.value!!.isNotEmpty()) {
                    viewModel.page = 1
                    viewModel.keywordSearchResults.value = arrayListOf()
                    userLocation()
                    clearFocus()
                }
            }

            etKeyword.setOnFocusChangeListener { p0, p1 ->
                viewModel.isFocus.value = p1
            }

            rvKeywordSearch.apply {
                adapter = object :
                    BaseRecyclerAdapter.Create<KeywordSearchModel.RS.Documents, ItemKeywordSearchBinding>(
                        R.layout.item_keyword_search,
                        BR.itemKeywordSearch,
                        BR.keywordSearchListener
                    ) {
                    override fun onItemClick(item: KeywordSearchModel.RS.Documents, view: View) {
                        navigate(
                            R.id.action_keywordSearchFragment_to_preViewMapFragment,
                            Bundle().apply {
                                putString("lon", item.lon)
                                putString("lat", item.lat)
                                putString("name", item.place_name)
                            },
                            NavOptions.Builder().setPopUpTo(R.id.keywordSearchFragment, true)
                                .build()
                        )
                    }

                    override fun onItemLongClick(
                        item: KeywordSearchModel.RS.Documents,
                        view: View
                    ): Boolean = true
                }
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (viewModel.isFocus.value!!) {
                            clearFocus()
                        }
                        if (!recyclerView.canScrollVertically(1) && !viewModel.isLoading.value!! && viewModel.page > 0 && viewModel.keywordSearchResults.value!!.size > 0) {
                            userLocation()
                        }
                    }
                })
            }
        }
    }

    /**
     * 포커스 초기화
     */
    private fun clearFocus(){
        viewModel.isFocus.value = false
        binding.etKeyword.clearFocus()
    }

    @SuppressLint("MissingPermission")
    private fun userLocation() {
        locationManager!!.getLastKnownLocation(
            locationManager!!.getBestProvider(
                Criteria(),
                false
            )!!
        )?.run {
            viewModel.callApi(latitude, longitude)
        }
    }

}