import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [Index(value = ["remoteId"], unique = true)]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val remoteId: Int? = null,
    val title: String,
    val isDone: Boolean = false,
    val priority: Int = 1,
    val description: String = ""
)