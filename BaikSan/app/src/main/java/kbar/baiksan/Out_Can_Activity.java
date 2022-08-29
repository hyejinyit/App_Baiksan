package kbar.baiksan;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

//출고취소
/*
 * 스캔 - 출고내역조회(이미 출고된 바코드인지 ) - 출고취소처리
*/
public class Out_Can_Activity extends AppCompatActivity {

    Button btn_can; //닫기버튼
    View gubunbg;
    View btn_scan; //카메라스캔
    EditText Escan; //스캔텍스트박스
    TextView orderno; //오더넘버
    TextView buyer; //거래처
    TextView item; //제품명
    TextView color_type; //색상+무늬
    TextView spec; //규격
    TextView gubun; //구분(메인,샘플)
    TextView o_qty; //발주수량
    TextView i_qty; //입고수량
    TextView s_qty; //스캔수량 => 현재 바코드의 수량

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.out_can_activity);

        btn_scan = findViewById(R.id.btn_scan);
        btn_can = findViewById(R.id.btn_can);
        Escan = findViewById(R.id.Escan);
        orderno = findViewById(R.id.orderno);
        buyer = findViewById(R.id.buyer);
        item = findViewById(R.id.item);
        color_type = findViewById(R.id.color_type);
        spec = findViewById(R.id.spec);
        o_qty = findViewById(R.id.o_qty);
        i_qty = findViewById(R.id.i_qty);
        s_qty = findViewById(R.id.s_qty);
        gubun = findViewById(R.id.gubun);
        gubunbg = findViewById(R.id.gubunbg);

        //메모리에서 작업정보 들고오기
        SharedPreferences login_data = getSharedPreferences("login_data", MODE_PRIVATE);
        String m_worker = login_data.getString("worker", null);
        TextView p_worker = (TextView)findViewById(R.id.p_worker);
        p_worker.setText(m_worker);

        //카메라스캔
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(40);

                //카메라 스캔
                IntentIntegrator integrator = new IntentIntegrator(Out_Can_Activity.this);
                integrator.setOrientationLocked(true);
                integrator.setPrompt("바코드 스캔을 위해\n상자안에 위치시켜 주세요\n\n");  // 밑에 문구 수정 가능
                integrator.initiateScan();

            }
        });

        //바코드스캔
        Escan.requestFocus();
        Escan.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {

                    Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    vibe.vibrate(40);

                    String scan_data = Escan.getText().toString().trim();
                    if (scan_data.length() == 0) {
                        goDialog("확  인", "바코드를 스캔해주세요.");
                    } else {
                        select_outcheck(scan_data); //출고내역조회
                    }

                    return true;
                }
                return false;
            }
        });

        //닫기버튼
        btn_can.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(40);

                Intent i = new Intent(Out_Can_Activity.this, Out_sub_Activity.class);
                startActivity(i);
                finish();
            }
        });
    }

    //카메라 스캔 결과 반환
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                Escan.setText("");
                Escan.requestFocus();
            } else {
                if (result.getContents().length() == 0) {
                    goDialog("확  인", "바코드를 스캔해주세요.");
                } else {
                    String scan_data = result.getContents();
                    select_outcheck(scan_data); //출고내역조회

                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //출고내역조회
    public void select_outcheck(String bar){
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
                try {
                    JSONObject root = new JSONObject(str);
                    JSONArray ja = root.getJSONArray("results");
                    //값이 있을떄
                    if (ja.length() > 0) {

                        for (int i = 0; i < ja.length(); i++) {
                            JSONObject jo = ja.getJSONObject(i);

                            col1 = jo.getString("out_cnt");//0:출고된적없음, 1~: 출고된적있음

                            if(col1.equals("0")){
                                select_info(bar,"e_out"); //출고된 내역없음!!!
                            }else{
                                select_info(bar,"ok");
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        //목록 가져오기
        phpDown task = new phpDown();
        task.execute(MainActivity.App_path + "select_outcheck.php?bar=" + bar);
        Log.d("php", MainActivity.App_path + "select_outcheck.php?bar=" + bar);
    }

    //바코드정보
    public void select_info(String bar,String error_ck){
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
                String col1, col2, col3, col4, col5, col6, col7;
                String col8, col9, col10,col11, col12;
                try {
                    JSONObject root = new JSONObject(str);
                    JSONArray ja = root.getJSONArray("results");
                    //값이 있을떄
                    if (ja.length() > 0) {

                        for (int i = 0; i < ja.length(); i++) {
                            JSONObject jo = ja.getJSONObject(i);

                            col1 = jo.getString("order_no"); //오더번호
                            col2 = jo.getString("roll_no"); //일련번호
                            col3 = jo.getString("o_bizname"); //거래처
                            col4 = jo.getString("o_item"); //제품명
                            col5 = jo.getString("o_crname"); //색상
                            col6 = jo.getString("o_type"); //무늬
                            col7 = jo.getString("o_spec"); //규격
                            col8 = jo.getString("o_orderqty"); //발주수량
                            col9 = jo.getString("roll_qty"); //스캔수량
                            col10 = jo.getString("oqty"); //이전출고수량
                            col11 = jo.getString("roll_ea");//수량단위
                            col12 = jo.getString("o_gubun"); //라벨구분

                            String s_gubun = "메인";
                            if(col12.equals("S")){
                                gubunbg.setBackgroundResource(R.drawable.gubunbg);
                                s_gubun = "샘플";
                            }else{
                                gubunbg.setBackgroundColor(Color.WHITE);
                                s_gubun = "메인";
                            }
                            orderno.setText(col1+"("+col2+")");
                            buyer.setText(col3);
                            item.setText(col4);
                            color_type.setText(col5+" / "+col6);
                            spec.setText(col7);
                            gubun.setText(s_gubun);
                            o_qty.setText(col8+col11);
                            s_qty.setText(col9+col11);

                            if(error_ck.equals("e_out")){
                                select_outqty(col1, col11);
                                goDialog("확  인", "출고처리되지않은 바코드입니다.");
                            }
                            else{
                                delete_out(bar,col1,col11);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        //목록 가져오기
        phpDown task = new phpDown();
        task.execute(MainActivity.App_path + "select_info.php?bar=" + bar);
        Log.d("php", MainActivity.App_path + "select_info.php?bar=" + bar);
    }

    //출고처리
    public void delete_out(String bar,String order_no, String ea){
        class phpUp extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {

                try {
                    String data = MainActivity.App_path + "delete_out.php?";
                    data += URLEncoder.encode("bar", "UTF-8") + "=" + URLEncoder.encode(bar, "UTF-8");  //

                    Log.d("php", data);
                    URL url = new URL(data);
                    URLConnection conn = url.openConnection();

                    conn.setDoOutput(true);
                    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

                    wr.write(data);
                    wr.flush();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line = null;

                    // Read Server Response
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                        break;
                    }
                    return sb.toString();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return "0"; // 디비에서 덮어 씌우기를 하는 중
            }
        }
        phpUp task1 = new phpUp();
        task1.execute();
        Toast.makeText(Out_Can_Activity.this, "출고취소 완료!\n(" + bar + ")", Toast.LENGTH_SHORT).show();
        select_outqty(order_no,ea);
        Escan.setText("");
        Escan.requestFocus();
    }

    //출고수량 확인
    public void select_outqty(String orderno,String ea){
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
                try {
                    JSONObject root = new JSONObject(str);
                    JSONArray ja = root.getJSONArray("results");
                    //값이 있을떄
                    if (ja.length() > 0) {

                        for (int i = 0; i < ja.length(); i++) {
                            JSONObject jo = ja.getJSONObject(i);

                            col1 = jo.getString("out_qty"); //해당오더 총 출고수량

                            i_qty.setText(col1+ea); //출고수량 표시

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        //목록 가져오기
        phpDown task = new phpDown();
        task.execute(MainActivity.App_path + "select_outqty.php?orderno=" + orderno);
        Log.d("php", MainActivity.App_path + "select_outqty.php?orderno=" + orderno);
    }

    //팝업메세지
    public void goDialog(String title, String msg) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle(title);
        alert.setMessage(msg);

        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                Escan.setText("");
                Escan.requestFocus();
            }
        });
        alert.show();
    }

    //자체뒤로가기
    @Override
    public void onBackPressed() {
        Intent i = new Intent(Out_Can_Activity.this, Out_sub_Activity.class);
        startActivity(i);
    }
}

