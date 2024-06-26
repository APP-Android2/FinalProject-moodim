package kr.co.lion.modigm.ui.join

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentJoinStep1Binding
import kr.co.lion.modigm.ui.join.vm.JoinStep1ViewModel

class JoinStep1Fragment : Fragment() {

    lateinit var binding: FragmentJoinStep1Binding

    private val joinStep1ViewModel: JoinStep1ViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_join_step1, container, false)
        binding.viewModel = joinStep1ViewModel
        binding.lifecycleOwner = this

        settingTextInputLayoutError()

        return binding.root
    }

    // 에러 메시지 설정
    private fun settingTextInputLayoutError(){
        joinStep1ViewModel.emailValidation.observe(viewLifecycleOwner){
            binding.textInputLayoutJoinUserEmail.error = it
        }
        joinStep1ViewModel.pwValidation.observe(viewLifecycleOwner){
            binding.textInputLayoutJoinUserPassword.error = it
        }
        joinStep1ViewModel.pwCheckValidation.observe(viewLifecycleOwner){
            binding.textInputLayoutJoinUserPasswordCheck.error = it
        }
    }

}