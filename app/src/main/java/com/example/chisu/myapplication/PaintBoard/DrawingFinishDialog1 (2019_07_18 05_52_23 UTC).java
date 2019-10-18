package com.example.chisu.myapplication.PaintBoard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chisu.myapplication.R;


public class DrawingFinishDialog1 extends Activity {

    //액션바 관련 문제는 앱컴퍁액티비티를 그냥 액티비티로 수정하면 된다.

    TextView title;
    EditText inputName;
    Button namingButton;
    Button closeBtn;

    String direction;
    String drawScore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.drawing_finish_dialog1);

        title = findViewById(R.id.titleView);
        inputName = findViewById(R.id.artNameInput);
        namingButton = findViewById(R.id.namingButton);

        //받은 걸 그대로 전해주기
        Intent intent = getIntent();
        direction = intent.getStringExtra("direction");
        drawScore = intent.getStringExtra("drawScore");
        Log.e("drawScore", drawScore);


        closeBtn = findViewById(R.id.closeButton);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // dispose this activity
                finish();
            }
        });

        namingButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                String inputedArtName = inputName.getText().toString();

                Intent intent = new Intent(getApplicationContext(), DrawingFinishDialog2.class);
                if (inputedArtName != null){ //사용자가 이름을 입력했는지 체크를 클라에서 할 수 있게. 로그인 참고.

                    //사용자가 입력한 작품의 제목
                    intent.putExtra("inputedArtName", inputedArtName);
                    intent.putExtra("direction", direction);
                    intent.putExtra("drawScore", drawScore);
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(getApplicationContext(), "작품의 이름을 입력해 주세요.", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }}
