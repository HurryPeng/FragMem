package cc.hurrypeng.www.fragmem;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.nio.file.FileStore;
import java.util.List;

/**
 * Created by haora on 2018.07.15.
 */

public class Util {

    private static final String TAG="Util";

    static public Gson gson = new Gson();

    static public final int REQUEST_EDIT_FRAG = 1;
    static public final int REQUEST_NEW_FRAG = 2;
    static public final int RESULT_EDIT_SAVED = 1;
    static public final int RESULT_EDIT_DISCARDED = 2;

    static public class Frag implements Serializable {

        private static final long serialVersionUID = 1L;

        private int id;
        private String title;
        private String content;

        public Frag(int id, String title, String content) {
            this.id = id;
            this.title = title;
            this.content = content;
        }

        public Frag()
        {
            this(0 ,"default title", "default content");
        }

        public Frag(String serialized) {
            this();
            try {
                byte bytes[] = new byte[serialized.length()];
                for(int i = 0; i < serialized.length(); i++) bytes[i] = (byte) serialized.codePointAt(i);
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                Frag frag = (Frag) ois.readObject();
                this.setTitle(frag.getTitle());
                this.setContent(frag.getContent());
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }

        public void setId(int id) {
            this.id = id;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

        String serialize() {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(this);
                oos.close();
                byte bytes[] = baos.toByteArray();
                String str = "";
                StringBuilder stringBuilder = new StringBuilder();
                for(byte b : bytes) stringBuilder.append((char) b);
                return stringBuilder.toString();
            }
            catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }

    }

    static public void saveFile(Context context, String filename, String inputText) {
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            //out = context.openFileOutput(filename, Context.MODE_PRIVATE);
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

    static public void saveFileToExternal(Context context, String filename, String inputText) {
        saveFile(context, context.getExternalFilesDir(null) + "/" + filename, inputText);
    }

    @NonNull
    static public String loadFile(Context context, String filename) {
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

    @NonNull
    static public String loadFileFromExternal(Context context,String filename) {
        return loadFile(context, context.getExternalFilesDir(null) + "/" + filename);
    }

    static public void saveFragList(Context context, List<Frag> fragList) {
        String strJson = gson.toJson(fragList);
        saveFileToExternal(context, "frags.json", strJson);
    }

    static public List<Frag> getFragList(Context context) {
        String strJson = Util.loadFileFromExternal(context, "frags.json");
        strJson = strJson.replaceAll(" ", "nbsp"); // Gson will drop dead when it meets a space in a json string
        List<Frag> fragList = gson.fromJson(strJson, new TypeToken<List<Frag>>(){}.getType());
        for (Frag frag : fragList) {
            String title = frag.getTitle();
            title = title.replaceAll("nbsp", " ");
            frag.setTitle(title);
            String content = frag.getContent();
            content = content.replaceAll("nbsp", " ");
            frag.setContent(content);
        }
        return fragList;
    }
}
