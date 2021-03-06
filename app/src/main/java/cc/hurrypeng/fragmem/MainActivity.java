package cc.hurrypeng.fragmem;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.bottomappbar.BottomAppBar;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.zzhoujay.richtext.RichText;

import java.util.ArrayList;
import java.util.List;

import cc.hurrypeng.fragmem.Util.*;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    List<Frag> fragList = new ArrayList<>();

    BottomAppBar bottomAppBar;
    FloatingActionButton floatingActionButton;
    RecyclerView recyclerView;
    FragAdapter fragAdapter;

    FileHelper fileHelper;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        setSupportActionBar(bottomAppBar);

        RichText.initCacheDir(getCacheDir());

        fileHelper = new FileHelper(this);

        recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        fragAdapter = new FragAdapter(this, fragList);
        recyclerView.setAdapter(fragAdapter);

        floatingActionButton = findViewById(R.id.floatingActionButton);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, MemoriseActivity.class);
                startActivity(intent);
            }
        });

        fileHelper.getFragList(fragList);
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
            case android.R.id.home: { // Add new frag, taking up the place of the drawer button
                fragList.add(0, new Frag(System.currentTimeMillis()));
                fileHelper.saveFragList(fragList);
                Intent intent = new Intent(MainActivity.this, EditFragActivity.class);
                intent.putExtra("request", Util.REQUEST_NEW_FRAG);
                intent.putExtra("position", 0);
                startActivityForResult(intent, Util.REQUEST_NEW_FRAG);
            }
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Util.REQUEST_NEW_FRAG: {
                switch (resultCode) {
                    case Util.RESULT_EDIT_DISCARDED: {
                        fragList.remove(0);
                        fileHelper.saveFragList(fragList);
                        break;
                    }
                    case Util.RESULT_EDIT_SAVED: {
                        fileHelper.getFragList(fragList);
                        fragAdapter.notifyItemInserted(0);
                        recyclerView.scrollToPosition(0);
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

    static private class FragAdapter extends RecyclerView.Adapter<FragAdapter.ViewHolder> {

        private Context context;
        private List<Frag> fragList = new ArrayList<>();

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textViewTitle;
            TextView textViewContent;

            public ViewHolder(View view) {
                super(view);
                textViewTitle = view.findViewById(R.id.fragItemTitle);
                textViewContent = view.findViewById(R.id.fragItemContent);
            }
        }

        public FragAdapter(Context context, List<Frag> _fragList) {
            this.context = context;
            fragList = _fragList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_frag, parent, false);
            final ViewHolder holder = new ViewHolder(view);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = holder.getAdapterPosition();
                    Intent intent = new Intent(context, FragDetailActivity.class);
                    intent.putExtra("position", position);
                    ((AppCompatActivity)context).startActivityForResult(intent, Util.REQUEST_FRAG_DETAIL);
                }
            });
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
            Frag frag = fragList.get(position);
            viewHolder.textViewTitle.setText(frag.getTitle());
            RichText.fromMarkdown(frag.getContent()).clickable(false).into(viewHolder.textViewContent);
        }

        @Override
        public int getItemCount() {
            return fragList.size();
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
