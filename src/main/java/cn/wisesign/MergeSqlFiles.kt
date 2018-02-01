package cn.wisesign

import cn.wisesign.utils.Exceptions
import org.apache.commons.io.FileUtils
import java.io.File

class
MergeSqlFiles : CommandProcessor(){

    companion object {
        var sqlFilePath: String = ""
    }

    override fun exec(args: Array<String>) {
        val list = args.asList().drop(1)
        list.map { it.split("=") }
                .forEach {
                    if (it.size > 1) {
                        if (it[0] == "-sqlFilePath") {
                            MergeSqlFiles.sqlFilePath = it[1]
                        }
                    } else {
                        throw Exception(Exceptions.INVAILD_PARAM)
                    }
                }
        val sqlFileDirector = File(sqlFilePath)
        val mergeResult = File(sqlFilePath+File.separator+"mergeResult.sql")
        if(mergeResult.exists()){
            mergeResult.delete()
        }
        mergeResult.createNewFile()
        sqlFileDirector.listFiles().filter { it.name.endsWith(".sql") }
                .forEach {
                    mergeResult.appendText("")
                    mergeResult.appendText(FileUtils.readFileToString(it,"UTF-8"))
                }
    }

}