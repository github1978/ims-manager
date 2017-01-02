package cn.wisesign.ims.manager;

import cn.wisesign.ims.manager.utils.Exceptions;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.FileSet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

class Main {

	public static Map<String,Class<? extends CommandProcessor>> commands = new HashMap<String, Class<? extends CommandProcessor>>();

    public static void main(String[] args) throws Exception {

//        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
//        Project proj = new Project();
//        FileSet fileSet = new FileSet();
//        fileSet.setProject(proj);
//        fileSet.setDir(new File("I:\\work\\tomcat-linux"));
//        Zip zip = new Zip();
//        zip.setProject(proj);
//        zip.setDestFile(new File("D:\\temp\\backup"+df.format(new Date())+".zip"));
//        zip.addFileset(fileSet);
//        zip.setEncoding("UTF-8");
//        zip.execute();

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
