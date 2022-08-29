package kbar.baiksan;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

//출고관리 서브메뉴
public class Out_sub_Activity extends AppCompatActivity implements View.OnClickListener {

    View btn_out; // 정상출고
    View btn_del; // 주문취소출고
    View btn_out_can; // 출고취소
    View btn_can; //닫기버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.out_sub_activity);

        //레이아웃 아이디와 매칭
        btn_out = findViewById(R.id.btn_out); // 정상출고
        btn_del = findViewById(R.id.btn_del); // 주문취소출고
        btn_out_can = findViewById(R.id.btn_out_can); //출고취소
        btn_can = findViewById(R.id.btn_can); //닫기버튼

        btn_out.setOnClickListener(this);
        btn_del.setOnClickListener(this);
        btn_out_can.setOnClickListener(this);
        btn_can.setOnClickListener(this);

        //메모리에서 작업정보 들고오기
        SharedPreferences login_data = getSharedPreferences("login_data", MODE_PRIVATE);
        String m_worker = login_data.getString("worker", null);
        TextView p_worker = (TextView)findViewById(R.id.p_worker);
        p_worker.setText(m_worker);

    }

    //메뉴클릭
    @Override
    public void onClick(View v) {
        Intent i;
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(40);
        switch (v.getId()) {

            case R.id.btn_out: //정상출고처리
                i = new Intent(Out_sub_Activity.this, Out_Activity.class);
                startActivity(i);
                break;
            case R.id.btn_del: //주문취소출고
                i = new Intent(Out_sub_Activity.this, Out_Del_Activity.class);
                startActivity(i);
                break;
            case R.id.btn_out_can: //출고취소
                i = new Intent(Out_sub_Activity.this, Out_Can_Activity.class);
                startActivity(i);
                break;
            case R.id.btn_can: //닫기버튼
               i = new Intent(Out_sub_Activity.this, MainActivity.class);
                startActivity(i);
                break;
        }
    }

    //자체뒤로가기
    @Override
    public void onBackPressed() {
        Intent i = new Intent(Out_sub_Activity.this, MainActivity.class);
        startActivity(i);
    }

}
