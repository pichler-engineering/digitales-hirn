package at.pichler.digitaleshirn.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import at.pichler.digitaleshirn.data.entity.InboxEntryEntity
import at.pichler.digitaleshirn.data.entity.NoteEntity
import at.pichler.digitaleshirn.data.entity.ProjectEntity
import at.pichler.digitaleshirn.data.entity.TaskEntity
import at.pichler.digitaleshirn.model.Priority
import at.pichler.digitaleshirn.vm.MainViewModel
import at.pichler.digitaleshirn.vm.TaskInput
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy")
private val timeFmt = DateTimeFormatter.ofPattern("HH:mm")

@Composable
fun DigitalesHirnApp(vm: MainViewModel) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(vm, navController) }
        composable("today") { TodayScreen(vm, navController) }
        composable("inbox") { InboxScreen(vm, navController) }
        composable("projects") { ProjectsScreen(vm, navController) }
        composable("notes") { NotesScreen(vm, navController) }
        composable(
            route = "project/{projectId}",
            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
        ) {
            ProjectDetailScreen(vm, navController, it.arguments?.getLong("projectId") ?: 0L)
        }
        composable("task/new") { TaskEditorScreen(vm, navController, null) }
        composable(
            route = "task/edit/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.LongType })
        ) {
            TaskEditorScreen(vm, navController, it.arguments?.getLong("taskId"))
        }
    }
}

@Composable
private fun HomeScreen(vm: MainViewModel, navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var spokenText by rememberSaveable { mutableStateOf("") }

    val speechLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val text = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                .orEmpty()
            spokenText = text
            if (text.isNotBlank()) vm.handleSpeechResult(text)
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Sprich deinen Gedanken")
            }
            speechLauncher.launch(intent)
        } else {
            scope.launch { snackbarHostState.showSnackbar("Mikrofonberechtigung wurde abgelehnt") }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Digitales Hirn", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HomeTile("Gedanke", Modifier.weight(1f)) { }
                HomeTile("Heute", Modifier.weight(1f)) { navController.navigate("today") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HomeTile("Projekte", Modifier.weight(1f)) { navController.navigate("projects") }
                HomeTile("Inbox", Modifier.weight(1f)) { navController.navigate("inbox") }
            }
            HomeTile("Notizen", Modifier.fillMaxWidth()) { navController.navigate("notes") }

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                FloatingActionButton(
                    onClick = {
                        val granted = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (granted) {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
                                putExtra(RecognizerIntent.EXTRA_PROMPT, "Sprich deinen Gedanken")
                            }
                            speechLauncher.launch(intent)
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier.size(84.dp)
                ) { Icon(Icons.Default.Mic, contentDescription = "Mikrofon", modifier = Modifier.size(40.dp)) }
            }

            if (spokenText.isNotBlank()) {
                Text("Erkannt: $spokenText", style = MaterialTheme.typography.bodyLarge)
            }

            Button(onClick = { navController.navigate("task/new") }, modifier = Modifier.fillMaxWidth()) {
                Text("Aufgabe erstellen")
            }
        }
    }
}

@Composable
private fun HomeTile(title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier
            .height(110.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TodayScreen(vm: MainViewModel, navController: NavHostController) {
    val items by vm.todayTasks.collectAsState()
    ScreenScaffold("Heute", navController, fab = {
        FloatingActionButton(onClick = { navController.navigate("task/new") }) { Icon(Icons.Default.Add, null) }
    }) { padding ->
        LazyColumn(contentPadding = padding, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(items) { (task, projectName) ->
                TaskCard(task, projectName ?: "-", onEdit = { navController.navigate("task/edit/${task.id}") }, onDelete = { vm.deleteTask(task) }) {
                    vm.toggleTaskDone(task)
                }
            }
        }
    }
}

@Composable
private fun InboxScreen(vm: MainViewModel, navController: NavHostController) {
    val entries by vm.inbox.collectAsState()
    var editing by remember { mutableStateOf<InboxEntryEntity?>(null) }
    var text by rememberSaveable { mutableStateOf("") }

    ScreenScaffold("Inbox", navController) { padding ->
        LazyColumn(contentPadding = padding, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(entries) { entry ->
                Card {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(entry.text)
                        Text("Erstellt: ${entry.createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}")
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(onClick = { vm.convertInboxToTask(entry) }, label = { Text("Als Aufgabe") })
                            AssistChip(onClick = {
                                editing = entry
                                text = entry.text
                            }, label = { Text("Bearbeiten") })
                            AssistChip(onClick = { vm.deleteInbox(entry) }, label = { Text("Löschen") })
                        }
                    }
                }
            }
        }
    }

    if (editing != null) {
        AlertDialog(
            onDismissRequest = { editing = null },
            title = { Text("Inbox-Eintrag") },
            text = {
                OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Text") })
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.saveInbox(editing?.id, text)
                    editing = null
                }) { Text("Speichern") }
            },
            dismissButton = { TextButton(onClick = { editing = null }) { Text("Abbrechen") } }
        )
    }
}

@Composable
private fun ProjectsScreen(vm: MainViewModel, navController: NavHostController) {
    val projects by vm.projects.collectAsState()
    var newName by rememberSaveable { mutableStateOf("") }
    var renameProject by remember { mutableStateOf<ProjectEntity?>(null) }
    var renameText by rememberSaveable { mutableStateOf("") }

    ScreenScaffold("Projekte", navController) { padding ->
        Column(modifier = Modifier.padding(padding).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("Neues Projekt") }
                )
                Button(onClick = {
                    if (newName.isNotBlank()) {
                        vm.saveProject(name = newName.trim())
                        newName = ""
                    }
                }) { Text("Erstellen") }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(projects) { project ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { navController.navigate("project/${project.id}") }) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(project.name)
                            Row {
                                IconButton(onClick = {
                                    renameProject = project
                                    renameText = project.name
                                }) { Icon(Icons.Default.Edit, null) }
                                IconButton(onClick = { vm.deleteProject(project) }) { Icon(Icons.Default.Delete, null) }
                            }
                        }
                    }
                }
            }
        }
    }

    if (renameProject != null) {
        AlertDialog(
            onDismissRequest = { renameProject = null },
            title = { Text("Projekt umbenennen") },
            text = { OutlinedTextField(value = renameText, onValueChange = { renameText = it }, label = { Text("Name") }) },
            confirmButton = {
                TextButton(onClick = {
                    val project = renameProject ?: return@TextButton
                    vm.saveProject(project.id, renameText)
                    renameProject = null
                }) { Text("Speichern") }
            },
            dismissButton = { TextButton(onClick = { renameProject = null }) { Text("Abbrechen") } }
        )
    }
}

@Composable
private fun ProjectDetailScreen(vm: MainViewModel, navController: NavHostController, projectId: Long) {
    val tasks by vm.tasks.collectAsState()
    val projectName = vm.projectName(projectId)
    val filtered = tasks.filter { it.projectId == projectId }
    ScreenScaffold(projectName, navController) { padding ->
        LazyColumn(contentPadding = padding, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(filtered) { task ->
                TaskCard(task, projectName, onEdit = { navController.navigate("task/edit/${task.id}") }, onDelete = { vm.deleteTask(task) }) {
                    vm.toggleTaskDone(task)
                }
            }
        }
    }
}

@Composable
private fun NotesScreen(vm: MainViewModel, navController: NavHostController) {
    val notes by vm.notes.collectAsState()
    var query by rememberSaveable { mutableStateOf("") }
    var editNote by remember { mutableStateOf<NoteEntity?>(null) }
    var title by rememberSaveable { mutableStateOf("") }
    var text by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(query) { vm.setNoteQuery(query) }

    ScreenScaffold("Notizen", navController, fab = {
        FloatingActionButton(onClick = {
            editNote = NoteEntity(title = "", text = "")
            title = ""
            text = ""
        }) { Icon(Icons.Default.Note, null) }
    }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("Suche") }, modifier = Modifier.fillMaxWidth())
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(notes) { note ->
                    Card {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(note.title, fontWeight = FontWeight.SemiBold)
                            Text(note.text)
                            Text("${note.createdAt.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}")
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AssistChip(onClick = {
                                    editNote = note
                                    title = note.title
                                    text = note.text
                                }, label = { Text("Bearbeiten") })
                                AssistChip(onClick = { vm.deleteNote(note) }, label = { Text("Löschen") })
                            }
                        }
                    }
                }
            }
        }
    }

    if (editNote != null) {
        AlertDialog(
            onDismissRequest = { editNote = null },
            title = { Text(if (editNote?.id == 0L) "Notiz erstellen" else "Notiz bearbeiten") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titel") })
                    OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text("Text") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (title.isNotBlank()) {
                        val noteId = editNote?.id?.takeIf { it != 0L }
                        vm.saveNote(noteId, title, text)
                        editNote = null
                    }
                }) { Text("Speichern") }
            },
            dismissButton = { TextButton(onClick = { editNote = null }) { Text("Abbrechen") } }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskEditorScreen(vm: MainViewModel, navController: NavHostController, taskId: Long?) {
    val projects by vm.projects.collectAsState()
    val existing = taskId?.let(vm::getTask)

    var title by rememberSaveable(taskId) { mutableStateOf(existing?.title ?: "") }
    var description by rememberSaveable(taskId) { mutableStateOf(existing?.description ?: "") }
    var selectedProjectId by rememberSaveable(taskId) { mutableStateOf(existing?.projectId) }
    var priority by rememberSaveable(taskId) { mutableStateOf(existing?.priority ?: Priority.NORMAL) }
    var dueDateText by rememberSaveable(taskId) { mutableStateOf(existing?.dueDate?.toString() ?: "") }
    var dueTimeText by rememberSaveable(taskId) { mutableStateOf(existing?.dueTime?.toString() ?: "") }
    var reminderEnabled by rememberSaveable(taskId) { mutableStateOf(existing?.reminderEnabled ?: false) }
    var completed by rememberSaveable(taskId) { mutableStateOf(existing?.completed ?: false) }

    ScreenScaffold(if (taskId == null) "Neue Aufgabe" else "Aufgabe bearbeiten", navController) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titel") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Beschreibung") }, modifier = Modifier.fillMaxWidth())
            Text("Projekt")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { selectedProjectId = null },
                    label = { Text("Keins") }
                )
                projects.forEach { project ->
                    AssistChip(onClick = { selectedProjectId = project.id }, label = { Text(project.name) })
                }
            }

            Text("Priorität")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Priority.entries.forEach { p ->
                    AssistChip(onClick = { priority = p }, label = { Text(p.label) })
                }
            }

            OutlinedTextField(
                value = dueDateText,
                onValueChange = { dueDateText = it },
                label = { Text("Datum (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = dueTimeText,
                onValueChange = { dueTimeText = it },
                label = { Text("Uhrzeit (HH:MM)") },
                modifier = Modifier.fillMaxWidth()
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                Text("Erinnerung aktiv")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = completed, onCheckedChange = { completed = it })
                Text("Erledigt")
            }

            Button(onClick = {
                val parsedDate = dueDateText.takeIf { it.isNotBlank() }?.runCatching(LocalDate::parse)?.getOrNull()
                val parsedTime = dueTimeText.takeIf { it.isNotBlank() }?.runCatching(LocalTime::parse)?.getOrNull()
                if (title.isNotBlank()) {
                    vm.saveTask(
                        TaskInput(
                            title = title.trim(),
                            description = description.ifBlank { null },
                            projectId = selectedProjectId,
                            priority = priority,
                            dueDate = parsedDate,
                            dueTime = parsedTime,
                            reminderEnabled = reminderEnabled,
                            completed = completed
                        ),
                        taskId
                    ) {
                        navController.popBackStack()
                    }
                }
            }, modifier = Modifier.fillMaxWidth()) { Text("Speichern") }

            if (existing != null) {
                TextButton(onClick = {
                    vm.deleteTask(existing)
                    navController.popBackStack()
                }, modifier = Modifier.fillMaxWidth()) { Text("Aufgabe löschen") }
            }
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskEntity,
    projectName: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleDone: () -> Unit
) {
    val completedBg = if (task.completed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
    Card(colors = CardDefaults.cardColors(containerColor = completedBg)) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = task.completed, onCheckedChange = { onToggleDone() })
                Text(task.title, fontWeight = FontWeight.SemiBold)
            }
            task.description?.takeIf { it.isNotBlank() }?.let { Text(it) }
            Text("Projekt: $projectName")
            Text("Priorität: ${task.priority.label}")
            Text("Datum: ${task.dueDate?.format(dateFmt) ?: "-"}  Zeit: ${task.dueTime?.format(timeFmt) ?: "-"}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = onEdit, label = { Text("Bearbeiten") })
                AssistChip(onClick = onDelete, label = { Text("Löschen") })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScreenScaffold(
    title: String,
    navController: NavHostController,
    fab: @Composable (() -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Zurück")
                        }
                    }
                }
            )
        },
        floatingActionButton = { fab?.invoke() }
    ) { inner ->
        content(PaddingValues(start = 12.dp, top = inner.calculateTopPadding(), end = 12.dp, bottom = 16.dp))
    }
}

@Composable
private fun <T> kotlinx.coroutines.flow.StateFlow<T>.collectAsState() =
    androidx.compose.runtime.collectAsState(value)
