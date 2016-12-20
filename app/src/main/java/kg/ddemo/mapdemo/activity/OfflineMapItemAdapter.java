package kg.ddemo.mapdemo.activity;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import com.amap.api.maps.offlinemap.OfflineMapStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kg.ddemo.mapdemo.R;

/**
 * @author :key.guan
 * @package :kg.ddemo.mapdemo.activity
 * @date : 2016/12/19
 * Description:
 * Copyright (c) 2016. DJI All Rights Reserved.
 */
public class OfflineMapItemAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "OfflineMapItemAdapter";
    private Context mC;
    private ArrayList<OfflineMapProvince> mGroupList = new ArrayList<>();
    private HashMap<Object, List<OfflineMapCity>> cityMap = new HashMap<Object, List<OfflineMapCity>>();// 保存二级目录的市
    public OfflineMapItemAdapter(Context context, ArrayList<OfflineMapProvince> mGroupList ){
        mC = context;
        mGroupList.addAll(mGroupList);
        
    }
    @Override
    public int getGroupCount() {
        return mGroupList.size();
    }

    /**
     * 获取一级标签内容
     */
    @Override
    public Object getGroup(int groupPosition) {
        return mGroupList.get(groupPosition).getProvinceName();
    }

    /**
     * 获取一级标签的ID
     */
    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    /**
     * 获取一级标签下二级标签的总数
     */
    @Override
    public int getChildrenCount(int groupPosition) {
        return cityMap.get(groupPosition).size();
    }

    /**
     * 获取一级标签下二级标签的内容
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return cityMap.get(groupPosition).get(childPosition).getCity();
    }

    /**
     * 获取二级标签的ID
     */
    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    /**
     * 指定位置相应的组视图
     */
    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * 对一级标签进行设置
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        TextView group_text;
        ImageView group_image;
        if (convertView == null) {
            convertView = (RelativeLayout) RelativeLayout.inflate(
                    mC, R.layout.offlinemap_group, null);
        }
        group_text = (TextView) convertView.findViewById(R.id.group_text);
        group_image = (ImageView) convertView
                .findViewById(R.id.group_image);
        group_text.setText(mGroupList.get(groupPosition)
                .getProvinceName());
      /*  if (isOpen[groupPosition]) {
            group_image.setImageDrawable(mC.getResources().getDrawable(
                    R.drawable.downarrow));
        } else {
            group_image.setImageDrawable(mC.getResources().getDrawable(
                    R.drawable.rightarrow));
        }*/
        return convertView;
    }

    /**
     * 对一级标签下的二级标签进行设置
     */
    @Override
    public View getChildView(final int groupPosition,
                             final int childPosition, boolean isLastChild, View convertView,
                             ViewGroup parent) {
        if (convertView == null) {
            convertView = (RelativeLayout) RelativeLayout.inflate(
                    mC, R.layout.offlinemap_child, null);
        }
        ViewHolder holder = new ViewHolder(convertView);
        holder.cityName.setText(cityMap.get(groupPosition)
                .get(childPosition).getCity());
        holder.citySize.setText((cityMap.get(groupPosition).get(
                childPosition).getSize())
                / (1024 * 1024f) + "MB");

        if (cityMap.get(groupPosition).get(childPosition).getState() == OfflineMapStatus.SUCCESS) {
            holder.cityDown.setText("下载完成");
        } else if (cityMap.get(groupPosition).get(childPosition).getState() == OfflineMapStatus.LOADING) {
            /*if (groupPosition == OfflineAMapActivity.this.groupPosition
                    && childPosition == OfflineAMapActivity.this.childPosition) {
                holder.mDownTv.setText("正在下载" + completeCode + "%");
            }*/
        } else if (cityMap.get(groupPosition).get(childPosition).getState() == OfflineMapStatus.UNZIP) {
           // holder.mDownTv.setText("正在解压" + completeCode + "%");
        } else if (cityMap.get(groupPosition).get(childPosition).getState() == OfflineMapStatus.LOADING) {
            holder.cityDown.setText("下载");
        }
        return convertView;
    }

    class ViewHolder {
        TextView cityName;
        TextView citySize;
        TextView cityDown;

        public ViewHolder(View view) {
            cityName = (TextView) view.findViewById(R.id.city_name);
            citySize = (TextView) view.findViewById(R.id.city_size);
            cityDown = (TextView) view.findViewById(R.id.city_down);
        }
    }

    /**
     * 当选择子节点的时候，调用该方法
     */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
