package com.yimeinew.adapter;

import android.content.Context;
import android.text.Layout;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.yimeinew.activity.R;
import com.yimeinew.entity.Pair;

import java.util.List;

public class SpinnerAdapterImpl  {

    public  static ArrayAdapter getSpinnerAdapter(Context context, List<Pair> pairList){
        ArrayAdapter<Pair> adapter = new ArrayAdapter<Pair>(context, R.layout.simple_spinner_item_text, pairList);
        return adapter;
    }

}
