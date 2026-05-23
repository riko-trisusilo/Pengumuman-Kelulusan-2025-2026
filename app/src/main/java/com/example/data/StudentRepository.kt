package com.example.data

import kotlinx.coroutines.flow.Flow

class StudentRepository(private val studentDao: StudentDao) {
    val allStudents: Flow<List<Student>> = studentDao.getAllStudents()
    val appConfig: Flow<AppConfig?> = studentDao.getConfigFlow()

    suspend fun checkGraduation(examNumber: String, nisn: String): Student? {
        return studentDao.checkGraduation(examNumber, nisn)
    }

    suspend fun findStudentByQuery(query: String): Student? {
        return studentDao.findStudentByQuery(query)
    }

    fun searchStudents(query: String): Flow<List<Student>> {
        return studentDao.searchStudents(query)
    }

    suspend fun insertStudent(student: Student) {
        studentDao.insertStudent(student)
    }

    suspend fun insertStudents(students: List<Student>) {
        studentDao.insertStudents(students)
    }

    suspend fun updateStudent(student: Student) {
        studentDao.updateStudent(student)
    }

    suspend fun deleteStudent(student: Student) {
        studentDao.deleteStudent(student)
    }

    suspend fun deleteAllStudents() {
        studentDao.deleteAllStudents()
    }

    suspend fun getConfig(): AppConfig? {
        return studentDao.getConfig()
    }

    suspend fun saveConfig(config: AppConfig) {
        studentDao.saveConfig(config)
    }
}
