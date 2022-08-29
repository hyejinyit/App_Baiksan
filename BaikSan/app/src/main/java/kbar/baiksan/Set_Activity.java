package kbar.baiksan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

//작업자등록
public class Set_Activity extends AppCompatActivity {

    TextView txtworker;
    Spinner spinW;
    Button btn_can;
    Button btn_ok;

    ArrayAdapter<String> adpW; //작업자 어댑터
    ArrayList<String> ListW = new ArrayList<String>(); //작업자 list

    String s_worker; //선택한 작업자

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_activity);

        txtworker = findViewById(R.id.txtworker); //현재 작업자
        spinW = findViewById(R.id.spinW); //작업자 스피너
        btn_can = findViewById(R.id.btn_can);//닫기버튼
        btn_ok = findViewById(R.id.btn_ok);//확인버튼

        select_worker(); //현재설정된 작업자 정보 가져오기
        getWorker(); //작업자스피너 목록

        //확인버튼
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(40);
                if (s_worker.equals("null")) {
                    goDialog("확  인", "작업자를 선택해주세요.", "0", "default");
                } else {
                    goDialog("확  인", "작업정보를 설정하시겠습니까?", "1", "ok");
                }
            }
        });

        //닫기버튼
        btn_can.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(40);

                Intent i = new Intent(Set_Activity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        });
    }

    //현재설정된 작업자 정보 가져오기
    public void select_worker(){
        //메모리에서 작업정보 들고오기
        SharedPreferences login_data = getSharedPreferences("login_data", MODE_PRIVATE);
        String m_worker = login_data.getString("worker", null);

        if (m_worker == null) {
            this.setTitle(getString(R.string.app_name));
            txtworker.setText("작업자 미선택");
        } else {
            this.setTitle(getString(R.string.app_name)+ "(" + m_worker + ")");
            txtworker.setText(m_worker);
        }
    }

    //작업자 정보 저장
    public void saved_worker(){

        SharedPreferences login_data = getSharedPreferences("login_data", MODE_PRIVATE);
        SharedPreferences.Editor m_data = login_data.edit();
        m_data.putString("worker", s_worker); //
        m_data.commit();

        select_worker();
    }

    //작업자 목록 불러오기
    public void getWorker() {
        class phpDown extends AsyncTask<String, Integer, String> {
            @Override
            protected String doInBackground(String... urls) {
                StringBuilder jsonHtml = new StringBuilder();
                try {
                    URL url = new URL(urls[0]);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    if (conn != null) {
                        conn.setConnectTimeout(10000);
                        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                            for (; ; ) {
                                String line = br.readLine();
                                if (line == null) break;
                                jsonHtml.append(line + "\n");
                            }
                            br.close();
                        }
                        conn.disconnect();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                Log.d("RESPONSE1", "task 결과값 : " + jsonHtml.toString());
                return jsonHtml.toString();
            }

            @Override
            protected void onPostExecute(String str) {

                String col1;
                ListW.clear();
                ListW.add("[작업자 선택]");
                try {
                    JSONObject root = new JSONObject(str);
                    JSONArray ja = root.getJSONArray("results");
                    //값이 있을떄
                    if (ja.length() > 0) {


                        for (int i = 0; i < ja.length(); i++) {

                            JSONObject jo = ja.getJSONObject(i);

                            col1 = jo.getString("worker");
                            ListW.add(col1);

                        }
                    }
                    spinWorker();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        phpDown task = new phpDown();
        task.execute(MainActivity.App_path + "workerspinner.php");
        Log.d("php", MainActivity.App_path + "workerspinner.php");
    }
    public void spinWorker() {
        adpW = new ArrayAdapter<String>(this, R.layout.spinner, ListW);
        adpW.setDropDownViewResource(R.layout.spinner);
        spinW.setAdapter(adpW);
        spinW.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    s_worker = adpW.getItem(position);
                } else if (position == 0) {
                    s_worker = "null";
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    //팝업메세지
    public void goDialog(String title, String msg, String gubun, String msg_gubun) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(title);
        alert.setMessage(msg);

        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                switch (msg_gubun) {
                    case "ok":
                        saved_worker();
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
        Intent i = new Intent(Set_Activity.this, MainActivity.class);
        startActivity(i);
    }
}
