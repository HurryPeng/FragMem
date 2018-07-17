package cc.hurrypeng.www.fragmem;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import cc.hurrypeng.www.fragmem.Util.*;

public class FragDetailActivity extends AppCompatActivity {

    List<Frag> fragList;
    Frag frag;

    SharedPreferences spFrag;
    SharedPreferences.Editor spFragEditor;

    TextView textViewTitle;
    TextView textViewContent;
    ImageView imageView;

    int position;

    FileHelper fileHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frag_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fileHelper = new FileHelper(this);

        textViewTitle = findViewById(R.id.title);
        textViewContent = findViewById(R.id.content);
        imageView = findViewById(R.id.imageView);

        Intent intentReceived = getIntent();
        position = intentReceived.getIntExtra("position", 0);
    }

    @Override
    protected void onStart() {
        super.onStart();

        fragList = fileHelper.getFragList();
        frag = fragList.get(position);

        textViewTitle.setText(frag.getTitle());
        textViewContent.setText(frag.getContent());

        if (!frag.getImagePath().equals("empty")) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(frag.getImagePath());
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_frag_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete: {
                AlertDialog.Builder dialog = new AlertDialog.Builder(FragDetailActivity.this);
                dialog.setMessage(getString(R.string.deleteFrag));
                dialog.setPositiveButton(getString(R.string.Delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        fragList.remove(position);
                        fileHelper.saveFragList(fragList);
                        Toast.makeText(FragDetailActivity.this, getString(R.string.fragDeleted), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                dialog.setNegativeButton(getString(R.string.Cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                dialog.show();
                break;
            }
            case R.id.edit: {
                Intent intent = new Intent(this, EditFragActivity.class);
                intent.putExtra("request", Util.REQUEST_EDIT_FRAG);
                intent.putExtra("position", position);
                startActivityForResult(intent, Util.REQUEST_EDIT_FRAG);
                //Toast.makeText(this, "edit WIP", Toast.LENGTH_SHORT).show();
                break;
            }
            default: break;
        }
        return super.onOptionsItemSelected(item);
    }
}
