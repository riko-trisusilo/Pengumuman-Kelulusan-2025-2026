package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Student::class, AppConfig::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "graduation_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDb(database.studentDao())
                }
            }
        }

        suspend fun populateDb(studentDao: StudentDao) {
            // Save Default Configuration
            studentDao.saveConfig(AppConfig())

            // Pre-populate with realistic students for SDN 008 Samarinda Ulu 2025/2026
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
            studentDao.insertStudents(initialStudents)
        }
    }
}
