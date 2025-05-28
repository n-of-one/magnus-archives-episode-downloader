package org.n_of_one

import java.io.File
import java.io.Writer

const val POSTS_ROOT = "../magnus_archives_transcripts/_posts/"
const val VAULT_ROOT = "../magnus-archives-analysis"
const val TRANSCRIPT_ROOT = "$VAULT_ROOT/transcripts/"
const val EPISODE_ROOT = "$VAULT_ROOT/episodes/"
const val preludes = 3

fun main() {

    val startEpisode = 21
    val numberOfEpisodes = 10
    val replaceExistingFiles = false


    val files = File(POSTS_ROOT).listFiles()!!
    val drop = preludes + startEpisode -1
    val rfiles = files.drop(drop).take(numberOfEpisodes)

    rfiles.forEach {
        transformFile(it, replaceExistingFiles)
    }
}

fun transformFile(file: File, replaceExistingFiles: Boolean) {

    val fileName = file.name

    val lines = file.readLines()
    val title = getProperty(lines, "episode_title", fileName)
    val episodeNumber = getProperty(lines, "episode_number", fileName)
    val caseNumber = getProperty(lines, "case_number", fileName)


    println( "Processing ${episodeNumber} : ${title}" )

    val transcriptLines = removeMetadata(lines, fileName)
    val sanitizedTranscriptLines = sanitize(transcriptLines)

    createTranscriptFile(title, episodeNumber, caseNumber, sanitizedTranscriptLines, replaceExistingFiles)
    createAnalysisFile(title, episodeNumber, replaceExistingFiles)

}

fun getProperty(lines: List<String>, property: String, fileName: String): String {
    val line = lines.find { it.startsWith(property) } ?: return "$property not found $fileName"
    val value = line.substringAfter("$property:").trim()
    return removeQuotes(value)
}

fun removeQuotes(input: String): String {
    var text = input
    if (text.startsWith("'") || text.startsWith("\"")) text = text.substring(1, text.length - 1)
    if (text.endsWith("'") || text.endsWith("\"")) text = text.substring(0, text.length - 2)

    return text
}

fun removeMetadata(lines: List<String>, fileName: String): List<String> {

    val linesAfterMetadataStart = lines.drop(1)
    val metadataEndIndex = linesAfterMetadataStart.indexOf("---")
    if (metadataEndIndex == -1) error("failed to parse metadata for $fileName")

    return linesAfterMetadataStart.drop(metadataEndIndex + 2)
}

fun sanitize(lines: List<String>): List<String> {
    return lines.map { line ->
        val trimmedLine = line.trim()
        sanitize(trimmedLine)
    }
}

fun sanitize(line: String): String {
    return line.replace("[CLICK]", "CLICK")
}

fun Writer.writeLine() {
    this.write("\n")
}

fun Writer.writeLine(line: String) {
    this.write(line)
    this.writeLine()
}

fun createTranscriptFileName(episodeNumber: String, title: String) = "Transcript ${episodeNumber} $title.md"

fun createTranscriptFile(title: String, episodeNumber: String, caseNumber: String, body: List<String>,
                         replaceExistingFiles: Boolean) {
    val fileName = createTranscriptFileName(episodeNumber, title)
    val file = File(TRANSCRIPT_ROOT, fileName)

    if (file.exists() && !replaceExistingFiles) {
        println("Skipping (file already exists) : $fileName")
        return
    }

    file.outputStream().writer().use { writer ->
        writer.writeLine("---")
        writer.writeLine("tags:")
        writer.writeLine("  - \"#transcript\"")
        writer.writeLine("  - \"#e$episodeNumber\"")
        writer.writeLine("---")
        writer.writeLine("# Transcript: $title")
        writer.writeLine()
        writer.writeLine("Episode: $episodeNumber")
        writer.writeLine("Case: $caseNumber")
        writer.writeLine()
        body.forEach { writer.writeLine(it)}
    }

    println( "Written: $fileName " )
}

fun createAnalysisFile(title: String, episodeNumber: String, replaceExistingFiles: Boolean) {
    val fileName = "${episodeNumber} - $title.md"
    val file = File(EPISODE_ROOT, fileName)

    if (file.exists() && !replaceExistingFiles) {
        println("Skipping (file already exists) : $fileName")
        return
    }

    val transcriptFileName = createTranscriptFileName(episodeNumber, title)

    file.outputStream().writer().use { writer ->
        writer.writeLine("---")
        writer.writeLine("transcript: \"[[${transcriptFileName.substringBeforeLast(".md")}]]\"")
        writer.writeLine("statement_of:")
        writer.writeLine("statement_date:")
        writer.writeLine("recorder: Jonathan Sims")
        writer.writeLine("event_date:")
        writer.writeLine("event_period:")
        writer.writeLine("event_place:")
        writer.writeLine("tags:")
        writer.writeLine("  - \"#analsysis\"")
        writer.writeLine("  - \"#e$episodeNumber\"")
        writer.writeLine("---")
        writer.writeLine("# $title")
        writer.writeLine()
        writer.writeLine("## People of interest")
        writer.writeLine("- (statement giver)")
        writer.writeLine("- ")
        writer.writeLine()
        writer.writeLine("## Concepts")
        writer.writeLine("- ")
        writer.writeLine()
    }

    println( "Written: $fileName " )

}

