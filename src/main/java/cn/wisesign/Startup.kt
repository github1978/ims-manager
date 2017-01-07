package cn.wisesign

import java.io.BufferedReader
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.ArrayList

import cn.wisesign.utils.Exceptions
import cn.wisesign.utils.Tools
import cn.wisesign.utils.asPath
import cn.wisesign.utils.getMainAppDirectName
import org.dom4j.Element
import org.dom4j.io.SAXReader
import java.io.File.separator
import java.io.IOException
import java.io.InputStreamReader

import java.lang.System.exit

class Startup : CommandProcessor(){

	companion object{
		var mainAppName:String = ""
		var maxMemSpace:String = ""
        var BOOTSTRAP_JAR = "bin${separator}bootstrap.jar"
	}

	override fun exec(args:Array<String>){

        if(PROCESS_FILE.exists()){
            println("系统已经启动，请先停止，再进行启动！")
            Thread.sleep(5000)
            return
        }
        val list = args.asList().drop(1)
        list.map { it.split("=") }
                .forEach {
                    if(it.size>1){
                        if(it[0] == "-mainAppName"){
                            mainAppName = it[1]
                        }
                        if(it[0] == "-maxMemSpace"){
                            maxMemSpace = it[1]
                        }
                    }else{
                        throw Exception(Exceptions.INVAILD_PARAM)
                    }
                }

		if(mainAppName == "" || maxMemSpace == ""){
			throw Exception(Exceptions.INVAILD_PARAM)
		}
		
		val myHomeDir = File(imsHome)
        println(myHomeDir.absolutePath)

        myHomeDir.listFiles()
                .filter { it.name.startsWith("server-") || it.name.startsWith("db") }
                .filterNot { it.isFile }
                .forEach {
                    callCommand(it.path, maxMemSpace)
                    Thread.sleep(1000)
                    println("start ${it.name} at ${it.path} ......")
                }

		while(true){
			if(isSuccess(mainAppName)){
                println("Success!!!")
				Thread.sleep(1000)
				exit(0)
			}
			Thread.sleep(5000)
		}
		
	}
	
	private fun isSuccess(mainAppName:String):Boolean{
		val xmlPath = "$imsHome/server-$mainAppName/conf/server.xml"
		val nodes = SAXReader().read(File(xmlPath)).selectNodes("//Server/Service/Connector[@protocol='HTTP/1.1']")
		val portEl: Element = nodes[0] as Element
		val url: URL
		try {
			val targetUrl = "http://127.0.0.1:${portEl.attributeValue("port")}/${Tools.getMainAppDirectName(mainAppName)}"
            println("[Test Url]:"+targetUrl)
			url = URL(targetUrl)
			val connection = url.openConnection() as HttpURLConnection
			connection.setRequestProperty("accept", "*/*")
			connection.setRequestProperty("connection", "Keep-Alive")
			connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)")
			connection.connect()
            val resCode = connection.responseCode
			if(resCode!=-1 && resCode!=404 && resCode!=500) {
                println("[done]请在浏览器中访问:$targetUrl!")
                PROCESS_FILE.createNewFile()
				return true
			}
			println("[returnCode]:$resCode.retry.")
			return false
		} catch (e:Exception) {
			return false
		} 
	}
	
	private fun callCommand(appPath:String , maxMemSpace:String):Thread{
		val t = Thread(ExecThread(appPath,maxMemSpace))
		t.start()
		return t
	}

    private class ExecThread(appPath:String , maxMemSpace:String) : Runnable {

        var appPath:String = ""
        var maxMemSpace:String = ""

        init {
            this.appPath = appPath
            this.maxMemSpace = maxMemSpace
        }

        override fun run() {
			val os = System.getProperty("os.name")
            when{
                os.toLowerCase().contains("win") -> execForWindows()
                os.toLowerCase().contains("linux") -> execForLinux()
                else -> throw Exception("Unkown OS")
            }
		}

        private fun execForWindows(){
			val cmd = ArrayList<String>()
			cmd.add("cmd.exe")
			cmd.add("/c")
			if(this.appPath.contains("db")){
				cmd.add("start /b manager-startup.bat")
			}else{
                val _tomcatHome = ( appPath + separator ).asPath()
				cmd.add((imsHome +"jre/bin/ims").asPath())
                cmd.add("-classpath")
                cmd.add("$_tomcatHome${BOOTSTRAP_JAR}")
                cmd.add("-Djava.util.logging.config.file=${appPath.asPath()}\\conf\\logging.properties")
                cmd.add("-Xms512m")
				if(appPath.contains(mainAppName)){
					cmd.add("-Xmx"+maxMemSpace+"m")
                }else{
					cmd.add("-Xmx512m")
                }
                cmd.add("-XX:PermSize=256M")
                cmd.add("-XX:MaxNewSize=512m")
                cmd.add("-XX:MaxPermSize=512m")
                cmd.add("-Xss256K")
                cmd.add("-XX:+DisableExplicitGC")
                cmd.add("-XX:SurvivorRatio=1")
                cmd.add("-XX:+UseConcMarkSweepGC")
                cmd.add("-XX:+UseParNewGC")
                cmd.add("-XX:+CMSParallelRemarkEnabled")
                cmd.add("-XX:+UseCMSCompactAtFullCollection")
                cmd.add("-XX:CMSFullGCsBeforeCompaction=0")
                cmd.add("-XX:+CMSClassUnloadingEnabled")
                cmd.add("-XX:LargePageSizeInBytes=128M")
                cmd.add("-XX:+UseFastAccessorMethods")
                cmd.add("-XX:+UseCMSInitiatingOccupancyOnly")
                cmd.add("-XX:CMSInitiatingOccupancyFraction=70")
                cmd.add("-XX:SoftRefLRUPolicyMSPerMB=0")
                cmd.add("-Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager")
                cmd.add("-Djava.endorsed.dirs=${_tomcatHome}endorsed")
                cmd.add("-Dcatalina.base=$_tomcatHome")
                cmd.add("-Dcatalina.home=$_tomcatHome")
                cmd.add("-Djava.io.tmpdir=${_tomcatHome}temp")
                cmd.add("org.apache.catalina.startup.Bootstrap")
                cmd.add("start")
            }
			println(cmd)
            val pb = ProcessBuilder(cmd)
            pb.redirectErrorStream(true)
            pb.directory(File(appPath))
            try {
                val br = BufferedReader(InputStreamReader(pb.start().inputStream))
                br.forEachLine {
                    if(it.contains("error") || it.contains("Error") || it.contains("ERROR")){
                        println("启动出错：$it")
                        br.close()
                        Shutdown().exec(arrayOf())
                    }
                }
				br.close()
            } catch (e: IOException) {
				e.printStackTrace()
            }
		}
		
		private fun execForLinux(){
			val cmd = ArrayList<String>()
            cmd.add("/bin/sh")
            cmd.add("-c")
            cmd.add("$appPath/bin/startup.sh")
            val pb = ProcessBuilder(cmd)
            pb.redirectErrorStream(true)
            pb.directory(File("$appPath/bin"))
            println(cmd)
            try {
                val br = BufferedReader(InputStreamReader(pb.start().inputStream))
                br.forEachLine(::println)
				br.close()
            } catch (e: IOException) {
				e.printStackTrace()
            }
		}
		
	}
	
}
