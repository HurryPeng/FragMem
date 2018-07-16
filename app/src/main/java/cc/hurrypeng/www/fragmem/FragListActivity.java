package cc.hurrypeng.www.fragmem;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.List;

import cc.hurrypeng.www.fragmem.Util.*;

public class FragListActivity extends AppCompatActivity {

    List<Frag> fragList;

    RecyclerView recyclerView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frag_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragList.add(new Frag(fragList.size() + 1, getString(R.string.newFrag), getString(R.string.newContent)));
                Util.saveFragList(FragListActivity.this, fragList);
                Intent intent = new Intent(FragListActivity.this, EditFragActivity.class);
                intent.putExtra("request", Util.REQUEST_NEW_FRAG);
                intent.putExtra("position", fragList.size() - 1);
                startActivityForResult(intent, Util.REQUEST_NEW_FRAG);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        fragList = Util.getFragList(this);
        FragAdapter adapter = new FragAdapter(this, fragList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Util.REQUEST_NEW_FRAG: {
                switch (resultCode) {
                    case Util.RESULT_EDIT_DISCARDED: {
                        fragList.remove(fragList.size() - 1);
                        Util.saveFragList(this ,fragList);
                        break;
                    }
                    default: break;
                }
                break;
            }
            default: break;
        }
    }
}
