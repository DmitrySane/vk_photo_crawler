package ru.serobyan.vk_photo_crawler.app

import ru.serobyan.vk_photo_crawler.app.arguments.AppCommand
import ru.serobyan.vk_photo_crawler.app.arguments.Arguments
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.downloader.VkPhotoDownloader
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.downloader.VkPhotoDownloaderContext
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.ids_crawler.VkGroupPhotoIdsCrawler
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.ids_crawler.VkGroupPhotoIdsCrawlerContext
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.urls_crawler.VkGroupPhotoUrlsCrawler
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.urls_crawler.VkGroupPhotoUrlsCrawlerContext
import ru.serobyan.vk_photo_crawler.utils.logging.operationLog
import java.io.Closeable

class App(
    private val vkGroupPhotoIdsCrawler: VkGroupPhotoIdsCrawler,
    private val vkGroupPhotoUrlsCrawler: VkGroupPhotoUrlsCrawler,
    private val vkPhotoDownloader: VkPhotoDownloader
) : Closeable {
    suspend fun run(arguments: Arguments) {
        operationLog("app_run") {
            arguments.commands
                .sortedBy { it.priority }
                .forEach { command ->
                    when (command) {
                        AppCommand.CRAWL_PHOTO_IDS -> vkGroupPhotoIdsCrawler.crawlPhotoIds(
                            VkGroupPhotoIdsCrawlerContext(
                                groupUrl = arguments.groupUrl,
                                login = arguments.login,
                                password = arguments.password
                            )
                        )
                        AppCommand.CRAWL_PHOTO_URLS -> vkGroupPhotoUrlsCrawler.crawlPhotoUrls(
                            VkGroupPhotoUrlsCrawlerContext(
                                login = arguments.login,
                                password = arguments.password,
                                groupUrl = arguments.groupUrl
                            )
                        )
                        AppCommand.DOWNLOAD_PHOTOS ->
                            vkPhotoDownloader.downloadPhotos(VkPhotoDownloaderContext())
                    }
                }
        }
    }

    override fun close() {
        vkGroupPhotoIdsCrawler.close()
        vkGroupPhotoUrlsCrawler.close()
    }
}