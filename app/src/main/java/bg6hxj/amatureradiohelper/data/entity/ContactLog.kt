package bg6hxj.amatureradiohelper.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contact_logs")
data class ContactLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contactTime: Long, // UTC timestamp
    val mode: String, // SSB/FM/CW/AM/RTTY
    val frequency: String,
    val cqZone: String?,
    val myCallsign: String,
    val theirCallsign: String,
    val rstSent: String,
    val rstReceived: String,
    val myPower: String?,
    val theirPower: String?,
    val theirQth: String?,
    val equipment: String?,
    val antenna: String?,
    val notes: String?
)
