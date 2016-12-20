package kg.ddemo.mapdemo.activity;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapStatus;

import java.util.ArrayList;

import kg.ddemo.mapdemo.R;
import kg.ddemo.mapdemo.util.FormatUtil;

/**
 *@desc   正在下载的列表
 *@ref:
 *@author : key.guan @ 2016/12/20 21:06
 */
public class OfflineMapDownProgressAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "OfflineMapItemAdapter";
    private Context mC;
    private ArrayList<OfflineMapCity> mChildList = new ArrayList<>();//保存二级目录的市或者直辖市
   // private HashMap<Object, List<OfflineMapCity>> cityMap = new HashMap<Object, List<OfflineMapCity>>();//
    public OfflineMapDownProgressAdapter(Context context, ArrayList<OfflineMapCity> childList ){
        mC = context;
        mChildList.addAll(childList);
        
    }
    @Override
    public int getGroupCount() {
        return 1;
    }

    /**
     * 获取一级标签内容
     */
    @Override
    public Object getGroup(int groupPosition) {
        return mChildList.size()+"个地区";
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
        return mChildList.size();
    }

    /**
     * 获取一级标签下二级标签的内容
     */
    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mChildList.get(childPosition).getCity();
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
            convertView = (RelativeLayout) RelativeLayout.inflate(mC
                    , R.layout.offline_map_down_progress_group, null);
        }
        group_text = (TextView) convertView.findViewById(R.id.offlinemap_group_title);
        group_image = (ImageView) convertView
                .findViewById(R.id.offlinemap_group_switch_img);
        group_text.setText( mChildList.size()+"个地区" );
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
                    mC, R.layout.offline_map_down_progress_child, null);
        }
        ViewHolder holder = new ViewHolder(convertView);
        holder.cityName.setText(mChildList.get(childPosition).getCity());
        holder.citySize.setText(FormatUtil.formatMB(mChildList.get(
                childPosition).getSize()));

        if ( mChildList.get(childPosition).getState() == OfflineMapStatus.SUCCESS) {
            holder.cityDown.setText("下载完成");
        } else if ( mChildList.get(childPosition).getState() == OfflineMapStatus.LOADING) {
            /*if (groupPosition == OfflineAMapActivity.this.groupPosition
                    && childPosition == OfflineAMapActivity.this.childPosition) {
                holder.mDownTv.setText("正在下载" + completeCode + "%");
            }*/
        } else if ( mChildList.get(childPosition).getState() == OfflineMapStatus.UNZIP) {
           // holder.mDownTv.setText("正在解压" + completeCode + "%");
        } else if ( mChildList.get(childPosition).getState() == OfflineMapStatus.LOADING) {
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
