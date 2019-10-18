package com.example.chisu.myapplication;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;


public class ArticleViewpagerAdapter extends PagerAdapter {

    private List<art> artList;
    private Context context;

    //바깥쪽에 리스트를 둔다. 그리고 이 생성자로 대입하면 된다. 그러면 여기의 리스트에 데이터를 넣을 필요가 없지.
    ArticleViewpagerAdapter(List<art> artList1 , Context ctx){
        this.artList = artList1;
        this.context = ctx;
    }

    //데이터를 양쪽에 둔 뒤에 뷰페이저에서 셋커런트아이템으로 3번째 아이템을 보게 한다면?
    @Override
    public int getCount() {
        return artList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view==object);
    }

    //ViewPager가 현재 보여질 Item(View객체)를 생성할 필요가 있는 때 자동으로 호출
    //쉽게 말해, 스크롤을 통해 현재 보여져야 하는 View를 만들어냄.
    //첫번째 파라미터 : ViewPager
    //두번째 파라미터 : ViewPager가 보여줄 View의 위치(가장 처음부터 0,1,2,3...)

    //Article 클래스에서 pageSelcted로 정보를 설정하는 것은 훌륭하나, 맨 처음이 문제다.
    //그 때문에 어댑터 클래스에서 초기 설정만 담당하도록, instantiateItem메소드에서
    //첫 화면에 처음 아이템(0번)의 정보들을 나타나도록 하고 그 뒤로는 아무것도 작동 안하게 한다.
    //그 뒤 작동은 pageSelected가 하기 때문이다. 밑의 isfirst는 그 용도.
    private int isfirst = 0;

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View item_view = layoutInflater.inflate(R.layout.swipe_layout, container ,false);

        //뷰페이저 내의 메인 이미지.
        ImageView photoView = item_view.findViewById(R.id.image_view);
//        com.github.chrisbanes.photoview.PhotoView photoView = item_view.findViewById(R.id.image_view);

        //뷰페이저 정보 설정. 다른 클래스의 객체를 조절하기 위해 스태틱을 사용했다.
        String newIp = "http://13.125.114.107";
        String imageResult = artList.get(position).getImage();
        imageResult = imageResult.substring(imageResult.lastIndexOf("m/")+1);
        imageResult = newIp+imageResult;

        Glide.with(context)
                .load(imageResult)
                .into(photoView);

        //리스트에 있는 숫자는 유지되기 때문이야. 그렇기 때문에 onresume에서 다시 http 통신으로 db에서 데이터를 불러와야 해.

        //뷰페이저의 첫 번째 페이지 초기화. Article클래스의 멤버들을 조절한다.
        if(position == 0 && isfirst ==0){
            Article.itemUser = artList.get(position).getUser();
            Article.recom1 = artList.get(position).getRecom();
            Article.value1 = artList.get(position).getValue();
            Article.comm1 = artList.get(position).getCommentNum();
//            Article.drawer = artList.get(position).getUser();

            Article.textViewTitle.setText(artList.get(position).getTitle());
            Article.textViewRecom.setText("좋아요 " + artList.get(position).getRecom() + "명");
            Article.textViewValue.setText("작품의 가치 : " + artList.get(position).getValue());
            Article.textViewComment.setText("댓글 " + artList.get(position).getCommentNum() + "개");
            Article.textViewCreated.setText(artList.get(position).getCreated());
//            Article.textViewDesc.setText(artList.get(position).getDesc());
            Article.textViewUser.setText(artList.get(position).getUser());

            Log.e("현재 댓글 개수  ", artList.get(position).getCommentNum());
            String newIp2 = "http://13.125.114.107";
            String imageResult2 = artList.get(position).getUserImage();
            imageResult2 = imageResult2.substring(imageResult2.lastIndexOf("m/")+1);
            imageResult2 = newIp2+imageResult2;

            Log.e("artstreet",imageResult);

            Glide.with(context)
                    .load(imageResult2)
                    .into(Article.imageViewUser);

            isfirst++;
        }

        container.addView(item_view);

        //뷰페이저에 정보 변화를 넣으면 문제인 게... 글자들이 그림과 함께 페이징된다..

        //화면을 클릭할 때마다 정보창이 생겼다 사라졌다 하는 클릭 리스너를 어댑터에 설치.
        item_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("화면 클릭" ,"ㅇ");

                if(!Article.isInfo){

                    //애니메이션 시도 후 실패
//                    Animation animation = AnimationUtils.loadAnimation(context, R.anim.alpha_anim);
//                    Article.artInfo.startAnimation(animation);

                    Article.artInfo.setAlpha(1);
                    Article.isInfo = true;
                    Log.e("정보화면이 보인다" ,"ㅇ");

                } else {
                    //setVisibility를 할 경우 클릭 등의 작동도 정지된다.
                    //때문에 투명화를 사용했다.
                    //artInfo.setVisibility(View.INVISIBLE);
                    Article.artInfo.setAlpha(0);
                    Log.e("정보화면이 안보인다" ,"ㅇ");
                    Article.isInfo = false;
                }
            }
        });

        return item_view;
    }

    //화면에 보이지 않은 View는 파괴를 해서 메모리를 관리함.
    //첫번째 파라미터 : ViewPager
    //두번째 파라미터 : 파괴될 View의 인덱스(가장 처음부터 0,1,2,3...)
    //세번째 파라미터 : 파괴될 객체(더 이상 보이지 않은 View 객체)
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        container.removeView((ConstraintLayout)object);
    }

}
