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

//입고처리
/*
<주요기능>
- 입고처리 : 스캔 - 입고중복체크 - 해당바코드의 오더정보 - 입고처리 - 입고수량 확인(재고수량표시)
*/
public class In_Activity extends AppCompatActivity {

    Button btn_can; //닫기버튼
    View btn_scan; //카메라스캔
    View gubunbg; //구분 배경
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

    String m_worker = "null";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.in_activity);

        btn_scan = findViewById(R.id.btn_scan);
        btn_can = findViewById(R.id.btn_can);
        Escan = findViewById(R.id.Escan);
        orderno = findViewById(R.id.orderno);
        buyer = findViewById(R.id.buyer);
        item = findViewById(R.id.item);
        color_type = findViewById(R.id.color_type);
        spec = findViewById(R.id.spec);
        gubun=findViewById(R.id.gubun);
        o_qty = findViewById(R.id.o_qty);
        i_qty = findViewById(R.id.i_qty);
        s_qty = findViewById(R.id.s_qty);
        gubunbg=findViewById(R.id.gubunbg);

        //메모리에서 작업정보 들고오기
        SharedPreferences login_data = getSharedPreferences("login_data", MODE_PRIVATE);
        m_worker = login_data.getString("worker", null);
        TextView p_worker = (TextView)findViewById(R.id.p_worker);
        p_worker.setText(m_worker);

        //카메라스캔
        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vibrator vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(40);

                //카메라 스캔
                IntentIntegrator integrator = new IntentIntegrator(In_Activity.this);
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
                        select_incheck(scan_data); //입고내역조회
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

                Intent i = new Intent(In_Activity.this, MainActivity.class);
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
                    select_incheck(scan_data); //입고내역조회

                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //입고내역조회
    public void select_incheck(String bar){
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

                            col1 = jo.getString("in_cnt");//0:입고된적없음, 1~: 입고된적있음

                            if(col1.equals("0")){
                                select_info(bar,"ok");
                            }else{
                                select_info(bar,"e_in"); //입고중복!!!
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
        task.execute(MainActivity.App_path + "select_incheck.php?bar=" + bar);
        Log.d("php", MainActivity.App_path + "select_incheck.php?bar=" + bar);
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
                            col10 = jo.getString("iqty"); //이전입고수량
                            col11 = jo.getString("roll_ea"); //수량단위
                            col12 = jo.getString("o_gubun"); //라벨구분

                            //원단구분
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

                            if(error_ck.equals("e_in")){ //입고중복인 경우 메세지창!!
                                select_inqty(col1, col8, col11,"ok");
                                goDialog("확  인", "이미 입고처리된 바코드입니다.");
                            }
                            else{
                                insert_in(bar,col1,col2,col9,col8,col11); //입고처리
                            }
                        }
                    }else{ //발주처리되지않은경우 => 전체 내용 리셋
                        gubunbg.setBackgroundColor(Color.WHITE);
                        orderno.setText("");
                        buyer.setText("");
                        item.setText("");
                        color_type.setText("");
                        spec.setText("");
                        gubun.setText("");
                        o_qty.setText("");
                        s_qty.setText("");
                        i_qty.setText("");
                        goDialog("확  인", "발주처리되지 않은 바코드입니다.");
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

    //입고처리
    public void insert_in(String bar,String order_no, String roll_no, String s_qty, String oqty,String ea){
        class phpUp extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {

                try {
                    String data = MainActivity.App_path + "insert_in.php?";
                    data += URLEncoder.encode("bar", "UTF-8") + "=" + URLEncoder.encode(bar, "UTF-8");  //바코드
                    data += "&" + URLEncoder.encode("order_no", "UTF-8") + "=" + URLEncoder.encode(order_no, "UTF-8"); //오더넘버
                    data += "&" + URLEncoder.encode("roll_no", "UTF-8") + "=" + URLEncoder.encode(roll_no, "UTF-8");  //롤넘버
                    data += "&" + URLEncoder.encode("worker", "UTF-8") + "=" + URLEncoder.encode(m_worker, "UTF-8");  //작업자
                    data += "&" + URLEncoder.encode("s_qty", "UTF-8") + "=" + URLEncoder.encode(s_qty, "UTF-8");  //스캔수량

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
        Toast.makeText(In_Activity.this, "입고 완료!\n(" + bar + ")", Toast.LENGTH_SHORT).show();
        select_inqty(order_no,oqty,ea,"no");
        Escan.setText("");
        Escan.requestFocus();
    }

    //전체 입고수량 확인
    public void select_inqty(String orderno, String oqty, String ea, String error_ck){
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
                String col1,col2;
                try {
                    JSONObject root = new JSONObject(str);
                    JSONArray ja = root.getJSONArray("results");
                    //값이 있을떄
                    if (ja.length() > 0) {

                        for (int i = 0; i < ja.length(); i++) {
                            JSONObject jo = ja.getJSONObject(i);

                            col1 = jo.getString("in_qty"); //해당오더 총 입고수량
                            col2 = jo.getString("out_qty"); //해당오더 총 출고수량

                            String total= String.valueOf(Integer.parseInt(col1)- Integer.parseInt(col2)); //재고수량

                            i_qty.setText(total+ea); //입고수량에 재고수량 표시

                            //발주수량만큼 입고가 다 되면 색 다르게 표시
                            if(oqty.equals(total)){
                                o_qty.setBackgroundColor(Color.GRAY);
                                i_qty.setBackgroundColor(Color.GRAY);
                                o_qty.setTextColor(Color.BLACK);
                                i_qty.setTextColor(Color.BLACK);
/*                                if(error_ck.equals("no")){ //전체 입고완료된 경우(오류가 없을때)
                                    goDialog("확 인","해당 오더의 요청발주수량 입고가 완료되었습니다.");
                                }*/
                            }else{
                                o_qty.setBackgroundResource(R.drawable.oqtybox);
                                i_qty.setBackgroundResource(R.drawable.iqtybox);
                                o_qty.setTextColor(Color.WHITE);
                                i_qty.setTextColor(Color.WHITE);
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
        task.execute(MainActivity.App_path + "select_inqty.php?orderno=" + orderno);
        Log.d("php", MainActivity.App_path + "select_inqty.php?orderno=" + orderno);
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
        Intent i = new Intent(In_Activity.this, MainActivity.class);
        startActivity(i);
    }

}

