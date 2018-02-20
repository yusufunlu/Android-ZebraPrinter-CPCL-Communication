package com.zebra.isv.scanandpairbluetooth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

/**
 * Created by dtyunlu on 15.06.2017.
 */
public class AdapterSpinnerGeneric extends ArrayAdapter<GenericTable>
{
    private Context context;

    List<GenericTable> itemList = null;

    public AdapterSpinnerGeneric(Context context, List<GenericTable> _itemList)
    {
        //se debe indicar el layout para el item que seleccionado (el que se muestra sobre el botón del botón)
        super(context, R.layout.adapter_spinner_layout, _itemList);
        this.context = context;
        this.itemList = _itemList;
    }

    //este método establece el elemento seleccionado sobre el botón del spinner
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.adapter_spinner_layout,null);
        }
        ((TextView) convertView.findViewById(R.id.title)).setText(itemList.get(position).getAd());

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        View row = convertView;
        if (row == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = layoutInflater.inflate(R.layout.adapter_spinner_layout, parent, false);
        }

        TextView title = (TextView) row.findViewById(R.id.title);
        title.setText(itemList.get(position).getAd());

        return row;
    }
}