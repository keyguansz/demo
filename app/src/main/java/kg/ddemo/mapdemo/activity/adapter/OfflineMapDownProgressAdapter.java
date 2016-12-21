package kg.ddemo.mapdemo.activity.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapStatus;

import java.util.ArrayList;

import kg.ddemo.mapdemo.R;
import kg.ddemo.mapdemo.util.DJILogUtil;
import kg.ddemo.mapdemo.util.FormatUtil;

/**
 *@desc   正在下载的列表
 *@ref:
 *@author : key.guan @ 2016/12/20 21:06
 */
public class OfflineMapDownProgressAdapter extends BaseExpandableListAdapter {
    private static final String TAG = "OfflineMapDownProgressAdapter";
    private Context mC;
    private ArrayList<OfflineMapCity> mChildList = new ArrayList<>();//保存二级目录的市或者直辖市

   // private HashMap<Object, List<OfflineMapCity>> cityMap = new HashMap<Object, List<OfflineMapCity>>();//
    public OfflineMapDownProgressAdapter(Context context, ArrayList<OfflineMapCity> childList ){
        mC = context;
        mChildList.addAll(childList);

    }

    public void updateView(){
        long size = 0;
        for(OfflineMapCity it : mChildList ){
            size = size + it.getSize();
        }
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
        DJILogUtil.E(TAG+"getGroupView,isExpanded"+isExpanded);

        TextView group_text;
        ImageView group_image;
        if (convertView == null) {
            convertView = RelativeLayout.inflate(mC
                    , R.layout.offline_map_down_progress_group, null);
        }
        group_text = (TextView) convertView.findViewById(R.id.offlinemap_group_title);
        group_image = (ImageView) convertView
                .findViewById(R.id.offlinemap_group_switch_img);
        group_text.setText( mChildList.size()+"个地区" );
        long size = 0;
        for( OfflineMapCity it : mChildList ){
            size = size + it.getSize();
        }
        ((TextView) convertView.findViewById(R.id.offlinemap_group_size)).setText(FormatUtil.formatMB(size));
        if( isExpanded ){
            group_image.setImageDrawable( mC.getResources().getDrawable(
                    R.drawable.offlinemap_group_switch_on));
        }else{
            group_image.setImageDrawable( mC.getResources().getDrawable(
                    R.drawable.offlinemap_group_switch_off));
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
        GroupHolder holder;
        if (convertView == null) {
            convertView = LinearLayout.inflate(
                    mC, R.layout.offline_map_down_progress_child, null);
            holder = new GroupHolder();
            convertView.setTag(holder);
        } else {
            holder = (GroupHolder) convertView.getTag();
        }
        holder.mTitleView = (TextView) convertView.findViewById(R.id.offline_map_item_title);
        holder.mSizeView = (TextView) convertView.findViewById(R.id.offline_map_item_size);
        holder.mBarView = (ProgressBar) convertView.findViewById(R.id.offline_map_item_bar);
        holder.mBarDescView = (TextView) convertView.findViewById(R.id.offline_map_item_bar_desc);
        holder.mStateDescView= (TextView) convertView.findViewById(R.id.offline_map_item_state_desc);
        holder.mStateImg = (ImageView) convertView.findViewById(R.id.offline_map_item_state_img);

        holder.mTitleView.setText(mChildList.get(childPosition).getCity());
        holder.mSizeView.setText(FormatUtil.formatMB(mChildList.get(
                childPosition).getSize()));

        int state = mChildList.get(childPosition).getState();
        int completeCode  = mChildList.get(childPosition).getcompleteCode();
      /*  public static final int CHECKUPDATES = 6;
        public static final int ERROR = -1;
        public static final int STOP = 5;
        public static final int LOADING = 0;
        public static final int UNZIP = 1;
        public static final int WAITING = 2;
        public static final int PAUSE = 3;
        public static final int SUCCESS = 4;*/
     /*   等待中，（继续下载，取消下载，可暂停）
        下载中，（暂停下载，取消下载，取消）
        已暂停，（继续下载，取消下载）*/
        holder.mBarDescView.setText(""+completeCode+"%");
        holder.mBarView.setProgress(completeCode);
        holder.mStateDescView.setTextColor(mC.getResources().getColor(R.color.offlinemap_gray_4a));
        holder.mStateImg.setBackgroundResource(R.drawable.offlinemap_group_down_start);
        if ( state == OfflineMapStatus.WAITING) {//有进度，等待中，暂停按钮
            holder.mStateDescView.setText("等待中");
        }  else if ( mChildList.get(childPosition).getState() == OfflineMapStatus.LOADING) {
            holder.mStateDescView.setText("下载中");
        }else if ( state == OfflineMapStatus.PAUSE ||  state == OfflineMapStatus.ERROR) {
            holder.mStateDescView.setTextColor(mC.getResources().getColor(R.color.offlinemap_red_ee));
            holder.mStateDescView.setText("已暂停");
        } else if ( state == OfflineMapStatus.UNZIP) {
           // holder.mDownTv.setText("正在解压" + completeCode + "%");
            holder.mStateDescView.setText("解压中");
        }/*else if ( state == OfflineMapStatus.SUCCESS) {
            // holder.mDownTv.setText("正在解压" + completeCode + "%");
            holder.mStateDescView.setText("已完成");
        }*/
        return convertView;
    }
    class GroupHolder {
        TextView mTitleView;
        TextView mSizeView;
        ProgressBar mBarView;
        TextView mBarDescView;
        TextView mStateDescView;
        ImageView mStateImg;
        public GroupHolder() {

        }
        public GroupHolder(View view) {
            mTitleView = (TextView) view.findViewById(R.id.offline_map_item_title);
            mSizeView = (TextView) view.findViewById(R.id.offline_map_item_size);
            mBarView = (ProgressBar) view.findViewById(R.id.offline_map_item_bar);
            mBarDescView = (TextView) view.findViewById(R.id.offline_map_item_bar_desc);
            mStateDescView= (TextView) view.findViewById(R.id.offline_map_item_state_desc);
            mStateImg = (ImageView) view.findViewById(R.id.offline_map_item_state_img);
        }
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
