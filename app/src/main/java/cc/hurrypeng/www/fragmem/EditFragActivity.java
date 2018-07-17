package cc.hurrypeng.www.fragmem;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

import cc.hurrypeng.www.fragmem.Util.*;

public class EditFragActivity extends AppCompatActivity {

    EditText editTextTitle;
    EditText editTextContent;
    ImageView imageView;

    int requestCode;
    int position;
    List<Frag> fragList;
    Frag frag;

    String newImagePath;
    Uri newImageUri;

    FileHelper fileHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_frag);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fileHelper = new FileHelper(this);

        editTextTitle = findViewById(R.id.title);
        editTextContent = findViewById(R.id.content);
        imageView = findViewById(R.id.imageView);

        Intent intentReceived = getIntent();
        requestCode = intentReceived.getIntExtra("request", 0);
        position = intentReceived.getIntExtra("position", 0);
        fragList = fileHelper.getFragList();
        frag = fragList.get(position);

        editTextTitle.setText(frag.getTitle());
        editTextContent.setText(frag.getContent());
    }

    @Override
    protected void onStart() {
        super.onStart();
        imageView.setImageResource(R.drawable.ic_add_a_photo_black_24dp);
        if (!frag.getImagePath().equals("empty")) { // Gson dies of an empty string
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(frag.getImagePath());
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                newImagePath = fileHelper.getExternalPath()+ "image_frag" + frag.getId() + ".jpg";
                File fileImage = new File(newImagePath);
                try {
                    if (!fileImage.exists()) {
                        fileImage.createNewFile();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (Build.VERSION.SDK_INT >= 24) {
                    newImageUri = FileProvider.getUriForFile(EditFragActivity.this,
                            "cc.hurrypeng.www.fragmem.fileprovider", fileImage);
                } else {
                    newImageUri = Uri.fromFile(fileImage);
                }
                Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
                intent.putExtra(MediaStore.EXTRA_OUTPUT, newImageUri);
                startActivityForResult(intent, Util.REQUEST_TAKE_PHOTO);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Util.REQUEST_TAKE_PHOTO: {
                if (resultCode == RESULT_OK) {
                    frag.setImagePath(newImagePath);
                }
                break;
            }
            default: break;
        }
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
                fileHelper.saveFragList(fragList);
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
