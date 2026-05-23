package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppConfig
import com.example.data.Student
import com.example.data.StudentRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class GraduationViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StudentRepository
    
    val allStudents: StateFlow<List<Student>>
    val appConfig: StateFlow<AppConfig>

    // Student Lookup State
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResult = MutableStateFlow<SearchResultState>(SearchResultState.Idle)
    val searchResult = _searchResult.asStateFlow()

    // Teacher View Search Query
    private val _teacherSearchQuery = MutableStateFlow("")
    val teacherSearchQuery = _teacherSearchQuery.asStateFlow()

    val teacherStudentsList: StateFlow<List<Student>>

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = StudentRepository(database.studentDao())

        allStudents = repository.allStudents
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

        appConfig = repository.appConfig
            .map { it ?: AppConfig() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = AppConfig()
            )

        teacherStudentsList = _teacherSearchQuery
            .debounce(200)
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    repository.allStudents
                } else {
                    repository.searchStudents(query)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            _searchResult.value = SearchResultState.Idle
        }
    }

    fun updateTeacherSearchQuery(query: String) {
        _teacherSearchQuery.value = query
    }

    fun performSearch() {
        val query = _searchQuery.value.trim()
        if (query.isBlank()) {
            _searchResult.value = SearchResultState.Error("Mohon masukkan NISN, Nomor Ujian, atau Nama Lengkap.")
            return
        }

        _searchResult.value = SearchResultState.Loading

        viewModelScope.launch {
            try {
                val config = repository.getConfig() ?: AppConfig()
                if (!config.isAnnouncementPublished) {
                    _searchResult.value = SearchResultState.NotPublished
                    return@launch
                }

                val student = repository.findStudentByQuery(query)
                if (student != null) {
                    _searchResult.value = SearchResultState.Success(student)
                } else {
                    _searchResult.value = SearchResultState.NotFound
                }
            } catch (e: Exception) {
                _searchResult.value = SearchResultState.Error(e.localizedMessage ?: "Terjadi kesalahan sistem.")
            }
        }
    }

    fun resetSearch() {
        _searchQuery.value = ""
        _searchResult.value = SearchResultState.Idle
    }

    fun toggleAnnouncement(published: Boolean) {
        viewModelScope.launch {
            val currentConfig = repository.getConfig() ?: AppConfig()
            repository.saveConfig(currentConfig.copy(isAnnouncementPublished = published))
        }
    }

    fun updateConfig(config: AppConfig) {
        viewModelScope.launch {
            repository.saveConfig(config)
        }
    }

    fun addOrUpdateStudent(student: Student) {
        viewModelScope.launch {
            val total = student.indonesian + student.mathematics + student.science +
                    student.socialStudies + student.civics + student.religion +
                    student.localContent + student.sports + student.art
            val average = total / 9.0
            val updatedStudent = student.copy(averageScore = Math.round(average * 10.0) / 10.0)
            repository.insertStudent(updatedStudent)
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
        }
    }

    fun resetAllDataToDefault() {
        viewModelScope.launch {
            repository.deleteAllStudents()
            val initialStudents = listOf(
                Student(
                    examNumber = "06-008-001",
                    nisn = "0134567890",
                    name = "Muhammad Rizky Pratama",
                    birthPlace = "Samarinda",
                    birthDate = "12 Maret 2014",
                    status = "LULUS",
                    indonesian = 85.0,
                    mathematics = 80.0,
                    science = 88.0,
                    socialStudies = 84.0,
                    civics = 86.0,
                    religion = 88.0,
                    localContent = 82.0,
                    sports = 90.0,
                    art = 85.0,
                    averageScore = 85.3,
                    notes = "Selamat atas kelulusannya! Tingkatkan prestasi di jenjang SMP/MTS."
                ),
                Student(
                    examNumber = "06-008-002",
                    nisn = "0134567891",
                    name = "Siti Aminah Zahra",
                    birthPlace = "Samarinda",
                    birthDate = "15 April 2014",
                    status = "LULUS",
                    indonesian = 92.0,
                    mathematics = 88.0,
                    science = 94.0,
                    socialStudies = 90.0,
                    civics = 92.0,
                    religion = 95.0,
                    localContent = 89.0,
                    sports = 85.0,
                    art = 90.0,
                    averageScore = 90.5,
                    notes = "Sangat luar biasa! Lulusan terbaik kelas VI. Pertahankan belajarmu!"
                ),
                Student(
                    examNumber = "06-008-003",
                    nisn = "0134567892",
                    name = "Ahmad Fauzi",
                    birthPlace = "Samarinda",
                    birthDate = "22 Mei 2014",
                    status = "LULUS",
                    indonesian = 78.0,
                    mathematics = 75.0,
                    science = 80.0,
                    socialStudies = 77.0,
                    civics = 79.0,
                    religion = 82.0,
                    localContent = 76.0,
                    sports = 85.0,
                    art = 80.0,
                    averageScore = 79.1,
                    notes = "Lulus. Semangat terus belajarnya di jenjang yang lebih tinggi!"
                ),
                Student(
                    examNumber = "06-008-004",
                    nisn = "0134567893",
                    name = "Putri Lestari Ningrum",
                    birthPlace = "Balikpapan",
                    birthDate = "04 Juni 2014",
                    status = "LULUS",
                    indonesian = 91.0,
                    mathematics = 95.0,
                    science = 92.0,
                    socialStudies = 88.0,
                    civics = 90.0,
                    religion = 92.0,
                    localContent = 87.0,
                    sports = 84.0,
                    art = 89.0,
                    averageScore = 89.8,
                    notes = "Lulus dengan hasil yang sangat baik. Semoga sukses selalu!"
                ),
                Student(
                    examNumber = "06-008-005",
                    nisn = "0134567894",
                    name = "Budi Santoso",
                    birthPlace = "Samarinda",
                    birthDate = "19 Juli 2014",
                    status = "LULUS",
                    indonesian = 80.0,
                    mathematics = 70.0,
                    science = 76.0,
                    socialStudies = 74.0,
                    civics = 75.0,
                    religion = 80.0,
                    localContent = 72.0,
                    sports = 88.0,
                    art = 78.0,
                    averageScore = 77.0,
                    notes = "Lulus. Tingkatkan belajarmu dan ibadahmu, Nak."
                ),
                Student(
                    examNumber = "06-008-006",
                    nisn = "0134567895",
                    name = "Ryan Hidayatullah",
                    birthPlace = "Samarinda",
                    birthDate = "30 Agustus 2014",
                    status = "LULUS",
                    indonesian = 88.0,
                    mathematics = 82.0,
                    science = 85.0,
                    socialStudies = 80.0,
                    civics = 82.0,
                    religion = 86.0,
                    localContent = 80.0,
                    sports = 92.0,
                    art = 83.0,
                    averageScore = 84.2,
                    notes = "Selamat atas kelulusannya. Tetap rajin belajar di SMP."
                ),
                Student(
                    examNumber = "06-008-007",
                    nisn = "0134567896",
                    name = "Sarah Amelia",
                    birthPlace = "Tenggarong",
                    birthDate = "11 September 2014",
                    status = "LULUS",
                    indonesian = 86.0,
                    mathematics = 88.0,
                    science = 90.0,
                    socialStudies = 84.0,
                    civics = 85.0,
                    religion = 90.0,
                    localContent = 82.0,
                    sports = 80.0,
                    art = 88.0,
                    averageScore = 85.9,
                    notes = "Lulus dengan membanggakan. Terus asah bakat dan potensimu."
                ),
                Student(
                    examNumber = "06-008-008",
                    nisn = "0134567897",
                    name = "Andi Wijaya Kusuma",
                    birthPlace = "Samarinda",
                    birthDate = "05 Oktober 2014",
                    status = "LULUS",
                    indonesian = 75.0,
                    mathematics = 78.0,
                    science = 73.0,
                    socialStudies = 72.0,
                    civics = 75.0,
                    religion = 80.0,
                    localContent = 70.0,
                    sports = 85.0,
                    art = 77.0,
                    averageScore = 76.1,
                    notes = "Lulus. Terus semangat, jangan mudah putus asa."
                ),
                Student(
                    examNumber = "06-008-009",
                    nisn = "0134567898",
                    name = "Dewa Saputra",
                    birthPlace = "Samarinda",
                    birthDate = "18 November 2014",
                    status = "LULUS",
                    indonesian = 82.0,
                    mathematics = 84.0,
                    science = 80.0,
                    socialStudies = 82.0,
                    civics = 80.0,
                    religion = 84.0,
                    localContent = 78.0,
                    sports = 86.0,
                    art = 80.0,
                    averageScore = 81.8,
                    notes = "Selamat atas kelulusannya. Pertahankan prestasimu di SMP."
                ),
                Student(
                    examNumber = "06-008-010",
                    nisn = "0134567899",
                    name = "Rania Safitri",
                    birthPlace = "Samarinda",
                    birthDate = "29 Desember 2014",
                    status = "DITANGGUHKAN",
                    indonesian = 60.0,
                    mathematics = 55.0,
                    science = 58.0,
                    socialStudies = 58.0,
                    civics = 60.0,
                    religion = 62.0,
                    localContent = 55.0,
                    sports = 78.0,
                    art = 70.0,
                    averageScore = 61.8,
                    notes = "Ditangguhkan. Perlu perbaikan nilai ujian sekolah matematika dan IPA melalui program konseling/remedial sebelum penyerahan SKL resmi."
                )
            )
            repository.insertStudents(initialStudents)
            repository.saveConfig(AppConfig())
        }
    }
}

sealed interface SearchResultState {
    object Idle : SearchResultState
    object Loading : SearchResultState
    object NotFound : SearchResultState
    object NotPublished : SearchResultState
    data class Success(val student: Student) : SearchResultState
    data class Error(val message: String) : SearchResultState
}
