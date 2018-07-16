package cc.hurrypeng.www.fragmem;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.List;

import cc.hurrypeng.www.fragmem.Util.*;

public class EditFragActivity extends AppCompatActivity {

    EditText editTextTitle;
    EditText editTextContent;

    int requestCode;
    int position;
    List<Frag> fragList;
    Frag frag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_frag);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        editTextTitle = findViewById(R.id.title);
        editTextContent = findViewById(R.id.content);

        Intent intentReceived = getIntent();
        requestCode = intentReceived.getIntExtra("request", 0);
        position = intentReceived.getIntExtra("position", 0);
        fragList = Util.getFragList(this);
        frag = fragList.get(position);

        editTextTitle.setText(frag.getTitle());
        editTextContent.setText(frag.getContent());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_edit_frag, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.done: {
                frag.setTitle(editTextTitle.getText().toString());
                frag.setContent(editTextContent.getText().toString());
                fragList.set(position, frag);
                Util.saveFragList(this, fragList);
                Toast.makeText(this, getString(R.string.fragSaved), Toast.LENGTH_SHORT).show();
                setResult(Util.RESULT_EDIT_SAVED);
                finish();
                break;
            }
            default: break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
                AlertDialog.Builder dialog = new AlertDialog.Builder(this);
                dialog.setMessage(getString(R.string.quitEditing));
                dialog.setCancelable(true);
                dialog.setPositiveButton(getString(R.string.Discard), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        setResult(Util.RESULT_EDIT_DISCARDED);
                        finish();
                    }
                });
                dialog.setNegativeButton(getString(R.string.keepEditing), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dialog.show();
            }
            default: break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
