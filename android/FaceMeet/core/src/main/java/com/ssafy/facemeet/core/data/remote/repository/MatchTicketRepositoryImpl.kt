// core/data/repository/MatchTicketRepositoryImpl.kt
package com.ssafy.facemeet.core.data.remote.repository

import com.google.gson.Gson
import com.ssafy.facemeet.core.data.remote.datasource.MatchTicketRemoteDataSource
import com.ssafy.facemeet.core.data.remote.dto.response.TakeMatchTicketResponse
import com.ssafy.facemeet.core.domain.model.TakeMatchTicket
import com.ssafy.facemeet.core.domain.repository.MatchTicketRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class MatchTicketRepositoryImpl @Inject constructor(
    private val remote: MatchTicketRemoteDataSource,
    private val gson: Gson                 // ✅ 주입
) : MatchTicketRepository {

    override suspend fun take(settingId: Long): Result<TakeMatchTicket> {
        return try {
            val r = remote.take(settingId)
            if (!r.success) {
                return Result.failure(Exception(r.message ?: "티켓 획득 실패"))
            }
            Result.success(
                TakeMatchTicket(
                    settingId = r.settingId,
                    acquiredAt = r.acquiredAt,
                    remainingCount = r.remainingCount ?: 0,
                    message = r.message,
                    success = r.success
                )
            )
        } catch (e: HttpException) {
            // ✅ 4xx/5xx에서 서버 메시지 꺼내기
            val msg = e.response()?.errorBody()?.string()?.let { body ->
                runCatching { gson.fromJson(body, TakeMatchTicketResponse::class.java).message }
                    .getOrNull()
            } ?: "요청을 처리할 수 없습니다. (${e.code()})"
            Result.failure(Exception(msg))
        } catch (e: IOException) {
            Result.failure(Exception("네트워크 연결을 확인해주세요."))
        } catch (e: Exception) {
            Result.failure(Exception(e.message ?: "알 수 없는 오류가 발생했어요."))
        }
    }
}
