package kbar.baiksan;

public class _datalist {

    //재고목록을 보여줘야하는 경우
    //스캔리스트
    String pre_scan_data; //방금 스캔한 바코드
    String t_barcode; //바코드
    String t_orderno; //오더넘버
    String t_item; //제품명
    String t_buyer; //거래처
    String t_color_type; //색상/무늬
    String t_spec; //규격
    String t_pqty; //수량

    //방금스캔한바코드
    public String getpre_scan_data() {
        return pre_scan_data;
    }
    public void setpre_scan_data(String pre_scan_data) {
        this.pre_scan_data = pre_scan_data;
    }

    //바코드
    public String gett_barcode() {
        return t_barcode;
    }
    public void sett_barcode(String t_barcode) {
        this.t_barcode = t_barcode;
    }

    //오더넘버
    public String gett_orderno() {
        return t_orderno;
    }
    public void sett_orderno(String t_orderno) {
        this.t_orderno = t_orderno;
    }

    //제품명
    public String gett_item() {
        return t_item;
    }
    public void sett_item(String t_item) {
        this.t_item = t_item;
    }

    //거래처
    public String gett_buyer() {
        return t_buyer;
    }
    public void sett_buyer(String t_buyer) {
        this.t_buyer = t_buyer;
    }

    //색상/무늬
    public String gett_color_type() {
        return t_color_type;
    }
    public void sett_color_type(String t_color_type) {
        this.t_color_type = t_color_type;
    }

    //규격
    public String gett_spec() {
        return t_spec;
    }
    public void sett_spec(String t_spec) {
        this.t_spec = t_spec;
    }

    //수량
    public String gett_pqty() {
        return t_pqty;
    }
    public void sett_pqty(String t_pqty) {
        this.t_pqty = t_pqty;
    }

}
