package com.example.chisu.myapplication.Comment;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.chisu.myapplication.MainRecyclerViewItemClickListener;
import com.example.chisu.myapplication.R;

import java.util.List;

public class CommentRecyclerViewAdapter extends RecyclerView.Adapter<CommentRecyclerViewAdapter.ViewHolder> {

    //컨텍스트 정의
    private Context context;

    //아이템 리스트 정의
     List<CommentRecyclerviewItem> commentItemList;

    //Glide의 erro, placeholder 옵션을 위한 requestoption 선언 및 초기화
    RequestOptions requestOptions = new RequestOptions();

    //어댑터의 생성자 정의
    public CommentRecyclerViewAdapter(List<CommentRecyclerviewItem> List, Context context1) {
        super();

        this.commentItemList = List;
        this.context = context1;
    }

    @Override
    public CommentRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        //코멘트 리사이클러뷰 아이템 인플레이트.
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_recyclerview_item_layout, parent, false);

        CommentRecyclerViewAdapter.ViewHolder viewHolder = new CommentRecyclerViewAdapter.ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CommentRecyclerViewAdapter.ViewHolder holder, final int position) {

        //리스트의 아이템 생성.
        final CommentRecyclerviewItem item = commentItemList.get(position);

        //여기서 사용되는 imageView는 아래 뷰홀더 클래스에서 선언되고 초기화된다.

        requestOptions.placeholder(R.drawable.ic_account_circle_black_25dp);
        requestOptions.error(R.drawable.ic_account_circle_black_25dp);

        Glide.with(context)
                .setDefaultRequestOptions(requestOptions)
                .load(item.getCommentUserIcon())
                .into(holder.commentUserImage);

        holder.textViewUser.setText(item.getCommentUsername());
        holder.commentCreated.setText(item.getCommentCreated());
        holder.commentContent.setText(item.getCommentContent());

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View v) {
                //댓글 중 하나를 롱클릭 했을 때 다이얼로그 액티비티가 하나 나타난다.
                //기본적으로 그 액티비티에는 프로필 보기가 나타나며,
                //자신이 쓴 댓글이면 그 댓글을 삭제할 수 있게 한다.
//                Toast.makeText(v.getContext(), "Recycle Click" + position, Toast.LENGTH_SHORT).show();

                //댓글 아이템의 id, username을 가져오고 리스트에서 제거할 용으로 item의 position도 가져온다.
                String commentId = item.getCommentId();
                String commentUser = item.getCommentUsername();
                int itemPosition = position;

                //인텐트로 보내고 액티비티를 시작시킨다.
                Intent intent = new Intent(context, CommentDialogActivity.class);
                intent.putExtra("commentId", commentId);
                intent.putExtra("commentUser", commentUser);
                intent.putExtra("itemPosition", itemPosition);
                intent.putExtra("itemId", CommentActivity.itemId);

                //액티비티가 아닌 곳에서 액티비티를 시작시키기 위한 플래그 세우기.
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);

                return true;
            }
        });

        holder.setMainRecyclerViewItemClickListener(new MainRecyclerViewItemClickListener() {

            //리사이클러뷰의 홀더의 아이템을 클릭했을 때 인텐트로 해당 유저의 이름을 프로필 액티비티로 전해준다.

            @Override
            public void onClick(View view, int position, boolean isLongClick) {
                //롱클릭이라면 프로필 및 삭제가 뜨게 한다.
                    Toast.makeText(context, "그냥 클릭", Toast.LENGTH_SHORT).show();

                //유저의 이름을 아이템에서 가져와서
//                String commentUser = item.getUsername();
//
//                //인텐트로 보내고 액티비티를 시작시킨다.
//                Intent intent = new Intent(context, UserProfile.class);
//                intent.putExtra("commentUser", commentUser);
//
//                //액티비티가 아닌 곳에서 액티비티를 시작시키기 위한 플래그 세우기.
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
            }
        });

    }

    //리스트의 사이즈 리턴. 필수 메소드.
    @Override
    public int getItemCount() {
        return commentItemList.size();
    }

    //뷰홀더 클래스 선언. 여기서 뷰 관련 작업, 즉 화면에 띄우는 작업을 하게 된다.
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        //아이템 클릭을 대비하기 위한 클릭리스너 선언.
        private MainRecyclerViewItemClickListener mainRecyclerViewItemClickListener;

        com.github.siyamed.shapeimageview.CircularImageView commentUserImage;
        TextView textViewUser;
        TextView commentCreated;
        TextView commentContent;

        //Initializing Views
        ViewHolder(View itemView) {
            super(itemView);

            commentUserImage = itemView.findViewById(R.id.commentUserImage);
            textViewUser = itemView.findViewById(R.id.commentUserName);
            commentCreated = itemView.findViewById(R.id.commentCreated);
            commentContent = itemView.findViewById(R.id.commentContent);

            itemView.setOnClickListener(this);
        }


        void setMainRecyclerViewItemClickListener(MainRecyclerViewItemClickListener mainRecyclerViewItemClickListener) {
            this.mainRecyclerViewItemClickListener = mainRecyclerViewItemClickListener;
        }

        @Override
        public void onClick(View view) {
            mainRecyclerViewItemClickListener.onClick(view, getAdapterPosition(), false);
        }
    }
}
