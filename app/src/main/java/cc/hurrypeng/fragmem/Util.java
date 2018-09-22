package cc.hurrypeng.fragmem;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Created by haora on 2018.07.15.
 */

public class Util {

    private static final String TAG="Util";

    static public Gson gson = new Gson();

    static public final int REQUEST_EDIT_FRAG = 1;
    static public final int REQUEST_NEW_FRAG = 2;
    static public final int REQUEST_FRAG_DETAIL = 3;

    static public final int RESULT_EDIT_SAVED = 1;
    static public final int RESULT_EDIT_DISCARDED = 2;
    static public final int RESULT_FRAG_DELETED = 3;
    static public final int RESULT_FRAG_VIEWED = 4;

    static public final int REQUEST_TAKE_PHOTO = 1;
    static public final int REQUEST_PICK_IMAGE = 2;

    static public final int REQUEST_EXTERNAL_STORAGE_PERMISSION = 1;

    static public class Frag {

        private long id;
        private String title;
        private String content;
        private String imagePath;
        private long timeLastMem;
        private int shortTermMemoryMax;
        private int longTermMemory;
        private int shortTermMemory;

        private static double Stability = 78644669.18; // Stability of memory. This value stands for a 33.33% memory after 24 hrs.

        public Frag(long id, String title, String content, String imagePath, long timeLastMem, int shortTermMemoryMax , int longTermMemory, int shortTermMemory) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.imagePath = imagePath;
            this.timeLastMem = timeLastMem;
            this.shortTermMemoryMax = shortTermMemoryMax;
            this.longTermMemory =longTermMemory;
            this.shortTermMemory =shortTermMemory;
        }

        public Frag(long id, String title, String content) {
            this(id, title, content, "empty", id, 0, 0, 0);
        }

        public Frag(long id) {
            this(id, "", "");
        }

        public Frag() {
            this(0);
        }

        public void setId(long id) {
            this.id = id;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setImagePath(String imagePath) {
            this.imagePath = imagePath;
        }

        public void setTimeLastMem(long timeLastMem) {
            this.timeLastMem = timeLastMem;
        }

        public void setShortTermMemoryMax(int shortTermMemoryMax) {
            this.shortTermMemoryMax = shortTermMemoryMax;
        }

        public void setLongTermMemory(int longTermMemory) {
            this.longTermMemory = longTermMemory;
        }

        public void setShortTermMemory(int shortTermMemory) {
            this.shortTermMemory = shortTermMemory;
        }

        public long getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        public String getImagePath() {
            return imagePath;
        }

        public long getTimeLastMem() {
            return timeLastMem;
        }

        public int getShortTermMemoryMax() {
            return shortTermMemoryMax;
        }

        public int getLongTermMemory () {
            return longTermMemory;
        }

        public int getShortTermMemory() {
            return shortTermMemory;
        }

        public int calculateShortTermMemory(long timeCurrentMillis) {
            shortTermMemory = (int) Math.round(longTermMemory + (shortTermMemoryMax - longTermMemory) * Math.pow(Math.E, - (timeCurrentMillis - timeLastMem)/ Stability));
            return shortTermMemory;
        }

    }

    static public class FileHelper {
        private Context context;
        private String SDPATH;
        private String EXTERNALPATH;
        private String DATAPATH;

        public FileHelper(Context context){
            this.context = context;
            SDPATH = Environment.getExternalStorageDirectory().getPath() + "//";
            EXTERNALPATH = context.getExternalFilesDir(null) + "/";
            DATAPATH = this.context.getFilesDir().getPath() + "//";
        }

        public String getExternalPath() {
            return EXTERNALPATH;
        }

        //表示SDCard存在并且可以读写
        public boolean isSDCardState(){
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                return true;
            }else{
                return false;
            }
        }

        /**获取SDCard文件路径*/
        public String getSDPath(){
            /*
            if(isSDCardState()){//如果SDCard存在并且可以读写
                SDPATH = Environment.getExternalStorageDirectory().getPath();
                return SDPATH;
            }else{
                return null;
            }
            */
            return SDPATH;
        }

        /**获取SDCard 总容量大小(MB)*/
        public long getSDCardTotal(){
            if(null != getSDPath()&& getSDPath().equals("")){
                StatFs statfs = new StatFs(getSDPath());
                //获取SDCard的Block总数
                long totalBlocks = statfs.getBlockCount();
                //获取每个block的大小
                long blockSize = statfs.getBlockSize();
                //计算SDCard 总容量大小MB
                long SDtotalSize = totalBlocks*blockSize/1024/1024;
                return SDtotalSize;
            }else{
                return 0;
            }
        }

        /**获取SDCard 可用容量大小(MB)*/
        public long getSDCardFree(){
            if(null != getSDPath()&& getSDPath().equals("")){
                StatFs statfs = new StatFs(getSDPath());
                //获取SDCard的Block可用数
                long availaBlocks = statfs.getAvailableBlocks();
                //获取每个block的大小
                long blockSize = statfs.getBlockSize();
                //计算SDCard 可用容量大小MB
                long SDFreeSize = availaBlocks*blockSize/1024/1024;
                return SDFreeSize;
            }else{
                return 0;
            }
        }

        public void saveExternalFile(String filename, String inputText) {
            saveDataFile(context.getExternalFilesDir(null) + "/" + filename, inputText);
        }

        @NonNull
        public String loadSDFile(String filename) {
            return loadDataFile(context.getExternalFilesDir(null) + "/" + filename);
        }

        public void saveFragList(List<Frag> fragList) {
            String strJson = gson.toJson(fragList);
            saveExternalFile("frags.json", strJson);
        }

        public void getFragList(List<Frag> fragList) {
            String strJson = loadSDFile("frags.json");
            strJson = strJson.replaceAll(" ", "nbsp"); // Gson will drop dead when it meets a space in a json string
            List<Frag> newFragList = gson.fromJson(strJson, new TypeToken<List<Frag>>(){}.getType());
            fragList.clear();
            if (newFragList != null) fragList.addAll(newFragList);
            for (Frag frag : fragList) {
                String title = frag.getTitle();
                title = title.replaceAll("nbsp", " ");
                frag.setTitle(title);
                String content = frag.getContent();
                content = content.replaceAll("nbsp", " ");
                frag.setContent(content);
            }
        }

        /**
         * 在SD卡上创建目录
         *
         * @param dirName
         *            要创建的目录名
         * @return 创建得到的目录
         */
        public File createSDDir(String dirName) {
            File dir = new File(SDPATH + dirName);
            dir.mkdir();
            return dir;
        }

        /**
         * 删除SD卡上的目录
         *
         * @param dirName
         */
        public boolean deleteSDDir(String dirName) {
            File dir = new File(SDPATH + dirName);
            return deleteDirection(dir);
        }


        /**
         * 判断文件是否已经存在
         *
         * @param fileName
         *         要检查的文件名
         * @return boolean, true表示存在，false表示不存在
         */
        public boolean isFileExist(String fileName) {
            File file = new File(SDPATH + fileName);
            return file.exists();
        }

        /**
         * 删除SD卡上的文件
         *
         * @param fileName
         */
        public boolean deleteSDFile(String fileName) {
            File file = new File(SDPATH + fileName);
            if (file == null || !file.exists() || file.isDirectory())
                return false;
            file.delete();
            return true;
        }


        /**
         * 修改SD卡上的文件或目录名
         *
         //* @param fileName
         */
        public boolean renameSDFile(String oldfileName, String newFileName) {
            File oleFile = new File(SDPATH + oldfileName);
            File newFile = new File(SDPATH + newFileName);
            return oleFile.renameTo(newFile);
        }


        /**
         * 拷贝SD卡上的单个文件
         *
         //* @param path
         * @throws IOException
         */

        public boolean copySDFileTo(String srcFileName, String destFileName) throws IOException {
            File srcFile = new File(SDPATH + srcFileName);
            File destFile = new File(SDPATH + destFileName);
            return copyFileTo(srcFile, destFile);
        }



        /**
         * 拷贝SD卡上指定目录的所有文件
         *
         * @param srcDirName
         * @param destDirName
         * @return
         * @throws IOException
         */
        public boolean copySDFilesTo(String srcDirName, String destDirName) throws IOException {
            File srcDir = new File(SDPATH + srcDirName);
            File destDir = new File(SDPATH + destDirName);
            return copyFilesTo(srcDir, destDir);
        }



        /**
         * 移动SD卡上的单个文件
         *
         * @param srcFileName
         * @param destFileName
         * @return
         * @throws IOException
         */

        public boolean moveSDFileTo(String srcFileName, String destFileName) throws IOException {
            File srcFile = new File(SDPATH + srcFileName);
            File destFile = new File(SDPATH + destFileName);
            return moveFileTo(srcFile, destFile);
        }



        /**
         * 移动SD卡上的指定目录的所有文件
         *
         * @param srcDirName
         * @param destDirName
         * @return
         * @throws IOException
         */

        public boolean moveSDFilesTo(String srcDirName, String destDirName) throws IOException {
            File srcDir = new File(SDPATH + srcDirName);
            File destDir = new File(SDPATH + destDirName);
            return moveFilesTo(srcDir, destDir);
        }

        public void saveDataFile(String filename, String inputText) {
            FileOutputStream out = null;
            BufferedWriter writer = null;
            try {
                out = new FileOutputStream(filename, false);
                writer = new BufferedWriter(new OutputStreamWriter(out));
                writer.write(inputText);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (writer != null) writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @NonNull
        static public String loadDataFile(String filename) {
            FileInputStream in = null;
            BufferedReader reader = null;
            StringBuilder content = new StringBuilder();
            try {
                in = new FileInputStream(filename);
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = reader.readLine()) != null) content.append(line);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return content.toString();
        }

        /**
         * 建立私有目录
         *
         * @param dirName
         * @return
         */
        public File creatDataDir(String dirName) {
            File dir = new File(DATAPATH + dirName);
            dir.mkdir();
            return dir;
        }



        /**
         * 删除私有文件
         *
         * @param fileName
         * @return
         */
        public boolean deleteDataFile(String fileName) {
            File file = new File(DATAPATH + fileName);
            return deleteFile(file);
        }



        /**
         * 删除私有目录
         *
         * @param dirName
         * @return
         */
        public boolean deleteDataDir(String dirName) {
            File file = new File(DATAPATH + dirName);
            return deleteDirection(file);
        }



        /**
         * 更改私有文件名
         *
         * @param oldName
         * @param newName
         * @return
         */

        public boolean renameDataFile(String oldName, String newName) {
            File oldFile = new File(DATAPATH + oldName);
            File newFile = new File(DATAPATH + newName);
            return oldFile.renameTo(newFile);
        }



        /**
         * 在私有目录下进行文件复制
         *
         * @param srcFileName
         *            ： 包含路径及文件名
         * @param destFileName
         * @return
         * @throws IOException
         */

        public boolean copyDataFileTo(String srcFileName, String destFileName) throws IOException {
            File srcFile = new File(DATAPATH + srcFileName);
            File destFile = new File(DATAPATH + destFileName);
            return copyFileTo(srcFile, destFile);
        }



        /**
         * 复制私有目录里指定目录的所有文件
         *
         * @param srcDirName
         * @param destDirName
         * @return
         * @throws IOException
         */

        public boolean copyDataFilesTo(String srcDirName, String destDirName)

                throws IOException {
            File srcDir = new File(DATAPATH + srcDirName);
            File destDir = new File(DATAPATH + destDirName);
            return copyFilesTo(srcDir, destDir);
        }



        /**
         * 移动私有目录下的单个文件
         *
         * @param srcFileName
         * @param destFileName
         * @return
         * @throws IOException
         */

        public boolean moveDataFileTo(String srcFileName, String destFileName)

                throws IOException {
            File srcFile = new File(DATAPATH + srcFileName);
            File destFile = new File(DATAPATH + destFileName);
            return moveFileTo(srcFile, destFile);
        }



        /**
         * 移动私有目录下的指定目录下的所有文件
         *
         * @param srcDirName
         * @param destDirName
         * @return
         * @throws IOException
         */

        public boolean moveDataFilesTo(String srcDirName, String destDirName)

                throws IOException {
            File srcDir = new File(DATAPATH + srcDirName);
            File destDir = new File(DATAPATH + destDirName);
            return moveFilesTo(srcDir, destDir);
        }


        /**
         * 删除一个文件
         *
         * @param file
         * @return
         */

        public boolean deleteFile(File file) {
            if (file.isDirectory())
                return false;
            return file.delete();
        }

        /**
         * 删除一个目录（可以是非空目录）
         *
         * @param dir
         */

        public boolean deleteDirection(File dir) {
            if (dir == null || !dir.exists() || dir.isFile()) {
                return false;
            }
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    file.delete();
                } else if (file.isDirectory()) {
                    deleteDirection(file);// 递归
                }
            }
            dir.delete();
            return true;
        }

        /**
         * 拷贝一个文件,srcFile源文件，destFile目标文件
         *
         //* @param path
         * @throws IOException
         */

        public boolean copyFileTo(File srcFile, File destFile) throws IOException {

            if (srcFile.isDirectory() || destFile.isDirectory())
                return false;// 判断是否是文件
            FileInputStream fis = new FileInputStream(srcFile);
            FileOutputStream fos = new FileOutputStream(destFile);
            int readLen = 0;
            byte[] buf = new byte[1024];
            while ((readLen = fis.read(buf)) != -1) {
                fos.write(buf, 0, readLen);
            }
            fos.flush();
            fos.close();
            fis.close();
            return true;
        }

        /**
         * 拷贝目录下的所有文件到指定目录
         *
         * @param srcDir
         * @param destDir
         * @return
         * @throws IOException
         */

        public boolean copyFilesTo(File srcDir, File destDir) throws IOException {

            if (!srcDir.isDirectory() || !destDir.isDirectory())
                return false;// 判断是否是目录
            if (!destDir.exists())
                return false;// 判断目标目录是否存在
            File[] srcFiles = srcDir.listFiles();
            for (int i = 0; i < srcFiles.length; i++) {
                if (srcFiles[i].isFile()) {
                    // 获得目标文件
                    File destFile = new File(destDir.getPath() + "//"
                            + srcFiles[i].getName());
                    copyFileTo(srcFiles[i], destFile);
                } else if (srcFiles[i].isDirectory()) {
                    File theDestDir = new File(destDir.getPath() + "//"
                            + srcFiles[i].getName());
                    copyFilesTo(srcFiles[i], theDestDir);
                }
            }
            return true;
        }

        /**
         * 移动一个文件
         *
         * @param srcFile
         * @param destFile
         * @return
         * @throws IOException
         */

        public boolean moveFileTo(File srcFile, File destFile) throws IOException {

            boolean is_copy = copyFileTo(srcFile, destFile);

            if (!is_copy)
                return false;
            deleteFile(srcFile);
            return true;
        }

        /**
         * 移动目录下的所有文件到指定目录
         *
         * @param srcDir
         * @param destDir
         * @return
         * @throws IOException
         */

        public boolean moveFilesTo(File srcDir, File destDir) throws IOException {
            if (!srcDir.isDirectory() || !destDir.isDirectory()) {
                return false;
            }

            File[] srcDirFiles = srcDir.listFiles();
            for (int i = 0; i < srcDirFiles.length; i++) {
                if (srcDirFiles[i].isFile()) {
                    File oneDestFile = new File(destDir.getPath() + "//"
                            + srcDirFiles[i].getName());
                    moveFileTo(srcDirFiles[i], oneDestFile);
                    deleteFile(srcDirFiles[i]);
                } else if (srcDirFiles[i].isDirectory()) {
                    File oneDestFile = new File(destDir.getPath() + "//"
                            + srcDirFiles[i].getName());
                    moveFilesTo(srcDirFiles[i], oneDestFile);
                    deleteDirection(srcDirFiles[i]);
                }
            }
            return true;
        }
    }

}
