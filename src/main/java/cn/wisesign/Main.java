package cn.wisesign;

import cn.wisesign.utils.Exceptions;

import java.util.HashMap;
import java.util.Map;

class Main {

	public static Map<String,Class<? extends CommandProcessor>> commands = new HashMap<String, Class<? extends CommandProcessor>>();

    public static void main(String[] args) throws Exception {

        commands.put("setup",Startup.class);
        commands.put("shutdown",Shutdown.class);
        commands.put("debug",DebugTest.class);
        commands.put("backup",Backup.class);
        commands.put("install",Install.class);

        if(args.length==0){
            System.out.println("第一个参数必须是：setup/shutdown/debug/backup 中的一种");
            throw new Exception(Exceptions.INVAILD_PARAM);
        }

		String sign  = args[0];

		if(commands.containsKey(sign)){
            commands.get(sign).newInstance().exec(args);
        }
		
	}



}
