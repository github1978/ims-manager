package cn.wisesign

import cn.wisesign.Backup.Companion.PREFIX
import cn.wisesign.Backup.Companion.cron
import cn.wisesign.Backup.Companion.localPath
import cn.wisesign.Backup.Companion.orignPath
import cn.wisesign.Backup.Companion.remotePath
import cn.wisesign.Backup.Companion.storeNum
import cn.wisesign.CommandProcessor.Companion.imsHome
import cn.wisesign.utils.*
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import org.apache.commons.io.FileUtils
import org.quartz.CronScheduleBuilder.cronSchedule
import org.quartz.Job
import org.quartz.JobBuilder.newJob
import org.quartz.JobExecutionContext
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.impl.StdSchedulerFactory
import sun.management.ManagementFactory
import java.util.*
import org.apache.tools.ant.Project
import org.apache.tools.ant.taskdefs.Zip
import java.text.SimpleDateFormat
import org.apache.tools.ant.types.FileSet
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPFile
import org.apache.commons.net.ftp.FTPReply
import java.io.*
import java.io.File.separator


class Backup : CommandProcessor() {

    companion object{
        var action:String = ""
        var localPath:String = ""
        // ftp:\\192.168.88.14:21@root@wisesign@/root/temp
        // sftp:\\192.168.88.14:22@root@wisesign@/root/temp
        // \\192.168.88.10\temp
        var remotePath:String = ""
        var orignPath:String = ""
        var cron:String = ""
        var storeNum:String = ""
        val PREFIX:String = "backup"
    }

    override fun exec(args: Array<String>) {

        val list = args.asList().drop(1)
        list.map { it.split("=") }
                .forEach {
                    if(it.size>1){
                        when(it[0]){
                            "-action" -> action = it[1]
                            "-localPath" -> localPath = it[1]
                            "-remotePath" -> remotePath = it[1]
                            "-cron" -> cron = it[1]
                            "-storeNum" -> storeNum = it[1]
                            else -> throw Exception(Exceptions.INVAILD_PARAM)
                        }
                    }else{
                        throw Exception(Exceptions.INVAILD_PARAM)
                    }
                }
        if(action == "start" && localPath == ""){
            throw Exception(Exceptions.INVAILD_PARAM)
        }

        when(action){
            "start" -> buildAndStartBackupProcess()
            "stop" -> stopBackupProcess()
            else -> throw Exception(Exceptions.INVAILD_PARAM)
        }

    }

    fun stopBackupProcess(){
        if(BACKUP_PROCESS_FILE.exists()){
            Runtime.getRuntime().exec("taskkill /f /pid ${BACKUP_PROCESS_FILE.readText()}")
            BACKUP_PROCESS_FILE.delete()
        }
    }

    fun buildAndStartBackupProcess(){

        logger.info("the backup-process start]]]]]]]]]]]]]]]]]]]]]]]]]]")

        val sf = StdSchedulerFactory()
        val sched = sf.scheduler
        val jobDetail = newJob(BackupJob::class.java).withIdentity("imsbackup", "cn.wisesign").build()
        val trigger = newTrigger().withIdentity("imsbackupTrigger", "cn.wisesign")
                .withSchedule(cronSchedule(cron)).build()
        sched.scheduleJob(jobDetail, trigger)
        sched.start()
        BACKUP_PROCESS_FILE.writeText(ManagementFactory.getRuntimeMXBean().name.split("@")[0])
    }
}

class BackupJob : Job {

    companion object{
        var host:String = ""
        var port:String = ""
        var user:String = ""
        var passwd:String = ""
    }

    override fun execute(p0: JobExecutionContext?) {

        logger.info("the job start run!!!")
        logger.info("param-localPath:${localPath}")
        logger.info("param-remotePath:${remotePath}")
        logger.info("param-cron:${cron}")

        if(!File(localPath).exists()){
            File(localPath).mkdir()
        }

        logger.info("shutdown system start!!!")
        Runtime.getRuntime().exec(runShell("shutdown"))
        Thread.sleep(10000)
        logger.info("shutdown system end!!!")

        try {
            logger.info("zip the system start!!!")
            val zipFile = zipBackupFiles()
            logger.info("zip the system end!!!")

            logger.info("setup system start!!!")
            Runtime.getRuntime().exec(runShell("startup"))
            logger.info("setup system end!!!")

            if(remotePath != ""){
                logger.info("send the backup tp remote start!!!")
                when{
                    File(remotePath).exists() -> windowsCopy(zipFile)
                    remotePath.startsWith("ftp") -> ftpCopy(zipFile)
                    remotePath.startsWith("sftp") -> sftpCopy(zipFile)
                    else -> logger.error("the param-remotePath:'$remotePath' is wrong.")
                }
                logger.info("send the backup to remote end!!!")
            }
        } catch (e:Exception){
            logger.error(e.message)
        }
        logger.info("the job end!!!")
    }

    fun zipBackupFiles():File{
        val df = SimpleDateFormat("yyyyMMddHHmm")
        val zipFileName = "backup${df.format(Date())}.zip"
        val zipFile = File("$localPath/$zipFileName")
        val prj = Project()
        val zip = Zip()
        zip.project = prj
        zip.destFile = zipFile
        val fileSet = FileSet()
        fileSet.project = prj
        fileSet.dir = File(imsHome)
        fileSet.appendExcludes(arrayOf(
                "managerlogs/**",
                "export/**",
                "LuneneIndex/**",
                // --------------
                "server-omm/**",
                "db/**",
                "server-report/**",
                "updates/**"
        ))
        zip.addFileset(fileSet)
        zip.execute()
        return zipFile
    }

    fun windowsCopy(srcFile: File){
        FileUtils.copyFile(srcFile, File("$remotePath$separator${srcFile.name}"))
        val targetDirector:File = File(remotePath)
        if(storeNum != "" && targetDirector.list().size>Integer.parseInt(storeNum)){
            var minbackup:Int = 0
            targetDirector.list()
                    .asSequence()
                    .filterNot { File(it).isDirectory }
                    .forEach {
                        if(it == null){
                            return
                        }
                        val thenum = Integer.parseInt(it.split(PREFIX)[1].split(".zip")[0])
                        when{
                            minbackup==0 -> minbackup = thenum
                            minbackup>0 -> {
                                if(minbackup<thenum)
                                    minbackup = thenum
                            }
                        }
                    }
            File("$remotePath$separator$PREFIX$minbackup.zip").delete()
        }
    }

    fun ftpCopy(srcFile: File){
        decodeFtpProtcol()
        if(port ==""){ port = "21" }
        var ftp = FTPClient()
        ftp.connectTimeout = 1800000 //ftp连接超时 30 分钟

        // 尝试连接ftp服务器
        ftp.connect(host,Integer.parseInt(port))
        val reply = ftp.replyCode
        if (!FTPReply.isPositiveCompletion(reply)){
            ftp.disconnect()
            logger.error("连接远程ftp服务器时出错！$reply")
        }

        // 登录
        ftp.setFileType(FTPClient.BINARY_FILE_TYPE)
        if(!ftp.login(user, passwd)){
            ftp.disconnect()
            logger.error("登录ftp失败！检查用户名密码是否正确！")
        }

        // 执行备份操作
        ftp.makeDirectory(orignPath)
        try {
            ftp.upload(srcFile)
            if(storeNum != "" && ftp.listFiles(orignPath).size>Integer.parseInt(storeNum)) { // 删除旧备份
                var minbackup:Int = 0
                var orignFilelist = ftp.listFiles(orignPath).asList()
                orignFilelist
                        .sortedBy { it.timestamp.timeInMillis }
                        .filterNot { it.isDirectory }
                        .forEach {
                            if(it!=null){
                                val thenum = Integer.parseInt(it.name.split(PREFIX)[1].split(".zip")[0])
                                when{
                                    minbackup == 0 -> minbackup = thenum
                                    minbackup > 0 -> {
                                        if(minbackup<thenum)
                                            minbackup = thenum
                                    }

                                }
                            }
                        }
                ftp.remove("$orignPath/$PREFIX$minbackup.zip")
            }
        }catch (e:Exception){
            logger.error(e.message)
        }finally {
            ftp.disconnect()
            ftp.quit()
        }

    }

    fun sftpCopy(srcFile: File){
        decodeFtpProtcol()
        if(port ==""){ port = "22" }
        val jsch = JSch()
        val session = jsch.getSession(user, host, Integer.parseInt(port))
        session.setPassword(passwd)
        val config = Properties()
        config.put("StrictHostKeyChecking", "no")
        session.setConfig(config)
        session.timeout = 1800000
        session.connect()
        val channel = session.openChannel("sftp")
        val sftp = channel as ChannelSftp
        sftp.connect()
        sftp.put(srcFile.absolutePath, orignPath, ChannelSftp.OVERWRITE)
        sftp.quit()
        channel.disconnect()
        session.disconnect()
    }

    fun decodeFtpProtcol() {
        val url = remotePath.split("@")[0].split(":\\\\")[1].split(":")
        host = url[0]
        if(url.size>1){
            port = url[1]
        }
        val list = remotePath.split("@").drop(1)
        user = list[0]
        passwd = list[1]
        orignPath = list[2]
    }

}
