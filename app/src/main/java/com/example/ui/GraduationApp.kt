package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppConfig
import com.example.data.Student
import com.example.ui.theme.*

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun GraduationApp(
    viewModel: GraduationViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allStudents by viewModel.allStudents.collectAsStateWithLifecycle()
    val appConfig by viewModel.appConfig.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResult by viewModel.searchResult.collectAsStateWithLifecycle()
    val teacherSearchQuery by viewModel.teacherSearchQuery.collectAsStateWithLifecycle()
    val teacherStudents by viewModel.teacherStudentsList.collectAsStateWithLifecycle()

    var activeTab by remember { mutableStateOf(0) } // 0 = Student Portal, 1 = Teacher Console
    var isTeacherUnlocked by remember { mutableStateOf(false) }
    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    // Dialogs
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(EmeraldDark, EmeraldMedium)
                        )
                    )
                    .padding(top = 16.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Graduation Cap Icon",
                        tint = GoldAccent,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            .padding(6.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SDN 008 SAMARINDA ULU",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = GentleWhite,
                                fontSize = 18.sp,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Text(
                            text = "Aplikasi Kelulusan Siswa Kelas 6 • TP 2025/2026",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = GoldLight,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // custom segments tab bar with beautiful ripple effects and MaterialDesign style
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    TabSegmentButton(
                        text = "🏡 Portal Siswa",
                        isSelected = activeTab == 0,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_portal_siswa"),
                        onClick = { activeTab = 0 }
                    )
                    TabSegmentButton(
                        text = "🔒 Konsol Guru",
                        isSelected = activeTab == 1,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tab_konsol_guru"),
                        onClick = {
                            if (isTeacherUnlocked) {
                                activeTab = 1
                            } else {
                                showPinDialog = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(GentleGray)
                .verticalScroll(rememberScrollState())
        ) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn() + slideInHorizontally { if (targetState > initialState) 1000 else -1000 } with
                            fadeOut() + slideOutHorizontally { if (targetState > initialState) -1000 else 1000 }
                },
                label = "TabTransition"
            ) { tab ->
                when (tab) {
                    0 -> StudentPortalView(
                        searchQuery = searchQuery,
                        searchResult = searchResult,
                        appConfig = appConfig,
                        onQueryChange = { viewModel.updateSearchQuery(it) },
                        onSearch = { viewModel.performSearch() },
                        onReset = { viewModel.resetSearch() }
                    )
                    1 -> TeacherConsoleView(
                        allStudents = teacherStudents,
                        appConfig = appConfig,
                        teacherSearchQuery = teacherSearchQuery,
                        onTeacherSearchChange = { viewModel.updateTeacherSearchQuery(it) },
                        onToggleAnnouncement = { viewModel.toggleAnnouncement(it) },
                        onAddStudentClick = {
                            editingStudent = null
                            showAddEditDialog = true
                        },
                        onEditStudentClick = { student ->
                            editingStudent = student
                            showAddEditDialog = true
                        },
                        onDeleteStudentClick = { student ->
                            viewModel.deleteStudent(student)
                            Toast.makeText(context, "${student.name} berhasil dihapus.", Toast.LENGTH_SHORT).show()
                        },
                        onResetDefault = {
                            viewModel.resetAllDataToDefault()
                            Toast.makeText(context, "Data berhasil direset ke standar.", Toast.LENGTH_SHORT).show()
                        },
                        onOpenSettings = { showSettingsDialog = true },
                        onLockConsole = {
                            isTeacherUnlocked = false
                            activeTab = 0
                            Toast.makeText(context, "Konsol Guru dikunci.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // Security Gate Dialog
    if (showPinDialog) {
        Dialog(onDismissRequest = {
            showPinDialog = false
            pinInput = ""
            pinError = false
        }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GentleWhite),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Secure Area",
                        tint = EmeraldMedium,
                        modifier = Modifier
                            .size(48.dp)
                            .background(EmeraldLight, CircleShape)
                            .padding(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Keamanan Konsol Guru",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkSlate
                    )
                    Text(
                        text = "Silakan masukkan PIN Guru untuk mengakses panel manajemen data kelulusan.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                                pinInput = it
                                pinError = false
                            }
                        },
                        label = { Text("PIN Akses (4-Digit)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = pinError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_input_field"),
                        singleLine = true,
                        supportingText = {
                            if (pinError) {
                                Text("PIN salah! Coba lagi.", color = MaterialTheme.colorScheme.error)
                            } else {
                                Text("PIN Standar Aplikasi: 1234", color = Color.Gray)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = {
                            showPinDialog = false
                            pinInput = ""
                            pinError = false
                        }) {
                            Text("Batal", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (pinInput == "1234") {
                                    isTeacherUnlocked = true
                                    showPinDialog = false
                                    pinInput = ""
                                    pinError = false
                                    activeTab = 1
                                    Toast.makeText(context, "Akses Konsol Guru Diterima", Toast.LENGTH_SHORT).show()
                                } else {
                                    pinError = true
                                    pinInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldMedium),
                            modifier = Modifier.testTag("pin_submit_button")
                        ) {
                            Text("Masuk")
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Student Dialog
    if (showAddEditDialog) {
        var examNo by remember { mutableStateOf(editingStudent?.examNumber ?: "") }
        var nisn_st by remember { mutableStateOf(editingStudent?.nisn ?: "") }
        var name_st by remember { mutableStateOf(editingStudent?.name ?: "") }
        var birthPlc by remember { mutableStateOf(editingStudent?.birthPlace ?: "Samarinda") }
        var birthDt by remember { mutableStateOf(editingStudent?.birthDate ?: "10 Jan 2014") }
        var status_st by remember { mutableStateOf(editingStudent?.status ?: "LULUS") }
        var notes_st by remember { mutableStateOf(editingStudent?.notes ?: "") }

        // Scores
        var indonesianS by remember { mutableStateOf(editingStudent?.indonesian?.toString() ?: "80") }
        var mathS by remember { mutableStateOf(editingStudent?.mathematics?.toString() ?: "80") }
        var scienceS by remember { mutableStateOf(editingStudent?.science?.toString() ?: "80") }
        var socialS by remember { mutableStateOf(editingStudent?.socialStudies?.toString() ?: "80") }
        var civicsS by remember { mutableStateOf(editingStudent?.civics?.toString() ?: "80") }
        var religionS by remember { mutableStateOf(editingStudent?.religion?.toString() ?: "80") }
        var localS by remember { mutableStateOf(editingStudent?.localContent?.toString() ?: "80") }
        var sportsS by remember { mutableStateOf(editingStudent?.sports?.toString() ?: "80") }
        var artS by remember { mutableStateOf(editingStudent?.art?.toString() ?: "80") }

        Dialog(onDismissRequest = { showAddEditDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GentleWhite),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Text(
                        text = if (editingStudent == null) "Tambah Data Siswa" else "Edit Data Siswa",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = EmeraldDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    ) {
                        OutlinedTextField(
                            value = name_st,
                            onValueChange = { name_st = it },
                            label = { Text("Nama Lengkap") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            singleLine = true
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = nisn_st,
                                onValueChange = { if (it.length <= 10) nisn_st = it },
                                label = { Text("NISN") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = examNo,
                                onValueChange = { examNo = it },
                                label = { Text("No. Ujian") },
                                placeholder = { Text("06-008-xxx") },
                                modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                                singleLine = true
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = birthPlc,
                                onValueChange = { birthPlc = it },
                                label = { Text("Tempat Lahir") },
                                modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = birthDt,
                                onValueChange = { birthDt = it },
                                label = { Text("Tanggal Lahir") },
                                placeholder = { Text("DD Bbb YYYY") },
                                modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                                singleLine = true
                            )
                        }

                        // Status Selector
                        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Text("Status Kelulusan:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = DarkSlate)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = status_st == "LULUS",
                                    onClick = { status_st = "LULUS" },
                                    colors = RadioButtonDefaults.colors(selectedColor = EmeraldMedium)
                                )
                                Text("LULUS", modifier = Modifier.clickable { status_st = "LULUS" })
                                Spacer(modifier = Modifier.width(24.dp))
                                RadioButton(
                                    selected = status_st == "DITANGGUHKAN",
                                    onClick = { status_st = "DITANGGUHKAN" },
                                    colors = RadioButtonDefaults.colors(selectedColor = ErrorRed)
                                )
                                Text("DITANGGUHKAN", modifier = Modifier.clickable { status_st = "DITANGGUHKAN" })
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        Text("Nilai Ujian Sekolah:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = EmeraldMedium)

                        ScoreInputField("Bahasa Indonesia", indonesianS) { indonesianS = it }
                        ScoreInputField("Matematika", mathS) { mathS = it }
                        ScoreInputField("ILMU Pengetahuan Alam (IPA)", scienceS) { scienceS = it }
                        ScoreInputField("ILMU Pengetahuan Sosial (IPS)", socialS) { socialS = it }
                        ScoreInputField("PPKn", civicsS) { civicsS = it }
                        ScoreInputField("Pendidikan Agama", religionS) { religionS = it }
                        ScoreInputField("Muatan Lokal", localS) { localS = it }
                        ScoreInputField("PJOK (Olahraga)", sportsS) { sportsS = it }
                        ScoreInputField("Seni Budaya & Prakarya", artS) { artS = it }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        OutlinedTextField(
                            value = notes_st,
                            onValueChange = { notes_st = it },
                            label = { Text("Catatan / Pesan Guru") },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            placeholder = { Text("Contoh: Selamat atas kelulusannya! Tetap berprestasi.") }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddEditDialog = false }) {
                            Text("Batal", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                if (name_st.isBlank() || nisn_st.isBlank() || examNo.isBlank()) {
                                    Toast.makeText(context, "Mohon lengkapi Nama, NISN, dan Nomor Ujian.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val std = Student(
                                    id = editingStudent?.id ?: 0,
                                    examNumber = examNo,
                                    nisn = nisn_st,
                                    name = name_st,
                                    birthPlace = birthPlc,
                                    birthDate = birthDt,
                                    status = status_st,
                                    indonesian = indonesianS.toDoubleOrNull() ?: 0.0,
                                    mathematics = mathS.toDoubleOrNull() ?: 0.0,
                                    science = scienceS.toDoubleOrNull() ?: 0.0,
                                    socialStudies = socialS.toDoubleOrNull() ?: 0.0,
                                    civics = civicsS.toDoubleOrNull() ?: 0.0,
                                    religion = religionS.toDoubleOrNull() ?: 0.0,
                                    localContent = localS.toDoubleOrNull() ?: 0.0,
                                    sports = sportsS.toDoubleOrNull() ?: 0.0,
                                    art = artS.toDoubleOrNull() ?: 0.0,
                                    notes = notes_st
                                )
                                viewModel.addOrUpdateStudent(std)
                                showAddEditDialog = false
                                Toast.makeText(context, "Data berhasil disimpan.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldMedium)
                        ) {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }

    // Settings Configuration Dialog
    if (showSettingsDialog) {
        var schoolNm by remember { mutableStateOf(appConfig.schoolName) }
        var acadYr by remember { mutableStateOf(appConfig.academicYear) }
        var annDate by remember { mutableStateOf(appConfig.announcementDate) }
        var hmName by remember { mutableStateOf(appConfig.headmasterName) }
        var hmNip by remember { mutableStateOf(appConfig.headmasterNip) }

        Dialog(onDismissRequest = { showSettingsDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = GentleWhite),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Pengaturan Sekolah",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = EmeraldDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = schoolNm,
                        onValueChange = { schoolNm = it },
                        label = { Text("Nama Sekolah") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = acadYr,
                        onValueChange = { acadYr = it },
                        label = { Text("Tahun Pelajaran") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = annDate,
                        onValueChange = { annDate = it },
                        label = { Text("Tanggal Kelulusan") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = hmName,
                        onValueChange = { hmName = it },
                        label = { Text("Nama Kepala Sekolah") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = hmNip,
                        onValueChange = { hmNip = it },
                        label = { Text("NIP Kepala Sekolah") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showSettingsDialog = false }) {
                            Text("Batal", color = Color.Gray)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                val conf = appConfig.copy(
                                    schoolName = schoolNm,
                                    academicYear = acadYr,
                                    announcementDate = annDate,
                                    headmasterName = hmName,
                                    headmasterNip = hmNip
                                )
                                viewModel.updateConfig(conf)
                                showSettingsDialog = false
                                Toast.makeText(context, "Pengaturan disimpan.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldMedium)
                        ) {
                            Text("Simpan")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabSegmentButton(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) GentleWhite else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) EmeraldDark else GentleWhite.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ScoreInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1.5f),
            style = MaterialTheme.typography.bodyMedium,
            color = DarkSlate
        )
        OutlinedTextField(
            value = value,
            onValueChange = {
                if (it.isEmpty() || it.toDoubleOrNull() != null) {
                    onValueChange(it)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .width(100.dp)
                .padding(start = 8.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.End)
        )
    }
}

// 1. Student Portal lookup UI
@Composable
fun StudentPortalView(
    searchQuery: String,
    searchResult: SearchResultState,
    appConfig: AppConfig,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onReset: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = GentleWhite),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon",
                    tint = EmeraldMedium,
                    modifier = Modifier
                        .size(40.dp)
                        .background(EmeraldLight, CircleShape)
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Periksa Status Kelulusan Anda",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = DarkSlate,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Gunakan NISN (10-digit), Nomor Ujian Sekolah (contoh: 06-008-001) atau Nama Lengkap untuk melakukan pencarian mandiri.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = EmeraldDark)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = onReset) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Clear text", tint = Color.Gray)
                            }
                        }
                    },
                    label = { Text("Ketik NISN / Nomor Ujian / Nama") },
                    placeholder = { Text("Contoh: 0134567890") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_search_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onSearch,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldMedium),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("student_search_button")
                        .height(48.dp)
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Periksa Kelulusan", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Results Section
        AnimatedVisibility(
            visible = searchResult != SearchResultState.Idle,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            when (searchResult) {
                is SearchResultState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = EmeraldMedium)
                    }
                }
                is SearchResultState.NotFound -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Siswa Tidak Ditemukan",
                                tint = ErrorRed,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Data Siswa Tidak Ditemukan",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF991B1B)
                                )
                                Text(
                                    text = "Pastikan NISN atau No Ujian yang Anda masukkan benar dan terdaftar di SDN 008 Samarinda Ulu.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF7F1D1D)
                                )
                            }
                        }
                    }
                }
                is SearchResultState.NotPublished -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Belum Dibuka",
                                    tint = WarningOrange,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Pengumuman Belum Dibuka Resmi",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF92400E)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Mohon maaf, halaman pengumuman kelulusan mandiri kelas VI SDN 008 belum dibuka secara resmi oleh pihak sekolah.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF78350F)
                            )
                        }
                    }
                }
                is SearchResultState.Error -> {
                    val errMsg = (searchResult as SearchResultState.Error).message
                    Text(
                        text = errMsg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                is SearchResultState.Success -> {
                    val student = (searchResult as SearchResultState.Success).student
                    GraduationCertificateCard(student = student, appConfig = appConfig) {
                        Toast.makeText(context, "Surat Keterangan Kelulusan (SKL) berhasil diunduh ke Penyimpanan lokal.", Toast.LENGTH_SHORT).show()
                    }
                }
                else -> {}
            }
        }
    }
}

// Full High-Fidelity Indonesian SKL Display
@Composable
fun GraduationCertificateCard(
    student: Student,
    appConfig: AppConfig,
    onPrintClick: () -> Unit
) {
    val totalScore = student.indonesian + student.mathematics + student.science +
            student.socialStudies + student.civics + student.religion +
            student.localContent + student.sports + student.art

    Card(
        colors = CardDefaults.cardColors(containerColor = GentleWhite),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            width = 2.dp,
            color = if (student.status == "LULUS") SuccessGreen else ErrorRed
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("graduation_certificate_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Success Header Accent
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = if (student.status == "LULUS") Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (student.status == "LULUS") {
                        Text(
                            text = "🎉 SELAMAT! 🎉",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = SuccessGreen,
                            letterSpacing = 1.sp
                        )
                    } else {
                        Text(
                            text = "⚠️ PERHATIAN! ⚠️",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = ErrorRed,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "STATUS KELULUSAN SISWA: ${student.status}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (student.status == "LULUS") EmeraldDark else Color(0xFF991B1B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SKL Letter Layout with dashed border
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawBehind {
                        val stroke = Stroke(
                            width = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                        drawRoundRect(
                            color = Color.LightGray,
                            style = stroke
                        )
                    }
                    .padding(14.dp)
            ) {
                Column {
                    // Indonesian Formal Header
                    Text(
                        text = "PEMERINTAH KOTA SAMARINDA",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = DarkSlate
                    )
                    Text(
                        text = "DINAS PENDIDIKAN DAN KEBUDAYAAN",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = DarkSlate
                    )
                    Text(
                        text = appConfig.schoolName.uppercase(),
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = EmeraldDark
                    )
                    Text(
                        text = "Jl. KS Tubun, Samarinda Ulu, Kota Samarinda, Kaltim",
                        fontSize = 9.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Gray
                    )

                    // Line divider
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(DarkSlate)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "SURAT KETERANGAN KELULUSAN (SKL)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = DarkSlate,
                        textDecoration = TextDecoration.Underline
                    )
                    Text(
                        text = "Nomor: 421.2/06/008/VI/2026",
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Yang bertanda tangan di bawah ini, Kepala Sekolah ${appConfig.schoolName} Kota Samarinda menerangkan bahwa:",
                        fontSize = 11.sp,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkSlate
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Student Details
                    StudentDetailRow("Nama Siswa", student.name)
                    StudentDetailRow("NISN", student.nisn)
                    StudentDetailRow("Nomor Peserta Ujian", student.examNumber)
                    StudentDetailRow("Tempat, Tanggal Lahir", "${student.birthPlace}, ${student.birthDate}")

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Berdasarkan Kriteria Kelulusan yang berlaku, siswa tersebut di atas dinyatakan:",
                        fontSize = 11.sp,
                        color = DarkSlate
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = student.status,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = if (student.status == "LULUS") SuccessGreen else ErrorRed
                        )
                    }

                    Text(
                        text = "Dengan perincian perolehan nilai Ujian Sekolah (US/Asesmen):",
                        fontSize = 11.sp,
                        color = DarkSlate,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 6.dp)
                    )

                    // Table Scores
                    LocalScoresTable(student = student, totalScore = totalScore)

                    Spacer(modifier = Modifier.height(14.dp))

                    // Signature block with realistic signature
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(180.dp)
                        ) {
                            Text(text = "Samarinda, ${appConfig.announcementDate}", fontSize = 10.sp, color = DarkSlate)
                            Text(text = "Kepala Sekolah,", fontSize = 10.sp, color = DarkSlate, fontWeight = FontWeight.SemiBold)

                            // Digital stamp and sign zone
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .size(130.dp, 60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                // Greenish stamp watermark and sig mockup
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    // Official stamp circle
                                    drawCircle(
                                        color = EmeraldMedium.copy(alpha = 0.22f),
                                        radius = 50f,
                                        center = Offset(70f, 75f),
                                        style = Stroke(width = 4f)
                                    )
                                    // Signature scribble
                                    drawLine(
                                        color = Color(0xFF1E3A8A).copy(alpha = 0.85f),
                                        start = Offset(50f, 90f),
                                        end = Offset(130f, 60f),
                                        strokeWidth = 5f
                                    )
                                    drawLine(
                                        color = Color(0xFF1E3A8A).copy(alpha = 0.85f),
                                        start = Offset(80f, 100f),
                                        end = Offset(110f, 50f),
                                        strokeWidth = 3f
                                    )
                                }
                                Text(
                                    text = "SDN 008",
                                    fontSize = 7.sp,
                                    color = EmeraldMedium.copy(alpha = 0.5f),
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Text(
                                text = appConfig.headmasterName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkSlate,
                                textDecoration = TextDecoration.Underline
                            )
                            Text(
                                text = "NIP. ${appConfig.headmasterNip}",
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Wali kelas notes
            if (student.notes.isNotBlank()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = GoldLight),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = EmeraldDark, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pesan Guru & Wali Kelas VI:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = EmeraldDark)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = student.notes, style = MaterialTheme.typography.bodySmall, color = DarkSlate)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // CTA Action print
            Button(
                onClick = onPrintClick,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Unduh Resmi Surat SKL", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun StudentDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1.2f),
            fontSize = 11.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = ":",
            modifier = Modifier.width(12.dp),
            fontSize = 11.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            modifier = Modifier.weight(2f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = DarkSlate
        )
    }
}

@Composable
fun LocalScoresTable(student: Student, totalScore: Double) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray)
    ) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EmeraldLight)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Mata Pelajaran Ujian Sekolah", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f), color = EmeraldDark)
            Text("Nilai Angka", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = EmeraldDark)
        }

        Divider(color = Color.LightGray)

        StudentScoreRow("1. Bahasa Indonesia", student.indonesian)
        StudentScoreRow("2. Matematika", student.mathematics)
        StudentScoreRow("3. Ilmu Pengetahuan Alam (IPA)", student.science)
        StudentScoreRow("4. Ilmu Pengetahuan Sosial (IPS)", student.socialStudies)
        StudentScoreRow("5. PPKn", student.civics)
        StudentScoreRow("6. Pendidikan Agama", student.religion)
        StudentScoreRow("7. Muatan Lokal (Budaya Kaltim)", student.localContent)
        StudentScoreRow("8. Pendidikan Jasmani & Olahraga (PJOK)", student.sports)
        StudentScoreRow("9. Seni Budaya & Prakarya (SBdP)", student.art)

        Divider(color = Color.LightGray)

        // Totals
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F5F9))
                .padding(4.dp)
        ) {
            Text("Jumlah Total Nilai", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f), color = DarkSlate)
            Text(String.format("%.1f", totalScore), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = DarkSlate)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFFFAF0))
                .padding(4.dp)
        ) {
            Text("Rata-rata Nilai", fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f), color = EmeraldDark)
            Text(String.format("%.1f", student.averageScore), fontSize = 10.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = EmeraldDark)
        }
    }
}

@Composable
fun StudentScoreRow(subject: String, score: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp, horizontal = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(subject, fontSize = 10.sp, modifier = Modifier.weight(2f), color = DarkSlate)
        Text(String.format("%.1f", score), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = DarkSlate)
    }
}

// 2. Teacher Console UI Content
@Composable
fun TeacherConsoleView(
    allStudents: List<Student>,
    appConfig: AppConfig,
    teacherSearchQuery: String,
    onTeacherSearchChange: (String) -> Unit,
    onToggleAnnouncement: (Boolean) -> Unit,
    onAddStudentClick: () -> Unit,
    onEditStudentClick: (Student) -> Unit,
    onDeleteStudentClick: (Student) -> Unit,
    onResetDefault: () -> Unit,
    onOpenSettings: () -> Unit,
    onLockConsole: () -> Unit
) {
    val totalCount = allStudents.size
    val lulusCount = allStudents.count { it.status == "LULUS" }
    val averageScoreClass = if (totalCount > 0) allStudents.map { it.averageScore }.average() else 0.0
    val lulustage = if (totalCount > 0) (lulusCount.toFloat() / totalCount * 100).toInt() else 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Stats Row Card
        Card(
            colors = CardDefaults.cardColors(containerColor = EmeraldDark),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Statistik Kelas VI",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = GoldLight
                    )
                    IconButton(onClick = onOpenSettings) {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = "School Settings", tint = GentleWhite)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MiniStatItem("Siswa", totalCount.toString())
                    MiniStatItem("Lulus", "$lulusCount ($lulustage%)")
                    MiniStatItem("Rerata US", String.format("%.1f", averageScoreClass))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Toggle Announcement switch
        Card(
            colors = CardDefaults.cardColors(containerColor = GentleWhite),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Halaman Publik Siswa",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DarkSlate
                    )
                    Text(
                        text = if (appConfig.isAnnouncementPublished) "Siswa dpt mengakses kelulusan secara mandiri." else "Pencarian mandiri dinonaktifkan (kunci publik).",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Switch(
                    checked = appConfig.isAnnouncementPublished,
                    onCheckedChange = { onToggleAnnouncement(it) },
                    colors = SwitchDefaults.colors(checkedThumbColor = EmeraldMedium)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Student Data Administration
        Card(
            colors = CardDefaults.cardColors(containerColor = GentleWhite),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Daftar Kelulusan Siswa Kelas 6",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = EmeraldMedium
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Teacher Search Field
                OutlinedTextField(
                    value = teacherSearchQuery,
                    onValueChange = onTeacherSearchChange,
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = EmeraldMedium) },
                    label = { Text("Cari Siswa...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Actions buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onAddStudentClick,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldMedium),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .testTag("add_student_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Siswa SKL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onResetDefault,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset Default", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // List Items (since we are nested in verticalScroll parent, we can render using standard Column to prevent scrolling collision)
                if (allStudents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tidak ada records siswa.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                } else {
                    allStudents.forEach { student ->
                        TeacherStudentRow(
                            student = student,
                            onEdit = { onEditStudentClick(student) },
                            onDelete = { onDeleteStudentClick(student) }
                        )
                        Divider(color = Color(0xFFF1F5F9))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Lock & Close Button
        OutlinedButton(
            onClick = onLockConsole,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
            border = BorderStroke(1.dp, ErrorRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("lock_console_button")
                .height(46.dp)
        ) {
            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Kunci & Keluar Konsol", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MiniStatItem(label: String, valStr: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = GoldLight)
        Text(text = valStr, fontSize = 16.sp, fontWeight = FontWeight.Black, color = GentleWhite)
    }
}

@Composable
fun TeacherStudentRow(
    student: Student,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(student.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSlate)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("NISN: ${student.nisn}", fontSize = 10.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                _StatusBadge(status = student.status)
            }
            Text("Rerata: ${student.averageScore} • No Ujian: ${student.examNumber}", fontSize = 10.sp, color = EmeraldDark)
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Siswa",
                    tint = EmeraldMedium,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus Siswa",
                    tint = ErrorRed,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun _StatusBadge(status: String) {
    Box(
        modifier = Modifier
            .background(
                color = if (status == "LULUS") Color(0xFFD1FAE5) else Color(0xFFFEE2E2),
                shape = RoundedCornerShape(4.dp)
            )
            .padding(vertical = 2.dp, horizontal = 6.dp)
    ) {
        Text(
            text = status,
            fontSize = 8.sp,
            fontWeight = FontWeight.Bold,
            color = if (status == "LULUS") Color(0xFF065F46) else Color(0xFF991B1B)
        )
    }
}
