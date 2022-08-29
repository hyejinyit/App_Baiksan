package kbar.baiksan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

//메인페이지
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static String App_path = "http://kbar3.cafe24.com/app/baiksan/20220812/"; // 실디비
    //public static String App_path = "http://kbar3.cafe24.com/app/baiksan/test/"; // 테스트디비

    View btn_in; // 입고처리
    View btn_in_can; // 입고취소처리
    View btn_out; // 출고처리
    View btn_jego; //재고실사
    View btn_set; //작업자등록
    TextView worker; //선택된 작업자
    TextView verCode; //버전정보
    TextView txtPrivate; //개인정보처리방침

    String m_worker; //작업자
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //레이아웃 아이디와 매칭
        btn_in = findViewById(R.id.btn_in); //입고관리
        btn_in_can = findViewById(R.id.btn_in_can); //입고취소처리
        btn_out = findViewById(R.id.btn_out); //출고처리
        btn_jego = findViewById(R.id.btn_jego); //재고실사
        btn_set = findViewById(R.id.btn_set); //작업자등록
        worker = findViewById(R.id.worker); //선택된작업자
        verCode = findViewById(R.id.verCode); //버전
        txtPrivate = findViewById(R.id.txtPrivate); //개인정보보호방침

        btn_in.setOnClickListener(this);
        btn_in_can.setOnClickListener(this);
        btn_out.setOnClickListener(this);
        btn_jego.setOnClickListener(this);
        btn_set.setOnClickListener(this);
        verCode.setOnClickListener(this);
        txtPrivate.setOnClickListener(this);

        SpannableString content = new SpannableString("개인정보처리방침");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        txtPrivate.setText(content);

        //테스트디비확인
        if (App_path.split("/")[5].equals("test")) {
            Toast.makeText(this, "테스트DB입니다.", Toast.LENGTH_SHORT).show();
        }

        checkToVersion(); //버전확인
        select_worker(); //현재 설정된 작업자 정보 가져오기
    }

    //현재 설정된 작업자 정보 가져오기
    public void select_worker(){
        //메모리에서 작업정보 들고오기
        SharedPreferences login_data = getSharedPreferences("login_data", MODE_PRIVATE);
        m_worker = login_data.getString("worker", null);

        if (m_worker == null) {
            this.setTitle(getString(R.string.app_name));
            worker.setText("작업자 미선택");
        } else {
            worker.setText(m_worker);
        }
    }

    //최신버전 확인
    public void checkToVersion() {
        class phpUp extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... urls) {
                StringBuilder jsonHtml = new StringBuilder();
                try {
                    // 연결 url 설정
                    URL url = new URL(urls[0]);
                    // 커넥션 객체 생성
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    // 연결되었으면.
                    if (conn != null) {
                        conn.setConnectTimeout(10000);

                        // 연결되었음 코드가 리턴되면.
                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

                            for (; ; ) {
                                // 웹상에 보여지는 텍스트를 라인단위로 읽어 저장.
                                String line = br.readLine();
                                if (line == null) break;
                                // 저장된 텍스트 라인을 jsonHtml에 붙여넣음
                                jsonHtml.append(line + "\n");
                            }
                            br.close();
                        }
                        conn.disconnect();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                Log.d("RESPONSE", "task 결과값 : " + jsonHtml.toString());
                return jsonHtml.toString();
                //return response;
            }

            @Override
            protected void onPostExecute(String str) {
                String ver_a = null; //가장 최근 버전
                String ver_pre = getResources().getString(R.string.ver_code);
                try {
                    JSONObject root = new JSONObject(str);
                    JSONArray ja = root.getJSONArray("results");

                    if (ja.length() > 0) {
                        //값이 있을떄 -> 입고 안함
                        for (int i = 0; i < ja.length(); i++) {
                            JSONObject jo = ja.getJSONObject(0);
                            ver_a = jo.getString("VER_CODE");
                        }
                        Log.d("Ver_a", ver_a); //php버전
                        Log.d("Ver_pre", ver_pre); //앱버전

                        //php버전이 더 높으면 앱 업데이트하세요 요청
                        //현재버전과 최신버전이 동일하지 않을 떄
                        //ftp > resource
                        if (Integer.parseInt(ver_a) > Integer.parseInt(ver_pre)) {
                            goDialog("업데이트", "어플리케이션의 상위 버전이 있습니다. \n업데이트 하시겠습니까?", "1", "update");
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        phpUp task1 = new phpUp();
        task1.execute(MainActivity.App_path + "chkversion.php");
        Log.d("task", MainActivity.App_path + "chkversion.php");
    }

    //메뉴클릭
    @Override
    public void onClick(View v) {
        Intent i;
        Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibe.vibrate(40);
        switch (v.getId()) {

            case R.id.btn_in: //입고처리
                if(m_worker==null){ //작업자 선택 확인
                    goDialog("확  인", "작업자를 선택해주세요!", "0", "");
                }else{
                    i = new Intent(MainActivity.this, In_Activity.class);
                    startActivity(i);
                }
                break;
            case R.id.btn_in_can: //입고취소처리
                if(m_worker==null){//작업자 선택 확인
                    goDialog("확  인", "작업자를 선택해주세요!", "0", "");
                }else{
                    i = new Intent(MainActivity.this, In_Can_Activity.class);
                    startActivity(i);
                }
                break;
            case R.id.btn_out: //출고관리
                if(m_worker==null){//작업자 선택 확인
                    goDialog("확  인", "작업자를 선택해주세요!", "0", "");
                }else{
                    i = new Intent(MainActivity.this, Out_sub_Activity.class);
                    startActivity(i);
                }
                break;
            case R.id.btn_jego: //재고실사
                if(m_worker==null){//작업자 선택 확인
                    goDialog("확  인", "작업자를 선택해주세요!", "0", "");
                }else{
                    i = new Intent(MainActivity.this, Jego_Activity.class);
                    startActivity(i);
                }
                break;
            case R.id.btn_set: //작업자등록
                i = new Intent(MainActivity.this, Set_Activity.class);
                startActivity(i);
                break;
            case R.id.verCode: //버전확인
                if (v.isClickable()) {
                    Context mContext;
                    mContext = getApplicationContext();
                    Toast toast = Toast.makeText(mContext, "Ver. " + getResources().getString(R.string.ver_code), Toast.LENGTH_SHORT);
                    toast.show();
                }
                break;
            case R.id.txtPrivate: //개인정보처리방침
                if (v.isClickable()) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://kbar.cafe24.com/app_pip/pip.php"));
                    startActivity(intent);
                }
                break;
        }
    }

    //팝업메세지
    public void goDialog(String title, String msg, String gubun, String msg_gubun) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(title);
        alert.setMessage(msg);

        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                switch (msg_gubun) {
                    case "update":
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://kbar3.cafe24.com:8080/DownloadApp_baiksan/index.jsp"));
                        startActivity(intent);
                        break;
                    case "exit":
                        ActivityCompat.finishAffinity(MainActivity.this);
                        System.runFinalization();
                        System.exit(0);
                        break;
                    default:
                        break;
                }
            }
        });
        if (gubun.equals("1")) {
            alert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
        }
        alert.show();
    }

    //자체뒤로가기
    @Override
    public void onBackPressed() {
        goDialog("종  료", "백산 PDA 프로그램을 \n종료하시겠습니까?", "1", "exit");
    }

}