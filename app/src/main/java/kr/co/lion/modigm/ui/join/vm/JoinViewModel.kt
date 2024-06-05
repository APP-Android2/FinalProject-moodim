package kr.co.lion.modigm.ui.join.vm

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.model.UserData
import kr.co.lion.modigm.repository.UserInfoRepository

class JoinViewModel : ViewModel() {

    // 리포지터리
    private val _userInfoRepository = UserInfoRepository()
    // 파이어베이스 인증
    private val _auth = FirebaseAuth.getInstance()

    // 파이어베이스에 연동된 유저
    private var _user: MutableLiveData<FirebaseUser?> = MutableLiveData()
    val user: LiveData<FirebaseUser?> = _user

    // sns provider(제공자 이름, kakao, github)
    private var _userProvider: MutableLiveData<String> = MutableLiveData()
    val userProvider: LiveData<String> = _userProvider
    fun setUserProvider(provider:String){
        _userProvider.value = provider
    }

    // sns email
    private var _snsEmail: MutableLiveData<String> = MutableLiveData()
    val snsEmail: LiveData<String> = _snsEmail
    fun setSnsEmail(){
        if(_auth.currentUser != null){
            _snsEmail.value = _auth.currentUser?.email
            Log.d("test1234", "email : ${_auth.currentUser?.email}")
        }
    }

    // sns Custom Token(카카오톡)
    private var _snsCustomToken: MutableLiveData<String> = MutableLiveData()
    val snsCustomToken: LiveData<String> = _snsCustomToken
    fun setSnsCustomToken(customToken:String){
        _snsCustomToken.value = customToken
    }

    // sns Credential(깃허브)
    private var _snsCredential: MutableLiveData<AuthCredential> = MutableLiveData()
    val snsCredential: LiveData<AuthCredential> = _snsCredential
    fun setSnsCredential(credential:AuthCredential){
        _snsCredential.value = credential
    }

    // auth에 등록된 이메일, 뒤로가기로 이메일을 수정한 경우 비교용
    var verifiedEmail = ""

    // 전화번호 인증
    private var _phoneVerification: MutableLiveData<Boolean> = MutableLiveData()
    val phoneVerification: LiveData<Boolean> = _phoneVerification

    // 이미 전화번호가 등록되었는지 여부
    var isPhoneAlreadyRegistered = MutableLiveData(false)

    fun setPhoneVerificated(verificated:Boolean){
        _phoneVerification.value = verificated
    }

    // 이미 등록된 전화번호 계정의 이메일
    private var _alreadyRegisteredUserEmail: MutableLiveData<String> = MutableLiveData()
    val alreadyRegisteredUserEmail: LiveData<String> = _alreadyRegisteredUserEmail
    // 이미 등록된 전화번호 계정의 프로바이더
    private var _alreadyRegisteredUserProvider: MutableLiveData<String> = MutableLiveData()
    val alreadyRegisteredUserProvider: LiveData<String> = _alreadyRegisteredUserProvider

    fun setAleradyRegisteredUser(email:String, provider:String){
        _alreadyRegisteredUserEmail.value = email
        _alreadyRegisteredUserProvider.value = provider
    }

    private val _phoneCredential: MutableLiveData<AuthCredential> = MutableLiveData()

    fun setPhoneCredential(credential: AuthCredential){
        _phoneCredential.value = credential
    }

    // 회원가입 완료 여부
    private var _joinCompleted = MutableLiveData(false)
    val joinCompleted: LiveData<Boolean> = _joinCompleted

    // 회원 객체 생성을 위한 정보
    private val _uid = MutableLiveData<String>()
    private val _email = MutableLiveData<String>()
    private val _password = MutableLiveData<String>()
    fun setEmailAndPw(email:String, pw:String){
        _email.value = email
        _password.value = pw
    }

    private val _userName = MutableLiveData<String>()
    private val _phoneNumber = MutableLiveData<String>()
    fun setUserNameAndPhoneNumber(name:String, phone:String){
        _userName.value = name
        _phoneNumber.value = phone
    }

    private val _interests = MutableLiveData<MutableList<Int>>()
    fun setInterests(interests:MutableList<Int>){
        _interests.value = interests
    }

    // FirebaseAuth에 이메일 계정 등록
    suspend fun createEmailUser(): String {
        // 오류 메시지
        var error = ""
        try {
            val authResult = _auth.createUserWithEmailAndPassword(_email.value?:"", _password.value?:"").await()
            _user.value = authResult.user
            _uid.value = authResult.user?.uid?:""
            verifiedEmail = _email.value?:""
            //userCredential.user?.delete()
            //_auth.signOut()
        }catch (e:FirebaseAuthException){
            if(e.errorCode=="ERROR_EMAIL_ALREADY_IN_USE"){
                error = "이미 등록되어 있는 이메일 계정입니다."
            }
        }
        return error
    }

    // 회원가입 이탈 시 이미 Auth에 등록되어있는 인증 정보 삭제
    fun deleteCurrentUser(){
        // 이메일 인증 정보 삭제
        if(verifiedEmail.isNotEmpty()){
            _user.value?.delete()
        }
        // 전화번호 인증 정보는 이미 중복확인을 할 때 삭제해놓기 때문에 필요없음
    }

    // UserInfoData 객체 생성
    private fun createUserInfoData(): UserData {
        val user = UserData()
        user.userName = _userName.value?:""
        user.userPhone = _phoneNumber.value?:""
        user.userInterestList = _interests.value?: mutableListOf()
        user.userUid = _uid.value?:""

        user.userProvider = userProvider.value?:""
        user.userEmail = _snsEmail.value?:""
        // 각 화면에서 응답받은 정보 가져와서 객체 생성 후 return
        return user
    }

    // 회원가입 완료 전에 이메일 계정과 전화번호 계정을 통합
    private fun linkEmailAndPhone(){
        _auth.signInWithEmailAndPassword(_email.value?:"", _password.value?:"").addOnCompleteListener { loginTask ->
            if(loginTask.isSuccessful){
                _auth.currentUser?.linkWithCredential(_phoneCredential.value!!)?.addOnCompleteListener { linkTask ->
                    if(!linkTask.isSuccessful){
                        Log.d("testError", "linkWithCredential : ${linkTask.exception}")
                    }
                }
            }else{
                Log.d("testError", "signInWithEmailAndPassword : ${loginTask.exception}")
            }
        }
    }

    // 이메일 계정 회원 가입 완료
    suspend fun completeJoinEmailUser(){
        // 메일 계정을 전화번호 계정과 연결
        linkEmailAndPhone()
        _user.value = _auth.currentUser

        // UserInfoData 객체 생성
        val user = createUserInfoData()
        // 파이어스토어에 데이터 저장
        _userInfoRepository.insetUserData(user)

        _joinCompleted.value = true
    }

    // 회원가입 완료 전에 SNS 계정과 전화번호 계정을 통합
    private suspend fun linkSnsAndPhone(){
        var signInResult: AuthResult? = null
        when(userProvider.value){
            "kakao" -> {
                // 카카오 등 파이어베이스에서 지원하지 않는 공급자는 customToken으로 로그인
                signInResult = _auth.signInWithCustomToken(snsCustomToken.value?:"").await()
            }
            "github" -> {
                // 깃허브 등 파이어베이스에서 지원하는 공급자는 credential로 로그인
                signInResult = _auth.signInWithCredential(snsCredential.value!!).await()
            }
        }
        _uid.value = signInResult?.user?.uid
        // 로그인 계정과 전화번호 연결
        signInResult?.user?.linkWithCredential(_phoneCredential.value!!)?.await()
    }

    // SNS 계정 회원 가입 완료
    fun completeJoinSnsUser(){
        viewModelScope.launch {
            // SNS 계정과 전화번호 계정을 연결
            linkSnsAndPhone()
            _user.value = _auth.currentUser

            // UserInfoData 객체 생성
            val user = createUserInfoData()
            // 파이어스토어에 데이터 저장
            _userInfoRepository.insetUserData(user)

            _joinCompleted.value = true
        }
    }
}