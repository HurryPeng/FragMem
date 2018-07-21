package cc.hurrypeng.www.fragmem;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cc.hurrypeng.www.fragmem.Util.*;

public class EditFragActivity extends AppCompatActivity {

    EditText editTextTitle;
    TextInputEditText textInputEditTextContent;
    ImageView imageView;

    int requestCode;
    int position;
    List<Frag> fragList = new ArrayList<>();
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

        editTextTitle = findViewById(R.id.editTextTitle);
        textInputEditTextContent = findViewById(R.id.textInputEditTextContent);
        imageView = findViewById(R.id.imageView);

        Intent intentReceived = getIntent();
        requestCode = intentReceived.getIntExtra("request", 0);
        position = intentReceived.getIntExtra("position", 0);
        fileHelper.getFragList(fragList);
        frag = fragList.get(position);

        editTextTitle.setText(frag.getTitle());
        textInputEditTextContent.setText(frag.getContent());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (frag.getImagePath().equals("empty")) { // Gson dies of an empty string
            imageView.setImageResource(R.drawable.ic_insert_photo_accent_24dp);
            imageView.setAdjustViewBounds(false);
        } else {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(frag.getImagePath());
                imageView.setImageBitmap(bitmap);
                imageView.setAdjustViewBounds(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(EditFragActivity.this, imageView);
                if (frag.getImagePath().equals("empty"))
                {
                    popupMenu.getMenuInflater().inflate(R.menu.popup_add_image,popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId())
                            {
                                case R.id.menuItemCamera: {
                                    takePhotoWithCamera();
                                    break;
                                }
                                case R.id.menuItemGallery: {
                                    pickImageFromGallery();
                                    break;
                                }
                                default: break;
                            }
                            return false;
                        }
                    });
                } else {
                    popupMenu.getMenuInflater().inflate(R.menu.popup_replace_image,popupMenu.getMenu());
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId())
                            {
                                case R.id.menuItemCamera: {
                                    takePhotoWithCamera();
                                    break;
                                }
                                case R.id.menuItemGallery: {
                                    pickImageFromGallery();
                                    break;
                                }
                                case R.id.menuItemDelete: {
                                    frag.setImagePath("empty");
                                    onStart();
                                }
                                default: break;
                            }
                            return false;
                        }
                    });
                }
                popupMenu.show();
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
            case Util.REQUEST_PICK_IMAGE: {
                if (resultCode == RESULT_OK) {
                    Log.e("TAG", "onActivityResulted: " );
                    String extImagePath;
                    if (Build.VERSION.SDK_INT >= 19) {
                        extImagePath = handleImageOnKitKat(data);
                    } else {
                        extImagePath = handleImageBeforeKitKat(data);
                    }
                    File imageGallery = new File(extImagePath);
                    newImagePath = fileHelper.getExternalPath()+ "image_frag" + frag.getId() + ".jpg";
                    File imageCopy = new File(newImagePath);
                    try {
                        fileHelper.copyFileTo(imageGallery, imageCopy);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    frag.setImagePath(newImagePath);
                }
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
            case R.id.menuItemDone: {
                frag.setTitle(editTextTitle.getText().toString());
                frag.setContent(textInputEditTextContent.getText().toString());
                fragList.set(position, frag);
                fileHelper.saveFragList(fragList);
                Toast.makeText(this, getString(R.string.fragSaved), Toast.LENGTH_SHORT).show();
                Intent intentReturn = new Intent();
                intentReturn.putExtra("position", position);
                setResult(Util.RESULT_EDIT_SAVED, intentReturn);
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

    private void takePhotoWithCamera() {
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

    private void pickImageFromGallery() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, Util.REQUEST_PICK_IMAGE);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    @TargetApi(19)
    private String handleImageOnKitKat(Intent data) {
        Log.e("TAG", "handleImageOnKitKat: 0");
        String imagePath = null;
        newImagePath = fileHelper.getExternalPath()+ "image_frag" + frag.getId() + ".jpg";
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this, uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
                Log.e("TAG", "handleImageOnKitKat: 1");
            } else {
                if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                    imagePath = getImagePath(contentUri, null);
                    Log.e("TAG", "handleImageOnKitKat: 2");
                }
            }
        } else {
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                imagePath = getImagePath(uri, null);
                Log.e("TAG", "handleImageOnKitKat: 3");
            }

            if ("file".equalsIgnoreCase(uri.getScheme())) {
                imagePath = uri.getPath();
                Log.e("TAG", "handleImageOnKitKat: 4");
            }
        }
        return imagePath;
    }

    private String handleImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        return imagePath;
    }
}
