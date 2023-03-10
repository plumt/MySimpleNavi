package com.yun.mysimplenavi.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import com.yun.mysimplenavi.BR
import com.yun.mysimplenavi.R
import com.yun.mysimplenavi.ui.main.MainViewModel

abstract class BaseFragment<B : ViewDataBinding, M : ViewModel> : Fragment() {

    lateinit var binding: B

    abstract val viewModel: M

    abstract fun setVariable(): Int

    @LayoutRes
    abstract fun getResourceId(): Int

    abstract fun onBackEvent()

    abstract fun isOnBackEvent(): Boolean

    val sharedViewModel: MainViewModel by activityViewModels()

    fun navigate(resId: Int, bundle: Bundle? = null, options: NavOptions? = null) {
        try {
//            bundle?.apply {
//                view?.findNavController()?.navigate(resId, this,options)
//            } ?: view?.findNavController()?.navigate(resId,bundle,options)
            view?.findNavController()?.navigate(resId, bundle, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, getResourceId(), container, false)
        binding.lifecycleOwner = activity
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.setVariable(setVariable(), viewModel)
        binding.setVariable(BR.main, sharedViewModel)

        if (isOnBackEvent()) {
            requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
                onBackEvent()
            }
        }


    }
}