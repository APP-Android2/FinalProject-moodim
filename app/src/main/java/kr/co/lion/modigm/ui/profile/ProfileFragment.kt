package kr.co.lion.modigm.ui.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentLoginBinding
import kr.co.lion.modigm.databinding.FragmentProfileBinding
import kr.co.lion.modigm.ui.MainActivity
import kr.co.lion.modigm.ui.profile.adapter.HostStudyAdapter
import kr.co.lion.modigm.ui.profile.adapter.LinkAdapter
import kr.co.lion.modigm.ui.profile.adapter.PartStudyAdapter
import kr.co.lion.modigm.ui.profile.vm.ProfileViewModel
import kr.co.lion.modigm.util.FragmentName
import java.net.URL

class ProfileFragment: Fragment() {
    lateinit var fragmentProfileBinding: FragmentProfileBinding
    lateinit var mainActivity: MainActivity
    private val addressViewModel: ProfileViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentProfileBinding = FragmentProfileBinding.inflate(inflater,container,false)
        // AddressModifyViewModel = AddressModifyViewModel()
        // fragmentAddressModifyBinding.lifecycleOwner = this
        mainActivity = activity as MainActivity

        return fragmentProfileBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerViewLink()
        setupRecyclerViewPartStudy()
        setupRecyclerViewHostStudy()
        //setupAddressList()
        //setupButtonAdd()
    }



    private fun setupToolbar() {
        fragmentProfileBinding.apply {
            toolbarProfile.apply {
                // title
                title = "프로필"

                // 툴바 메뉴
                inflateMenu(R.menu.menu_profile)
                setOnMenuItemClickListener {
                    when (it.itemId) {
                        R.id.menu_item_profile_setting -> {
                            mainActivity.replaceFragment(FragmentName.SETTINGS, true, true, null)
                        }

                        R.id.menu_item_profile_more -> {
                            // mainActivity.replaceFragment(FragmentName.CART_FRAGMENT, true, true, null)
                        }
                    }
                    true
                }

                // 모든 메뉴를 보이지 않는 상태로 둔다.
                // 사용자 정보를 가져온 다음 메뉴를 노출 시킨다.
                menu.findItem(R.id.menu_item_profile_setting).isVisible = false
                menu.findItem(R.id.menu_item_profile_more).isVisible = false

                // 본인의 프로필일 때: 설정 아이콘
                if (true) {
                    // 설정 아이콘 표시
                    menu.findItem(R.id.menu_item_profile_setting).isVisible = true
                } else {
                    // 타인의 프로필일 때: 뒤로 가기, 더보기 아이콘
                    // 뒤로 가기
                    setNavigationIcon(R.drawable.icon_arrow_back_24px)
                    setNavigationOnClickListener {
                        mainActivity.replaceFragment(FragmentName.PROFILE, false,true,null)
                    }

                    // 더보기 아이콘 표시
                    menu.findItem(R.id.menu_item_profile_more).isVisible = true
                }
            }
        }
    }

    private fun setupRecyclerViewLink() {
        // 어댑터 선언
        val linkAdapter: LinkAdapter = LinkAdapter(
            // 빈 리스트를 넣어 초기화
            emptyList(),

            // 항목을 클릭: Url을 받아온다
            rowClickListener = { linkUrl ->
                Log.d("테스트 rowClickListener deliveryIdx", linkUrl)
                viewLifecycleOwner.lifecycleScope.launch {
                    Log.d("테스트 rowClickListener deliveryIdx", extractDomain(linkUrl))

                }
            }
        )

        // 리사이클러뷰 구성
        fragmentProfileBinding.apply {
            recyclerVIewProfileLink.apply {
                // 리사이클러뷰 어댑터
                adapter = linkAdapter

                // 리사이클러뷰 레이아웃
                layoutManager = LinearLayoutManager(mainActivity, RecyclerView.HORIZONTAL, false)

            }
        }
    }

    private fun setupRecyclerViewPartStudy() {
        // 어댑터 선언
        val partStudyAdapter: PartStudyAdapter = PartStudyAdapter(
            // 빈 리스트를 넣어 초기화
            emptyList(),

            // 항목을 클릭: 스터디 고유번호를 이용하여 해당 스터디 화면으로 이동한다
            rowClickListener = { linkUrl ->
                Log.d("테스트 rowClickListener deliveryIdx", linkUrl)
                viewLifecycleOwner.lifecycleScope.launch {
                    Log.d("테스트 rowClickListener deliveryIdx", extractDomain(linkUrl))
                    mainActivity.replaceFragment(FragmentName.STUDY, true, true, null)
                }
            }
        )

        // 리사이클러뷰 구성
        fragmentProfileBinding.apply {
            recyclerViewProfilePartStudy.apply {
                // 리사이클러뷰 어댑터
                adapter = partStudyAdapter

                // 리사이클러뷰 레이아웃
                layoutManager = LinearLayoutManager(mainActivity, RecyclerView.HORIZONTAL, false)
            }
        }
    }

    private fun setupRecyclerViewHostStudy() {
        // 어댑터 선언
        val hostStudyAdapter: HostStudyAdapter = HostStudyAdapter(
            // 빈 리스트를 넣어 초기화
            emptyList(),

            // 항목을 클릭: 스터디 고유번호를 이용하여 해당 스터디 화면으로 이동한다
            rowClickListener = { linkUrl ->
                Log.d("테스트 rowClickListener deliveryIdx", linkUrl)
                viewLifecycleOwner.lifecycleScope.launch {
                    Log.d("테스트 rowClickListener deliveryIdx", extractDomain(linkUrl))
                    mainActivity.replaceFragment(FragmentName.STUDY, true, true, null)
                }
            }
        )

        // 리사이클러뷰 구성
        fragmentProfileBinding.apply {
            recyclerViewProfileHostStudy.apply {
                // 리사이클러뷰 어댑터
                adapter = hostStudyAdapter

                // 리사이클러뷰 레이아웃
                layoutManager = LinearLayoutManager(mainActivity)
            }
        }
    }

    // URL에서 도메인을 추출하는 함수
    private fun extractDomain(url: String): String {
        return try {
            val uri = URL(url)
            val domain = uri.host
            // www. 접두사를 제거
            if (domain.startsWith("www.")) domain.substring(4) else domain
        } catch (e: Exception) {
            "invalid"
        }
    }
}