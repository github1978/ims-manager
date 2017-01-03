@file:Suppress("unused")

package cn.wisesign.utils

import cn.wisesign.CommandProcessor
import org.apache.commons.net.ftp.FTPClient
import java.io.File
import org.slf4j.LoggerFactory



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

fun runShell(shellName:String):String{
	when(Tools.getOsType()){
		0 -> return "cmd.exe /c start $shellName.bat"
		1 -> return "sh $shellName.sh"
		else -> return ""
	}
}

val logger = LoggerFactory.getLogger(Tools::class.java)!!