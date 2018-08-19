package cc.hurrypeng.fragmem;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;
import com.zzhoujay.richtext.RichText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import cc.hurrypeng.fragmem.Util.*;

public class MemoriseActivity extends AppCompatActivity {

    private final int STATE_NO = -1;
    private final int STATE_VAGUE = 0;
    private final int STATE_YES = 1;

    List<Frag> fragListSorted = new ArrayList<>();
    Frag frag;
    int position;

    FileHelper fileHelper;

    FrameLayout layoutFrame;
    ConstraintLayout layoutContent;
    ConstraintLayout layoutHide;
    Button buttonNo;
    Button buttonVague;
    Button buttonYes;
    TextView textViewTitle;
    TextView textViewMem;
    TextView textViewContent;
    ImageView imageView;
    PhotoView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorise);

        fileHelper = new FileHelper(this);

        layoutFrame = findViewById(R.id.layoutFrame);
        layoutContent = findViewById(R.id.layoutContent);
        layoutHide = findViewById(R.id.layoutHide);
        buttonNo = findViewById(R.id.buttonNo);
        buttonVague = findViewById(R.id.buttonVague);
        buttonYes = findViewById(R.id.buttonYes);
        textViewTitle = findViewById(R.id.editTextTitle);
        textViewMem = findViewById(R.id.textViewMemory);
        textViewContent = findViewById(R.id.textViewContent);
        imageView = findViewById(R.id.imageView);
        photoView = findViewById(R.id.photoView);

        long timeCurrent = System.currentTimeMillis();
        fileHelper.getFragList(fragListSorted);
        for (Frag frag : fragListSorted) {
            frag.calculateShortTermMemory(timeCurrent);
        }
        Collections.sort(fragListSorted, new Comparator<Frag>() {
            @Override
            public int compare(Frag frag1, Frag frag2) {
                if (frag1.getShortTermMemory() > frag2.getShortTermMemory()) return 1;
                if (frag1.getShortTermMemory() == frag2.getShortTermMemory()) return 0;
                return -1;
            }
        });
        position = -1;
        if (!fragListSorted.isEmpty()) frag = fragListSorted.get(0);

        nextFrag(0);

        layoutContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                if(position >= fragListSorted.size()) return;
                if(layoutHide.getVisibility() == View.VISIBLE) {
                    layoutHide.setVisibility(View.GONE);
                }
                else {
                    layoutHide.setVisibility(View.VISIBLE);
                }
            }
        });

        buttonNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextFrag(STATE_NO);
            }
        });

        buttonVague.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextFrag(STATE_VAGUE);
            }
        });

        buttonYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nextFrag(STATE_YES);
            }
        });

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

    void nextFrag(int recallState) {

        if(position >= fragListSorted.size()) return;

        if(position != -1) {
            frag.setTimeLastMem(System.currentTimeMillis());
            frag.setShortTermMemoryMax(100);
            switch (recallState) {
                case STATE_NO: {
                    break;
                }
                case STATE_VAGUE: {
                    frag.setLongTermMemory((int) Math.round(frag.getLongTermMemory() + (100 - frag.getShortTermMemory())*0.1));
                    break;
                }
                case STATE_YES: {
                    frag.setLongTermMemory((int) Math.round(frag.getLongTermMemory() + (100 - frag.getShortTermMemory())*0.2));
                    break;
                }
            }

            fragListSorted.set(position, frag);
            List<Frag> fragList = new ArrayList<>(fragListSorted);
            Collections.sort(fragList, new Comparator<Frag>() {
                @Override
                public int compare(Frag frag1, Frag frag2) {
                    if (frag1.getId() > frag2.getId()) return 1;
                    if (frag1.getId() == frag2.getId()) return 0;
                    return -1;
                }
            });
            fileHelper.saveFragList(fragList);
        }

        position++;

        if(position >= fragListSorted.size()) {
            layoutHide.setVisibility(View.INVISIBLE);
            textViewMem.setVisibility(View.INVISIBLE);
            textViewTitle.setText(getString(R.string.noMoreFrags));
            return;
        }

        frag = fragListSorted.get(position);
        textViewTitle.setText(frag.getTitle());
        Date date = new Date(frag.getTimeLastMem());
        String stringMem = getString(R.string.STM) + frag.getShortTermMemory() + "   " + getString(R.string.LTM) + frag.getLongTermMemory() + '\n' + getString(R.string.lastReview) + SimpleDateFormat.getDateTimeInstance().format(date);
        textViewMem.setText(stringMem);
        RichText.fromMarkdown(frag.getContent()).clickable(false).into(textViewContent);
        if (frag.getImagePath().equals("empty")) {
            imageView.setImageDrawable(null);
            photoView.setImageDrawable(null);
        }
        else {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(frag.getImagePath());
                imageView.setImageBitmap(bitmap);
                photoView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        layoutHide.setVisibility(View.GONE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK: {
                if (photoView.getVisibility() == View.VISIBLE) {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    photoView.setBackgroundColor(0x00ffffff);
                    photoView.setVisibility(View.INVISIBLE);
                    return true;
                }
            }
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }
}
