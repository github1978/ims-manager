package cn.wisesign

import cn.wisesign.utils.*
import org.apache.commons.io.FileUtils
import java.io.File

class GetPackage : CommandProcessor() {

    companion object {
        var mainAppName: String = ""
        var exclude: String = ""
    }

    override fun exec(args: Array<String>) {
        val list = args.asList().drop(1)
        list.map { it.split("=") }
                .forEach {
                    if (it.size > 1) {
                        if (it[0] == "-mainAppName") {
                            GetPackage.mainAppName = it[1]
                        }else if(it[0] == "-exclude"){
                            GetPackage.exclude = it[1]
                        }
                    } else {
                        throw Exception(Exceptions.INVAILD_PARAM)
                    }
                }
        val webappsDist = File(Tools.getMainAppDirectPath(GetPackage.mainAppName))
        val packageDist = File("$imsHome/package/${Tools.getNowTimeSpecialStr()}/${GetPackage.mainAppName}")
        if(!packageDist.exists()){
            packageDist.mkdir()
        }
        FileUtils.copyDirectory(webappsDist,packageDist,FileNameFilter(exclude.asArray()))
    }

}