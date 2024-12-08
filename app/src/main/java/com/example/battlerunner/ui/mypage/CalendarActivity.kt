package com.example.battlerunner.ui.mypage

import RunningRecordDecorator
import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.battlerunner.R
import com.example.battlerunner.data.local.DBHelper
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarView: MaterialCalendarView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        // MaterialCalendarView 가져오기
        calendarView = findViewById(R.id.calendarView)

        // DBHelper 인스턴스 생성
        val dbHelper = DBHelper.getInstance(this)

        // DB에서 기록이 있는 날짜 가져오기
        val datesWithRecords = dbHelper.getAllRunningDates() // SQLite에서 날짜 리스트 가져오기

        // 날짜 리스트를 CalendarDay 형태로 변환
        val decoratedDates = datesWithRecords.map {
            // yyyy-MM-dd 형식의 문자열을 Date 객체로 변환
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it)
            // Date 객체를 CalendarDay로 변환
            CalendarDay.from(date)
        }.toSet()

        // MaterialCalendarView에 데코레이터 적용
        calendarView.addDecorator(RunningRecordDecorator(this, decoratedDates))

        // 창닫기 버튼 설정
        val closeBtn = findViewById<ImageButton>(R.id.closeBtn)
        closeBtn.setOnClickListener {
            finish() // 현재 Activity 종료
        }

        // 특정 날짜 클릭 리스너 설정
        calendarView.setOnDateChangedListener(OnDateSelectedListener { _, date, _ ->
            val dateKey = "${date.year}-${date.month + 1}-${date.day}"

            // SQLite에서 해당 날짜의 모든 기록 가져오기
            val records = dbHelper.getRecordsByDate(dateKey)

            if (records.isNotEmpty()) {
                if (records.size > 1) {
                    // 여러 기록이 있을 경우 선택 팝업 표시
                    showRecordsPopup(records)
                } else {
                    // 기록이 하나일 경우 바로 팝업 표시
                    val record = records.first()
                    showPopup(record.first, record.second, record.third)
                }
            } else {
                // 해당 날짜에 기록이 없을 경우
                Toast.makeText(this, "해당 날짜에는 러닝이 없었어요", Toast.LENGTH_SHORT).show()
            }
        })

    }

    // 해당 날짜에 기록이 여러 개일 때 -> 하나를 선택할 수 있는 팝업 표시 메서드
    private fun showRecordsPopup(records: List<Triple<String, Long, Float>>) {
        // 팝업 빌더 생성
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("\n기록을 선택하세요\n\n")

        // 리스트 아이템 생성 (시간 및 거리 표시)
        val items = records.map { record ->
            Log.d("Calendar", "{$record.first}")
            val time = record.first.substringAfterLast("_").substringBefore(".").split("-").let { parts ->
                "${parts[0]}시 ${parts[1]}분" // 시간(HH시 mm분) 형식으로 변환
            }
            "종료 시각: $time, 거리: ${String.format("%.2f", record.third)} m\n"


        }.toTypedArray()

        // 리스트 선택 리스너 설정
        dialogBuilder.setItems(items) { _, which ->
            val selectedRecord = records[which]
            showPopup(selectedRecord.first, selectedRecord.second, selectedRecord.third)
        }

        // 취소 버튼 추가
        dialogBuilder.setNegativeButton("취소", null)

        // 팝업 표시
        dialogBuilder.show()
    }

    // 해당 날짜에 기록이 하나일 때 -> 팝업 표시 메서드
    private fun showPopup(imagePath: String?, elapsedTime: Long, distance: Float) {
        // 팝업 레이아웃 inflate
        val dialogView = LayoutInflater.from(this).inflate(R.layout.popup_running_data, null)

        // 팝업 내부 View 참조
        val popupImage = dialogView.findViewById<ImageView>(R.id.popupRunningImage)
        val popupElapsedTime = dialogView.findViewById<TextView>(R.id.popupElapsedTime)
        val popupDistance = dialogView.findViewById<TextView>(R.id.popupDistance)

        // 이미지 경로를 기반으로 Bitmap 생성
        if (imagePath != null && File(imagePath).exists()) {
            val bitmap = BitmapFactory.decodeFile(imagePath) // 이미지 파일 읽기
            popupImage.setImageBitmap(bitmap) // ImageView에 이미지 설정
            popupElapsedTime.text = "소요 시간: ${elapsedTime / 1000 / 60} min" // 시간 변환
            popupDistance.text = "달린 거리: ${String.format("%.2f", distance)} m" // 거리 표시
        } else {
            // 이미지가 없을 경우 기본 이미지와 메시지 표시
            popupImage.setImageResource(R.drawable.placeholder) // 기본 이미지
            popupElapsedTime.text = "No data available"
            popupDistance.text = ""
        }

        // 팝업 표시
        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()
    }
}
