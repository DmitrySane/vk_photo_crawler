package ru.serobyan.vk_photo_crawler.app.arguments

import org.apache.commons.cli.*
import ru.serobyan.vk_photo_crawler.utils.logging.IOperationLogger
import ru.serobyan.vk_photo_crawler.utils.logging.subOperationLog
import ru.serobyan.vk_photo_crawler.utils.logging.subOrRootOperationLog

object ArgumentsParser {
    suspend fun parse(args: Array<String>, logger: IOperationLogger? = null): Arguments {
        return logger.subOrRootOperationLog("parse_cli_args") { subLogger ->
            try {
                subLogger.put("args", args)
                val cmd = parser.parse(options, args)
                val commands = parseCommands(cmd.getOptionValue(optionCommands))
                val arguments = Arguments(
                    commands = commands.takeIf { it.isNotEmpty() } ?: AppCommand.default,
                    groupUrl = cmd.getOptionValue(optionVkGroupUrl),
                    login = cmd.getOptionValue(optionLogin),
                    password = cmd.getOptionValue(optionPassword),
                    photoLimit = cmd.getOptionValue(optionPhotoLimit)?.toInt() ?: Int.MAX_VALUE
                )
                subLogger.put("parsed_args", arguments)
                arguments
            } catch (e: ParseException) {
                formatter.printHelp("vk_photo_crawler", options)
                throw e
            }
        }
    }

    private fun parseCommands(commands: String?): Set<AppCommand> {
        return commands?.map { AppCommand.fromConsoleCommand(it.toString()) }?.toSet() ?: setOf()
    }

    private val parser: CommandLineParser = DefaultParser()
    private val formatter = HelpFormatter()

    private const val optionCommands = "commands"
    private const val optionPhotoLimit = "photo_limit"
    private const val optionVkGroupUrl = "vk_group_url"
    private const val optionLogin = "login"
    private const val optionPassword = "password"

    private val commands = Option.builder("c")
        .longOpt(optionCommands)
        .desc(AppCommand.availableConsoleCommands)
        .numberOfArgs(1)
        .required(false)
        .build()

    private val photoLimit = Option.builder("pl")
        .longOpt(optionPhotoLimit)
        .desc("For example '100'")
        .numberOfArgs(1)
        .required(false)
        .build()

    private val vkGroupUrl = Option.builder("g")
        .longOpt(optionVkGroupUrl)
        .desc("For example 'https://vk.com/academicart'")
        .numberOfArgs(1)
        .required(true)
        .build()

    private val login = Option.builder("l")
        .longOpt(optionLogin)
        .desc("Login for vk")
        .numberOfArgs(1)
        .required(true)
        .build()

    private val password = Option.builder("p")
        .longOpt(optionPassword)
        .desc("Password for vk")
        .numberOfArgs(1)
        .required(true)
        .build()

    val options = Options().apply {
        addOption(commands)
        addOption(photoLimit)
        addOption(vkGroupUrl)
        addOption(login)
        addOption(password)
    }
}