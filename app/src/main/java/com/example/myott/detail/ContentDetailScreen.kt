package com.example.myott.detail

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.myott.API.MovieDetail
import com.example.myott.API.Provider
import com.example.myott.AppContainer
import java.util.Locale

// 색상 (메인 화면과 같은 다크 + 보라 계열)
private val DetailBackground = Color(0xFF121212)
private val DetailCard = Color(0xFF202020)
private val DetailPurple = Color(0xFF9363EB)
private val DetailText = Color.White
private val DetailSubText = Color(0xFFAAAAAA)
private val DetailBorder = Color(0xFF333333)

private const val TMDB_IMAGE_URL = "https://image.tmdb.org/t/p/"

// 콘텐츠 상세 화면 — 메인 화면에서 포스터를 누르면 열린다.
@Composable
fun ContentDetailScreen(
    movieId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val app = context.applicationContext as AppContainer
    val viewModel: ContentDetailViewModel = viewModel(
        key = "detail_$movieId", // 콘텐츠마다 따로 만들기
        factory = ContentDetailViewModel.factory(movieId, app.contentScoreRepository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val record by viewModel.record.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DetailBackground)
    ) {
        val detail = uiState.detail
        when {
            uiState.loading -> {
                CircularProgressIndicator(
                    color = DetailPurple,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            detail == null -> {
                ErrorContent(
                    message = uiState.error ?: "정보를 불러오지 못했어요.",
                    onRetry = viewModel::loadDetail,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                DetailContent(
                    detail = detail,
                    isInterested = record?.isInterested == true,
                    isNotInterested = record?.isNotInterested == true,
                    onInterestClick = viewModel::onInterestClick,
                    onNotInterestedClick = viewModel::onNotInterestedClick,
                    onOttClick = { provider, url ->
                        viewModel.onOttClick(provider)
                        openUrl(context, url)
                    }
                )
            }
        }

        // 왼쪽 위 뒤로가기 버튼 (항상 표시)
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(8.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기", tint = Color.White)
        }
    }
}

// 상세 정보 본문
@Composable
private fun DetailContent(
    detail: MovieDetail,
    isInterested: Boolean,
    isNotInterested: Boolean,
    onInterestClick: () -> Unit,
    onNotInterestedClick: () -> Unit,
    onOttClick: (Provider, String) -> Unit,
) {
    var overviewExpanded by rememberSaveable { mutableStateOf(false) }
    val title = detail.title ?: detail.original_title.orEmpty()
    val director = detail.credits?.crew?.firstOrNull { it.job == "Director" }?.name
    val cast = detail.credits?.cast.orEmpty().sortedBy { it.order ?: Int.MAX_VALUE }.take(5).mapNotNull { it.name }
    val providers = koreanProviders(detail)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ----- 1. 상단 이미지 (배경 이미지가 없으면 포스터) -----
        val imagePath = detail.backdrop_path ?: detail.poster_path
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .background(DetailCard)
        ) {
            if (imagePath != null) {
                Image(
                    painter = rememberAsyncImagePainter("${TMDB_IMAGE_URL}w780$imagePath"),
                    contentDescription = "$title 이미지",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // 아래쪽을 배경색으로 자연스럽게 이어지게 하는 그라데이션
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Transparent, DetailBackground)
                        )
                    )
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            // ----- 2. 원제 · 제목 · 정보 줄 -----
            val originalTitle = detail.original_title
            if (!originalTitle.isNullOrBlank() && originalTitle != title) {
                Text(
                    text = originalTitle.uppercase(Locale.ROOT),
                    color = DetailSubText,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(
                text = title,
                color = DetailText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            InfoRow(detail)

            // ----- 3. 개요 (3줄까지, 더 보기) -----
            val overview = detail.overview
            if (!overview.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = overview,
                    color = DetailText,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    maxLines = if (overviewExpanded) Int.MAX_VALUE else 3,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable { overviewExpanded = !overviewExpanded },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (overviewExpanded) "접기" else "더 보기",
                        color = DetailPurple,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (overviewExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = DetailPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // ----- 4. 감독 · 출연 -----
            Spacer(modifier = Modifier.height(12.dp))
            if (director != null) PeopleRow(label = "감독", value = director)
            if (cast.isNotEmpty()) PeopleRow(label = "출연", value = cast.joinToString(" · "))

            // ----- 5. 관심 있어요 / 관심 없어요 -----
            Spacer(modifier = Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ChoiceButton(
                    text = "관심 있어요",
                    icon = if (isInterested) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    selected = isInterested,
                    onClick = onInterestClick,
                    modifier = Modifier.weight(1f)
                )
                ChoiceButton(
                    text = "관심 없어요",
                    icon = Icons.Filled.Close,
                    selected = isNotInterested,
                    onClick = onNotInterestedClick,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "취향을 알려주시면 다음 추천이 더 정확해져요.",
                color = DetailSubText,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // ----- 6. 어디서 볼 수 있나요? -----
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "어디서 볼 수 있나요?",
                color = DetailText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (providers.isEmpty()) {
                Text(
                    text = "국내 OTT 시청 정보가 없어요.",
                    color = DetailSubText,
                    fontSize = 14.sp
                )
            } else {
                val tmdbLink = detail.watchProviders?.results?.get("KR")?.link
                providers.forEach { (provider, label) ->
                    ProviderRow(
                        provider = provider,
                        label = label,
                        onClick = { onOttClick(provider, ottUrl(provider, title, tmdbLink)) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// 개봉 연도 · 장르 · 관람 등급 · 상영 시간 · 평점 한 줄
@Composable
private fun InfoRow(detail: MovieDetail) {
    val year = detail.release_date?.take(4)?.takeIf { it.isNotBlank() }
    val genres = detail.genres.orEmpty().mapNotNull { it.name }.take(3).joinToString(" / ")
    val certification = koreanCertification(detail)
    val runtime = detail.runtime?.takeIf { it > 0 }?.let { "${it}분" }
    val rating = detail.vote_average?.takeIf { it > 0 }?.let { String.format(Locale.ROOT, "%.1f", it / 2) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        val text = listOfNotNull(year, genres.ifBlank { null }).joinToString(" · ")
        if (text.isNotEmpty()) {
            Text(text = text, color = DetailSubText, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(8.dp))
        }
        if (certification != null) {
            Text(
                text = certification,
                color = DetailSubText,
                fontSize = 11.sp,
                modifier = Modifier
                    .border(1.dp, DetailSubText, RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        if (runtime != null) {
            Text(text = runtime, color = DetailSubText, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(8.dp))
        }
        if (rating != null) {
            Icon(Icons.Filled.Star, contentDescription = "평점", tint = DetailPurple, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(2.dp))
            Text(text = rating, color = DetailText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = " / 5", color = DetailSubText, fontSize = 13.sp)
        }
    }
}

// "감독  한지우" 같은 한 줄
@Composable
private fun PeopleRow(label: String, value: String) {
    Row(modifier = Modifier.padding(vertical = 2.dp)) {
        Text(text = label, color = DetailSubText, fontSize = 13.sp, modifier = Modifier.width(36.dp))
        Text(text = value, color = DetailText, fontSize = 13.sp)
    }
}

// 관심 있어요 / 관심 없어요 버튼 (선택되면 보라색 테두리)
@Composable
private fun ChoiceButton(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .background(if (selected) DetailPurple.copy(alpha = 0.2f) else DetailCard)
            .border(1.dp, if (selected) DetailPurple else DetailBorder, shape)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) DetailPurple else DetailText,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = DetailText,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// OTT 한 줄: 로고 · 이름 · (구독/대여·구매) · 화살표
@Composable
private fun ProviderRow(provider: Provider, label: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(DetailCard)
            .border(1.dp, DetailBorder, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
        ) {
            if (provider.logo_path != null) {
                Image(
                    painter = rememberAsyncImagePainter("${TMDB_IMAGE_URL}w92${provider.logo_path}"),
                    contentDescription = provider.provider_name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = provider.provider_name.orEmpty(),
                color = DetailText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Text(text = label, color = DetailSubText, fontSize = 12.sp)
        }
        Text(text = "보러가기", color = DetailPurple, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = DetailPurple
        )
    }
}

// 불러오기 실패 시
@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, color = DetailSubText, fontSize = 14.sp, textAlign = TextAlign.Center)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = DetailPurple)
        ) {
            Text("다시 시도")
        }
    }
}

// ---------------- 도우미 함수 ----------------

// 한국(KR) OTT 목록: 구독 → 대여 → 구매 순, 같은 서비스는 한 번만
private fun koreanProviders(detail: MovieDetail): List<Pair<Provider, String>> {
    val kr = detail.watchProviders?.results?.get("KR") ?: return emptyList()
    val result = mutableListOf<Pair<Provider, String>>()
    kr.flatrate.orEmpty().forEach { result += it to "구독" }
    kr.rent.orEmpty().forEach { result += it to "대여" }
    kr.buy.orEmpty().forEach { result += it to "구매" }
    return result.distinctBy { it.first.provider_id }
}

// 한국 관람 등급 (예: "15" → "15+", "All" → "ALL")
private fun koreanCertification(detail: MovieDetail): String? {
    val cert = detail.release_dates?.results
        ?.firstOrNull { it.iso_3166_1 == "KR" }
        ?.release_dates
        ?.mapNotNull { it.certification }
        ?.firstOrNull { it.isNotBlank() }
        ?: return null
    return when {
        cert.equals("All", ignoreCase = true) -> "ALL"
        cert.all { it.isDigit() } -> "$cert+"
        else -> cert
    }
}

// OTT 이름으로 이동할 주소 정하기 (모르는 서비스는 TMDB 시청 정보 페이지로)
private fun ottUrl(provider: Provider, title: String, tmdbLink: String?): String {
    val name = provider.provider_name.orEmpty().lowercase(Locale.ROOT)
    val query = Uri.encode(title)
    return when {
        "netflix" in name -> "https://www.netflix.com/search?q=$query"
        "watcha" in name -> "https://watcha.com/search?query=$query"
        "disney" in name -> "https://www.disneyplus.com"
        "wavve" in name -> "https://www.wavve.com"
        "tving" in name -> "https://www.tving.com"
        "coupang" in name -> "https://www.coupangplay.com"
        "apple" in name -> "https://tv.apple.com"
        "google" in name -> "https://play.google.com/store/search?q=$query&c=movies"
        else -> tmdbLink ?: "https://www.themoviedb.org"
    }
}

// 브라우저/앱으로 링크 열기
private fun openUrl(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "링크를 열 수 있는 앱이 없어요", Toast.LENGTH_SHORT).show()
    }
}
