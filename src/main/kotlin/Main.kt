package svcs

import java.io.File
import java.security.MessageDigest

fun hash(input: String): String {
    return MessageDigest
        .getInstance("SHA-256")
        .digest(input.toByteArray())
        .fold("") { str, it -> str + "%02x".format(it) }
}

fun allCommands() {
    println("These are SVCS commands:")
    println("config     Get and set a username.")
    println("add        Add a file to the index.")
    println("log        Show commit logs.")
    println("commit     Save changes.")
    println("checkout   Restore a file.")
}

fun fileStructure() {
    val vcsFolder = File("./vcs")
    val commitsFolder = File("./vcs/commits")
    val configFile = File("./vcs/config.txt")
    val indexFile = File("./vcs/index.txt")
    val logFile = File("./vcs/log.txt")
    if (!File("./vcs").exists()) {
        vcsFolder.mkdir()
    }
    if (!File("./vcs/commits").exists()) {
        commitsFolder.mkdir()
    }
    if (!File("./vcs/config.txt").exists()) {
        configFile.createNewFile()
    }
    if (!File("./vcs/index.txt").exists()) {
        indexFile.createNewFile()
    }
    if (!File("./vcs/log.txt").exists()) {
        logFile.createNewFile()
    }
}

fun main(args: Array<String>) {

    fileStructure()

    if (args.isEmpty() || args[0] == "--help") {
        allCommands()
    } else when (args[0]) {
        "config" -> {
            if (args.size <= 1) {
                val indexLine = File("./vcs/index.txt").readText()
                if (indexLine.isNotEmpty()) {
                    println("The username is $indexLine.")
                } else {
                    println("Please, tell me who you are.")
                }
            } else if (args.size <= 2) {
                val indexLine = File("./vcs/index.txt")
                indexLine.writeText(args[1])
                println("The username is ${indexLine.readText()}.")
            }
        }
        "add" -> {
            if (args.size <= 1) {
                val configList = File("./vcs/config.txt").readText()
                if (configList.isEmpty()) {
                    println("Add a file to the index.")
                } else {
                    println("Tracked files:")
                    println(configList)
                }
            } else if (args.size <= 2) {
                if (File("./${args[1]}").exists()) {
                    val configList = File("./vcs/config.txt").readText()
                    if (configList.isEmpty()) {
                        File("./vcs/config.txt").writeText(args[1])
                        println("The file \'${args[1]}\' is tracked.")
                    } else {
                        File("./vcs/config.txt").appendText("\n" + args[1])
                        println("The file \'${args[1]}\' is tracked.")
                    }
                } else {
                    println("Can't find '${args[1]}'.")
                }
            }
        }
        "log" -> {
            val logLine = File("./vcs/log.txt").readText()
            if (logLine.isEmpty()) {
                println("No commits yet.")
            } else {
                println(logLine)
            }
        }
        "commit" -> {
            if (args.size <= 1) {
                println("Message was not passed.")
            } else {
                // checking tracked files and creating common hash name
                val configFileCheck = File("./vcs/config.txt").readLines()
                var commonTextForHash = ""
                for (fileName in configFileCheck) {
                    if (File("./$fileName").exists()) {
                        commonTextForHash += File("./$fileName").readText()
                    }
                }
                val hashName = hash(commonTextForHash)
                // checking if the same hash exists
                if (File("./vcs/commits/$hashName").exists() || configFileCheck.isEmpty()) {
                    println("Nothing to commit.")
                } else {
                    // creating new hash-name folder
                    val hashFolder = File("./vcs/commits/$hashName")
                    if (!File("./vcs/commits/$hashName").exists()) {
                        hashFolder.mkdir()
                    }
                    // copying tracked files to the new hash-name folder
                    for (fileName in configFileCheck) {
                        if (File("./$fileName").exists()) {
                            File("./$fileName").copyTo(File("./vcs/commits/$hashName/$fileName"))
                        }
                    }
                    // writing the log
                    val userName = File("./vcs/index.txt").readText()
                    val logPrevious = File("./vcs/log.txt").readText()
                    File("./vcs/log.txt").writeText("commit $hashName")
                    File("./vcs/log.txt").appendText("\nAuthor: $userName")
                    File("./vcs/log.txt").appendText("\n${args[1]}")
                    File("./vcs/log.txt").appendText("\n")
                    File("./vcs/log.txt").appendText("\n$logPrevious")
                    println("Changes are committed.")
                }
            }
        }
        "checkout" -> {
            if (args.size <= 1) {
                println("Commit id was not passed.")
            } else {
                if (!File("./vcs/commits/${args[1]}").exists()) {
                    println("Commit does not exist.")
                } else {
                    val configFileCheck = File("./vcs/config.txt").readLines()
                    for (fileName in configFileCheck) {
                        if (File("./vcs/commits/${args[1]}/$fileName").exists()) {
                            File("./vcs/commits/${args[1]}/$fileName").copyTo(File("./$fileName"), overwrite = true)
                        }
                    }
                    println("Switched to commit ${args[1]}.")
                }
            }
        }
        else -> println("\'${args[0]}\' is not a SVCS command.")
    }
}