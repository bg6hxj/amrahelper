package bg6hxj.amatureradiohelper.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import bg6hxj.amatureradiohelper.data.entity.ContactLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contactLog: ContactLog): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<ContactLog>)

    @Update
    suspend fun update(contactLog: ContactLog)

    @Delete
    suspend fun delete(contactLog: ContactLog)

    @Query("SELECT * FROM contact_logs ORDER BY contactTime DESC")
    fun getAllLogs(): Flow<List<ContactLog>>

    @Query("SELECT * FROM contact_logs WHERE id = :id")
    suspend fun getLogById(id: Long): ContactLog?
    
    @Query("SELECT COUNT(*) FROM contact_logs")
    fun getLogCount(): Flow<Int>
}
