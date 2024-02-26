import com.tencent.bscp.helper.OptionHelper
import com.tencent.bscp.pojo.AppOption
import com.tencent.bscp.sdk.Client
import java.io.InputStream
import java.util.LinkedList
import kotlin.system.exitProcess
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml


fun main(args: Array<String>) {
    val test = PullFileTest()
    test.execute()
}

class PullFileTest {
    fun execute() {
        val config = loadConfig()
        LOGGER.isDebugEnabled
        val configItem: TestConfigItem = config.config!!
        val bscp: Client = try {
            Client(
                OptionHelper.withFeedAddrs(config.feedAddrs),
                OptionHelper.withBizID(config.biz),
                OptionHelper.withToken(config.token)
            )
        } catch (e: Exception) {
            LOGGER.error("init client failed", e)
            exitProcess(1)
            return
        }
        val opts: List<AppOption> = ArrayList()
        pullAppFiles(bscp, configItem.app, opts)
    }

    private fun pullAppFiles(bscp: Client, app: String?, opts: List<AppOption>) {
        try {
            val release = bscp.pullFiles(app, *opts.toTypedArray())

            // 文件列表, 可以自定义操作，如查看content, 写入文件等
            for (f in release.fileItems) {
                LOGGER.info("get event done. release id {}, item {}.", release.releaseID, f)
            }
        } catch (e: Exception) {
            LOGGER.error("Error pulling app files", e)
        }
    }

    private fun loadConfig(): Config {
        val inputStream: InputStream? = PullFileTest::class.java.getResourceAsStream("/config.yml")
        // 创建 YAML 解析器
        val yaml = Yaml()
        return yaml.loadAs(inputStream, Config::class.java)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(PullFileTest::class.java)
    }
}

class Config {
    var feedAddrs: LinkedList<String>? = null
    var biz = 0
    var token: String? = null
    var labels: java.util.ArrayList<LinkedHashMap<String, String>>? = null
    var tempDir: String? = null
    var config: TestConfigItem? = null
}

class TestConfigItem {
    var watchMode: Boolean? = null
    var keys: String? = null
    var key: String? = null
    var uid: String? = null
    var app: String? = null
    var labels: LinkedHashMap<String, String>? = null
}
