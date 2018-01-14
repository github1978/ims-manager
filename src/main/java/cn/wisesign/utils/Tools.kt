@file:Suppress("unused")

package cn.wisesign.utils

import cn.wisesign.CommandProcessor
import org.apache.commons.io.filefilter.IOFileFilter
import org.hyperic.sigar.Sigar
import org.hyperic.sigar.SigarPermissionDeniedException
import java.io.File
import org.slf4j.LoggerFactory
import java.util.*
import java.util.ArrayList
import java.text.SimpleDateFormat


val logger = LoggerFactory.getLogger(Tools::class.java)!!

class Tools {

	companion object{
		fun getSelfPath():String {
			when(getOsType()){
				1 -> return "/"+ Tools::class.java.protectionDomain.codeSource.location.path.split("/ims-manager.jar")[0].substring(1)
				else -> return Tools::class.java.classLoader.getResource("").path
			}
		}
		fun getOsType():Int{
			var os = System.getProperty("os.name")
			when{
				os.toLowerCase().contains("win") -> return 0
				os.toLowerCase().contains("linux") -> return 1
				else -> return -1
			}
		}
	}

}

fun Tools.Companion.getMainAppDirectName(mainAppName:String):String{
	val webapps = "${CommandProcessor.imsHome}/server-$mainAppName/webapps/"
	File(webapps).list()
			.filter { File("$webapps/$it/WEB-INF/classes/system_config.properties").exists() }
			.forEach { return it }
	return ""
}

fun Tools.Companion.getMainAppDirectPath(mainAppName:String):String{
	val webapps = "${CommandProcessor.imsHome}/server-$mainAppName/webapps/"
	File(webapps).list()
			.filter { File("$webapps/$it/WEB-INF/classes/system_config.properties").exists() }
			.forEach { return "$webapps/$it/" }
	return ""
}

fun String.asPath():String{
	when(Tools.getOsType()){
		1 -> return this.replace("\\","/")
		0 -> {
			if(this.startsWith("/")){
				return this.drop(1).replace("/","\\")
			}
			return this.replace("/","\\")
		}
		else -> return this
	}
}

fun String.asArray():List<String>{
	return if(this.contains(",")){
		this.split(",")
	}else{
		ArrayList<String>()
	}
}

fun runShell(shellName:String):String{
	when(Tools.getOsType()){
		0 -> return "cmd.exe /c start $shellName.bat"
		1 -> return "sh $shellName.sh"
		else -> return ""
	}
}

fun Tools.Companion.sigarGetProcesslist():List<Map<String,Any>>{
    initSigar()
	val sigar = Sigar()
    val result = ArrayList<Map<String,Any>>()
	sigar.procList.forEach loop@{
        try {
            val processInfoMap = HashMap<String,Any>()
            val processInfo = sigar.getProcExe(it)
            processInfoMap.put("pid",it)
            processInfoMap.put("execpath",processInfo.cwd)
            val pnameStrArr = processInfo.name.split(File.separator)
            processInfoMap.put("pname",pnameStrArr[pnameStrArr.size-1])
            result.add(processInfoMap)
        }catch (e: SigarPermissionDeniedException){
            return@loop
        }
	}
    return result
}

fun Tools.Companion.initSigar(){
    val envKey = "java.library.path"
    var javalibpath = System.getProperty(envKey)
    val selfpath = Tools.getSelfPath().asPath()
    if(!javalibpath.contains(selfpath)){
        if(Tools.getOsType()==0){
            javalibpath += ";$selfpath\\lib"
        }else{
            javalibpath += ":$selfpath/lib"
        }
        System.setProperty(envKey,javalibpath)
    }
}

fun Tools.Companion.getNowTimeSpecialStr():String{
	return SimpleDateFormat("yyyyMMddHHmmss").format(Date())
}

class FileNameFilter(val fileNames:List<String>): IOFileFilter {
	override fun accept(file: File): Boolean {
		fileNames.forEach {
			if(it == file.name){
				return false
			}
		}
		return true
	}

	override fun accept(dir: File?, name: String?): Boolean {
        fileNames.forEach {
            if(it == dir?.name) {
                return false
            }
        }
        return true
	}
}
