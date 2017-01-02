package cn.wisesign.ims.manager.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

/**
 *
 * @author lan
 */
public final class ZipUtil {

    /**
     * 打包文件
     * @param files 文件或文件夹的集合
     * @param out 输出的zip文件
     */
    public static void zip(File[] files, File out) {
        if (files != null) {
            Map<String, File> map = new HashMap<String, File>();
            for (File f : files) {
                list(f, null, map);
            }
            if (!map.isEmpty()) {
                try {
                    ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(out);
                    for (Map.Entry<String, File> entry : map.entrySet()) {
                        File file = entry.getValue();
                        ZipArchiveEntry zae = new ZipArchiveEntry(file, entry.getKey());
                        zaos.putArchiveEntry(zae);
                        InputStream is = new FileInputStream(file);
                        byte[] b = new byte[1024 * 5];
                        int i = -1;
                        while ((i = is.read(b)) != -1) {
                            zaos.write(b, 0, i);
                        }
                        is.close();
                        zaos.closeArchiveEntry();
                    }
                    zaos.finish();
                    zaos.close();
                } catch (IOException ex) {
                }
            }
        }
    }

    private static void list(File f, String parent, Map<String, File> map) {
        String name = f.getName();
        if (parent != null) {
            name = parent + "/" + name;//设置在zip包里的相对路径
        }
        if (f.isFile()) {
            map.put(name, f);
        } else if (f.isDirectory()) {
            for (File file : f.listFiles()) {
                list(file, name, map);
            }
        }
    }
    
    public static void main(String[] args){
    	zip(new File("D:\\git").listFiles(),new File("D:\\git.zip"));
    }
}