package com.example.battlerunner.data.local  // 패키지 정의

import android.content.ContentValues  // ContentValues 클래스 임포트
import android.content.Context  // Context 클래스 임포트
import android.database.sqlite.SQLiteDatabase  // SQLiteDatabase 클래스 임포트
import android.database.sqlite.SQLiteOpenHelper  // SQLiteOpenHelper 클래스 임포트
import android.util.Log  // Log 클래스 임포트

class DBHelper private constructor(context: Context) : SQLiteOpenHelper(context, "Login.db", null, 2) {

    companion object {
        @Volatile private var instance: DBHelper? = null  // 싱글턴 인스턴스 변수

        // 싱글턴 인스턴스를 반환하는 메서드
        fun getInstance(context: Context): DBHelper {
            return instance ?: synchronized(this) {
                instance ?: DBHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    // 데이터베이스 테이블 생성 메서드
    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL("CREATE TABLE IF NOT EXISTS users(id TEXT PRIMARY KEY, password TEXT, nick TEXT)")  // users 테이블 생성
        db.execSQL("CREATE TABLE IF NOT EXISTS login_info(user_id TEXT PRIMARY KEY, token TEXT, login_type TEXT)")  // login_info 테이블 생성
    }

    // 데이터베이스 버전 업그레이드 시 호출되는 메서드
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d("DBHelper", "onUpgrade 호출됨: oldVersion = $oldVersion, newVersion = $newVersion")  // 업그레이드 로그 출력
        db!!.execSQL("DROP TABLE IF EXISTS users")  // 기존 users 테이블 삭제
        db.execSQL("DROP TABLE IF EXISTS login_info")  // 기존 login_info 테이블 삭제
        onCreate(db)  // 새로운 테이블 생성
    }

    // 회원 정보를 저장하는 데이터 삽입 메서드
    fun insertUserData(id: String?, password: String?, nick: String?): Boolean {
        val db = writableDatabase  // 쓰기 가능한 데이터베이스 인스턴스 가져오기
        val contentValues = ContentValues().apply {
            put("id", id)  // ID 추가
            put("password", password)  // 비밀번호 추가
            put("nick", nick)  // 닉네임 추가
        }
        val result = db.insert("users", null, contentValues)  // users 테이블에 데이터 삽입
        return result != -1L  // 삽입 성공 여부 반환
    }

    // 로그인 정보를 저장하는 메서드
    fun saveLoginInfo(userId: String, token: String, loginType: String): Boolean {
        val db = writableDatabase  // 쓰기 가능한 데이터베이스 인스턴스 가져오기
        val contentValues = ContentValues().apply {
            put("user_id", userId)  // 사용자 ID 추가
            put("token", token)  // 토큰 추가
            put("login_type", loginType)  // 로그인 타입 추가
        }
        val result = db.insertWithOnConflict("login_info", null, contentValues, SQLiteDatabase.CONFLICT_REPLACE)  // 충돌 시 기존 데이터 덮어쓰기
        return result != -1L  // 삽입 성공 여부 반환
    }

    // 자동 로그인 정보를 가져오는 메서드
    fun getLoginInfo(): Pair<String, String>? {
        val db = readableDatabase  // 읽기 가능한 데이터베이스 인스턴스 가져오기
        val cursor = db.rawQuery("SELECT user_id, token FROM login_info LIMIT 1", null)  // 쿼리 실행하여 사용자 ID와 토큰 가져오기
        return if (cursor.moveToFirst()) {  // 결과가 있으면
            val userId = cursor.getString(cursor.getColumnIndexOrThrow("user_id"))  // user_id 컬럼의 값 가져오기
            val token = cursor.getString(cursor.getColumnIndexOrThrow("token"))  // token 컬럼의 값 가져오기
            cursor.close()  // 커서 닫기
            Pair(userId, token)  // 사용자 ID와 토큰을 Pair로 반환
        } else {
            cursor.close()  // 커서 닫기
            null  // 저장된 로그인 정보가 없을 경우 null 반환
        }
    }

    // 로그인 유형을 가져오는 메서드
    fun getLoginType(): String? {
        val db = readableDatabase  // 읽기 가능한 데이터베이스 인스턴스 가져오기
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

    // 로그인 정보 삭제 메서드 (로그아웃 시 사용)
    fun deleteLoginInfo() {
        writableDatabase.delete("login_info", null, null)  // login_info 테이블의 모든 데이터 삭제
    }

    // 특정 ID가 존재하는지 확인하는 메서드
    fun checkUser(id: String): Boolean {
        val db = readableDatabase  // 읽기 가능한 데이터베이스 인스턴스 가져오기
        val cursor = db.rawQuery("SELECT * FROM users WHERE id = ?", arrayOf(id))  // ID에 해당하는 데이터 조회
        val exists = cursor.count > 0  // 일치하는 데이터가 있는지 확인
        cursor.close()  // 커서 닫기
        return exists  // 결과 반환
    }

    // 사용자 ID를 가져오는 메서드
    fun getId(): String? {
        val db = readableDatabase  // 읽기 가능한 데이터베이스 인스턴스 가져오기
        val cursor = db.rawQuery("SELECT id FROM users LIMIT 1", null)  // 첫 번째 ID 가져오기
        var id: String? = null
        if (cursor.moveToFirst()) {
            id = cursor.getString(cursor.getColumnIndexOrThrow("id"))  // ID 값 가져오기
        }
        cursor.close()  // 커서 닫기
        return id  // 가져온 ID 반환
    }

    // 비밀번호를 가져오는 메서드
    fun getPassword(): String? {
        val db = readableDatabase  // 읽기 가능한 데이터베이스 인스턴스 가져오기
        val cursor = db.rawQuery("SELECT password FROM users LIMIT 1", null)  // 첫 번째 비밀번호 가져오기
        var password: String? = null
        if (cursor.moveToFirst()) {
            password = cursor.getString(cursor.getColumnIndexOrThrow("password"))  // 비밀번호 값 가져오기
        }
        cursor.close()  // 커서 닫기
        return password  // 비밀번호 반환
    }

    // 특정 사용자 정보를 가져오는 메서드
    fun getUserInfo(): Pair<String, String>? {
        val db = readableDatabase

        // login_info 테이블에서 user_id를 가져옵니다.
        val cursorLoginInfo = db.rawQuery("SELECT user_id FROM login_info LIMIT 1", null)
        var userInfo: Pair<String, String>? = null

        if (cursorLoginInfo.moveToFirst()) {
            val userId = cursorLoginInfo.getString(cursorLoginInfo.getColumnIndexOrThrow("user_id"))
            cursorLoginInfo.close()

            // users 테이블에서 해당 user_id의 정보를 가져옵니다.
            val cursorUser = db.rawQuery("SELECT id, nick FROM users WHERE id = ?", arrayOf(userId))
            if (cursorUser.moveToFirst()) {
                val id = cursorUser.getString(cursorUser.getColumnIndexOrThrow("id"))
                val nick = cursorUser.getString(cursorUser.getColumnIndexOrThrow("nick"))
                userInfo = Pair(id, nick)
            }
            cursorUser.close()
        } else {
            cursorLoginInfo.close()
        }

        return userInfo
    }


    // ID와 비밀번호가 일치하는지 확인하는 메서드
    fun checkUserPass(id: String, password: String): Boolean {
        val db = readableDatabase  // 읽기 가능한 데이터베이스 인스턴스 가져오기
        val cursor = db.rawQuery("SELECT * FROM users WHERE id = ? AND password = ?", arrayOf(id, password))  // ID와 비밀번호 일치 여부 조회
        val exists = cursor.count > 0  // 일치하는 데이터가 있는지 확인
        cursor.close()  // 커서 닫기
        return exists  // 결과 반환
    }
}
