package com.example.genreapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preferred_genres")
data class PreferredGenre(
    @PrimaryKey
    val genreId: String,       // 장르 식별자 (예: "ACTION", "ROMANCE", "SF")
    val genreName: String,     // 사용자에게 표시될 이름 (예: "액션", "로맨스")
    val isSelected: Boolean,   // 선택 여부
)
