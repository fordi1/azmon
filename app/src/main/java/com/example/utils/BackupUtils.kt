package com.example.utils

import android.content.Context
import android.net.Uri
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupUtils {
    private const val DB_NAME = "exam_designer_database"
    private val allowedEntries = setOf(DB_NAME, "$DB_NAME-wal", "$DB_NAME-shm")

    fun backupDatabaseToZip(context: Context, zipUri: Uri) {
        val dbFile = context.getDatabasePath(DB_NAME)
        val walFile = context.getDatabasePath("$DB_NAME-wal")
        val shmFile = context.getDatabasePath("$DB_NAME-shm")

        val filesToZip = listOfNotNull(
            if (dbFile.exists()) dbFile else null,
            if (walFile.exists()) walFile else null,
            if (shmFile.exists()) shmFile else null
        )

        val outputStream = requireNotNull(context.contentResolver.openOutputStream(zipUri)) {
            "Unable to open backup destination"
        }
        outputStream.use {
            ZipOutputStream(it).use { zos ->
                for (file in filesToZip) {
                    FileInputStream(file).use { fis ->
                        val entry = ZipEntry(file.name)
                        zos.putNextEntry(entry)
                        fis.copyTo(zos)
                        zos.closeEntry()
                    }
                }
            }
        }
    }

    fun restoreDatabaseFromZip(context: Context, zipUri: Uri) {
        val dbFile = context.getDatabasePath(DB_NAME)
        val dbDir = dbFile.parentFile
        if (dbDir != null && !dbDir.exists()) {
            dbDir.mkdirs()
        }

        val inputStream = requireNotNull(context.contentResolver.openInputStream(zipUri)) {
            "Unable to open backup file"
        }
        var restoredDatabase = false
        inputStream.use {
            ZipInputStream(it).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    require(!entry.isDirectory && entry.name in allowedEntries) {
                        "Invalid backup entry: ${entry.name}"
                    }
                    val outputFile = context.getDatabasePath(entry.name)
                    FileOutputStream(outputFile).use { fos ->
                        zis.copyTo(fos)
                    }
                    if (entry.name == DB_NAME) restoredDatabase = true
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }
        }
        require(restoredDatabase) { "Backup does not contain the database file" }
    }
}
