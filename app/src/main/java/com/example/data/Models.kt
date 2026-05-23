package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val examNumber: String,
    val nisn: String,
    val name: String,
    val birthPlace: String,
    val birthDate: String,
    val status: String,     // "LULUS" or "DITANGGUHKAN"
    val indonesian: Double = 0.0,
    val mathematics: Double = 0.0,
    val science: Double = 0.0,
    val socialStudies: Double = 0.0,
    val civics: Double = 0.0,
    val religion: Double = 0.0,
    val localContent: Double = 0.0,
    val sports: Double = 0.0,
    val art: Double = 0.0,
    val averageScore: Double = 0.0,
    val notes: String = ""
)

@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey val id: Int = 1,
    val isAnnouncementPublished: Boolean = true,
    val schoolName: String = "SDN 008 Samarinda Ulu",
    val academicYear: String = "2025/2026",
    val announcementDate: String = "15 Juni 2026",
    val headmasterName: String = "Hj. Ratnawati, S.Pd., M.Pd.",
    val headmasterNip: String = "19720412 199603 2 004"
)
