package com.example.chisu.myapplication;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

/**
 * Created by jisu7 on 2018-03-02.
 */

//Drawings에서 보여줄 리사이클러뷰의 어댑터.
public class DrawingsRecyclerviewAdpater extends RecyclerView.Adapter<DrawingsRecyclerviewAdpater.ViewHolder>{

    //컨텍스트 정의
    private Context context;

    //아이템 리스트 정의
    private List<DrawingsRecyclerviewItem> drawingsItemList;

    //Glide의 erro, placeholder 옵션을 위한 requestoption 선언 및 초기화
    RequestOptions requestOptions = new RequestOptions();

    //어댑터의 생성자 정의
    public DrawingsRecyclerviewAdpater(List<DrawingsRecyclerviewItem> List, Context context1){
        super();

        this.drawingsItemList = List;
        this.context = context1;
    }

    @Override
    public int getItemCount() {
        return drawingsItemList.size();
    }

    @Override
    public DrawingsRecyclerviewAdpater.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //코멘트 리사이클러뷰 아이템 인플레이트.
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.drawings_recyclerview_item_layout, parent, false);

        DrawingsRecyclerviewAdpater.ViewHolder viewHolder = new DrawingsRecyclerviewAdpater.ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DrawingsRecyclerviewAdpater.ViewHolder holder, int position) {

        //리스트의 아이템 생성.
        final DrawingsRecyclerviewItem item =  drawingsItemList.get(position);

        //여기서 사용되는 imageView는 아래 뷰홀더 클래스에서 선언되고 초기화된다.

        requestOptions.placeholder(R.drawable.ic_account_circle_black_25dp);
        requestOptions.error(R.drawable.ic_account_circle_black_25dp);

        Glide.with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(item.getDrawingsImage())
                .into(holder.drawingImageView);

        holder.setMainRecyclerViewItemClickListener(new MainRecyclerViewItemClickListener() {
            //리사이클러뷰의 홀더의 아이템을 클릭했을 때 인텐트로 해당 유저의 이름을 Article 액티비티로 전해준다.

            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                //유저의 이름을 아이템에서 가져와서
//                String itemId = item.getDrawingsId();
////                Toast.makeText(context, itemId, Toast.LENGTH_SHORT).show();
//
//                String itemUser = item.getDrawingsUser();
//                //인텐트로 보내고 액티비티를 시작시킨다.
//                Intent intent = new Intent(context, Article.class);
//                intent.putExtra("itemId", itemId);
//                intent.putExtra("itemUser", itemUser);
//
//                Log.e("아이디 유저 ", itemId + "," + itemUser);
//
//                //액티비티가 아닌 곳에서 액티비티를 시작시키기 위한 플래그 세우기.
////                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
            }

        });

    }
    //뷰홀더 클래스 선언. 여기서 뷰 관련 작업을 하게 된다.
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        //아이템 클릭을 대비하기 위한 클릭리스너 선언.
        private MainRecyclerViewItemClickListener mainRecyclerViewItemClickListener;

        public ImageView drawingImageView;

        //Initializing Views
        public ViewHolder(View itemView) {
            super(itemView);
            drawingImageView = itemView.findViewById(R.id.drawingsView);

            itemView.setOnClickListener(this);
        }

        public void setMainRecyclerViewItemClickListener(MainRecyclerViewItemClickListener mainRecyclerViewItemClickListener){
            this.mainRecyclerViewItemClickListener = mainRecyclerViewItemClickListener;
        }

        @Override
        public void onClick(View view) {
            mainRecyclerViewItemClickListener.onClick(view, getAdapterPosition(), false);
        }
    }
}
