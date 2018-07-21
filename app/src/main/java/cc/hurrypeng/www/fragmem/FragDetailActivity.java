package cc.hurrypeng.www.fragmem;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cc.hurrypeng.www.fragmem.Util.*;

public class FragDetailActivity extends AppCompatActivity {

    List<Frag> fragList = new ArrayList<>();
    Frag frag;

    TextView textViewTitle;
    TextView textViewMem;
    TextView textViewContent;
    ImageView imageView;
    PhotoView photoView;
    FrameLayout layoutFrame;

    int position;

    FileHelper fileHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frag_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fileHelper = new FileHelper(this);

        textViewTitle = findViewById(R.id.editTextTitle);
        textViewMem = findViewById(R.id.textViewMemory);
        textViewContent = findViewById(R.id.textInputEditTextContent);
        imageView = findViewById(R.id.imageView);
        photoView = findViewById(R.id.photoView);
        layoutFrame = findViewById(R.id.layoutFrame);


        Intent intentReceived = getIntent();
        position = intentReceived.getIntExtra("position", 0);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                photoView.setBackgroundColor(0xff000000);
                photoView.setVisibility(View.VISIBLE);
            }
        });

        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                photoView.setBackgroundColor(0x00ffffff);
                photoView.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        fileHelper.getFragList(fragList);
        frag = fragList.get(position);

        textViewTitle.setText(frag.getTitle());
        Date date = new Date(frag.getTimeLastMem());
        String stringMem = getString(R.string.STM) + frag.calculateShortTermMemory(System.currentTimeMillis()) + "   " + getString(R.string.LTM) + frag.getLongTermMemory() + '\n' + getString(R.string.lastReview) + SimpleDateFormat.getDateTimeInstance().format(date);
        textViewMem.setText(stringMem);
        textViewContent.setText(frag.getContent());

        if (frag.getImagePath().equals("empty")) {
            imageView.setImageDrawable(null);
            photoView.setImageDrawable(null);
            } else {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(frag.getImagePath());
                imageView.setImageBitmap(bitmap);
                photoView.setImageBitmap(bitmap);
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
            case R.id.menuItemDelete: {
                AlertDialog.Builder dialog = new AlertDialog.Builder(FragDetailActivity.this);
                dialog.setMessage(getString(R.string.deleteFrag));
                dialog.setPositiveButton(getString(R.string.Delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        fragList.remove(position);
                        fileHelper.saveFragList(fragList);
                        Toast.makeText(FragDetailActivity.this, getString(R.string.fragDeleted), Toast.LENGTH_SHORT).show();
                        Intent intentReturn = new Intent();
                        intentReturn.putExtra("position", position);
                        setResult(Util.RESULT_FRAG_DELETED, intentReturn);
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
            case R.id.menuItemEdit: {
                Intent intent = new Intent(this, EditFragActivity.class);
                intent.putExtra("request", Util.REQUEST_EDIT_FRAG);
                intent.putExtra("position", position);
                startActivityForResult(intent, Util.REQUEST_EDIT_FRAG);
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
                Intent intentReturn = new Intent();
                intentReturn.putExtra("position", position);
                setResult(Util.RESULT_FRAG_VIEWED, intentReturn);
                finish();
            }
            default: break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
