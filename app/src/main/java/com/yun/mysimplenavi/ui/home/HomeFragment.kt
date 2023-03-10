package com.yun.mysimplenavi.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.yun.mysimplenavi.BR
import com.yun.mysimplenavi.R
import com.yun.mysimplenavi.base.BaseFragment
import com.yun.mysimplenavi.databinding.FragmentHomeBinding
import com.yun.mysimplenavi.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import net.daum.mf.map.api.MapView

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {

    override val viewModel: HomeViewModel by viewModels()
    override fun setVariable(): Int = BR.home
    override fun getResourceId(): Int = R.layout.fragment_home
    override fun isOnBackEvent(): Boolean = false
    override fun onBackEvent() { }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        binding.run {

            btnKeyword.setOnClickListener {
                navigate(R.id.action_homeFragment_to_keywordSearchFragment)
            }

            btnMap.setOnClickListener {
                navigate(R.id.action_homeFragment_to_mapSearchFragment)
            }

        }

    }
}