package kr.co.lion.modigm.model

data class ChatRoomData(
    var chatIdx: Int = 0, // 채팅방 고유 ID
    var chatTitle: String = "", // 채팅방 이름
    var chatRoomImage: String = "",
    var chatMemberList: List<String?> = listOf(), // 채팅방 참여 멤버 UID 목록
    var participantCount: Int = 2, // 최소 참여자 수 2 (그룹 채팅방일 때만 해당)
    var groupChat: Boolean = false, // 그룹 채팅방 여부
    var lastChatMessage: String = "",
    var lastChatFullTime: Long = 0,
    var lastChatTime: String = "",
    var unreadMessageCount: MutableMap<String, Int> = mutableMapOf() // 사용자별 안 읽은 메시지 개수를 추적하는 필드
) {
    constructor(): this(
        0,"", "", listOf(), 2, false,
        "", 0, "",
    )
}