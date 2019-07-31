package org.onap.ccsdk.cds.blueprintsprocessor

import org.junit.rules.TemporaryFolder
import java.io.File
import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes
import javax.annotation.PreDestroy

class ExtendedTemporaryFolder {
    private val tempFolder = TemporaryFolder()

    init {
        tempFolder.create()
    }

    @PreDestroy
    fun delete() = tempFolder.delete()

    /**
     * A delegate to org.junit.rules.TemporaryFolder.TemporaryFolder.newFolder(String).
     */
    fun newFolder(folder: String): File = tempFolder.newFolder(folder)

    /**
     * Delete all files under the root temporary folder recursively. The folders are preserved.
     */
    fun deleteAllFiles() {
        Files.walkFileTree(tempFolder.root.toPath(), object : SimpleFileVisitor<Path>() {
            @Throws(IOException::class)
            override fun visitFile(file: Path?, attrs: BasicFileAttributes?): FileVisitResult {
                file?.toFile()?.delete()
                return FileVisitResult.CONTINUE
            }
        })
    }
}
