package kr.co.lion.modigm.ui.detail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentDetailJoinMemberBinding
import kr.co.lion.modigm.ui.chat.vm.ChatRoomViewModel
import kr.co.lion.modigm.ui.detail.adapter.DetailJoinMembersAdapter
import kr.co.lion.modigm.ui.detail.vm.DetailViewModel
import kr.co.lion.modigm.ui.profile.ProfileFragment
import kr.co.lion.modigm.util.FragmentName

class DetailJoinMemberFragment : Fragment() {

    lateinit var binding: FragmentDetailJoinMemberBinding
    private val viewModel: DetailViewModel by activityViewModels()
    private val chatRoomViewModel: ChatRoomViewModel by activityViewModels()
    private lateinit var adapter: DetailJoinMembersAdapter

    // 현재 선택된 스터디 idx 번호를 담을 변수(임시)
    var studyIdx = 0

    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailJoinMemberBinding.inflate(layoutInflater)

        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid ?: ""

        // 상품 idx
        studyIdx = arguments?.getInt("studyIdx")!!

        adapter = DetailJoinMembersAdapter(viewModel, chatRoomViewModel, currentUserId, studyIdx) { user ->
            val profileFragment = ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString("uid", user.userUid)
                }
            }

            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.containerMain, profileFragment)
                .addToBackStack(FragmentName.PROFILE.str)
                .commit()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        viewModel.studyUids.observe(viewLifecycleOwner) { uids ->
            viewModel.loadUserDetails(uids)
        }

        viewModel.userDetails.observe(viewLifecycleOwner) { userDetails ->
            userDetails?.let {
                adapter.submitList(it)
            }
        }

        viewModel.loadStudyUids(studyIdx)

    }

    fun setupRecyclerView() {
        binding.recyclerviewDetailJoin.layoutManager = LinearLayoutManager(context)
        binding.recyclerviewDetailJoin.adapter = adapter
    }

}