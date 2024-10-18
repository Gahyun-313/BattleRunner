package com.example.battlerunner

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

// SQLiteOpenHelper를 상속받은 DBHelper 클래스 (싱글턴 패턴 적용)
class DBHelper private constructor(context: Context) : SQLiteOpenHelper(context, "Login.db", null, 1) {

    // 컴패니언 객체: 클래스 인스턴스를 앱 전체에서 하나만 생성하도록 함
    companion object {

        // 싱글턴 인스턴스를 저장할 변수
        @Volatile private var instance: DBHelper? = null

        // DBHelper 인스턴스를 반환하는 메서드 (싱글턴)
        fun getInstance(context: Context): DBHelper {
            // instance가 null일 때 동기화하여 생성, 이미 있으면 기존 instance 반환
            return instance ?: synchronized(this) {
                instance ?: DBHelper(context.applicationContext).also { instance = it }
            }
        }
    }

    // 데이터베이스 테이블 생성
    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL("CREATE TABLE users(id TEXT PRIMARY KEY, password TEXT, nick TEXT)")  // users 테이블 생성
    }

    // 데이터베이스 버전이 업그레이드되었을 때 호출
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d("DBHelper", "onUpgrade 호출됨: oldVersion = $oldVersion, newVersion = $newVersion")
        db!!.execSQL("DROP TABLE IF EXISTS users")  // 기존 users 테이블을 삭제하고 재생성
    }

    // 데이터 삽입 메서드
    fun insertData(id: String?, password: String?, nick: String?): Boolean {
        val db = writableDatabase  // 쓰기 권한으로 데이터베이스 열기
        val contentValues = ContentValues().apply {
            put("id", id)
            put("password", password)
            put("nick", nick)
        }
        val result = db.insert("users", null, contentValues)  // users 테이블에 데이터 삽입
        return result != -1L  // 삽입 성공 여부 반환
    }

    // ID가 존재하는지 확인하는 메서드
    fun checkUser(id: String): Boolean {
        val db = readableDatabase
        val trimmedId = id.trim()  // 공백 제거
        val cursor = db.rawQuery("SELECT * FROM users WHERE id = ?", arrayOf(id))
        val exists = cursor.count > 0  // 일치하는 데이터가 있는지 확인
        /*
        *   0 <- 0  : 없으면 false 반환
        *   1 <- 1+ : 있으면 true 반환
        */

        Log.d("DBHelper", "checkUser: cursor count = ${cursor.count}")  // 로그 추가
        cursor.close()  // 커서 닫기
        return exists  // 결과 반환
    }

    // 데이터베이스에서 ID를 가져오는 메서드
    fun getId(): String? {
        val db = readableDatabase  // 읽기 권한으로 데이터베이스 열기
        val cursor = db.rawQuery("SELECT id FROM users LIMIT 1", null)  // 첫 번째 사용자 ID를 조회하는 쿼리 실행
        var id: String? = null
        if (cursor.moveToFirst()) {
            id = cursor.getString(cursor.getColumnIndexOrThrow("id"))  // 조회된 ID를 가져옴
            Log.d("DBHelper", "User ID: $id")  // 로그로 ID 출력
        }
        cursor.close()  // 커서 닫기
        return id  // 가져온 ID 반환
    }

    // 데이터베이스에서 비밀번호를 가져오는 메서드
    fun getPassword(): String? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT password FROM users LIMIT 1", null)
        var password: String? = null
        if (cursor.moveToFirst()) {
            password = cursor.getString(cursor.getColumnIndexOrThrow("password"))
        }
        cursor.close()  // 커서 닫기
        return password  // 비밀번호 반환
    }

    // 사용자 정보(ID, 닉네임)를 가져오는 메서드
    fun getUserInfo(userId: String?): Pair<String, String>? {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT id, nick FROM users WHERE id = ?", arrayOf(userId))

        var userInfo: Pair<String, String>? = null
        if (cursor.moveToFirst()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow("id"))
            val nick = cursor.getString(cursor.getColumnIndexOrThrow("nick"))
            userInfo = Pair(id, nick)
        }
        cursor.close()  // 커서 닫기
        return userInfo  // 사용자 정보 반환
    }

    // ID와 비밀번호가 일치하는지 확인하는 메서드
    fun checkUserpass(id: String, password: String): Boolean {
        val db = writableDatabase
        val cursor = db.rawQuery("SELECT * FROM users WHERE id = ? AND password = ?", arrayOf(id, password))
        val exists = cursor.count > 0  // 일치하는 데이터가 있는지 확인
        cursor.close()  // 커서 닫기
        return exists  // 결과 반환
    }
}
