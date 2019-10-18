package com.example.chisu.myapplication;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.ViewHolder> {

    //메인 페이지의 리사이클러뷰의 아이템을 나타내는 어댑터.
    //이 클래스에 대한 자세한 설명은 이와 비슷한 CommentRecyclerViewAdapter에 기술되어 있으니 참고.

    //Imageloader to load image
    private ImageLoader imageLoader;
    private Context context;

    //List to store all arts
    List<art> artList;

    //Constructor of this class
    public MainRecyclerViewAdapter(List<art> artList, Context context){
        super();

        this.artList = artList;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.art_item_for_main_recyclerview, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        //Getting the particular item from the list
        final art artisan =  artList.get(position);

        //Loading image from url
        imageLoader = CustomVolleyRequest.getInstance(context).getImageLoader();

        //이미지리스너를 사용한다. 리스너의 첫 번째 인자는 이미지뷰, 2번째 인자는 기본 이미지, 3번째 인자는 에러 시 나타날 이미지이다.
        imageLoader.get(artisan.getImage(), ImageLoader.getImageListener(holder.imageView, R.drawable.rectan, android.R.drawable.ic_dialog_alert));

        //Showing data on the views
        holder.imageView.setImageUrl(artisan.getImage(), imageLoader);

        holder.setMainRecyclerViewItemClickListener(new MainRecyclerViewItemClickListener() {
            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                int artId = artisan.getProductId();
                String artUser = artisan.getUser();
                Intent intent = new Intent(context, Article.class);
                intent.putExtra("itemId", artId);
                //삭제할 때 사용자 구분을 위한 아이디 보내기
                intent.putExtra("itemUser", artUser);

                //뷰페이지 구분을 위한 구분자 보내기
                intent.putExtra("viewPageNumber", String.valueOf(TabActivity.viewPageNumber));
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

                Log.e("아이디 유저 ", String.valueOf(artId) + "," + artUser);

            }
        });

    }

    @Override
    public int getItemCount() {
        return artList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private MainRecyclerViewItemClickListener mainRecyclerViewItemClickListener;

        //Views
         NetworkImageView imageView;
         TextView textViewTitle;
         TextView textViewUser;

        //Initializing Views
         ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewUser = itemView.findViewById(R.id.textViewUser);

            itemView.setOnClickListener(this);
        }

         void setMainRecyclerViewItemClickListener(MainRecyclerViewItemClickListener mainRecyclerViewItemClickListener){
            this.mainRecyclerViewItemClickListener = mainRecyclerViewItemClickListener;
        }

        @Override
        public void onClick(View view) {
            mainRecyclerViewItemClickListener.onClick(view, getAdapterPosition(), false);
        }
    }
}