package com.example.battlerunner

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context) : SQLiteOpenHelper(context, "Login.db", null, 1) {
    // users 테이블 생성
    override fun onCreate(MyDB: SQLiteDatabase?) {
        // users 테이블을 생성하는 SQL문 실행
        MyDB!!.execSQL("create Table users(id TEXT primary key, password TEXT, nick TEXT)")
    }

    // 정보 갱신
    override fun onUpgrade(MyDB: SQLiteDatabase?, i: Int, i1: Int) {
        // 기존의 users 테이블이 존재하면 삭제하고 다시 생성하도록 설정
        MyDB!!.execSQL("drop Table if exists users")
    }

    // id, password, nick 삽입 (성공시 true, 실패시 false)
    fun insertData (id: String?, password: String?, nick: String?): Boolean {
        // 데이터베이스에 쓰기 권한을 요청
        val MyDB = this.writableDatabase

        // 사용자 정보를 담기 위한 ContentValues 객체 생성
        val contentValues = ContentValues()
        contentValues.put("id", id)
        contentValues.put("password", password)
        contentValues.put("nick", nick)

        // users 테이블에 데이터 삽입
        val result = MyDB.insert("users", null, contentValues)

        // 데이터베이스 연결을 닫음 -> 너무 빨리 닫으면 에러 나므로 실행 중인 액티비티 종료 후에 닫도록 하자.
        //MyDB.close()

        // 삽입 성공 여부 반환 (삽입 실패 시 -1 반환)
        return result != -1L
    }

    // 사용자 아이디가 없으면 false, 이미 존재하면 true
    @SuppressLint("Recycle")
    fun checkUser(id: String?): Boolean {
        // 데이터베이스에 읽기 권한을 요청
        val MyDB = this.readableDatabase

        // 사용자 존재 여부 확인
        var res = true
        val cursor = MyDB.rawQuery("Select * from users where id =?", arrayOf(id))

        // 사용자가 존재하지 않으면 false 반환
        if (cursor.count <= 0) res = false

        // 결과 반환
        return res
    }

    // 해당 id, password가 있는지 확인 (없다면 false)
    fun checkUserpass(id: String, password: String) : Boolean {
        // 데이터베이스에 쓰기 권한을 요청
        val MyDB = this.writableDatabase

        // 사용자 정보와 비밀번호가 일치하는지 확인
        var res = true
        val cursor = MyDB.rawQuery(
            "Select * from users where id = ? and password = ?",
            arrayOf(id, password)
        )

        // 일치하는 데이터가 없으면 false 반환
        if (cursor.count <= 0) res = false

        // 결과 반환
        return res
    }

    fun getUserInfo(id: String?): Pair<String?, String?>? {
        // 데이터베이스를 읽기 모드로 열기
        val MyDB = this.readableDatabase

        // 사용자 ID로 'users' 테이블에서 id와 nick(닉네임)을 조회하는 쿼리 실행
        val cursor = MyDB.rawQuery("Select id, nick from users where id = ?", arrayOf(id))

        // 조회된 결과가 있으면
        return if (cursor.moveToFirst()) {
            val userId = cursor.getString(0) // 첫 번째 컬럼(id)의 값을 가져옴
            val userNick = cursor.getString(1) // 두 번째 컬럼(nick)의 값을 가져옴
            cursor.close() // 커서 닫기
            Pair(userId, userNick) // 조회된 id와 nick을 Pair로 반환
        } else {
            cursor.close()
            null // null 반환 (사용자 정보 없음)
        }
    }


    // DB name을 Login.db로 설정
    companion object {
        const val DBNAME = "Login.db"  // 데이터베이스 이름 설정
    }
}
