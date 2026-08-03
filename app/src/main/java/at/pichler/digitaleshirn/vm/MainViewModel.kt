package at.pichler.digitaleshirn.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import at.pichler.digitaleshirn.data.entity.InboxEntryEntity
import at.pichler.digitaleshirn.data.entity.NoteEntity
import at.pichler.digitaleshirn.data.entity.ProjectEntity
import at.pichler.digitaleshirn.data.entity.TaskEntity
import at.pichler.digitaleshirn.model.Priority
import at.pichler.digitaleshirn.reminder.ReminderScheduler
import at.pichler.digitaleshirn.repository.AppRepository
import at.pichler.digitaleshirn.speech.GermanSpeechParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainViewModel(
    private val repository: AppRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    val tasks = repository.observeTasks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val inbox = repository.observeInbox().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val projects = repository.observeProjects().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val noteQuery = MutableStateFlow("")
    val notes = noteQuery.flatMapLatest { query ->
        if (query.isBlank()) repository.observeNotes() else repository.observeNotesByQuery(query)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val todayTasks = combine(tasks, projects) { taskList, projectList ->
        val today = LocalDate.now()
        val projectMap = projectList.associateBy { it.id }
        taskList.filter { it.dueDate == today || (!it.completed && it.dueDate != null && it.dueDate.isBefore(today)) }
            .sortedWith(compareBy<TaskEntity> { it.completed }.thenBy { it.dueTime ?: java.time.LocalTime.MAX })
            .map { task -> task to projectMap[task.projectId]?.name }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun projectName(projectId: Long?): String = projects.value.firstOrNull { it.id == projectId }?.name ?: "-"

    fun saveTask(input: TaskInput, taskId: Long? = null, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            val task = TaskEntity(
                id = taskId ?: 0,
                title = input.title,
                description = input.description,
                projectId = input.projectId,
                priority = input.priority,
                dueDate = input.dueDate,
                dueTime = input.dueTime,
                reminderEnabled = input.reminderEnabled,
                completed = input.completed
            )

            val id = if (taskId == null) repository.createTask(task) else {
                repository.updateTask(task)
                taskId
            }

            if (task.reminderEnabled && task.dueDate != null && task.dueTime != null && !task.completed) {
                reminderScheduler.schedule(task.copy(id = id))
            } else {
                reminderScheduler.cancel(id)
            }
            onDone?.invoke()
        }
    }

    fun getTask(taskId: Long): TaskEntity? = tasks.value.firstOrNull { it.id == taskId }

    fun toggleTaskDone(task: TaskEntity) {
        viewModelScope.launch {
            val updated = task.copy(completed = !task.completed)
            repository.updateTask(updated)
            if (updated.completed) reminderScheduler.cancel(updated.id) else reminderScheduler.schedule(updated)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            reminderScheduler.cancel(task.id)
        }
    }

    fun saveInbox(id: Long? = null, text: String, projectId: Long? = null) {
        viewModelScope.launch {
            if (id == null) repository.createInbox(InboxEntryEntity(text = text, projectId = projectId))
            else repository.updateInbox(InboxEntryEntity(id = id, text = text, projectId = projectId))
        }
    }

    fun deleteInbox(entry: InboxEntryEntity) {
        viewModelScope.launch { repository.deleteInbox(entry) }
    }

    fun convertInboxToTask(entry: InboxEntryEntity) {
        viewModelScope.launch {
            repository.createTask(
                TaskEntity(
                    title = entry.text.take(80),
                    description = entry.text,
                    projectId = entry.projectId,
                    priority = Priority.NORMAL
                )
            )
            repository.deleteInbox(entry)
        }
    }

    fun saveProject(id: Long? = null, name: String) {
        viewModelScope.launch {
            if (id == null) repository.createProject(ProjectEntity(name = name))
            else repository.updateProject(ProjectEntity(id = id, name = name))
        }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch { repository.deleteProject(project) }
    }

    fun saveNote(id: Long? = null, title: String, text: String, projectId: Long? = null) {
        viewModelScope.launch {
            if (id == null) repository.createNote(NoteEntity(title = title, text = text, projectId = projectId))
            else repository.updateNote(NoteEntity(id = id, title = title, text = text, projectId = projectId))
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch { repository.deleteNote(note) }
    }

    fun setNoteQuery(query: String) {
        noteQuery.value = query
    }

    fun handleSpeechResult(text: String) {
        viewModelScope.launch {
            val parse = GermanSpeechParser.parse(text)
            if (parse.taskInput != null && !parse.shouldFallbackToInbox) {
                saveTask(parse.taskInput)
            } else {
                repository.createInbox(InboxEntryEntity(text = text))
            }
        }
    }
}

class MainViewModelFactory(
    private val repository: AppRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MainViewModel(repository, reminderScheduler) as T
    }
}
