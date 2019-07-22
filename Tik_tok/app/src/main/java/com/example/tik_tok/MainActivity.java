package com.example.tik_tok;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.tik_tok.bean.Feed;
import com.example.tik_tok.bean.FeedResponse;
import com.example.tik_tok.newtork.IMiniDouyinService;
import com.example.tik_tok.newtork.RetrofitManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements MyItemClickListener{

    final int CAMERA_REQUEST_CODE = 1;
    final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 2;
    final int AUDIO_REQUEST_CODE = 3;
    private static final int PICK_IMAGE = 1;
    private static final int PICK_VIDEO = 2;
    private static final int GRANT_PERMISSION = 3;
    private static final String TAG = "MainActivity";
    private RecyclerView mRv;
    private List<Feed> mFeeds = new ArrayList<>();
    public Uri mSelectedImage;
    private Uri mSelectedVideo;
    public Button mBtn;
    private Button mBtnRefresh;
    private int pos = 0;

    List<Call> mCallList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBtns();
        initRecyclerView();
    }

    private void initBtns() {
      mBtnRefresh = findViewById(R.id.btn_refresh);
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private MyItemClickListener mListener;
        public MyViewHolder(@NonNull View itemView, MyItemClickListener listener) {
            super(itemView);
            this.mListener = listener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mListener.onItemClick(view,getPosition());
        }
    }

    public class MyAdapter extends RecyclerView.Adapter<MyViewHolder>{

        private MyItemClickListener mListener;
        private List<Feed> mData;

        public MyAdapter(List<Feed> data){
            this.mData = data;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            ImageView imageView = (ImageView)holder.itemView;
            String url = mFeeds.get(position).getImage_url();
            Glide.with(imageView.getContext()).load(url).into(imageView);
        }

        @Override
        public int getItemCount() {
            return mFeeds.size();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            imageView.setAdjustViewBounds(true);
            MyViewHolder mVH = new MyViewHolder(imageView,mListener);
            return mVH;
        }

        public void setListener(MyItemClickListener listener){
            this.mListener = listener;
        }
    }

    private void initRecyclerView() {
        MyAdapter myAdapter= new MyAdapter(new ArrayList<Feed>());
        mRv = findViewById(R.id.rv);
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mRv.setAdapter(myAdapter);
        myAdapter.setListener(this);
    }

    public void fetchFeed(View view) {
        mBtnRefresh.setText("requesting...");
        mBtnRefresh.setEnabled(false);

        Retrofit retrofit = RetrofitManager.get("http://test.androidcamp.bytedance.com/");

        Call<FeedResponse> feedResponseCall = retrofit.create(IMiniDouyinService.class).fetchFeed();
        mCallList.add(feedResponseCall);

        feedResponseCall.enqueue(new Callback<FeedResponse>() {
            @Override
            public void onResponse(Call<FeedResponse> call, Response<FeedResponse> response) {
                System.out.println(response.body().getFeeds().get(0).getStudent_id());
                loadPics(getList(response.body().getFeeds(),pos));
                resetRefreshBtn();
            }

            @Override
            public void onFailure(Call<FeedResponse> call, Throwable t) {
                resetRefreshBtn();
            }
        });
    }


    public void jumpToUpload(View view){
        startActivity(new Intent(MainActivity.this,UploadActivity.class));
    }

    public void previousPage(View view){
        System.out.println(pos);
        if(pos == 0){
            Toast.makeText(MainActivity.this,"already first page!",Toast.LENGTH_SHORT).show();
        }
        else{
            pos-=5;
            fetchFeed(view);
        }
    }

    public void nextPage(View view){
        pos += 5;
        fetchFeed(view);
    }

    public void jumpToCamera(View view){
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},CAMERA_REQUEST_CODE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.RECORD_AUDIO},AUDIO_REQUEST_CODE);
        }
        startActivity(new Intent(MainActivity.this, Custom_camera.class));
    }

    private void resetRefreshBtn() {
        mBtnRefresh.setText("refresh feed");
        mBtnRefresh.setEnabled(true);
    }

    private void loadPics(List<Feed> feeds) {
        mFeeds = feeds;
        mRv.getAdapter().notifyDataSetChanged();
    }

    private List<Feed> getList(List<Feed> feed, int start){
        List<Feed> return_feed;
        if (start+4 > feed.size()) {
            List<Feed> sub1 = feed.subList(start,feed.size()-1);
            return_feed = new ArrayList<>(sub1);
            sub1.clear();
            return return_feed;
        } else {
            List<Feed> sub2 = feed.subList(start, start+5);
            return_feed = new ArrayList<>(sub2);
            sub2.clear();
            return return_feed;
        }
    };

    @Override
    public void onItemClick(View view, int position){
        String url = mFeeds.get(position).getVideo_url();
        Intent intent = new Intent(MainActivity.this,Video_player.class);
        intent.putExtra("value",url);
        startActivity(intent);
    }

}
