package com.example.battlerunner.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.battlerunner.data.model.BattleRecord
import com.example.battlerunner.data.model.LoginInfo
import com.example.battlerunner.data.model.User

/**
 * SQLite 데이터베이스 관리 클래스
 * 싱글턴 패턴을 사용 -> 애플리케이션 전역에서 동일한 인스턴스를 사용!
 */

class DBHelper private constructor(context: Context) : SQLiteOpenHelper(context, "Login.db", null, 8) {

    companion object {
        @Volatile private var instance: DBHelper? = null  // 싱글턴 인스턴스

        // 싱글턴 인스턴스를 반환하는 메서드
        fun getInstance(context: Context): DBHelper {
            return instance ?: synchronized(this) {
                instance ?: DBHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    // 데이터베이스 테이블 생성 메서드
    override fun onCreate(db: SQLiteDatabase?) {
        // login_info 테이블 생성
        db!!.execSQL("""
        CREATE TABLE IF NOT EXISTS login_info(
            user_id TEXT PRIMARY KEY, -- 사용자 ID
            password TEXT,            -- 비밀번호 (또는 토큰)
            name TEXT,                -- 사용자 이름
            login_type TEXT           -- 로그인 타입 (custom, kakao, google)
        )
        """)
        // running_records 테이블 생성
        db.execSQL("""
        CREATE TABLE IF NOT EXISTS running_records(
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            date TEXT NOT NULL,
            image_path TEXT NOT NULL,
            elapsed_time INTEGER NOT NULL,
            distance REAL NOT NULL
        )
        """)
        // battle_records 테이블 생성
        db.execSQL(
            """
        CREATE TABLE IF NOT EXISTS battle_records(
            id INTEGER PRIMARY KEY AUTOINCREMENT,   -- n번쨰
            start_date TEXT NOT NULL,               -- 배틀 시작일
            end_date TEXT,                          -- 배틀 종료일
            opponent_name TEXT NOT NULL,            -- 상대 이름
            image_path TEXT NOT NULL,               -- 결과 사진 저장 경로
            elapsed_time INTEGER,                   -- 총 시간
            distance REAL                           -- 총 거리
        )
        """
        )

        // friends 테이블 생성
        db.execSQL("""
        CREATE TABLE IF NOT EXISTS friends(
            user_id TEXT PRIMARY KEY,
            username TEXT,
            profile_image INTEGER
        )
    """)
    }

    // 데이터베이스 버전 업그레이드 시 호출되는 메서드
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d("DBHelper", "onUpgrade 호출됨: oldVersion = $oldVersion, newVersion = $newVersion")  // 업그레이드 로그 출력
        // 기존 테이블 삭제 후 새로 생성
        db!!.execSQL("DROP TABLE IF EXISTS running_records")
        db.execSQL("DROP TABLE IF EXISTS login_info")
        db.execSQL("DROP TABLE IF EXISTS battle_records")
        onCreate(db)
    }

    // <러닝 기록> 러닝 기록 저장 메서드
    fun insertRunningRecord(date: String, imagePath: String, elapsedTime: Long, distance: Float): Boolean {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("date", date) // 날짜와 시간을 포함 (ex. "2023-11-26_12-45-00")
            put("image_path", imagePath)
            put("elapsed_time", elapsedTime)
            put("distance", distance)
        }
        val result = db.insert("running_records", null, contentValues)
        return result != -1L // 삽입 성공 여부 반환
    }

    // <배틀 기록> 배틀 기록 저장 메서드
    fun insertBattleStartRecord(startDate: String, opponentName: String): Boolean {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("start_date", startDate) // 시작 날짜
            put("opponent_name", opponentName) // 상대 이름
        }
        val result = db.insert("battle_records", null, contentValues)
        return result != -1L // 삽입 성공 여부 반환
    }
    fun insertBattleRecord(endDate: String, imagePath: String, elapsedTime: Long, distance: Float): Boolean {
        val db = writableDatabase
        val contentValues = ContentValues().apply {

            put("end_date", endDate) // 날짜와 시간을 포함 (ex. "2023-11-26_12-45-00")
            put("image_path", imagePath)
            put("elapsed_time", elapsedTime)
            put("distance", distance)
        }
        val result = db.insert("battle_records", null, contentValues)
        return result != -1L // 삽입 성공 여부 반환
    }

    // <러닝 기록> 러닝 경로 이미지 가져오기
    fun getRunningImageByDate(date: String): String? {
        val db = this.readableDatabase
        val query = "SELECT image_path FROM running_records WHERE date = ?"
        val cursor = db.rawQuery(query, arrayOf("$date%"))
        return if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndexOrThrow("image_path"))
        } else {
            null
        }.also {
            cursor.close()
        }
    }

    // <러닝 기록> 러닝 데이터 가져오기
    fun getRunningMetaData(date: String): Pair<Long, Float>? {
        val db = this.readableDatabase
        val query = "SELECT elapsed_time, distance FROM running_records WHERE date = ?"
        val cursor = db.rawQuery(query, arrayOf(date))
        return if (cursor.moveToFirst()) {
            val elapsedTime = cursor.getLong(cursor.getColumnIndexOrThrow("elapsed_time"))
            val distance = cursor.getFloat(cursor.getColumnIndexOrThrow("distance"))
            Pair(elapsedTime, distance)
        } else {
            null
        }.also {
            cursor.close()
        }
    }

    // <러닝 기록> 캘린더에서 특정 날짜의 기록을 가져오는 메서드
    fun getRecordsByDate(date: String): List<Triple<String, Long, Float>> {
        val db = readableDatabase
        val query = "SELECT image_path, elapsed_time, distance FROM running_records WHERE date LIKE ?"
        val cursor = db.rawQuery(query, arrayOf("$date%")) // 해당 날짜로 시작하는 모든 기록 조회
        val records = mutableListOf<Triple<String, Long, Float>>()
        if (cursor.moveToFirst()) {
            do {
                val imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path"))
                val elapsedTime = cursor.getLong(cursor.getColumnIndexOrThrow("elapsed_time"))
                val distance = cursor.getFloat(cursor.getColumnIndexOrThrow("distance"))
                records.add(Triple(imagePath, elapsedTime, distance))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return records
    }

    // <러닝 기록> 기록이 있는 날짜를 반환하는 메서드
    fun getAllRunningDates(): List<String> {
        val db = this.readableDatabase
        val query = "SELECT DISTINCT SUBSTR(date, 1, 10) AS date FROM running_records" // 날짜 부분만 추출
        val cursor = db.rawQuery(query, null)

        val dates = mutableListOf<String>()
        if (cursor.moveToFirst()) {
            do {
                dates.add(cursor.getString(cursor.getColumnIndexOrThrow("date")))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return dates
    }

    // <배틀 기록> 배틀 기록 저장 메서드
    fun insertBattleRecord(
        startDate: String,
        endDate: String?,
        opponentName: String,
        imagePath: String,
        elapsedTime: Long?,
        distance: Float?
    ): Boolean {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("start_date", startDate) // 시작 날짜
            put("end_date", endDate) // 종료 날짜 (nullable)
            put("opponent_name", opponentName) // 상대 이름
            put("image_path", imagePath) // 이미지 경로
            put("elapsed_time", elapsedTime) // 경과 시간 (nullable)
            put("distance", distance) // 총 거리 (nullable)
        }
        val result = db.insert("battle_records", null, contentValues)
        return result != -1L // 삽입 성공 여부 반환
    }


    // <배틀 기록> 전체 배틀 기록 조회 메서드
    fun getBattleRecords(): List<BattleRecord> {
        val db = readableDatabase
        val query = "SELECT start_date, opponent_name, image_path FROM battle_records"
        val cursor = db.rawQuery(query, null)

        val records = mutableListOf<BattleRecord>()
        if (cursor.moveToFirst()) {
            do {
                val startDate = cursor.getString(cursor.getColumnIndexOrThrow("start_date"))
                val opponentName = cursor.getString(cursor.getColumnIndexOrThrow("opponent_name"))
                val imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path"))
                records.add(BattleRecord(startDate, opponentName, imagePath))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return records
    }

    // 특정 배틀 기록 조회 메서드
    fun getBattleRecord(date: String): BattleRecord? {
        val db = readableDatabase
        val query = "SELECT date, opponent_name, image_path FROM battle_records WHERE date = ?"
        val cursor = db.rawQuery(query, arrayOf(date))

        return if (cursor.moveToFirst()) {
            val battleDate = cursor.getString(cursor.getColumnIndexOrThrow("date"))
            val opponentName = cursor.getString(cursor.getColumnIndexOrThrow("opponent_name"))
            val imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path"))
            BattleRecord(battleDate, opponentName, imagePath)
        } else {
            null
        }.also {
            cursor.close()
        }
    }

    // 로그인 정보 저장
    fun saveAutoLoginInfo(loginInfo: LoginInfo): Boolean {
        Log.d("DBHelper", "저장하려는 데이터: $loginInfo")
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("user_id", loginInfo.userId)
            put("password", loginInfo.password)
            put("name", loginInfo.name)
            put("login_type", loginInfo.loginType)
        }
        val result = db.insert("login_info", null, contentValues)
        return if (result != -1L) {
            Log.d("DBHelper", "SQLite 저장 성공")
            true
        } else {
            Log.e("DBHelper", "SQLite 저장 실패")
            false
        }
    }


    // 자동 로그인 정보 조회
    fun getLoginInfo(): Pair<String, String>? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT user_id, password FROM login_info LIMIT 1", null)
        return if (cursor.moveToFirst()) {
            val userId = cursor.getString(cursor.getColumnIndexOrThrow("user_id"))
            val password = cursor.getString(cursor.getColumnIndexOrThrow("password"))
            cursor.close()
            Pair(userId, password)
        } else {
            cursor.close()
            null
        }
    }

    // 로그인 유형 가져오는 메서드
    fun getLoginType(): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT login_type FROM login_info LIMIT 1", null)  // login_type 컬럼 조회
        return if (cursor.moveToFirst()) {  // 결과가 있을 경우
            val loginType = cursor.getString(cursor.getColumnIndexOrThrow("login_type"))  // 로그인 타입 값 가져오기
            cursor.close()  // 커서 닫기
            loginType  // 로그인 타입 반환
        } else {
            cursor.close()  // 커서 닫기
            null  // 결과가 없을 경우 null 반환
        }
    }

    // <로그아웃> 로컬 로그인 정보 삭제 메서드
    fun deleteLoginInfo() {
        writableDatabase.delete("login_info", null, null)  // login_info 테이블의 모든 데이터 삭제
    }

    // 사용자 ID를 가져오는 메서드
    fun getUserId(): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT user_id FROM login_info LIMIT 1", null) // login_info 테이블에서 조회
        var id: String? = null
        if (cursor.moveToFirst()) {
            id = cursor.getString(cursor.getColumnIndexOrThrow("user_id"))  // user_id 값 가져오기
        }
        cursor.close()  // 커서 닫기
        return id  // 가져온 ID 반환
    }

    // 사용자 이름 가져오는 메서드
    fun getUserName(): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT name FROM login_info LIMIT 1", null) // login_info 테이블에서 조회
        var userName: String? = null
        if (cursor.moveToFirst()) {
            userName = cursor.getString(cursor.getColumnIndexOrThrow("name"))  // name 값 가져오기
        }
        cursor.close()  // 커서 닫기
        return userName  // 가져온 ID 반환
    }

    // 배틀 상대 이름 가져오는 메서드
    fun getOpponenetName(): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT opponent_name FROM battle_records LIMIT 1", null) // battle_records 테이블에서 조회
        var opponentName: String? = null
        if (cursor.moveToFirst()) {
            opponentName = cursor.getString(cursor.getColumnIndexOrThrow("opponent_name"))  // name 값 가져오기
        }
        cursor.close()  // 커서 닫기
        return opponentName  // 가져온 ID 반환
    }

    // 친구 추가 메서드
    fun addFriend(userId: String, username: String, profileImage: Int): Boolean {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put("user_id", userId)
            put("username", username)
            put("profile_image", profileImage)
        }
        val result = db.insertWithOnConflict("friends", null, contentValues, SQLiteDatabase.CONFLICT_IGNORE)
        return result != -1L // 삽입 성공 여부 반환
    }

    // 친구 목록 가져오기 메서드
    fun getFriends(): List<User> {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM friends", null)
        val friends = mutableListOf<User>()
        if (cursor.moveToFirst()) {
            do {
                val userId = cursor.getString(cursor.getColumnIndexOrThrow("user_id"))
                val username = cursor.getString(cursor.getColumnIndexOrThrow("username"))
                val profileImage = cursor.getInt(cursor.getColumnIndexOrThrow("profile_image"))
                friends.add(User(userId, username, profileImageResId = profileImage))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return friends
    }

    // 친구 삭제
    fun deleteFriend(userId: String): Boolean {
        val db = writableDatabase
        val result = db.delete("friends", "user_id = ?", arrayOf(userId))
        return result > 0 // 삭제 성공 여부 반환
    }

}
