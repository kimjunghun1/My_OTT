package com.example.genreapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 앱에서 회원이 등록한 정보를 저장하거나 불러올 수 있도록 데이터베이스를 만들고 관리
@Database(
    entities = [
        PreferredGenre::class, // 이 클래스는 PreferredGenre라는 데이터를 저장할 것이라는 뜻
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun genreDao(): GenreDao // GenreDao를 가져오는 함수

    companion object { // 클래스 객체를 생성하지 않아도 내부의 변수나 함수를 사용할 수 있는 영역
        @Volatile // 여러 스레드가 동시에 INSTANCE 사용 가능
        private var INSTANCE: AppDatabase? = null // 데이터베이스 객체를 보관하는 변수

        fun getDatabase(context: Context): AppDatabase { // 데이터베이스를 생성하는 함수
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "genre_database"
                ).build() // 데이터베이스 생성
                INSTANCE = instance
                instance // 새로 만든 데이터베이스 객체를 INSTANCE에 저장하고 반환
            }
        }
    }
}
