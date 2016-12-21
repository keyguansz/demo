package kg.ddemo.mapdemo.activity.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMapException;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import com.amap.api.maps.offlinemap.OfflineMapStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kg.ddemo.mapdemo.R;
import kg.ddemo.mapdemo.activity.OfflineAMapActivity;
import kg.ddemo.mapdemo.activity.OfflineMapItemAdapter;
import kg.ddemo.mapdemo.util.DJILogUtil;
import kg.ddemo.mapdemo.util.FormatUtil;

/**
 * @author :key.guan
 * @package :kg.ddemo.mapdemo.activity.adapter
 * @date : 2016/12/21
 * Description:
 * Copyright (c) 2016. DJI All Rights Reserved.
 */
public class OfflineMapAllCityAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "OfflineMapAllCityAdapter";
    private Context mC;
    private ArrayList<OfflineMapProvince> mGroupList = new ArrayList<>();
    private HashMap<Object, List<OfflineMapCity>> mChildMap = new HashMap<Object, List<OfflineMapCity>>();// 保存二级目录的市
    public OfflineMapAllCityAdapter(Context context, ArrayList<OfflineMapProvince> groupList,HashMap<Object, List<OfflineMapCity>> childMap ){
        mC = context;
        mGroupList.addAll(groupList);
        mChildMap.putAll(childMap);
    }
    /**
     *@desc 更新数据
     *@author : key.guan @ 2016/12/21 8:37
     */
    public void update(ArrayList<OfflineMapProvince> groupList,HashMap<Object, List<OfflineMapCity>> childMap ){
        mGroupList.clear();
        mChildMap.clear();
        if (null != groupList && !groupList.isEmpty()) {
            mGroupList.addAll(groupList);
        }
        if (null != childMap && !childMap.isEmpty()) {
            mChildMap.putAll(childMap);
        }
        notifyDataSetChanged();
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
        return mChildMap.get(groupPosition).size();
    }

    /**
     * 获取一级标签下二级标签的内容
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mChildMap.get(groupPosition).get(childPosition).getCity();
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
    public View getGroupView(final int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        GroupHolder holder;
        if (convertView == null) {
            holder = new GroupHolder();
            convertView = LinearLayout.inflate(mC, R.layout.offline_map_city_group, null);
            convertView.setTag(holder);
        } else {
            holder = (GroupHolder) convertView.getTag();
        }

			/*holder.mSwitchRoot =  convertView.findViewById(R.id.offline_map_group_switch_root);*/
        holder.mSwitchImg = (ImageView)convertView.findViewById(R.id.offlinemap_group_switch_img);
        holder.mTitleView = (TextView)convertView.findViewById(R.id.offlinemap_group_title);
        holder.mSizeView = (TextView)convertView.findViewById(R.id.offlinemap_group_size);
        holder.mDownView = (ImageView)convertView.findViewById(R.id.offlinemap_group_down);

        if (isExpanded) {
            holder.mSwitchImg.setImageDrawable(mC.getResources().getDrawable(
                    R.drawable.offlinemap_group_switch_on));
        } else {
            holder.mSwitchImg.setImageDrawable(mC.getResources().getDrawable(
                    R.drawable.offlinemap_group_switch_off));
        }

        holder.mTitleView.setText(mGroupList.get(groupPosition)
                .getProvinceName());
		/*	if( groupPosition >
					SpelicalProvinceNum){


			}*/
        holder.mSizeView.setText(FormatUtil.formatMB( mGroupList.get(groupPosition)
                .getSize()));
			/*holder.mSizeView.setText(FormatUtil.formatMB( mGroupList.get(groupPosition)
					.getSize()));*/


        holder.mDownView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {//下载当前的节点的所有数据
                int status = mGroupList.get(groupPosition).getState();
                DJILogUtil.E("mDownView,status="+status+"mGroupList = "+mGroupList.get(groupPosition).getProvinceName());
					/*if( status == OfflineMapStatus.LOADING
							|| status !=  OfflineMapStatus.UNZIP ){
						return;
					}*/
                AlertDialog.Builder builder = new AlertDialog.Builder(mC);
                builder.setMessage("确认下载吗？");
                builder.setTitle("提示");
                builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        DJILogUtil.I("mDownView");
                        try{
                            isStart = mOfflineMapManager
                                    .downloadByProvinceName(mGroupList.get(
                                            groupPosition).getProvinceName());
                        }
                        catch (AMapException e) {
                            e.printStackTrace();
                            Log.e("离线地图下载", "离线地图下载抛出异常" + e.getErrorMessage());
                        }

                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();

            }
        });
        int status = mGroupList.get(groupPosition).getState();
        DJILogUtil.E("getGroupView,status="+status+"groupPosition = "+groupPosition);
        switch (status) {
            case OfflineMapStatus.SUCCESS:
                holder.mDownView.setImageDrawable(mC.getResources().getDrawable(
                        R.drawable.offlinemap_group_down_complete));

                break;
            case OfflineMapStatus.LOADING:
					/*holder.mDownView.setImageDrawable(getResources().getDrawable(
							R.drawable.offlinemap_group_down_pause));*/
                OfflineAMapActivity.this.mDownProgress = status;
                break;
            case OfflineMapStatus.UNZIP:
                OfflineAMapActivity.this.mDownProgress = status;

                break;
            case OfflineMapStatus.WAITING:
                holder.mDownView.setImageDrawable(getResources().getDrawable(
                        R.drawable.offlinemap_group_down_pause));
                break;
            case OfflineMapStatus.PAUSE:
                holder.mDownView.setImageDrawable(getResources().getDrawable(
                        R.drawable.offlinemap_group_down_start));
                break;
            case OfflineMapStatus.STOP:
                holder.mDownView.setImageDrawable(getResources().getDrawable(
                        R.drawable.offlinemap_group_down_start));
                break;
            case OfflineMapStatus.ERROR:
                break;
            default:
                holder.mDownView.setImageDrawable(getResources().getDrawable(
                        R.drawable.offlinemap_group_down_start));
                break;
        }

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
                    getBaseContext(), R.layout.offline_map_child, null);
        }
        ChildHolder holder = new ChildHolder(convertView);
        holder.cityName.setText(mChildMap.get(groupPosition)
                .get(childPosition).getCity());
        holder.citySize.setText(FormatUtil.formatMB(mChildMap.get(groupPosition).get(
                childPosition).getSize()));

        if (mChildMap.get(groupPosition).get(childPosition).getState() == OfflineMapStatus.SUCCESS) {
            //holder.mDownTv.setText("安装完成");
            holder.mDownTv.setVisibility(View.GONE);
            holder.mDownImage.setVisibility(View.VISIBLE);
            holder.mDownImage.setImageDrawable(getResources().getDrawable(
                    R.drawable.offlinemap_group_down_complete));
        } else if (mChildMap.get(groupPosition).get(childPosition).getState() == OfflineMapStatus.PAUSE) {
            holder.mDownTv.setVisibility(View.VISIBLE);
            holder.mDownImage.setVisibility(View.GONE);
            holder.mDownTv.setText("已暂停");
        } else if (mChildMap.get(groupPosition).get(childPosition).getState() == OfflineMapStatus.WAITING) {
            holder.mDownTv.setVisibility(View.VISIBLE);
            holder.mDownImage.setVisibility(View.GONE);
            holder.mDownTv.setText("等待中");
        }else if (mChildMap.get(groupPosition).get(childPosition).getState() == OfflineMapStatus.LOADING) {
            if (groupPosition == OfflineAMapActivity.this.mProvincePosition
                    && childPosition == OfflineAMapActivity.this.mCityPosition) {
                holder.mDownTv.setVisibility(View.VISIBLE);
                holder.mDownImage.setVisibility(View.GONE);
                holder.mDownTv.setText("正在下载" + mDownProgress + "%");
            }else {
                //	holder.mDownTv.setText("下载");
                holder.mDownTv.setVisibility(View.GONE);
                holder.mDownImage.setVisibility(View.VISIBLE);
                holder.mDownImage.setImageDrawable(getResources().getDrawable(
                        R.drawable.offlinemap_group_down_start));
            }
        } else if (mChildMap.get(groupPosition).get(childPosition).getState() == OfflineMapStatus.UNZIP) {
            holder.mDownTv.setVisibility(View.VISIBLE);
            holder.mDownImage.setVisibility(View.GONE);
            holder.mDownTv.setText("正在解压" + mDownProgress + "%");
        }
        return convertView;
    }

    class ChildHolder {
        TextView cityName;
        TextView citySize;
        TextView mDownTv;
        ImageView mDownImage;//用于开始,完成
        public ChildHolder(View view) {
            cityName = (TextView) view.findViewById(R.id.city_name);
            citySize = (TextView) view.findViewById(R.id.city_size);
            mDownTv = (TextView) view.findViewById(R.id.offlin_emap_item_down_tv);
            mDownImage = (ImageView) view.findViewById(R.id.offline_map_item_down_img);
        }
    }
    class GroupHolder {
        View mSwitchRoot;
        ImageView mSwitchImg;
        TextView mTitleView;
        TextView mSizeView;
        ImageView mDownView;
    }

    /**
     * 当选择子节点的时候，调用该方法
     */
    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

}
