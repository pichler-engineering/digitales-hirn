package at.pichler.digitaleshirn.repository

import at.pichler.digitaleshirn.data.AppDatabase
import at.pichler.digitaleshirn.data.entity.InboxEntryEntity
import at.pichler.digitaleshirn.data.entity.NoteEntity
import at.pichler.digitaleshirn.data.entity.ProjectEntity
import at.pichler.digitaleshirn.data.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

class AppRepository(private val db: AppDatabase) {
    fun observeTasks(): Flow<List<TaskEntity>> = db.taskDao().observeAll()
    fun observeInbox(): Flow<List<InboxEntryEntity>> = db.inboxDao().observeAll()
    fun observeProjects(): Flow<List<ProjectEntity>> = db.projectDao().observeAll()
    fun observeProjectTasks(projectId: Long): Flow<List<TaskEntity>> = db.taskDao().observeByProject(projectId)
    fun observeNotes(): Flow<List<NoteEntity>> = db.noteDao().observeAll()
    fun observeNotesByQuery(query: String): Flow<List<NoteEntity>> = db.noteDao().observeBySearch(query)

    suspend fun createTask(task: TaskEntity): Long = db.taskDao().insert(task)
    suspend fun updateTask(task: TaskEntity) = db.taskDao().update(task)
    suspend fun deleteTask(task: TaskEntity) = db.taskDao().delete(task)
    suspend fun getTask(taskId: Long): TaskEntity? = db.taskDao().getById(taskId)

    suspend fun createInbox(entry: InboxEntryEntity): Long = db.inboxDao().insert(entry)
    suspend fun updateInbox(entry: InboxEntryEntity) = db.inboxDao().update(entry)
    suspend fun deleteInbox(entry: InboxEntryEntity) = db.inboxDao().delete(entry)

    suspend fun createProject(project: ProjectEntity): Long = db.projectDao().insert(project)
    suspend fun updateProject(project: ProjectEntity) = db.projectDao().update(project)
    suspend fun deleteProject(project: ProjectEntity) = db.projectDao().delete(project)
    suspend fun getProject(projectId: Long): ProjectEntity? = db.projectDao().getById(projectId)

    suspend fun createNote(note: NoteEntity): Long = db.noteDao().insert(note)
    suspend fun updateNote(note: NoteEntity) = db.noteDao().update(note)
    suspend fun deleteNote(note: NoteEntity) = db.noteDao().delete(note)

    suspend fun reminderCandidates(): List<TaskEntity> = db.taskDao().getReminderCandidates()
}
