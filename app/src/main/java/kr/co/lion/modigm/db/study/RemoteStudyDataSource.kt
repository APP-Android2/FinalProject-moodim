package kr.co.lion.modigm.db.study

import android.content.Context
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kr.co.lion.modigm.model.StudyData
import kr.co.lion.modigm.model.UserData

class RemoteStudyDataSource {
    private val studyCollection = Firebase.firestore.collection("Study")

    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid

    // 전체 스터디 목록을 가져온다.
    suspend fun getStudyAllData(): List<StudyData> {

        return try {
            val query = studyCollection
            val querySnapshot = query.get().await()
            querySnapshot.map { it.toObject(StudyData::class.java) }

        } catch (e: Exception) {
            Log.e("Firebase Error", "Error dbGetStudyAllData : ${e.message}")
            emptyList()
        }
    }

    // 전체 스터디 목록 중 모집중인 스터디만 가져온다.
    suspend fun getStudyStateTrueData(): List<StudyData> {

        return try {
            val query = studyCollection.whereEqualTo("studyState",true)
            val querySnapshot = query.get().await()
            querySnapshot.map { it.toObject(StudyData::class.java) }

        } catch (e: Exception) {
            Log.e("Firebase Error", "Error dbGetStudyAllData : ${e.message}")
            emptyList()
        }
    }

    // 내 스터디 목록을 가져온다.
    suspend fun getStudyMyData(): List<StudyData> {

        return try {
            // 현재 사용자 uid를 가져옵니다.
            val currentUserUid = "3DAiWpgwoZShL21ehAQcgolHYRA3"

            if (currentUserUid != null) {
                // studyUidList에 현재 사용자 uid가 포함된 스터디를 가져옵니다.
                val query = studyCollection.whereArrayContains("studyUidList", currentUserUid)
                val querySnapshot = query.get().await()
                querySnapshot.map { it.toObject(StudyData::class.java) }
            } else {
                Log.e("Firebase Error", "User is not authenticated")
                emptyList()
            }

        } catch (e: Exception) {
            Log.e("Firebase Error", "Error dbGetStudyMyData : ${e.message}")
            emptyList()
        }
    }

    // 사용자가 참여한 스터디 목록을 가져온다.
    suspend fun loadStudyPartData(uid: String): MutableList<StudyData> {
        // 사용자 정보를 담을 리스트
        val studyList = mutableListOf<StudyData>()

        try {
            // studyUidList에 해당 uid가 포함된 문서들을 가져온다.
            val querySnapshot = studyCollection.whereArrayContains("studyUidList", uid).get().await()

            // 가져온 문서의 수 만큼 반복한다.
            querySnapshot.forEach {
                // StudyData 객체에 담는다.
                val study = it.toObject(StudyData::class.java)
                // 리스트에 담는다.
                studyList.add(study)
            }
        } catch (e: Exception) {
            Log.e("studyds", "loadStudyPartData: ${e.message}")
        }

        return studyList
    }

    // 사용자가 진행한 스터디 목록을 가져온다.
    suspend fun loadStudyHostData(uid: String): MutableList<StudyData> {
        // 사용자 정보를 담을 리스트
        val studyList = mutableListOf<StudyData>()

        try {
            // studyUidList에 해당 uid가 포함된 문서들을 가져온다.
            val querySnapshot = studyCollection.whereArrayContains("studyUidList", uid).get().await()

            // 가져온 문서의 수 만큼 반복한다.
            querySnapshot.forEach { documentSnapshot ->
                // StudyData 객체에 담는다.
                val study = documentSnapshot.toObject(StudyData::class.java)
                // 첫 번째 요소(스터디 진행자)가 uid와 일치하는지 확인한다.
                if (study.studyUidList.isNotEmpty() && study.studyUidList[0] == uid) {
                    // 리스트에 담는다.
                    studyList.add(study)
                }
            }
        } catch (e: Exception) {
            Log.e("studyds", "loadStudyPartData: ${e.message}")
        }

        return studyList
    }

    companion object{

        // uid를 통해 특정 사용자가 참여한 스터디 목록을 불러온다
        suspend fun loadUserPartStudy(uid: String) : MutableList<StudyData>{
            // 사용자 정보를 담을 리스트
            val studyList = mutableListOf<StudyData>()

            val job1 = CoroutineScope(Dispatchers.IO).launch {
                // Study 컬렉션 접근 객체를 가져온다.
                val collectionReference = Firebase.firestore.collection("Study")
                // studyUidList에 해당 uid가 포함된 문서들을 가져온다.
                val querySnapshot = collectionReference.whereArrayContains("studyUidList", uid).get().await()
                // 가져온 문서의 수 만큼 반복한다.
                querySnapshot.forEach {
                    // StudyData 객체에 담는다.
                    val study = it.toObject(StudyData::class.java)
                    // 리스트에 담는다.
                    studyList.add(study)
                }
            }
            job1.join()

            return studyList
        }

        // uid를 통해 특정 사용자가 진행한 스터디 목록을 불러온다
        suspend fun loadUserHostStudy(uid: String) : MutableList<StudyData>{
            // 사용자 정보를 담을 리스트
            val studyList = mutableListOf<StudyData>()

            val job1 = CoroutineScope(Dispatchers.IO).launch {
                // Study 컬렉션 접근 객체를 가져온다.
                val collectionReference = Firebase.firestore.collection("Study")

                // studyUidList에 해당 uid가 포함된 문서들을 가져온다.
                val querySnapshot = collectionReference.whereArrayContains("studyUidList", uid).get().await()
                // 가져온 문서의 수 만큼 반복한다.
                querySnapshot.forEach { documentSnapshot ->
                    // StudyData 객체에 담는다.
                    val study = documentSnapshot.toObject(StudyData::class.java)
                    // 첫 번째 요소(스터디 진행자)가 uid와 일치하는지 확인한다.
                    if (study.studyUidList.isNotEmpty() && study.studyUidList[0] == uid) {
                        // 리스트에 담는다.
                        studyList.add(study)
                    }
                }
            }
            job1.join()

            return studyList
        }

        // 사용자 프로필 사진을 받아오는 메서드
        suspend fun loadStudyThumbnail(context: Context, imageFileName: String, imageView: ImageView){
            // 이미지가 등록되어 있지 않으면 불러오지 않는다
            if (imageFileName.isNotEmpty()) {
                val job1 = CoroutineScope(Dispatchers.IO).launch {
                    // 이미지에 접근할 수 있는 객체를 가져온다.
                    val storageRef = Firebase.storage.reference.child("studyPic/$imageFileName")
                    // 이미지의 주소를 가지고 있는 Uri 객체를 받아온다.
                    val imageUri = storageRef.downloadUrl.await()
                    // 이미지 데이터를 받아와 이미지 뷰에 보여준다.
                    CoroutineScope(Dispatchers.Main).launch {
                        Glide.with(context).load(imageUri).into(imageView)
                    }
                }
                job1.join()
                // 이미지는 용량이 매우 클 수 있다. 즉 이미지 데이터를 내려받는데 시간이 오래걸릴 수도 있다.
                // 이에, 이미지 데이터를 받아와 보여주는 코루틴을 작업이 끝날 때 까지 대기 하지 않는다.
                // 그 이유는 데이터를 받아오는데 걸리는 오랜 시간 동안 화면에 아무것도 나타나지 않을 수 있기 때문이다.
                // 따라서 이 메서드는 제일 마지막에 호출해야 한다.(다른 것들을 모두 보여준 후에...)
            }
        }

        // 모든 사용자의 정보를 가져온다.
        suspend fun getAllUsers() : MutableList<UserData>{
            // 사용자 정보를 담을 리스트
            val userList = mutableListOf<UserData>()

            val job1 = CoroutineScope(Dispatchers.IO).launch {
                // 모든 사용자 정보를 가져온다
                val querySnapshot = Firebase.firestore.collection("User").get().await()
                // 가져온 문서의 수 만큼 반복한다.
                querySnapshot.forEach {
                    // UserData 객체에 담는다.
                    val user = it.toObject(UserData::class.java)
                    // 리스트에 담는다.
                    userList.add(user)
                }
            }
            job1.join()

            return userList
        }

        // 사용자 정보를 수정하는 메서드
        suspend fun updateUserData(user: UserData){
            val job1 = CoroutineScope(Dispatchers.IO).launch {
                // 컬렉션에 접근할 수 있는 객체를 가져온다.
                val collectionReference = Firebase.firestore.collection("User")

                // 컬렉션이 가지고 있는 문서들 중에 수정할 사용자 정보를 가져온다.
                val query = collectionReference.whereEqualTo("userUid", user.userUid).get().await()

                // 저장할 데이터를 담을 HashMap을 만들어준다.
                val map = mutableMapOf<String, Any?>()
                map["userName"] = user.userName
                map["userPhone"] = user.userPhone
                map["userProfilePic"] = user.userProfilePic
                map["userIntro"] = user.userIntro
                map["userInterestList"] = user.userInterestList
                map["userLinkList"] = user.userLinkList
                map["userUid"] = user.userUid

                // 저장한다.
                // 가져온 문서 중 첫 번째 문서에 접근하여 데이터를 수정한다.
                query.documents[0].reference.update(map)
            }

            job1.join()
        }

        // 사용자의 상태를 변경하는 메서드
        suspend fun invalidateMember(uid: String){
            val job1 = CoroutineScope(Dispatchers.IO).launch {
                // 컬렉션에 접근할 수 있는 객체를 가져온다.
                val collectionReference = Firebase.firestore.collection("Members")
                // 컬렉션이 가지고 있는 문서들 중에 userIdx 필드가 지정된 사용자 번호값하고 같은 Document들을 가져온다.
                val query = collectionReference.whereEqualTo("uid", uid).get().await()

                // 저장할 데이터를 담을 HashMap을 만들어준다.
                val map = mutableMapOf<String, Any>()
                map["state"] = false
                // 저장한다.
                // 가져온 문서 중 첫 번째 문서에 접근하여 데이터를 수정한다.
                query.documents[0].reference.update(map)
            }
            job1.join()
        }
    }

}