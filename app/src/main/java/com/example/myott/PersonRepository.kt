
package com.example.myott

import com.example.myott.API.Credits
import kotlinx.coroutines.flow.Flow

class PersonRepository(
    private val personDao: PersonDao
) {

    // 최대 저장 인원
    private val MAX_DIRECTORS = 3
    private val MAX_ACTORS = 5

    // 감독 목록 실시간 관찰
    val directors: Flow<List<PersonRecord>> =
        personDao.observeDirectors()

    // 배우 목록 실시간 관찰
    val actors: Flow<List<PersonRecord>> =
        personDao.observeActors()

    // 상세페이지의 제작진 정보에서 감독 등록
    suspend fun registerDirectors(credits: Credits?) {

        // 제작진 정보가 없으면 종료
        if (credits?.crew == null) return

        // 담당 직업이 Director인 사람만 필터링
        val directorList = credits.crew.filter {
            it.job == "Director" && !it.name.isNullOrBlank()
        }

        // 감독 목록 순서대로 저장
        for (director in directorList) {

            // 인물 ID와 이름이 존재하는지 확인
            val personId = director.id
            val personName = director.name ?: continue

            // 감독 저장
            registerPerson(
                personId = personId,
                personName = personName,
                personType = "DIRECTOR",
                maxCount = MAX_DIRECTORS
            )
        }
    }

    // 상세페이지의 출연진 정보에서 배우 등록
    suspend fun registerActors(credits: Credits?) {

        // 출연진 정보가 없으면 종료
        if (credits?.cast == null) return

        // 출연 순서(order)가 빠른 배우 5명 선택
        val actorList = credits.cast
            .sortedBy { it.order ?: Int.MAX_VALUE }
            .take(MAX_ACTORS)

        // 배우 목록 순서대로 저장
        for (actor in actorList) {

            // 이름이 없는 배우는 제외
            val personName = actor.name ?: continue

            // 배우 저장
            registerPerson(
                personId = actor.id,
                personName = personName,
                personType = "ACTOR",
                maxCount = MAX_ACTORS
            )
        }
    }

    // 감독과 배우 모두 등록
    suspend fun registerPeople(credits: Credits?) {

        // 감독 등록
        registerDirectors(credits)

        // 배우 등록
        registerActors(credits)
    }

    // 공통 인물 저장 기능
    private suspend fun registerPerson(
        personId: Int,
        personName: String,
        personType: String,
        maxCount: Int
    ) {

        // 이미 등록된 인물인지 확인
        val existingPerson = personDao.getPerson(
            personId = personId,
            personType = personType
        )

        // 이미 등록되어 있으면 중복 저장하지 않음
        if (existingPerson != null) return

        // 현재 등록된 인원 확인
        val currentCount = personDao.getPersonCount(personType)

        // 인원 제한을 초과하는 경우 가장 오래된 사람 삭제
        if (currentCount >= maxCount) {
            personDao.deleteOldestPerson(personType)
        }

        // 새로운 인물 저장
        personDao.insertPerson(
            PersonRecord(
                personId = personId,
                personName = personName,
                personType = personType
            )
        )
    }
}
