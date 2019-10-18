package com.example.chisu.myapplication;

public class URLs {

    /**
     *  URL을 한 곳에 모으기 위한 클래스다.
     */

    //api라는 php파일에 ?를 통해서 각각 다른 기능을 하게 한다. 로그인/가입 말고 다른 기능에 사용해도 좋을 거 같다.
    public static final String ROOT_URL = "http://13.125.114.107/Api.php?apicall=";

    public static final String URL_REGISTER = ROOT_URL + "signup";
    public static final String URL_LOGIN= ROOT_URL + "login";

    //글 작성 URL
    public static final String UPLOAD_URL = ROOT_URL + "uploadpic";

    //프로필 변경 URL
    public static final String PROFILE_CHANGES = "http://13.125.114.107/profileApi.php";

    //뷰페이저용 URL
    public static final String VIEW_PAGER_LOAD = "http://13.125.114.107/retrieveApi.php";

    //메인 리사이클러뷰 URL
    public static final String FEED = "http://13.125.114.107/feedApi.php";

    //좋아요 URL. 해당하는 api = like(좋아요 올리기/취소), show(좋아요 보기)
    public static final String LIKE = "http://13.125.114.107/likeApi.php?apicall=";

    //댓글 URL. 해당하는 api = check(댓글 보기)
    public static final String COMMENT_URL = "http://13.125.114.107/commentApi.php?apicall=";

    //줌 화면 URL.
    public static final String ZOOM_IMAGE_URL = "http://13.125.114.107/articleApi.php";

    //팔로우 관련 URL
    public static final String FOLLOW_URL = "http://13.125.114.107/followApi.php?apicall=";

    //지도 관련 URL
    public static final String LOCATION_FROM_MAPS = "http://13.125.114.107/locationApi.php?apicall=";

    //드로잉 관련 URL
    public static final String DRAWING_URL = "http://13.125.114.107/drawingsApi.php?apicall=";

    //AR 관련 URL
    public static final String AR_URL = "http://13.125.114.107/ArApi.php?apicall=";

    //태그 관련 url
    public static final String TAG_URL = "http://13.125.114.107/TagApi.php?apicall=";


}
