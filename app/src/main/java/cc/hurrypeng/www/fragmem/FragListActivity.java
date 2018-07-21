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
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import cc.hurrypeng.www.fragmem.Util.*;

public class FragListActivity extends AppCompatActivity {

    List<Frag> fragList = new ArrayList<>();

    RecyclerView recyclerView;
    FragAdapter fragAdapter;

    SharedPreferences sp;
    SharedPreferences.Editor spEditor;

    FileHelper fileHelper;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frag_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fileHelper = new FileHelper(this);
        fileHelper.getFragList(fragList);

        sp = getSharedPreferences("fragmem", MODE_PRIVATE);
        spEditor = sp.edit();

        recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        fragAdapter = new FragAdapter(this, fragList);
        recyclerView.setAdapter(fragAdapter);

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fragList.add(new Frag(System.currentTimeMillis()));
                fileHelper.saveFragList(fragList);
                Intent intent = new Intent(FragListActivity.this, EditFragActivity.class);
                intent.putExtra("request", Util.REQUEST_NEW_FRAG);
                intent.putExtra("position", fragList.size() - 1);
                startActivityForResult(intent, Util.REQUEST_NEW_FRAG);
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Util.REQUEST_NEW_FRAG: {
                switch (resultCode) {
                    case Util.RESULT_EDIT_DISCARDED: {
                        fragList.remove(fragList.size() - 1);
                        fileHelper.saveFragList(fragList);
                        break;
                    }
                    case Util.RESULT_EDIT_SAVED: {
                        fileHelper.getFragList(fragList);
                        int position = data.getIntExtra("position", 0);
                        fragAdapter.notifyItemInserted(position);
                        break;
                    }
                    default: break;
                }
                break;
            }
            case Util.REQUEST_FRAG_DETAIL: {
                switch (resultCode) {
                    case Util.RESULT_FRAG_VIEWED: {
                        fileHelper.getFragList(fragList);
                        int position = data.getIntExtra("position", 0);
                        Log.e("TAG", "onActivityResult: notifyChanged" + position);
                        fragAdapter.notifyItemChanged(position);
                        break;
                    }
                    case Util.RESULT_FRAG_DELETED: {
                        fileHelper.getFragList(fragList);
                        int position = data.getIntExtra("position", 0);
                        fragAdapter.notifyItemRemoved(position);
                        break;
                    }
                    default: break;
                }
                break;
            }
            default: break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //fragAdapter.notifyDataSetChanged();
    }
}
