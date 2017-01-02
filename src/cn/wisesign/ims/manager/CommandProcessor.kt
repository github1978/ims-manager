package cn.wisesign.ims.manager

import cn.wisesign.ims.manager.utils.Tools
import java.io.File
import java.text.SimpleDateFormat

abstract class CommandProcessor {

    companion object{
        var imsHome:String = Tools.getSelfPath()
        var PROCESS_FILE = File("${imsHome}PROCESS_SIGN")
        var BACKUP_PROCESS_FILE = File("${imsHome}BACKUP_PROCESS_SIGN")
        var LOG_DATEFORMAT = SimpleDateFormat("yyyyMMddHHmmss.sss")
    }

    abstract fun exec(args:Array<String>)
}
