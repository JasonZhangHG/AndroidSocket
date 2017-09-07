package io.github.mayubao.kuaichuan.ui.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.github.mayubao.kuaichuan.R;
import io.github.mayubao.kuaichuan.common.BlueBean;
import io.github.mayubao.kuaichuan.common.CommonAdapter;



public class BlueSendAdapter extends CommonAdapter<BlueBean> {

    public BlueSendAdapter(Context context, List<BlueBean> dataList) {
        super(context, dataList);
    }

    @Override
    public View convertView(int position, View convertView) {
        ScanResultHolder viewHolder = null;

        if (convertView == null) {
            convertView = View.inflate(getContext(), R.layout.item_wifi_scan_result, null);
            viewHolder = new ScanResultHolder();
            viewHolder.iv_device = (ImageView) convertView.findViewById(R.id.iv_device);
            viewHolder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            viewHolder.tv_mac = (TextView) convertView.findViewById(R.id.tv_mac);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ScanResultHolder) convertView.getTag();
        }

        BlueBean blueBean = getDataList().get(position);
        if (blueBean != null) {
            viewHolder.tv_name.setText(blueBean.getName());
            viewHolder.tv_mac.setText(blueBean.getUid());
        }

        return convertView;
    }

    static class ScanResultHolder {
        ImageView iv_device;
        TextView tv_name;
        TextView tv_mac;
    }
}
