package ru.serobyan.vk_photo_crawler.di

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.netty.handler.codec.http.DefaultHttpResponse
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponseStatus
import io.netty.handler.codec.http.HttpVersion
import net.lightbody.bmp.BrowserMobProxy
import net.lightbody.bmp.BrowserMobProxyServer
import net.lightbody.bmp.client.ClientUtil
import net.lightbody.bmp.proxy.CaptureType
import net.lightbody.bmp.util.HttpMessageContents
import net.lightbody.bmp.util.HttpMessageInfo
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.kodein.di.*
import org.openqa.selenium.Proxy
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.remote.CapabilityType
import ru.serobyan.vk_photo_crawler.app.App
import ru.serobyan.vk_photo_crawler.model.VkPhotoTable
import ru.serobyan.vk_photo_crawler.service.vk.cookie.CookieStorage
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.downloader.VkPhotoDownloader
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.ids_crawler.VkGroupPhotoIdsCrawler
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.ids_crawler.VkGroupPhotoIdsGetter
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.urls_crawler.VkGroupPhotoUrlGetter
import ru.serobyan.vk_photo_crawler.service.vk.group.photo.urls_crawler.VkGroupPhotoUrlsCrawler
import ru.serobyan.vk_photo_crawler.service.vk.login.VkLoginService
import java.net.Inet4Address
import javax.sql.DataSource

val seleniumModule = DI.Module("selenium") {
    bind<WebDriver>(tag = "proxy") with singleton {
        System.setProperty("webdriver.chrome.driver", Config.pathToChromeDriver)
        ChromeDriver(instance<ChromeOptions>(tag = "proxy"))
    }
    bind<WebDriver>(tag = "no-proxy") with singleton {
        System.setProperty("webdriver.chrome.driver", Config.pathToChromeDriver)
        ChromeDriver()
    }
}

val proxyModule = DI.Module("proxy") {
    bind<BrowserMobProxy>() with singleton {
        val proxy: BrowserMobProxy = BrowserMobProxyServer()
        proxy.addRequestFilter { request: HttpRequest, _: HttpMessageContents, _: HttpMessageInfo ->
            val forbidden = listOf(".jpg", ".png", ".gif")
            if (forbidden.any { it in request.uri }) {
                DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND)
            }
            null
        }
        proxy.enableHarCaptureTypes(CaptureType.RESPONSE_CONTENT)
        proxy.start()
        proxy
    }
    bind<Proxy>() with singleton {
        val proxyServer = instance<BrowserMobProxy>()
        val seleniumProxy = ClientUtil.createSeleniumProxy(proxyServer)
        val hostIp = Inet4Address.getLocalHost().hostAddress
        val port = proxyServer.port
        seleniumProxy.httpProxy = "$hostIp:$port"
        seleniumProxy.sslProxy = "$hostIp:$port"
        seleniumProxy
    }
    bind<ChromeOptions>(tag = "proxy") with singleton {
        val options = ChromeOptions()
        val seleniumProxy = instance<Proxy>()
        options.setCapability(CapabilityType.PROXY, seleniumProxy)
        options.addArguments("--ignore-certificate-errors")
        options
    }
}

val cookieStorageModule = DI.Module("cookie_storage") {
    bind<CookieStorage>() with singleton { CookieStorage() }
}

val downloadModule = DI.Module("download") {
    bind<VkPhotoDownloader>() with singleton {
        VkPhotoDownloader(
            photosDir = Config.photosDir
        )
    }
}

val dbModule = DI.Module("db") {
    bind<HikariConfig>() with singleton {
        HikariConfig().apply {
            jdbcUrl = Config.jdbcUrl
            driverClassName = "org.sqlite.JDBC"
            maximumPoolSize = 1
        }
    }
    bind<DataSource>() with singleton { HikariDataSource(instance()) }
    bind<Database>() with eagerSingleton {
        Database.connect(instance<DataSource>()).also {
            transaction { SchemaUtils.create(VkPhotoTable) }
        }
    }
}

val kvModule = DI.Module("vk") {
    bind<VkGroupPhotoIdsCrawler>() with singleton {
        VkGroupPhotoIdsCrawler(
            vkLoginService = instance(tag = "proxy"),
            vkGroupPhotoIdsGetter = instance()
        )
    }
    bind<VkGroupPhotoUrlsCrawler>() with singleton {
        VkGroupPhotoUrlsCrawler(
            vkLoginService = instance(tag = "no-proxy"),
            vkGroupPhotoUrlGetter = instance()
        )
    }
    bind<VkLoginService>(tag = "proxy") with singleton {
        VkLoginService(
            driver = instance(tag = "proxy"),
            cookieStorage = instance()
        )
    }
    bind<VkLoginService>(tag = "no-proxy") with singleton {
        VkLoginService(
            driver = instance(tag = "no-proxy"),
            cookieStorage = instance()
        )
    }
    bind<VkGroupPhotoIdsGetter>() with singleton {
        VkGroupPhotoIdsGetter(
            driver = instance(tag = "proxy"),
            proxy = instance()
        )
    }
    bind<VkGroupPhotoUrlGetter>() with singleton {
        VkGroupPhotoUrlGetter(
            driver = instance(tag = "no-proxy")
        )
    }
}

val appModule = DI.Module("app") {
    bind<App>() with singleton {
        App(
            vkGroupPhotoIdsCrawler = instance(),
            vkGroupPhotoUrlsCrawler = instance(),
            vkPhotoDownloader = instance(),
            driverWithProxy = instance(tag = "no-proxy"),
            driver = instance(tag = "proxy"),
            proxy = instance()
        )
    }
}

val di = DI {
    import(seleniumModule)
    import(proxyModule)
    import(cookieStorageModule)
    import(kvModule)
    import(dbModule)
    import(downloadModule)
    import(appModule)
}