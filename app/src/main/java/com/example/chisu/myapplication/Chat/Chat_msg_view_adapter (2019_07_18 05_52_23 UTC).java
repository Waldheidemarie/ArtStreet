package com.example.chisu.myapplication.Chat;//package com.example.jisu7.artstreet;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
//import android.graphics.Color;
//import android.support.v7.widget.RecyclerView;
//import android.util.Base64;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.bumptech.glide.Glide;
//import com.mymusic.orvai.high_pitched_tone.Interface.ItemClickListener;
//import com.mymusic.orvai.high_pitched_tone.R;
//import com.mymusic.orvai.high_pitched_tone.Shared_Preference.SharedPrefManager;
//import com.mymusic.orvai.high_pitched_tone.models.Chat_msg;
//import com.mymusic.orvai.high_pitched_tone.models.URLs;
//
//import java.util.List;
//
//import de.hdodenhof.circleimageview.CircleImageView;
//
//import static android.view.View.GONE;
//
///**
// * Created by orvai on 2018-02-20.
// */
//
//public class Chat_msg_view_adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
//
//    List<Chat_msg> msg_list;
//    Context mCtx;
//    String user_id;
//    Bitmap image;
//    public static final int ITEM_TYPE_MSG = 0;
//    public static final int ITEM_TYPE_ENTER_MSG = 1;
//    public static final int ITEM_TYPE_IMAGE = 2;
//
//    public Chat_msg_view_adapter(List<Chat_msg> msg_list, Context mCtx) {
//        this.msg_list = msg_list;
//        this.mCtx = mCtx;
//    }
//
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        if (viewType == ITEM_TYPE_MSG) {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_msg, parent, false);
//            return new Message_ViewHolder(view);
//        } else if (viewType == ITEM_TYPE_ENTER_MSG) {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_enter_room_msg, parent, false);
//            return new User_Modify_ViewHolder(view);
//        } else {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_image, parent, false);
//            return new Image_ViewHolder(view);
//        }
//    }
//
//
//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//        user_id = SharedPrefManager.getInstance(mCtx).getUser().getUser_id();
//        final Chat_msg r_msg_list = msg_list.get(position);
//
//        if (holder.getItemViewType() == ITEM_TYPE_MSG) {
//            Message_ViewHolder msg_viewholder = (Message_ViewHolder) holder;
//
//            if (user_id.equals(r_msg_list.getChatting_user_id())) { // 메시지 보낸 사람이 나라면
//                LinearLayout.LayoutParams msg_params = (LinearLayout.LayoutParams) ((Message_ViewHolder) holder).user_msg.getLayoutParams();
//                LinearLayout.LayoutParams id_params = (LinearLayout.LayoutParams) ((Message_ViewHolder) holder).user_id.getLayoutParams();
//                LinearLayout.LayoutParams time_params = (LinearLayout.LayoutParams) ((Message_ViewHolder) holder).msg_time.getLayoutParams();
//                msg_params.gravity = Gravity.RIGHT;
//                id_params.gravity = Gravity.RIGHT;
//                time_params.gravity = Gravity.RIGHT;
//                msg_viewholder.user_id.setText(r_msg_list.getChatting_user_id());
//                msg_viewholder.user_msg.setText(r_msg_list.getChatting_msg());
//                msg_viewholder.user_msg.setBackgroundResource(R.drawable.chat_right);
//                msg_viewholder.msg_time.setText(r_msg_list.getChatting_server_time());
//                msg_viewholder.user_pic.setVisibility(GONE);
//
//            } else { // 그외 사람이 메시지를 보냈다면
//                msg_viewholder.user_id.setText(r_msg_list.getChatting_user_id());
//                msg_viewholder.user_msg.setText(r_msg_list.getChatting_msg());
//                msg_viewholder.user_msg.setBackgroundResource(R.drawable.chat_left);
//                msg_viewholder.msg_time.setText(r_msg_list.getChatting_server_time());
//
//                if (!r_msg_list.getChatting_user_pic().equals("null")) { // 유저 사진 값이 null이라는 String 값이 아니라면
//                    Glide.with(mCtx).load(URLs.URL_IMAGE_ROOT + r_msg_list.getChatting_user_pic()).into(msg_viewholder.user_pic);
//
//                } else {
//                    msg_viewholder.user_pic.setImageResource(R.drawable.default_pic);
//                }
//            }
//
//            msg_viewholder.setItemClickListener(new ItemClickListener() {
//                @Override
//                public void onClick(View view, int position, boolean isLongClick) {
//                    if (!isLongClick) {
//                        Toast.makeText(mCtx, "메시지", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//
//        } else if (holder.getItemViewType() == ITEM_TYPE_ENTER_MSG) {
//            User_Modify_ViewHolder mdfy_viewholder = (User_Modify_ViewHolder) holder;
//            if (r_msg_list.getChatting_user_modify() == 0) { // (0은 퇴장, 1은 입장, 2는 강퇴)
//                mdfy_viewholder.mdy_user_id.setText(r_msg_list.getChatting_user_id() + "님이 대화방에서 나갔습니다.");
//                mdfy_viewholder.mdy_user_id.setBackgroundColor(Color.GRAY);
//            } else if (r_msg_list.getChatting_user_modify() == 1) {
//                mdfy_viewholder.mdy_user_id.setText(r_msg_list.getChatting_user_id() + "님이 대화방에 들어왔습니다.");
//            }
//            mdfy_viewholder.setItemClickListener(new ItemClickListener() {
//                @Override
//                public void onClick(View view, int position, boolean isLongClick) {
//                    Toast.makeText(mCtx, "입/퇴장 메시지", Toast.LENGTH_SHORT).show();
//                }
//            });
//        } else {
//            Image_ViewHolder image_viewHolder = (Image_ViewHolder) holder;
//            if (user_id.equals(r_msg_list.getChatting_user_id())) { // 메시지 보낸 사람이 나라면
//                LinearLayout.LayoutParams image_params = (LinearLayout.LayoutParams) ((Image_ViewHolder) holder).image_view.getLayoutParams();
//                LinearLayout.LayoutParams id_params = (LinearLayout.LayoutParams) ((Image_ViewHolder) holder).user_id.getLayoutParams();
//                LinearLayout.LayoutParams time_params = (LinearLayout.LayoutParams) ((Image_ViewHolder) holder).msg_time.getLayoutParams();
//                image_params.gravity = Gravity.RIGHT;
//                id_params.gravity = Gravity.RIGHT;
//                time_params.gravity = Gravity.RIGHT;
//                image_viewHolder.user_id.setText(r_msg_list.getChatting_user_id());
//                byte[] decodedString = Base64.decode(r_msg_list.getChatting_msg(), Base64.DEFAULT);
//                image = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//                image_viewHolder.image_view.setImageBitmap(image);
//                image_viewHolder.image_view.setBackgroundResource(R.drawable.chat_right);
//                image_viewHolder.msg_time.setText(r_msg_list.getChatting_server_time());
//                image_viewHolder.user_pic.setVisibility(GONE);
//
//            } else { // 그외 사람이 메시지를 보냈다면
//                image_viewHolder.user_id.setText(r_msg_list.getChatting_user_id());
//                byte[] decodedString = Base64.decode(r_msg_list.getChatting_msg(), Base64.DEFAULT);
//                image = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
//                image_viewHolder.image_view.setImageBitmap(image);
//                image_viewHolder.image_view.setBackgroundResource(R.drawable.chat_left);
//                image_viewHolder.msg_time.setText(r_msg_list.getChatting_server_time());
//
//                if (!r_msg_list.getChatting_user_pic().equals("null")) { // 유저 사진 값이 null이라는 String 값이 아니라면
//                    Glide.with(mCtx).load(URLs.URL_IMAGE_ROOT + r_msg_list.getChatting_user_pic()).into(image_viewHolder.user_pic);
//
//                } else {
//                    image_viewHolder.user_pic.setImageResource(R.drawable.default_pic);
//                }
//            }
//
//            image_viewHolder.setItemClickListener(new ItemClickListener() {
//                @Override
//                public void onClick(View view, int position, boolean isLongClick) {
//                    if (!isLongClick) {
//                        Toast.makeText(mCtx, "이미지", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//        }
//
//    }
//
//    @Override
//    public int getItemViewType(int position) { // 하나의 리사이클러 뷰에 서로 다른 레이아웃을 2개 이상 넣을 때 쓰는 메소드
//        if (msg_list.get(position).getChatting_user_modify() == 0 || msg_list.get(position).getChatting_user_modify() == 1) {
//            return ITEM_TYPE_ENTER_MSG;
//        } else if (msg_list.get(position).isChatting_image() == false) {
//            return ITEM_TYPE_MSG;
//        } else {
//            return ITEM_TYPE_IMAGE;
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return msg_list.size();
//    }
//
//
//    public class Message_ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
//
//        private ItemClickListener itemClickListener;
//        public TextView user_id, user_msg, msg_time;
//        public CircleImageView user_pic;
//
//        public Message_ViewHolder(View itemView) {
//            super(itemView);
//            itemView.setOnClickListener(this);
//            itemView.setOnLongClickListener(this);
//            user_id = (TextView) itemView.findViewById(R.id.list_item_chat_user_id);
//            user_msg = (TextView) itemView.findViewById(R.id.list_item_chat_msg);
//            user_pic = (CircleImageView) itemView.findViewById(R.id.list_item_chat_user_pic);
//            msg_time = (TextView) itemView.findViewById(R.id.list_item_chat_time);
//        }
//
//        public void setItemClickListener(ItemClickListener itemClickListener) {
//            this.itemClickListener = itemClickListener;
//        }
//
//        @Override
//        public void onClick(View view) {
//            itemClickListener.onClick(view, getAdapterPosition(), false);
//        }
//
//        @Override
//        public boolean onLongClick(View view) {
//            itemClickListener.onClick(view, getAdapterPosition(), true);
//            return true;
//        }
//    }
//
//
//    public class User_Modify_ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
//
//        private ItemClickListener itemClickListener;
//        public TextView mdy_user_id;
//
//        public User_Modify_ViewHolder(View itemView) {
//            super(itemView);
//            itemView.setOnClickListener(this);
//            itemView.setOnLongClickListener(this);
//            mdy_user_id = (TextView) itemView.findViewById(R.id.list_item_chat_user_enter);
//        }
//
//        public void setItemClickListener(ItemClickListener itemClickListener) {
//            this.itemClickListener = itemClickListener;
//        }
//
//        @Override
//        public void onClick(View view) {
//            itemClickListener.onClick(view, getAdapterPosition(), false);
//        }
//
//        @Override
//        public boolean onLongClick(View view) {
//            itemClickListener.onClick(view, getAdapterPosition(), true);
//            return true;
//        }
//    }
//
//
//    public class Image_ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
//
//        private ItemClickListener itemClickListener;
//        public TextView user_id, msg_time;
//        public CircleImageView user_pic;
//        public ImageView image_view;
//
//        public Image_ViewHolder(View itemView) {
//            super(itemView);
//            itemView.setOnClickListener(this);
//            itemView.setOnLongClickListener(this);
//            user_id = (TextView) itemView.findViewById(R.id.list_item_chat_user_id2);
//            image_view = (ImageView) itemView.findViewById(R.id.list_item_chat_image);
//            user_pic = (CircleImageView) itemView.findViewById(R.id.list_item_chat_user_pic2);
//            msg_time = (TextView) itemView.findViewById(R.id.list_item_chat_time2);
//        }
//
//        public void setItemClickListener(ItemClickListener itemClickListener) {
//            this.itemClickListener = itemClickListener;
//        }
//
//        @Override
//        public void onClick(View view) {
//            itemClickListener.onClick(view, getAdapterPosition(), false);
//        }
//
//        @Override
//        public boolean onLongClick(View view) {
//            itemClickListener.onClick(view, getAdapterPosition(), true);
//            return true;
//        }
//    }
//
//
//}