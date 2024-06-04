package kr.co.lion.modigm.ui.write.more

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kr.co.lion.modigm.databinding.FragmentBottomSheetWriteProceedBinding
import kr.co.lion.modigm.ui.write.vm.WriteViewModel

class BottomSheetWriteProceedFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomSheetWriteProceedBinding
    val viewModel: WriteViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = FragmentBottomSheetWriteProceedBinding.inflate(inflater)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        settingEvent()
    }

    fun settingEvent() {
        binding.apply {
            // 닫기 종료
            imageButtonWriteProceedBottomSheetClose.setOnClickListener {
                val location = textFieldWriteProceedBottomSheetSearch.text.toString()
                viewModel.settingLocation(location)
                Log.d("TedMoon", "text Bottom : ${viewModel.writeProceedLocation.value}")
                dismiss()
            }


            textFieldWriteProceedBottomSheetSearch.apply {

                // 키보드에서 return 클릭 시 키보드 없애기
                setOnEditorActionListener { v, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_DONE){
                        // 키보드 숨기기
                        v.clearFocus()
                        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.windowToken, 0 )
                        true
                    } else {
                        false
                    }
                }
            }

        }
    }

}