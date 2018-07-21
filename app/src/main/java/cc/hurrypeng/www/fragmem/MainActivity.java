package cc.hurrypeng.www.fragmem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import cc.hurrypeng.www.fragmem.Util.*;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    Button buttonFrag;
    Button buttonMem;

    SharedPreferences sp;
    SharedPreferences.Editor spEditor;

    FileHelper fileHelper;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sp = getSharedPreferences("fragmem", MODE_PRIVATE);
        spEditor = sp.edit();

        fileHelper = new FileHelper(this);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        buttonFrag = findViewById(R.id.buttonFrag);
        buttonMem = findViewById(R.id.buttonMem);

        buttonFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FragListActivity.class);
                startActivity(intent);
            }
        });

        buttonMem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MemoriseActivity.class);
                startActivity(intent);
            }
        });

        // generate a set of frags when the app is installed
        if (!sp.getBoolean("initialised", false)) {
            fileHelper.saveExternalFile("frags.json", "[{\"id\":1,\"title\":\"qwert\",\"content\":\"qwert means a kind of keyboard\",\"imagePath\":\"empty\",\"timeLastMem\":1531908909000,\"shortTermMemoryMax\":100,\"longTermMemory\":20,\"shortTermMemory\":0},{\"id\":2,\"title\":\"yuiop\",\"content\":\"yuiop is just noting\",\"imagePath\":\"empty\",\"timeLastMem\":1531908909000,\"shortTermMemoryMax\":68,\"longTermMemory\":36,\"shortTermMemory\":0},{\"id\":3,\"title\":\"asdfg\",\"content\":\"asdfg are most commonly used in CoD series\",\"imagePath\":\"empty\",\"timeLastMem\":1531908909000,\"shortTermMemoryMax\":100,\"longTermMemory\":36,\"shortTermMemory\":0},{\"id\":4,\"title\":\"hjkll\",\"content\":\"hjkll holds your right hand when typing\",\"imagePath\":\"empty\",\"timeLastMem\":1531908909000,\"shortTermMemoryMax\":100,\"longTermMemory\":20,\"shortTermMemory\":0},{\"id\":5,\"title\":\"zxcvb\",\"content\":\"zxcvb is sometimes used as a password\",\"imagePath\":\"empty\",\"timeLastMem\":1531908909000,\"shortTermMemoryMax\":100,\"longTermMemory\":20,\"shortTermMemory\":0},{\"id\":6,\"title\":\"nmmmd\",\"content\":\"nmmmd quite gross eh\",\"imagePath\":\"empty\",\"timeLastMem\":1531908909000,\"shortTermMemoryMax\":100,\"longTermMemory\":20,\"shortTermMemory\":0}]");
            spEditor.putBoolean("initialised", true);
            spEditor.putInt("nextId", 7);
            spEditor.apply();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemSettings: {
                Toast.makeText(this, "Settings page WIP", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.menuItemAbout: {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setTitle(getString(R.string.app_name));
                dialog.setMessage(getString(R.string.aboutMessage));
                dialog.show();
                break;
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
