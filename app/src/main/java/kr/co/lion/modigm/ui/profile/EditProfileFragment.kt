package kr.co.lion.modigm.ui.profile

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kr.co.lion.modigm.R
import kr.co.lion.modigm.databinding.FragmentEditProfileBinding
import kr.co.lion.modigm.ui.profile.adapter.LinkAddAdapter
import kr.co.lion.modigm.ui.profile.vm.EditProfileViewModel
import kr.co.lion.modigm.ui.profile.vm.ProfileViewModel
import kr.co.lion.modigm.util.FragmentName
import kr.co.lion.modigm.util.Interest
import kr.co.lion.modigm.util.JoinType
import kr.co.lion.modigm.util.ModigmApplication
import kr.co.lion.modigm.util.Picture

class EditProfileFragment(private val profileFragment: ProfileFragment) : Fragment() {
    lateinit var fragmentEditProfileBinding: FragmentEditProfileBinding
    private val editProfileViewModel: EditProfileViewModel by activityViewModels()

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var user: FirebaseUser

    // 어댑터 선언
    private lateinit var linkAddAdapter: LinkAddAdapter
    // 앨범 실행을 위한 런처
    lateinit var albumLauncher: ActivityResultLauncher<Intent>
    // 촬영된 사진이 저장된 경로 정보를 가지고 있는 Uri 객체
    var newImageUri: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentEditProfileBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false)

        // Bind ViewModel and lifecycle owner
        fragmentEditProfileBinding.editProfileViewModel = editProfileViewModel
        fragmentEditProfileBinding.lifecycleOwner = this

        firebaseAuth = Firebase.auth
        user = firebaseAuth.currentUser!!

        return fragmentEditProfileBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setupAlbumLauncher()
    }

    private fun initView() {
        setupToolbar()
        setupUserInfo()
        setupButtonChangePic()
        setupButtonChangePhone()
        setupRecyclerViewLink()
        setupButtonLinkAdd()
        setupButtonDone()

        observeData()
    }

    private fun setupToolbar() {
        fragmentEditProfileBinding.toolbarEditProfile.apply {
            setNavigationOnClickListener {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
    }

    private fun setupUserInfo() {
        // 데이터베이스로부터 데이터를 불러와 뷰모델에 담기
        editProfileViewModel.loadUserData(user, requireContext(), fragmentEditProfileBinding.imageProfilePic)
    }

    private fun setupButtonChangePic() {
        fragmentEditProfileBinding.imageEditProfileChangePic.setOnClickListener {
            startAlbumLauncher()
        }
    }

    private fun setupButtonChangePhone() {
        fragmentEditProfileBinding.buttonEditProfilePhone.setOnClickListener {
            // Fragment 교체
            requireActivity().supportFragmentManager.commit {
                add(R.id.containerMain, ChangePhoneFragment())
                addToBackStack(FragmentName.CHANGE_PHONE.str)
            }
        }
    }

    private fun setupRecyclerViewLink() {
        // 리사이클러뷰 어댑터와 뷰모델을 함께 초기화
        linkAddAdapter = LinkAddAdapter(emptyList(), requireContext(), editProfileViewModel)
        fragmentEditProfileBinding.apply {
            recyclerViewEditProfileLink.apply {
                adapter = linkAddAdapter
                layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }

    private fun setupButtonLinkAdd() {
        fragmentEditProfileBinding.buttonEditProfileLink.setOnClickListener {
            // 리스트에 링크 추가
            editProfileViewModel.addLinkToList()
            // 링크 텍스트필드 비우기
            editProfileViewModel.editProfileNewLink.value = ""
        }
    }

    private fun setupButtonDone() {
        fragmentEditProfileBinding.buttonEditProfileDone.setOnClickListener {
            // 데이터베이스 업데이트
            editProfileViewModel.updateUserData(profileFragment, newImageUri)

            // 스낵바 띄우기
            val snackbar = Snackbar.make(fragmentEditProfileBinding.root, "정보가 업데이트되었습니다.", Snackbar.LENGTH_LONG)
            snackbar.show()

            // 이전 프래그먼트로 돌아간다
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    // 앨범 런처 설정
    fun setupAlbumLauncher() {
        // 앨범 실행을 위한 런처
        val albumContract = ActivityResultContracts.StartActivityForResult()
        albumLauncher = registerForActivityResult(albumContract){
            // 사진 선택을 완료한 후 돌아왔다면
            if(it.resultCode == AppCompatActivity.RESULT_OK){
                // 선택한 이미지의 경로 데이터를 관리하는 Uri 객체를 추출한다.
                newImageUri = it.data?.data
                if(newImageUri != null){
                    // 안드로이드 Q(10) 이상이라면
                    val bitmap = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                        // 이미지를 생성할 수 있는 객체를 생성한다.
                        val source = ImageDecoder.createSource(requireContext().contentResolver, newImageUri!!)
                        // Bitmap을 생성한다.
                        ImageDecoder.decodeBitmap(source)
                    } else {
                        // 컨텐츠 프로바이더를 통해 이미지 데이터에 접근한다.
                        val cursor = requireContext().contentResolver.query(newImageUri!!, null, null, null, null)
                        if(cursor != null){
                            cursor.moveToNext()

                            // 이미지의 경로를 가져온다.
                            val idx = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                            val source = cursor.getString(idx)

                            // 이미지를 생성한다
                            BitmapFactory.decodeFile(source)
                        }  else {
                            null
                        }
                    }

                    // 회전 각도값을 가져온다.
                    val degree = Picture.getDegree(requireContext(), newImageUri!!)
                    // 회전 이미지를 가져온다
                    val bitmap2 = Picture.rotateBitmap(bitmap!!, degree.toFloat())
                    // 크기를 줄인 이미지를 가져온다.
                    val bitmap3 = Picture.resizeBitmap(bitmap2, 1024)

                    fragmentEditProfileBinding.imageProfilePic.setImageBitmap(bitmap3)
                    editProfileViewModel.editProfilePicSrc.value = "${System.currentTimeMillis()}.jpg"
                }
            }
        }
    }

    // 앨범 런처를 실행하는 메서드
    fun startAlbumLauncher(){
        // 앨범에서 사진을 선택할 수 있도록 셋팅된 인텐트를 생성한다.
        val albumIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        // 실행할 액티비티의 타입을 설정(이미지를 선택할 수 있는 것이 뜨게 한다)
        albumIntent.setType("image/*")
        // 선택할 수 있는 파들의 MimeType을 설정한다.
        // 여기서 선택한 종류의 파일만 선택이 가능하다. 모든 이미지로 설정한다.
        val mimeType = arrayOf("image/*")
        albumIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
        // 액티비티를 실행한다.
        albumLauncher.launch(albumIntent)
    }

    fun observeData() {
        // 데이터 변경 관찰
        // 로그인 방식
        editProfileViewModel.editProfileProvider.observe(viewLifecycleOwner) { provider ->
            when (provider) {
                JoinType.KAKAO -> fragmentEditProfileBinding.textFieldEditProfileEmail.helperText = "카카오로 로그인된 계정입니다."
                JoinType.GITHUB -> fragmentEditProfileBinding.textFieldEditProfileEmail.helperText = "깃허브로 로그인된 계정입니다."
                JoinType.EMAIL -> fragmentEditProfileBinding.textFieldEditProfileEmail.helperText = "이메일로 로그인된 계정입니다."
                JoinType.ERROR -> fragmentEditProfileBinding.textFieldEditProfileEmail.helperText = "이메일로 로그인된 계정입니다."
            }
        }

        // 관심 분야 chipGroup
        editProfileViewModel.editProfileInterestList.observe(viewLifecycleOwner) { list ->
            // 기존 칩들 제거
            fragmentEditProfileBinding.chipGroupProfile.removeAllViews()

            // 리스트가 변경될 때마다 for 문을 사용하여 아이템을 처리
            for (interestNum in list) {
                // 아이템 처리 코드
                fragmentEditProfileBinding.chipGroupProfile.addView(Chip(context).apply {
                    // chip 텍스트 설정: 저장되어 있는 숫자로부터 enum 클래스를 불러오고 저장된 str 보여주기
                    text = Interest.fromNum(interestNum)!!.str

                    setTextAppearance(R.style.ChipTextStyle)

                    // 자동 padding 없애기
                    setEnsureMinTouchTargetSize(false)
                    // 배경 흰색으로 지정
                    setChipBackgroundColorResource(android.R.color.white)
                    // 클릭 불가
                    isClickable = false
                    // chip에서 X 버튼 보이게 하기
                    isCloseIconVisible = true
                    // X버튼 누르면 chip 없어지게 하기
                    setOnCloseIconClickListener {
                        fragmentEditProfileBinding.chipGroupProfile.removeView(this)

                        // ViewModel의 리스트에서 해당 항목 삭제
                        val currentList = editProfileViewModel.editProfileInterestList.value?.toMutableList()
                        currentList?.remove(interestNum)
                        editProfileViewModel.editProfileInterestList.value = currentList
                    }
                })
            }
            // 마지막 칩은 칩을 추가하는 버튼으로 사용
            fragmentEditProfileBinding.chipGroupProfile.addView(Chip(context).apply {
                // chip 텍스트 설정
                text = "+"

                setTextAppearance(R.style.ChipTextStyle)

                // 자동 padding 없애기
                setEnsureMinTouchTargetSize(false)
                // 배경 흰색으로 지정
                setChipBackgroundColorResource(R.color.dividerView)
                // 클릭하면 바텀시트 올라옴
                setOnClickListener {
                    val bottomSheet = InterestBottomSheetFragment()
                    bottomSheet.show(childFragmentManager, bottomSheet.tag)
                }
            })
        }

        // 링크 리스트
        editProfileViewModel.editProfileLinkList.observe(viewLifecycleOwner) { linkList ->
            linkAddAdapter.updateData(linkList)
        }
    }
}