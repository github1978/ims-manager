package cn.wisesign.ims.manager

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.ArrayList

import cn.wisesign.ims.manager.utils.Tools
import java.io.File
import java.lang.System.exit

class Shutdown :CommandProcessor() {

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
		var getIMSProcess=Runtime.getRuntime().exec("tasklist")
		var imsbr = BufferedReader(InputStreamReader(getIMSProcess.inputStream))
		imsbr.forEachLine {
			if (it.startsWith(runfile)) {
				var items = it.split(" ")
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
		var cmd = ArrayList<String>()
		cmd.add("/bin/bash")
		cmd.add("-c")
		cmd.add("ps -ef|grep \"$myhome\"|grep PermSize|awk '{print $2}'|xargs kill -9")
		var pb = ProcessBuilder(cmd)
		pb.redirectErrorStream(true)
		try {
			var p = pb.start()
			var br = BufferedReader(InputStreamReader(p.inputStream))
			br.forEachLine {

			}
			br.close()
		} catch (e:IOException ) {
			e.printStackTrace()
		}
	}
}
