package cn.wisesign

import cn.wisesign.utils.Exceptions
import cn.wisesign.utils.Tools
import cn.wisesign.utils.getMainAppDirectPath
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Properties

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

        applySystemConfigProperties()

        println("Complete!!!")
        Thread.sleep(1000)
    }

    fun copyJavaExeForIms(){

    }

    fun applySystemConfigProperties(){
        var system_config = Properties()
        REAL_SYSTEM_CONFIG_PATH = "${Tools.getMainAppDirectPath(mainAppName)}$SYSTEM_CONFIG_PATH"
        println(REAL_SYSTEM_CONFIG_PATH)
        try {
            val ins = FileInputStream(REAL_SYSTEM_CONFIG_PATH)
            system_config.load(ins)
            var out = FileOutputStream(REAL_SYSTEM_CONFIG_PATH)
            system_config.put(DOC_LIBRARY_BASE_PATH, "$imsHome$DOC_LIBRARY_DIRECTOR_NAME")
            system_config.put(ATTACHMENT_ROOT_PATH, "$imsHome$ATTACHMENT_DIRECTOR_NAME")
            system_config.put(PROCESS_EXPORT_EXCEL_PATH, "$imsHome$PROCESS_EXPORT_DIRECTOR_NAME")
            system_config.put(INDEX_PATH, "$imsHome$INDEX_DIRECTOR_NAME")
            system_config.put(IMS_HOME, "$imsHome$IMS_HOME_PATH")
            system_config.put(REPORT_EXEURL, "$imsHome$REPORT_PATH")
            system_config.store(out, "")
        } catch (e: IOException) {
            println(e.message)
        }
    }

}
