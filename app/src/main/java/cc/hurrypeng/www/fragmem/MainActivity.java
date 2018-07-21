package cc.hurrypeng.www.fragmem;

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

import java.util.ArrayList;
import java.util.List;

import cc.hurrypeng.www.fragmem.Util.*;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    List<Frag> fragList = new ArrayList<>();

    Button buttonFrag;
    Button buttonMem;
    BottomAppBar bottomAppBar;
    FloatingActionButton floatingActionButton;
    RecyclerView recyclerView;
    FragAdapter fragAdapter;

    SharedPreferences sp;
    SharedPreferences.Editor spEditor;

    FileHelper fileHelper;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomAppBar = findViewById(R.id.bottomAppBar);
        setSupportActionBar(bottomAppBar);

        sp = getSharedPreferences("fragmem", MODE_PRIVATE);
        spEditor = sp.edit();

        fileHelper = new FileHelper(this);
        fileHelper.getFragList(fragList);

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

        // generate a set of frags when the app is installed
        if (!sp.getBoolean("initialised", false)) {
            fileHelper.saveExternalFile("frags.json", "[{\"id\":1,\"title\":\"qwert\",\"content\":\"qwert means a kind of keyboard\",\"imagePath\":\"empty\",\"timeLastMem\":1531908909000,\"shortTermMemoryMax\":100,\"longTermMemory\":20,\"shortTermMemory\":0},{\"id\":2,\"title\":\"yuiop\",\"content\":\"yuiop is just noting\",\"imagePath\":\"empty\",\"timeLastMem\":1531908909000,\"shortTermMemoryMax\":68,\"longTermMemory\":36,\"shortTermMemory\":0},{\"id\":3,\"title\":\"asdfg\",\"content\":\"asdfg are most commonly used in CoD series\",\"imagePath\":\"empty\",\"timeLastMem\":1531908909000,\"shortTermMemoryMax\":100,\"longTermMemory\":36,\"shortTermMemory\":0},{\"id\":4,\"title\":\"hjkll\",\"content\":\"hjkll holds your right hand when typing\",\"imagePath\":\"empty\",\"timeLastMem\":1531908909000,\"shortTermMemoryMax\":100,\"longTermMemory\":20,\"shortTermMemory\":0},{\"id\":5,\"title\":\"zxcvb\",\"content\":\"zxcvb is sometimes used as a password\",\"imagePath\":\"empty\",\"timeLastMem\":1531908909000,\"shortTermMemoryMax\":100,\"longTermMemory\":20,\"shortTermMemory\":0},{\"id\":6,\"title\":\"nmmmd\",\"content\":\"nmmmd quite gross eh\",\"imagePath\":\"empty\",\"timeLastMem\":1531908909000,\"shortTermMemoryMax\":100,\"longTermMemory\":20,\"shortTermMemory\":0}]");
            spEditor.putBoolean("initialised", true);
            spEditor.putInt("nextId", 7);
            spEditor.apply();
        }
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
            case R.id.home: {
                fragList.add(new Frag(System.currentTimeMillis()));
                fileHelper.saveFragList(fragList);
                Intent intent = new Intent(MainActivity.this, EditFragActivity.class);
                intent.putExtra("request", Util.REQUEST_NEW_FRAG);
                intent.putExtra("position", fragList.size() - 1);
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
            viewHolder.textViewContent.setText(frag.getContent());
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
