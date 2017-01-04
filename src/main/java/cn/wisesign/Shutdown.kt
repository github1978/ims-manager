package cn.wisesign

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList

import cn.wisesign.utils.Tools
import java.io.File
import java.lang.System.exit

class Shutdown : CommandProcessor() {

	companion object{
		var runfile = "ims.exe"
	}

	override fun exec(args:Array<String>){
		when(Tools.getOsType()){
			0 -> execForWindows(imsHome)
			1 -> execForLinux(imsHome)
			else -> throw Exception("Unkown OS")
		}
		println("ims is stopped!will close after 5 secs.")
        PROCESS_FILE.delete()
		Thread.sleep(5000)
	}
	
	private fun execForWindows(excutablepath:String ){
		val getIMSProcess=Runtime.getRuntime().exec("tasklist")
		val imsbr = BufferedReader(InputStreamReader(getIMSProcess.inputStream))
		imsbr.forEachLine {
			if (it.startsWith(runfile)) {
				val items = it.split(" ")
				for (item in items
				) {
					if(item != "" && item != runfile){
						Runtime.getRuntime().exec("taskkill /f /pid " + item)
						break
					}
				}
			}
		}
	}
	
	private fun execForLinux( myhome:String){
		val cmd = ArrayList<String>()
		cmd.add("/bin/bash")
		cmd.add("-c")
		cmd.add("ps -ef|grep \"$myhome\"|grep PermSize|awk '{print $2}'|xargs kill -9")
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
