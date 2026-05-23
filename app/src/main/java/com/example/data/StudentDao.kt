package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Int): Student?

    @Query("SELECT * FROM students WHERE examNumber = :examNumber OR nisn = :nisn LIMIT 1")
    suspend fun checkGraduation(examNumber: String, nisn: String): Student?

    @Query("SELECT * FROM students WHERE examNumber = :query OR examNumber LIKE '%' || :query || '%' OR nisn = :query OR name LIKE '%' || :query || '%' LIMIT 1")
    suspend fun findStudentByQuery(query: String): Student?

    @Query("SELECT * FROM students WHERE examNumber LIKE '%' || :query || '%' OR nisn LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%'")
    fun searchStudents(query: String): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students")
    suspend fun deleteAllStudents()

    // Configuration queries
    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<AppConfig?>

    @Query("SELECT * FROM app_config WHERE id = 1 LIMIT 1")
    suspend fun getConfig(): AppConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: AppConfig)
}
