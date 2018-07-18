package cc.hurrypeng.www.fragmem;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import cc.hurrypeng.www.fragmem.Util.*;

public class MemoriseActivity extends AppCompatActivity {

    private final int STATE_NO = -1;
    private final int STATE_VAGUE = 0;
    private final int STATE_YES = 1;

    List<Frag> fragListSorted;
    Frag frag;
    int position;
    boolean visible;

    FileHelper fileHelper;

    View layoutCard;
    View layoutHide;
    Button buttonNo;
    Button buttonVague;
    Button buttonYes;
    TextView textViewTitle;
    TextView textViewMem;
    TextView textViewContent;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memorise);

        fileHelper = new FileHelper(this);

        layoutCard = findViewById(R.id.layoutCard);
        layoutHide = findViewById(R.id.layoutHide);
        buttonNo = findViewById(R.id.buttonNo);
        buttonVague = findViewById(R.id.buttonVague);
        buttonYes = findViewById(R.id.buttonYes);
        textViewTitle = findViewById(R.id.title);
        textViewMem = findViewById(R.id.memory);
        textViewContent = findViewById(R.id.content);
        imageView = findViewById(R.id.imageView);

        long timeCurrent = System.currentTimeMillis();
        fragListSorted = fileHelper.getFragList();
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
        frag = fragListSorted.get(0);

        nextFrag(0);

        layoutCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(position >= fragListSorted.size()) return;
                if(visible) {
                    layoutHide.setVisibility(View.INVISIBLE);
                    visible = false;
                }
                else {
                    layoutHide.setVisibility(View.VISIBLE);
                    visible = true;
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
                    frag.setLongTermMemory((int) Math.round(100 - (100 - frag.getLongTermMemory())*0.9));
                    break;
                }
                case STATE_YES: {
                    frag.setLongTermMemory((int) Math.round(100 - (100 - frag.getLongTermMemory())*0.8));
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
        textViewContent.setText(frag.getContent());
        if (frag.getImagePath().equals("empty")) {
            imageView.setVisibility(View.INVISIBLE);
        }
        else {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(frag.getImagePath());
                imageView.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        layoutHide.setVisibility(View.INVISIBLE);
        visible = false;
    }
}
