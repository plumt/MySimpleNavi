package com.yun.mysimplenavi.ui.search.keyword

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.NavOptions
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.run {

            btnSearch.setOnClickListener {
                viewModel.callApi()
            }

            rvKeywordSearch.apply {
                adapter = object : BaseRecyclerAdapter.Create<KeywordSearchModel.RS.Documents, ItemKeywordSearchBinding>(
                    R.layout.item_keyword_search,
                    BR.itemKeywordSearch,
                    BR.keywordSearchListener
                ){
                    override fun onItemClick(item: KeywordSearchModel.RS.Documents, view: View) {
                        navigate(R.id.action_keywordSearchFragment_to_preViewMapFragment, Bundle().apply {
                            putString("lon",item.lon)
                            putString("lat",item.lat)
                            putString("name",item.place_name)
                        }, NavOptions.Builder().setPopUpTo(R.id.keywordSearchFragment, true).build())
                    }

                    override fun onItemLongClick(
                        item: KeywordSearchModel.RS.Documents,
                        view: View
                    ): Boolean = true
                }
            }
        }


    }

}