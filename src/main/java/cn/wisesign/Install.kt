package cn.wisesign

import cn.wisesign.utils.*
import com.nikhaldimann.inieditor.IniEditor
import org.apache.commons.io.FileUtils
import java.io.*
import java.io.File.separator

class Install : CommandProcessor() {

    companion object{
        var REAL_SYSTEM_CONFIG_PATH = ""
        var mainAppName = ""
        var DOC_LIBRARY_BASE_PATH = "doc.library.base.path"
        var DOC_LIBRARY_DIRECTOR_NAME = "doclib"
        var ATTACHMENT_ROOT_PATH = "attachment.root.path"
        var ATTACHMENT_DIRECTOR_NAME = "attachment"
        var PROCESS_EXPORT_EXCEL_PATH = "process.export.excel.path"
        var PROCESS_EXPORT_DIRECTOR_NAME = "export"

        var INDEX_PATH = "index.path"
        var INDEX_DIRECTOR_NAME = "LuneneIndex"
        var IMS_HOME = "ims.home"
        var IMS_HOME_PATH = "server-ims/webapps/IMS"
        var SYSTEM_CONFIG_PATH = "/WEB-INF/classes/system_config.properties"
        var REPORT_EXEURL = "report.exeURL"
        var REPORT_PATH = "server-report/webapps/FineReport/WEB-INF"
    }

    override fun exec(args:Array<String>){

        val list = args.asList().drop(1)
        list.map { it.split("=") }
                .forEach {
                    if(it.size>1){
                        if(it[0] == "-mainAppName"){
                            mainAppName = it[1]
                        }
                    }else{
                        throw Exception(Exceptions.INVAILD_PARAM)
                    }
                }
        if(mainAppName == ""){
            throw Exception(Exceptions.INVAILD_PARAM)
        }

        copyJavaExeForImsWindows()
        applySystemConfigProperties()
        applyMysqlDbForLinux()

        println("Complete!!!")
        Thread.sleep(1000)
    }

    fun applyMysqlDbForLinux(){
        val mysqld_section = "mysqld"
        val client_section = "client"
        val mysql_home = "$imsHome/db"
        val mycnf_path = "$mysql_home/my.cnf"
        val basedir = mysql_home
        val datadir = "$basedir/data"
        val socket = "$basedir/mysql.sock"
        val log_error = "$basedir/logs/mysql-error.log"
        val pid_file = "$basedir/mysqldb.pid"
        val my_cnf = IniEditor()
        if(File(mycnf_path).exists()) {
            my_cnf.load(InputStreamReader(FileInputStream(mycnf_path), "UTF-8"))
            my_cnf.set(mysqld_section, "basedir", basedir)
            my_cnf.set(mysqld_section, "datadir", datadir)
            my_cnf.set(mysqld_section, "socket", socket)
            my_cnf.set(client_section, "socket", socket)
            my_cnf.set(mysqld_section, "log-error", log_error)
            my_cnf.set(mysqld_section, "pid-file", pid_file)
            my_cnf.save(FileOutputStream(mycnf_path))
        }
    }

    fun copyJavaExeForImsWindows(){
        val imsexe = "${imsHome}jre/bin/ims.exe"
        val javaexe = "${imsHome}jre/bin/java.exe"
        if(!File(imsexe).exists() && File(javaexe).exists()){
            FileUtils.copyFile(File(javaexe),File(imsexe))
        }
    }

    fun applySystemConfigProperties(){
        val system_config = CommentedProperties()
        REAL_SYSTEM_CONFIG_PATH = "${Tools.getMainAppDirectPath(mainAppName)}$SYSTEM_CONFIG_PATH"
        println(REAL_SYSTEM_CONFIG_PATH)
        try {
            val ins = FileInputStream(REAL_SYSTEM_CONFIG_PATH)
            system_config.load(InputStreamReader(ins,"UTF-8"))
            val out = FileOutputStream(REAL_SYSTEM_CONFIG_PATH)
            system_config.setProperty(DOC_LIBRARY_BASE_PATH, "$imsHome$separator$DOC_LIBRARY_DIRECTOR_NAME")
            system_config.setProperty(ATTACHMENT_ROOT_PATH, "$imsHome$separator$ATTACHMENT_DIRECTOR_NAME")
            system_config.setProperty(PROCESS_EXPORT_EXCEL_PATH, "$imsHome$separator$PROCESS_EXPORT_DIRECTOR_NAME")
            system_config.setProperty(INDEX_PATH, "$imsHome$separator$INDEX_DIRECTOR_NAME")
            system_config.setProperty(IMS_HOME, Tools.Companion.getMainAppDirectPath(mainAppName))
            system_config.setProperty(REPORT_EXEURL, "$imsHome$separator$REPORT_PATH")
            system_config.store(out)
        } catch (e: IOException) {
            println(e.message)
        }
    }

}
