package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Alumna
import com.example.ui.viewmodel.QueenDanceViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditAlumnaScreen(
    viewModel: QueenDanceViewModel,
    alumnaIdToEdit: Int?, // Pass null to Add, pass id to Edit
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var tutor by remember { mutableStateOf("") }
    var plan by remember { mutableStateOf(2) }
    val normalDays = remember { mutableStateListOf<String>() }

    val activeAlumnas by viewModel.filteredAlumnas.collectAsState()
    val teachers by viewModel.activeTeachers.collectAsState()

    // Populate fields if editing
    LaunchedEffect(alumnaIdToEdit) {
        if (alumnaIdToEdit != null) {
            val studentToEdit = activeAlumnas.find { it.id == alumnaIdToEdit }
            if (studentToEdit != null) {
                name = studentToEdit.name
                lastName = studentToEdit.lastName
                tutor = studentToEdit.tutor
                plan = studentToEdit.plan
                normalDays.clear()
                normalDays.addAll(studentToEdit.normalDays.split(",").map { it.trim() })
            }
        }
    }

    val availableDays = listOf("Martes", "Jueves", "Viernes", "Sábado")
    val isFormValid = name.isNotBlank() && tutor.isNotBlank() && normalDays.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (alumnaIdToEdit != null) "Editar Alumna" else "Nuevo Registro",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("form_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Description paragraph
            Text(
                text = "Ingresa los datos de la alumna para administrar sus planes y asistencias.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Name Field
            Text(
                text = "Nombre",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .testTag("form_name_field"),
                placeholder = { Text("Nombre de la alumna") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Last Name Field
            Text(
                text = "Apellido",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
                    .testTag("form_lastname_field"),
                placeholder = { Text("Apellido (Opcional)") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Badge, contentDescription = null)
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Tutor / Professor Dropdown Selection / input
            Text(
                text = "Profesor/a o Tutor",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            var tutorExpanded by remember { mutableStateOf(false) }
            val teachersList = teachers.map { it.name }

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = tutor,
                    onValueChange = { tutor = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .clickable { tutorExpanded = true }
                        .testTag("form_tutor_field"),
                    placeholder = { Text("Selecciona o escribe el profesor/a") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.RecordVoiceOver, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { tutorExpanded = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                DropdownMenu(
                    expanded = tutorExpanded,
                    onDismissRequest = { tutorExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    teachersList.forEach { teacher ->
                        DropdownMenuItem(
                            text = { Text(teacher) },
                            onClick = {
                                tutor = teacher
                                tutorExpanded = false
                            }
                        )
                    }
                }
            }

            // Plan Selection (1, 2, 3, 4 Days per week Toggle Row)
                        Text(
                            text = "Plan (Días por semana)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(1, 2, 3, 4).forEach { option ->
                    val isSelected = plan == option
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clickable { plan = option }
                            .testTag("plan_option_$option"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$option Días",
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            // Normal Days Checkboxes (Multi selection)
            Text(
                text = "Días de asistencia normalmente",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableDays.forEach { day ->
                    val isChecked = normalDays.contains(day)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(23.dp))
                            .background(
                                if (isChecked) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                                else MaterialTheme.colorScheme.surfaceContainerLow
                            )
                            .border(
                                BorderStroke(
                                    1.dp,
                                    if (isChecked) MaterialTheme.colorScheme.secondary
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                ),
                                RoundedCornerShape(23.dp)
                            )
                            .clickable {
                                if (isChecked) {
                                    normalDays.remove(day)
                                } else {
                                    normalDays.add(day)
                                }
                            }
                            .testTag("day_chip_$day"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            color = if (isChecked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Alert warning if normal days counts does not match the selected plan days count
            if (normalDays.isNotEmpty() && normalDays.size != plan) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Alerta",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Seleccionaste ${normalDays.size} días de asistencia, pero el plan contratado es de $plan días.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Save Action Button
            Button(
                onClick = {
                    viewModel.addOrUpdateAlumna(
                        id = alumnaIdToEdit ?: 0,
                        name = name,
                        lastName = lastName,
                        tutor = tutor,
                        plan = plan,
                        normalDays = normalDays
                    )
                    onBack()
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("save_student_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Guardar Alumna",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
