package cn.wisesign.ims.manager

import cn.wisesign.ims.manager.Backup.Companion.cron
import cn.wisesign.ims.manager.Backup.Companion.localPath
import cn.wisesign.ims.manager.Backup.Companion.orignPath
import cn.wisesign.ims.manager.Backup.Companion.remotePath
import cn.wisesign.ims.manager.CommandProcessor.Companion.LOG_DATEFORMAT
import cn.wisesign.ims.manager.CommandProcessor.Companion.imsHome
import cn.wisesign.ims.manager.utils.Exceptions
import cn.wisesign.ims.manager.utils.logger
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
import java.io.File
import java.util.*
import org.apache.tools.ant.Project
import org.apache.tools.ant.taskdefs.Zip
import java.text.SimpleDateFormat
import org.apache.tools.ant.types.FileSet
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import java.io.FileInputStream


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
                            else -> throw Exception(Exceptions.INVAILD_PARAM)
                        }
                    }else{
                        throw Exception(Exceptions.INVAILD_PARAM)
                    }
                }
        if(action.equals("start") && localPath == ""){
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

class BackupJob : Job{

    companion object{
        var host:String = ""
        var port:String = ""
        var user:String = ""
        var passwd:String = ""
    }

    override fun execute(p0: JobExecutionContext?) {

        logger.info("the job start run at:${LOG_DATEFORMAT.format(Date())}")
        logger.info("param-localPath:$localPath")
        logger.info("param-remotePath:$remotePath")
        logger.info("param-cron:$cron")

        if(!File(localPath).exists()){
            File(localPath).mkdir()
        }

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
        zip.addFileset(fileSet)
        try {
            zip.execute()

            when{
                remotePath.startsWith("\\\\") -> {
                    FileUtils.copyFile(zipFile,File(remotePath))
                }
                remotePath.startsWith("ftp") -> ftpCopy(zipFile)
                remotePath.startsWith("sftp") -> sftpCopy(zipFile)
            }
        }catch (e:Exception){
            logger.error(e.message)
        }
        logger.info("the job end at:${LOG_DATEFORMAT.format(Date())}")
    }

    fun ftpCopy(srcFile:File){
        decodeFtpProtcol()
        if(port==""){ port = "21" }
        val ftp = FTPClient()
        ftp.connect(host,Integer.parseInt(port))
        ftp.login(user, passwd)
        ftp.setFileType(FTPClient.BINARY_FILE_TYPE)
        val reply = ftp.replyCode
        if (!FTPReply.isPositiveCompletion(reply)){
            ftp.disconnect()
            return
        }
        ftp.makeDirectory(orignPath)
        val ins = FileInputStream(srcFile)
        ftp.storeFile("$orignPath${srcFile.name}",ins)
        ins.close()
    }

    fun sftpCopy(srcFile:File){
        decodeFtpProtcol()
        if(port==""){ port = "22" }
        val jsch = JSch()
        val session = jsch.getSession(user, host, Integer.parseInt(port))
        session.setPassword(passwd)
        val config = Properties()
        config.put("StrictHostKeyChecking", "no")
        session.setConfig(config)
        session.timeout = 50000
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
