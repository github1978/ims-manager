package cn.wisesign

import cn.wisesign.utils.Exceptions

class DebugTest : CommandProcessor(){

	override fun exec(args:Array<String>) {
		var list = args.asList().drop(1)
        list.map { it.split("=") }
				.forEach {
					if(it.size>1){
						if(it[0] == "-test"){
							println(it[1])
						}
					}else{
						throw Exception(Exceptions.INVAILD_PARAM)
					}
				}
	}
}
