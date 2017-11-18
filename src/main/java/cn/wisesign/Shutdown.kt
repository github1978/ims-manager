package cn.wisesign

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList

import cn.wisesign.utils.Tools
import cn.wisesign.utils.asPath
import cn.wisesign.utils.sigarGetProcesslist
import java.io.File
import java.lang.System.exit

class Shutdown : CommandProcessor() {

	companion object{
		var runfile = arrayOf("ims.exe","java.exe")
	}

	override fun exec(args:Array<String>){
		when(Tools.getOsType()){
			0 -> execForWindows(imsHome.asPath())
			1 -> execForLinux(imsHome.asPath())
			else -> throw Exception("Unkown OS")
		}
		println("ims is stopped!will close after 5 secs.")
        PROCESS_FILE.delete()
		Thread.sleep(5000)
	}
	
	private fun execForWindows(excutablepath:String ){
        val processList = Tools.sigarGetProcesslist()
        processList.forEach {
            val execpath = it["execpath"].toString()
            val pname = it["pname"].toString()
            val pid = it["pid"]
            if(execpath.contains(excutablepath) && runfile.contains(pname)) {
                Runtime.getRuntime().exec("taskkill /f /pid " + pid)
            }
        }
	}
	
	private fun execForLinux( myhome:String){
		val cmd = ArrayList<String>()
		cmd.add("/bin/bash")
		cmd.add("-c")
		cmd.add("ps -ef|grep \"$myhome\"|grep org.apache.catalina.startup.Bootstrap|awk '{print $2}'|xargs kill -9")
		val pb = ProcessBuilder(cmd)
		pb.redirectErrorStream(true)
		try {
			val p = pb.start()
			val br = BufferedReader(InputStreamReader(p.inputStream))
			br.forEachLine {

			}
			br.close()
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}
}
