package com.example.battlerunner.ui.mypage

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.battlerunner.R
import com.example.battlerunner.data.model.BattleRecord

class BattleRecordAdapter(
    private val records: List<BattleRecord>, // 배틀 기록 리스트
    private val onRecordClick: (BattleRecord) -> Unit // 아이템 클릭 시 실행할 콜백 함수
) : RecyclerView.Adapter<BattleRecordAdapter.BattleRecordViewHolder>() {

    inner class BattleRecordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dateText: TextView = view.findViewById(R.id.recordDate) // 배틀이 종료된 날짜 표시
        val opponentNameText: TextView = view.findViewById(R.id.recordOpponentName) // 배틀 상대 이름 표시

        fun bind(record: BattleRecord) {
            dateText.text = record.date
            opponentNameText.text = record.opponentName
            itemView.setOnClickListener { onRecordClick(record) } // 클릭 리스너 설정
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BattleRecordViewHolder {
        // 아이템 레이아웃을 inflate하여 ViewHolder 생성
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_battle_record, parent, false)
        return BattleRecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: BattleRecordViewHolder, position: Int) {
        // 해당 위치의 배틀 기록 데이터를 ViewHolder에 바인딩
        holder.bind(records[position])
    }

    override fun getItemCount(): Int = records.size // 전체 아이템 수 반환
}