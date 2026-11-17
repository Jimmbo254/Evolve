package com.evolve.app.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface EvolveApi {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterBody): Response<AuthSession>

    @POST("auth/login")
    suspend fun login(@Body body: LoginBody): Response<AuthSession>

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshBody): Response<AuthSession>

    @POST("auth/logout")
    suspend fun logout(@Body body: LogoutBody): Response<Unit>

    @GET("profile")
    suspend fun profile(): Response<ApiData<UserProfile>>

    @PATCH("profile")
    suspend fun updateProfile(@Body body: ProfileUpdateBody): Response<ApiData<UserProfile>>

    @GET("plans")
    suspend fun plans(): Response<ApiData<List<PlanSummary>>>

    @GET("plans/{id}")
    suspend fun plan(@Path("id") id: String): Response<ApiData<PlanDetail>>

    @POST("plans")
    suspend fun createPlan(@Body body: PlanBody): Response<ApiData<PlanDetail>>

    @PUT("plans/{id}")
    suspend fun updatePlan(@Path("id") id: String, @Body body: PlanBody): Response<ApiData<PlanDetail>>

    @DELETE("plans/{id}")
    suspend fun deletePlan(@Path("id") id: String): Response<Unit>

    @POST("plans/{id}/activate")
    suspend fun activatePlan(@Path("id") id: String): Response<ApiData<PlanDetail>>

    @GET("meta")
    suspend fun meta(): Response<ApiData<Meta>>

    @GET("exercises")
    suspend fun exercises(
        @Query("search") search: String?,
        @Query("target") target: String?,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<ExercisePage>

    @GET("exercises/{id}")
    suspend fun exercise(
        @Path("id") id: String,
        @Query("lang") lang: String
    ): Response<ApiData<ExerciseDetail>>

    @GET("workouts/today")
    suspend fun today(): Response<TodayResponse>

    @POST("workouts")
    suspend fun startWorkout(@Body body: StartWorkoutBody): Response<ApiData<Workout>>

    @GET("workouts/{id}")
    suspend fun workout(@Path("id") id: String): Response<ApiData<WorkoutDetail>>

    @POST("workouts/{id}/sets")
    suspend fun addSet(
        @Path("id") id: String,
        @Body body: WorkoutSetBody
    ): Response<ApiData<WorkoutSet>>

    @POST("workouts/{id}/finish")
    suspend fun finishWorkout(@Path("id") id: String): Response<ApiData<Workout>>

    @GET("progress/summary")
    suspend fun progressSummary(): Response<ApiData<ProgressSummary>>

    @GET("progress/volume")
    suspend fun weeklyVolume(): Response<ApiData<List<VolumeDay>>>

    @GET("progress/records")
    suspend fun records(@Query("limit") limit: Int): Response<ApiData<List<RecordItem>>>
}
