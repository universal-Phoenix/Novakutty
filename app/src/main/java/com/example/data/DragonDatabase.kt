package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@Entity(tableName = "pi_services")
data class PiService(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val port: Int,
    val url: String,
    val isOnline: Boolean = false,
    val isCustom: Boolean = false,
    val lastChecked: Long = System.currentTimeMillis(),
    val avgLatencyMs: Int = 0
)

@Entity(tableName = "dragon_logs")
data class DragonLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String, // e.g. "SYSTEM", "EVE_AI", "USER_CMD", "PI_CORE"
    val message: String,
    val isAiResponse: Boolean = false
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val username: String,
    val securityPin: String,
    val role: String = "Developer",
    val displayName: String = "",
    val lastLoginTime: Long = 0
)

@Entity(tableName = "project_details")
data class ProjectDetail(
    @PrimaryKey val id: Long = 1,
    val name: String = "Universal Dragon",
    val subtitle: String = "Nova Intelligence Platform",
    val description: String = "A powerful digital platform created by Aslam, serving as the core of project Universal Dragon.",
    val creatorName: String = "Aslam",
    val status: String = "Synaptic Fusion Active",
    val repoUrl: String = "https://github.com/aslam/universal-dragon"
)

@Entity(tableName = "milestones")
data class Milestone(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val targetDate: String = "TBD",
    val status: String = "PENDING", // PENDING, ACTIVE, COMPLETED
    val progress: Float = 0f
)

@Entity(tableName = "team_members")
data class TeamMember(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val role: String,
    val bio: String = "",
    val isOnline: Boolean = false
)

@Entity(tableName = "project_resources")
data class ProjectResource(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val resourceName: String,
    val category: String,
    val description: String = "",
    val status: String = "ONLINE",
    val address: String = ""
)

// --- DAOs ---

@Dao
interface PiServiceDao {
    @Query("SELECT * FROM pi_services ORDER BY isCustom ASC, name ASC")
    fun getAllServicesFlow(): Flow<List<PiService>>

    @Query("SELECT * FROM pi_services ORDER BY isCustom ASC, name ASC")
    suspend fun getAllServices(): List<PiService>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: PiService): Long

    @Update
    suspend fun updateService(service: PiService)

    @Delete
    suspend fun deleteService(service: PiService)

    @Query("DELETE FROM pi_services WHERE isCustom = 1")
    suspend fun clearCustomServices()
}

@Dao
interface DragonLogDao {
    @Query("SELECT * FROM dragon_logs ORDER BY timestamp DESC LIMIT 200")
    fun getAllLogsFlow(): Flow<List<DragonLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DragonLog): Long

    @Query("DELETE FROM dragon_logs")
    suspend fun clearAllLogs()

    @Query("DELETE FROM dragon_logs WHERE id NOT IN (SELECT id FROM dragon_logs ORDER BY timestamp DESC LIMIT 200)")
    suspend fun pruneOldLogs()
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Query("SELECT * FROM users ORDER BY id ASC")
    fun getAllUsersFlow(): Flow<List<User>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUserById(id: Long)
}

@Dao
interface ProjectDetailDao {
    @Query("SELECT * FROM project_details WHERE id = 1 LIMIT 1")
    fun getProjectDetailFlow(): Flow<ProjectDetail?>

    @Query("SELECT * FROM project_details WHERE id = 1 LIMIT 1")
    suspend fun getProjectDetail(): ProjectDetail?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectDetail(projectDetail: ProjectDetail)
}

@Dao
interface MilestoneDao {
    @Query("SELECT * FROM milestones ORDER BY id ASC")
    fun getAllMilestonesFlow(): Flow<List<Milestone>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestone(milestone: Milestone): Long

    @Update
    suspend fun updateMilestone(milestone: Milestone)

    @Delete
    suspend fun deleteMilestone(milestone: Milestone)
}

@Dao
interface TeamMemberDao {
    @Query("SELECT * FROM team_members ORDER BY id ASC")
    fun getAllTeamMembersFlow(): Flow<List<TeamMember>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeamMember(member: TeamMember): Long

    @Update
    suspend fun updateTeamMember(member: TeamMember)

    @Delete
    suspend fun deleteTeamMember(member: TeamMember)
}

@Dao
interface ProjectResourceDao {
    @Query("SELECT * FROM project_resources ORDER BY id ASC")
    fun getAllResourcesFlow(): Flow<List<ProjectResource>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: ProjectResource): Long

    @Update
    suspend fun updateResource(resource: ProjectResource)

    @Delete
    suspend fun deleteResource(resource: ProjectResource)
}

// --- App Database ---

@Database(
    entities = [
        PiService::class,
        DragonLog::class,
        User::class,
        ProjectDetail::class,
        Milestone::class,
        TeamMember::class,
        ProjectResource::class
    ],
    version = 2,
    exportSchema = false
)
abstract class DragonDatabase : RoomDatabase() {
    abstract val piServiceDao: PiServiceDao
    abstract val dragonLogDao: DragonLogDao
    abstract val userDao: UserDao
    abstract val projectDetailDao: ProjectDetailDao
    abstract val milestoneDao: MilestoneDao
    abstract val teamMemberDao: TeamMemberDao
    abstract val projectResourceDao: ProjectResourceDao

    companion object {
        @Volatile
        private var INSTANCE: DragonDatabase? = null

        fun getInstance(context: Context): DragonDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DragonDatabase::class.java,
                    "universal_dragon_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
