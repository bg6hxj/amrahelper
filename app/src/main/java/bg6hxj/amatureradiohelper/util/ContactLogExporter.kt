package bg6hxj.amatureradiohelper.util

import android.content.Context
import android.net.Uri
import bg6hxj.amatureradiohelper.data.entity.ContactLog
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVPrinter
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * 通联日志导出数据模型 (用于 JSON 序列化)
 */
@Serializable
data class ContactLogExportData(
    val version: Int = 1,
    val exportTime: String,
    val logs: List<ContactLogJson>
)

@Serializable
data class ContactLogJson(
    val contactTime: Long,
    val mode: String,
    val frequency: String,
    val cqZone: String? = null,
    val myCallsign: String,
    val theirCallsign: String,
    val rstSent: String,
    val rstReceived: String,
    val myPower: String? = null,
    val theirPower: String? = null,
    val theirQth: String? = null,
    val equipment: String? = null,
    val antenna: String? = null,
    val notes: String? = null
)

/**
 * 通联日志导入/导出工具类
 */
object ContactLogExporter {
    
    private val json = Json { 
        prettyPrint = true 
        ignoreUnknownKeys = true
    }
    
    private val dateFormatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.of("UTC"))

    // CSV 列定义
    private val CSV_HEADERS = arrayOf(
        "contactTime", "mode", "frequency", "cqZone", 
        "myCallsign", "theirCallsign", "rstSent", "rstReceived",
        "myPower", "theirPower", "theirQth", "equipment", "antenna", "notes"
    )

    /**
     * 导出日志为 JSON 格式
     */
    fun exportToJson(context: Context, uri: Uri, logs: List<ContactLog>): Result<Int> {
        return try {
            val exportData = ContactLogExportData(
                version = 1,
                exportTime = dateFormatter.format(Instant.now()),
                logs = logs.map { it.toJsonModel() }
            )
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                BufferedWriter(OutputStreamWriter(outputStream, Charsets.UTF_8)).use { writer ->
                    writer.write(json.encodeToString(exportData))
                }
            }
            
            Result.success(logs.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 导出日志为 CSV 格式
     */
    fun exportToCsv(context: Context, uri: Uri, logs: List<ContactLog>): Result<Int> {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                BufferedWriter(OutputStreamWriter(outputStream, Charsets.UTF_8)).use { writer ->
                    val csvFormat = CSVFormat.DEFAULT.builder()
                        .setHeader(*CSV_HEADERS)
                        .build()
                    
                    CSVPrinter(writer, csvFormat).use { printer ->
                        logs.forEach { log ->
                            printer.printRecord(
                                log.contactTime,
                                log.mode,
                                log.frequency,
                                log.cqZone ?: "",
                                log.myCallsign,
                                log.theirCallsign,
                                log.rstSent,
                                log.rstReceived,
                                log.myPower ?: "",
                                log.theirPower ?: "",
                                log.theirQth ?: "",
                                log.equipment ?: "",
                                log.antenna ?: "",
                                log.notes ?: ""
                            )
                        }
                    }
                }
            }
            
            Result.success(logs.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 从 JSON 文件导入日志
     */
    fun importFromJson(context: Context, uri: Uri): Result<List<ContactLog>> {
        return try {
            val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            } ?: return Result.failure(Exception("无法读取文件"))
            
            val exportData = json.decodeFromString<ContactLogExportData>(content)
            val logs = exportData.logs.map { it.toEntity() }
            
            Result.success(logs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 从 CSV 文件导入日志
     */
    fun importFromCsv(context: Context, uri: Uri): Result<List<ContactLog>> {
        return try {
            val logs = mutableListOf<ContactLog>()
            
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8)).use { reader ->
                    val csvFormat = CSVFormat.DEFAULT.builder()
                        .setHeader()
                        .setSkipHeaderRecord(true)
                        .setIgnoreEmptyLines(true)
                        .setTrim(true)
                        .build()
                    
                    CSVParser(reader, csvFormat).use { parser ->
                        for (record in parser) {
                            try {
                                val log = ContactLog(
                                    id = 0, // 新生成 ID
                                    contactTime = record.get("contactTime").toLongOrNull() ?: System.currentTimeMillis(),
                                    mode = record.get("mode"),
                                    frequency = record.get("frequency"),
                                    cqZone = record.get("cqZone").takeIf { it.isNotBlank() },
                                    myCallsign = record.get("myCallsign"),
                                    theirCallsign = record.get("theirCallsign"),
                                    rstSent = record.get("rstSent"),
                                    rstReceived = record.get("rstReceived"),
                                    myPower = record.get("myPower").takeIf { it.isNotBlank() },
                                    theirPower = record.get("theirPower").takeIf { it.isNotBlank() },
                                    theirQth = record.get("theirQth").takeIf { it.isNotBlank() },
                                    equipment = record.get("equipment").takeIf { it.isNotBlank() },
                                    antenna = record.get("antenna").takeIf { it.isNotBlank() },
                                    notes = record.get("notes").takeIf { it.isNotBlank() }
                                )
                                logs.add(log)
                            } catch (e: Exception) {
                                // 跳过解析失败的行
                                continue
                            }
                        }
                    }
                }
            }
            
            Result.success(logs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 扩展函数：ContactLog -> ContactLogJson
    private fun ContactLog.toJsonModel() = ContactLogJson(
        contactTime = contactTime,
        mode = mode,
        frequency = frequency,
        cqZone = cqZone,
        myCallsign = myCallsign,
        theirCallsign = theirCallsign,
        rstSent = rstSent,
        rstReceived = rstReceived,
        myPower = myPower,
        theirPower = theirPower,
        theirQth = theirQth,
        equipment = equipment,
        antenna = antenna,
        notes = notes
    )

    // 扩展函数：ContactLogJson -> ContactLog
    private fun ContactLogJson.toEntity() = ContactLog(
        id = 0, // 新生成 ID
        contactTime = contactTime,
        mode = mode,
        frequency = frequency,
        cqZone = cqZone,
        myCallsign = myCallsign,
        theirCallsign = theirCallsign,
        rstSent = rstSent,
        rstReceived = rstReceived,
        myPower = myPower,
        theirPower = theirPower,
        theirQth = theirQth,
        equipment = equipment,
        antenna = antenna,
        notes = notes
    )
}
