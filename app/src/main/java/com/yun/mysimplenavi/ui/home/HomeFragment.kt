package com.yun.mysimplenavi.ui.home

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.yun.mysimplenavi.BR
import com.yun.mysimplenavi.R
import com.yun.mysimplenavi.base.BaseFragment
import com.yun.mysimplenavi.databinding.FragmentHomeBinding
import com.yun.mysimplenavi.ui.main.MainActivity
import com.yun.mysimplenavi.ui.main.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import net.daum.mf.map.api.MapView

@AndroidEntryPoint
class HomeFragment : BaseFragment<FragmentHomeBinding, HomeViewModel>() {

    override val viewModel: HomeViewModel by viewModels()
    override fun setVariable(): Int = BR.home
    override fun getResourceId(): Int = R.layout.fragment_home
    override fun isOnBackEvent(): Boolean = true
    override fun onBackEvent() {
        if (isAppExit) {
            (requireActivity() as MainActivity).finish()
        } else {
            Toast.makeText(requireActivity(), "뒤로 버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT).show()
            isAppExit = true
            Handler().postDelayed({
                isAppExit = false
            }, 2000)
        }
    }

    var isAppExit = false

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