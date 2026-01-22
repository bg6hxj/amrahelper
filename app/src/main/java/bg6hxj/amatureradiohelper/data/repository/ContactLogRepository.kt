package bg6hxj.amatureradiohelper.data.repository

import bg6hxj.amatureradiohelper.data.dao.ContactLogDao
import bg6hxj.amatureradiohelper.data.entity.ContactLog
import kotlinx.coroutines.flow.Flow

class ContactLogRepository(private val contactLogDao: ContactLogDao) {

    fun getAllLogs(): Flow<List<ContactLog>> {
        return contactLogDao.getAllLogs()
    }
    
    fun getLogCount(): Flow<Int> {
        return contactLogDao.getLogCount()
    }

    fun getLogCountAfter(timestamp: Long): Flow<Int> {
        return contactLogDao.getLogCountAfter(timestamp)
    }

    fun getLogsFiltered(query: String, startTime: Long, endTime: Long): Flow<List<ContactLog>> {
        return contactLogDao.getLogsFiltered(query, startTime, endTime)
    }

    suspend fun getLogById(id: Long): ContactLog? {
        return contactLogDao.getLogById(id)
    }

    suspend fun insertLog(contactLog: ContactLog): Long {
        return contactLogDao.insert(contactLog)
    }

    suspend fun insertAll(logs: List<ContactLog>) {
        contactLogDao.insertAll(logs)
    }

    suspend fun updateLog(contactLog: ContactLog) {
        contactLogDao.update(contactLog)
    }

    suspend fun deleteLog(contactLog: ContactLog) {
        contactLogDao.delete(contactLog)
    }
}
