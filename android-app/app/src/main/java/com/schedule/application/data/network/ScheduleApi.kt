package com.schedule.application.data.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ScheduleApi {
    @GET("api/v1/groups")
    suspend fun getGroups(
        @Header("X-Read-Key") readKey: String,
    ): List<String>

    @GET("api/v1/schedule/week")
    suspend fun getWeekSchedule(
        @Header("X-Read-Key") readKey: String,
        @Query("group_name") groupName: String,
    ): WeekScheduleDto

    @GET("api/v1/schedule/day")
    suspend fun getDaySchedule(
        @Header("X-Read-Key") readKey: String,
        @Query("group_name") groupName: String,
        @Query("day_of_week") dayOfWeek: Int,
    ): DayScheduleDto

    @POST("api/v1/proposals")
    suspend fun createProposal(
        @Header("X-Submit-Key") submitKey: String,
        @Body request: ProposalCreateRequest,
    ): ProposalReadDto

    @POST("api/v1/admin/login")
    suspend fun adminLogin(
        @Body request: AdminLoginRequest,
    ): TokenResponse

    @GET("api/v1/admin/schedules")
    suspend fun listAdminSchedules(
        @Header("Authorization") bearerToken: String,
        @Query("group_name") groupName: String? = null,
    ): List<ScheduleEntryDto>

    @POST("api/v1/admin/schedules/upsert")
    suspend fun upsertSchedule(
        @Header("Authorization") bearerToken: String,
        @Body request: ScheduleUpsertRequest,
    ): ScheduleEntryDto

    @PUT("api/v1/admin/schedules/{entry_id}")
    suspend fun updateSchedule(
        @Header("Authorization") bearerToken: String,
        @Path("entry_id") entryId: Int,
        @Body request: ScheduleUpdateRequest,
    ): ScheduleEntryDto

    @DELETE("api/v1/admin/schedules/{entry_id}")
    suspend fun deleteSchedule(
        @Header("Authorization") bearerToken: String,
        @Path("entry_id") entryId: Int,
    )
}
