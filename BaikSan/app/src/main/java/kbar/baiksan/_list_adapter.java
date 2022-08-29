package kbar.baiksan;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class _list_adapter extends BaseAdapter {

    public interface OnItemClickListener {
        void onItemClick(View view, String t_barcode);
    }

    _list_adapter.OnItemClickListener mListener2;

    public void setOnItemClickListener(_list_adapter.OnItemClickListener listener) {
        mListener2 = listener;
    }

    private Context mContext;
    private int layout;
    private ArrayList<_datalist> insertList;

    public _list_adapter(Context mContext, int layout, ArrayList<_datalist> outList) {
        this.mContext = mContext;
        this.layout = layout;
        this.insertList = outList;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return insertList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return insertList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }


    public static class ViewHolder {
        TextView t_barcode; //바코드
        TextView t_orderno; //오더넘버
        TextView t_item; //제품명
        TextView t_buyer; //거래처
        TextView t_color_type; //색상/무늬
        TextView t_spec; //규격
        TextView t_pqty; //수량
        View layout_bg; //배경
        View s_layout_bg; //방금스캔배경
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        _list_adapter.ViewHolder holder = new _list_adapter.ViewHolder();

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        convertView = inflater.inflate(layout, parent, false);
        holder.t_barcode = (TextView) convertView.findViewById(R.id.t_barcode);
        holder.t_orderno = (TextView) convertView.findViewById(R.id.t_orderno);
        holder.t_item = (TextView) convertView.findViewById(R.id.t_item);
        holder.t_buyer = (TextView) convertView.findViewById(R.id.t_buyer);
        holder.t_color_type = (TextView) convertView.findViewById(R.id.t_color_type);
        holder.t_spec = (TextView) convertView.findViewById(R.id.t_spec);
        holder.t_pqty = (TextView) convertView.findViewById(R.id.t_pqty);
        holder.layout_bg = (View) convertView.findViewById(R.id.layout_bg);
        holder.s_layout_bg = (View) convertView.findViewById(R.id.s_layout_bg);

        convertView.setTag(holder);
        holder.t_barcode.setText(insertList.get(position).gett_barcode());
        holder.t_orderno.setText(insertList.get(position).gett_orderno());
        holder.t_item.setText(insertList.get(position).gett_item());
        holder.t_buyer.setText(insertList.get(position).gett_buyer());
        holder.t_color_type.setText(insertList.get(position).gett_color_type());
        holder.t_spec.setText(insertList.get(position).gett_spec());
        holder.t_pqty.setText(insertList.get(position).gett_pqty());

        if(insertList.get(position).getpre_scan_data().equals(insertList.get(position).gett_barcode())){

            holder.layout_bg.setVisibility(View.INVISIBLE);
            holder.s_layout_bg.setVisibility(View.VISIBLE);

            //배경 롱클릭 이벤트
            holder.s_layout_bg.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mListener2 != null) {
                        mListener2.onItemClick(v, insertList.get(position).gett_barcode());
                    }
                    return false;
                }
            });
        }else{
            holder.layout_bg.setVisibility(View.VISIBLE);
            holder.s_layout_bg.setVisibility(View.INVISIBLE);

            //배경 롱클릭 이벤트
            holder.layout_bg.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mListener2 != null) {
                        mListener2.onItemClick(v, insertList.get(position).gett_barcode());
                    }
                    return false;
                }
            });
        }

        return convertView;
    }

}