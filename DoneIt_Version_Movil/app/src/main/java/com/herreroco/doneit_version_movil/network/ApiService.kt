package com.herreroco.doneit_version_movil.network

import com.herreroco.doneit_version_movil.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // 🔐 Login y Registro
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("api/auth/registro")
    suspend fun registrarse(@Body request: RegistroRequest): Response<Void>

    // 👤 Perfil del usuario autenticado
    @GET("api/auth/perfil")
    suspend fun getMiPerfil(): Response<Usuario>

    // 📁 Proyectos (todos protegidos con token)
    @GET("api/proyectos")
    suspend fun getProyectos(): Response<List<Proyecto>>

    @POST("api/proyectos")
    suspend fun crearProyecto(@Body proyecto: ProyectoRequest): Response<CrearProyectoResponse>

    @GET("api/proyectos/{id}")
    suspend fun getProyectoPorId(@Path("id") id: Int): Response<Proyecto>

    @PUT("api/proyectos/{id}")
    suspend fun editarProyecto(@Path("id") id: Int, @Body proyecto: ProyectoRequest): Response<Void>

    @DELETE("api/proyectos/{id}")
    suspend fun eliminarProyecto(@Path("id") id: Int): Response<Void>

    // ✅ Tareas (por proyecto)
    @POST("api/tareas")
    suspend fun crearTarea(@Body tarea: TareaRequest): Response<CrearTareaResponse>

    @GET("api/tareas/proyecto/{idProyecto}")
    suspend fun getTareasDeProyecto(@Path("idProyecto") idProyecto: Int): Response<List<Tarea>>

    @PUT("api/tareas/{id}")
    suspend fun editarTarea(@Path("id") id: Int, @Body request: EditarTareaRequest): Response<Unit>

    @DELETE("api/tareas/{id}")
    suspend fun eliminarTarea(@Path("id") id: Int): Response<Void>
}
