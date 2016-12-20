package kg.ddemo.mapdemo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.amap.api.maps.AMapException;
import com.amap.api.maps.MapView;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.amap.api.maps.offlinemap.OfflineMapManager.OfflineMapDownloadListener;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import com.amap.api.maps.offlinemap.OfflineMapStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kg.ddemo.mapdemo.R;
import kg.ddemo.mapdemo.util.DJILogUtil;
import kg.ddemo.mapdemo.util.FormatUtil;


/**
 * AMapV2地图中简单介绍离线地图下载
 */
public class OfflineAMapActivity extends Activity implements
		OfflineMapDownloadListener {

	private TextView mDownTabBtn;
	private TextView mCityTabBtn;
	private View mDownTabRoot;
	private View mCityTabRoot;
	private ExpandableListView mTabDownProgressExLv;
	private ExpandableListView mTabDownCompleteExLv;

	private interface TabType{
		int down = 0;
		int city = 1;
	}
	private OfflineMapManager mOfflineMapManager = null;// 离线地图下载控制器
	private List<OfflineMapProvince> mProvinceList = new ArrayList<OfflineMapProvince>();// 保存一级目录的省直辖市
	private HashMap<Object, List<OfflineMapCity>> mCityMap = new HashMap<Object, List<OfflineMapCity>>();// 保存二级目录的市
	private int mProvincePosition;// 记录一级目录的position
	private int mCityPosition;// 记录二级目录的position
	private int mDownProgress;// 记录下载比例
	private boolean isStart = false;// 判断是否开始下载,true表示开始下载，false表示下载失败
	private boolean[] mIsProvinceOpen;// 记录一级目录是否打开 I
	private ExpandableListView mExListView;
	private final int SpelicalProvinceNum = 3;//特殊省

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.offline_map_activity);

		init();
		init0();
	}
	private OfflineAMapActivity getActivity(){
		return OfflineAMapActivity.this;
	}

	private void init0() {
		mTabDownProgressExLv = (ExpandableListView)findViewById(R.id.offlinemap_tab_down_progress);
		mTabDownCompleteExLv = (ExpandableListView)findViewById(R.id.offlinemap_tab_down_complete);

		//mTabDownProgressExLv
		//mTabDownProgressExLv.setGroupIndicator(null);
		OfflineMapDownProgressAdapter mOfflineMapDownProgressAdapter
				= new OfflineMapDownProgressAdapter(getActivity(), mOfflineMapManager.getDownloadingCityList());
		mTabDownProgressExLv.setAdapter(mOfflineMapDownProgressAdapter);
	}

	private MapView mapView;
	private void init() {
		// 此版本限制，使用离线地图，请初始化一个MapView
		mapView = new MapView(this);
		mOfflineMapManager = new OfflineMapManager(this, this);
		mExListView = (ExpandableListView) findViewById(R.id.list);
		mExListView.setGroupIndicator(null);////将控件默认的左边箭头去掉，

		initCityTabData();

		mIsProvinceOpen = new boolean[mProvinceList.size()];
		// 为列表绑定数据源
		mExListView.setAdapter(adapter);
		mExListView
				.setOnGroupCollapseListener(new OnGroupCollapseListener() {
					@Override
					public void onGroupCollapse(int groupPosition) {
						DJILogUtil.E("onGroupCollapse");
						mIsProvinceOpen[groupPosition] = false;
					}
				});

		mExListView
				.setOnGroupExpandListener(new OnGroupExpandListener() {

					@Override
					public void onGroupExpand(int groupPosition) {
						mIsProvinceOpen[groupPosition] = true;
						DJILogUtil.I("onGroupCollapse");
					}
				});
		// 设置二级item点击的监听器
		mExListView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v,
										int groupPosition, int childPosition, long id) {
				DJILogUtil.E("onChildClick,groupPosition="+groupPosition+",childPosition="+childPosition
						+"city="+mCityMap
						.get(groupPosition).get(childPosition)
						.getCity());
				try {
					// 下载全国概要图、直辖市、港澳离线地图数据
					if (groupPosition == 0 || groupPosition == 1
							|| groupPosition == 2) {
						isStart = mOfflineMapManager.downloadByProvinceName(mCityMap
								.get(groupPosition).get(childPosition)
								.getCity());
					}
					// 下载各省的离线地图数据
					else {

						// 下载各省列表中的城市离线地图数据

							isStart = mOfflineMapManager.downloadByCityName(mCityMap
									.get(groupPosition).get(childPosition)
									.getCity());

					}
				} catch (AMapException e) {
					e.printStackTrace();
					Log.e("离线地图下载", "离线地图下载抛出异常" + e.getErrorMessage());
				}
				DJILogUtil.E("onChildClick,isStart="+isStart);
				// 保存当前正在正在下载省份或者城市的position位置
				if (isStart) {
					OfflineAMapActivity.this.mProvincePosition = groupPosition;
					OfflineAMapActivity.this.mCityPosition = childPosition;
				}
				return false;
			}
		});
		initHeader();
	}

	private void initCityTabData() {
		mProvinceList = mOfflineMapManager.getOfflineMapProvinceList();
		List<OfflineMapProvince> bigCityList = new ArrayList<OfflineMapProvince>();// 以省格式保存直辖市、港澳、全国概要图
		List<OfflineMapCity> cityList = new ArrayList<OfflineMapCity>();// 以市格式保存直辖市、港澳、全国概要图
		List<OfflineMapCity> gangaoList = new ArrayList<OfflineMapCity>();// 保存港澳城市
		List<OfflineMapCity> gaiyaotuList = new ArrayList<OfflineMapCity>();// 保存概要图
		for (int i = 0; i < mProvinceList.size(); i++) {
			OfflineMapProvince province = mProvinceList.get(i);
			List<OfflineMapCity> city = new ArrayList<OfflineMapCity>();
			OfflineMapCity aMapCity = getCicy(province);
			if (province.getCityList().size() != 1) {
				//city.add(aMapCity);//省也称为市的第一个标志;不在写入列表了
				city.addAll(province.getCityList());
			} else {//直辖市和香港和澳门，概要
				province.setSize(10000);
				cityList.add(aMapCity);
				bigCityList.add(province);
			}
			mCityMap.put(i + 3, city);
		}
		OfflineMapProvince title = new OfflineMapProvince();

		title.setProvinceName("概要图");
		mProvinceList.add(0, title);
		title = new OfflineMapProvince();
		title.setProvinceName("直辖市");
		mProvinceList.add(1, title);
		title = new OfflineMapProvince();
		title.setProvinceName("港澳");
		mProvinceList.add(2, title);
		mProvinceList.removeAll(bigCityList);

		for (OfflineMapProvince bigCity : bigCityList) {
			if (bigCity.getProvinceName().contains("全国概要图")) {
				gaiyaotuList.add(getCicy(bigCity));
				mProvinceList.get(0).setSize(bigCity.getSize());
			}else if (bigCity.getProvinceName().contains("北京")
					||bigCity.getProvinceName().contains("天津")
					||bigCity.getProvinceName().contains("上海")
					||bigCity.getProvinceName().contains("重庆")) {
			/*	gaiyaotuList.add(getCicy(bigCity));*/
				mProvinceList.get(1).setSize( mProvinceList.get(1).getSize() + bigCity.getCityList().get(0).getSize());
			}else if (bigCity.getProvinceName().contains("香港")
					|| bigCity.getProvinceName().contains("澳门")) {
				gangaoList.add(getCicy(bigCity));
				mProvinceList.get(2).setSize( mProvinceList.get(2).getSize() + bigCity.getSize());
			}
		}
		try {
			cityList.remove(4);// 从List集合体中删除香港
			cityList.remove(4);// 从List集合体中删除澳门
			cityList.remove(4);// 从List集合体中删除全国概要图
		} catch (Throwable e) {
			e.printStackTrace();
		}
	/*	cityList.removeAll(gangaoList);
		cityList.removeAll(gaiyaotuList);*/
		mCityMap.put(0, gaiyaotuList);// 在HashMap中第0位置添加全国概要图
		mCityMap.put(1, cityList);// 在HashMap中第1位置添加直辖市
		mCityMap.put(2, gangaoList);// 在HashMap中第2位置添加港澳
	}

	private void initHeader() {
		mDownTabBtn = (TextView)findViewById(R.id.offlinemap_tab_down_btn);
		mDownTabBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				swtichTab(TabType.down);
			}
		});
		mCityTabBtn = (TextView)findViewById(R.id.offlinemap_tab_city_btn);
		mCityTabBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				swtichTab(TabType.city);
			}
		});
		mDownTabRoot = findViewById(R.id.offlinemap_tab_down);
		mCityTabRoot = findViewById(R.id.offlinemap_tab_city);

	}

	private void swtichTab(int tabId) {
		if( tabId == TabType.down ){
			mDownTabBtn.setBackgroundResource(R.drawable.left_while_borde_rounded_focused);
			mDownTabBtn.setTextColor(getResources().getColor(R.color.main_green));
			mCityTabBtn.setBackgroundResource(R.drawable.left_while_borde_rounded);
			mCityTabBtn.setTextColor(getResources().getColor(R.color.offlinemap_gray_9b));
			mDownTabRoot.setVisibility(View.VISIBLE);
			mCityTabRoot.setVisibility(View.GONE);
			updateDownTabView();
		}else if( tabId == TabType.city ){
			mCityTabBtn.setBackgroundResource(R.drawable.left_while_borde_rounded_focused);
			mCityTabBtn.setTextColor(getResources().getColor(R.color.main_green));
			mDownTabBtn.setBackgroundResource(R.drawable.left_while_borde_rounded);
			mDownTabBtn.setTextColor(getResources().getColor(R.color.offlinemap_gray_9b));
			mDownTabRoot.setVisibility(View.GONE);
			mCityTabRoot.setVisibility(View.VISIBLE);
		//	updateCityTabView();
		}
	}

	private void updateDownTabView() {

	}

	/**
	 * 把一个省的对象转化为一个市的对象
	 */
	public OfflineMapCity getCicy(OfflineMapProvince aMapProvince) {
		OfflineMapCity aMapCity = new OfflineMapCity();
		aMapCity.setCity(aMapProvince.getProvinceName());
		aMapCity.setSize(aMapProvince.getSize());
		aMapCity.setCompleteCode(aMapProvince.getcompleteCode());
		aMapCity.setState(aMapProvince.getState());
		aMapCity.setUrl(aMapProvince.getUrl());
		return aMapCity;
	}

	final ExpandableListAdapter adapter = new BaseExpandableListAdapter() {

		@Override
		public int getGroupCount() {
			return mProvinceList.size();
		}

		/**
		 * 获取一级标签内容
		 */
		@Override
		public Object getGroup(int groupPosition) {
			return mProvinceList.get(groupPosition).getProvinceName();
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
			return mCityMap.get(groupPosition).size();
		}

		/**
		 * 获取一级标签下二级标签的内容
		 */
		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return mCityMap.get(groupPosition).get(childPosition).getCity();
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
				convertView = LinearLayout.inflate(
						getBaseContext(), R.layout.offline_map_city_group, null);
				convertView.setTag(holder);
			} else {
				holder = (GroupHolder) convertView.getTag();
			}

			/*holder.mSwitchRoot =  convertView.findViewById(R.id.offline_map_group_switch_root);*/
			holder.mSwitchImg = (ImageView)convertView.findViewById(R.id.offlinemap_group_switch_img);
			holder.mTitleView = (TextView)convertView.findViewById(R.id.offlinemap_group_title);
			holder.mSizeView = (TextView)convertView.findViewById(R.id.offlinemap_group_size);
			holder.mDownView = (ImageView)convertView.findViewById(R.id.offlinemap_group_down);

			//没有必要监听这个
			/*holder.mSwitchRoot.setClickable(true);
			holder.mSwitchRoot.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					DJILogUtil.E("mSwitchRoot");
					if ( mIsProvinceOpen[groupPosition] ){
						onGroupCollapsed(groupPosition);
					}else {
						onGroupExpanded(groupPosition);
						*//*holder.mSwitchImg.setImageDrawable(getResources().getDrawable(
								R.drawable.downarrow));*//*
					}
					mIsProvinceOpen[groupPosition] = !mIsProvinceOpen[groupPosition];
				}
			});*/
			if (mIsProvinceOpen[groupPosition]) {
				holder.mSwitchImg.setImageDrawable(getResources().getDrawable(
						R.drawable.offlinemap_group_switch_on));
			} else {
				holder.mSwitchImg.setImageDrawable(getResources().getDrawable(
						R.drawable.offlinemap_group_switch_off));
			}

			holder.mTitleView.setText(mProvinceList.get(groupPosition)
					.getProvinceName());
		/*	if( groupPosition >
					SpelicalProvinceNum){


			}*/
			holder.mSizeView.setText(FormatUtil.formatMB( mProvinceList.get(groupPosition)
					.getSize()));
			/*holder.mSizeView.setText(FormatUtil.formatMB( mProvinceList.get(groupPosition)
					.getSize()));*/


			holder.mDownView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {//下载当前的节点的所有数据
					int status = mProvinceList.get(groupPosition).getState();
					DJILogUtil.E("mDownView,status="+status+"mProvinceList = "+mProvinceList.get(groupPosition).getProvinceName());
					/*if( status == OfflineMapStatus.LOADING
							|| status !=  OfflineMapStatus.UNZIP ){
						return;
					}*/
					AlertDialog.Builder builder = new AlertDialog.Builder(OfflineAMapActivity.this);
					builder.setMessage("确认下载吗？");
					builder.setTitle("提示");
					 builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
					 @Override
					 public void onClick(DialogInterface dialog, int which) {
							   dialog.dismiss();
						 DJILogUtil.I("mDownView");
						 try{
							 isStart = mOfflineMapManager
									 .downloadByProvinceName(mProvinceList.get(
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
			int status = mProvinceList.get(groupPosition).getState();
			DJILogUtil.E("getGroupView,status="+status+"groupPosition = "+groupPosition);
			switch (status) {
				case OfflineMapStatus.SUCCESS:
					holder.mDownView.setImageDrawable(getResources().getDrawable(
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
			holder.cityName.setText(mCityMap.get(groupPosition)
					.get(childPosition).getCity());
			holder.citySize.setText(FormatUtil.formatMB(mCityMap.get(groupPosition).get(
					childPosition).getSize()));

			if (mCityMap.get(groupPosition).get(childPosition).getState() == OfflineMapStatus.SUCCESS) {
				//holder.mDownTv.setText("安装完成");
				holder.mDownTv.setVisibility(View.GONE);
				holder.mDownImage.setVisibility(View.VISIBLE);
				holder.mDownImage.setImageDrawable(getResources().getDrawable(
						R.drawable.offlinemap_group_down_complete));
			} else if (mCityMap.get(groupPosition).get(childPosition).getState() == OfflineMapStatus.PAUSE) {
				holder.mDownTv.setVisibility(View.VISIBLE);
				holder.mDownImage.setVisibility(View.GONE);
				holder.mDownTv.setText("已暂停");
			} else if (mCityMap.get(groupPosition).get(childPosition).getState() == OfflineMapStatus.WAITING) {
				holder.mDownTv.setVisibility(View.VISIBLE);
				holder.mDownImage.setVisibility(View.GONE);
				holder.mDownTv.setText("等待中");
			}else if (mCityMap.get(groupPosition).get(childPosition).getState() == OfflineMapStatus.LOADING) {
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
			} else if (mCityMap.get(groupPosition).get(childPosition).getState() == OfflineMapStatus.UNZIP) {
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

	};

	/**
	 * 离线地图下载回调方法
	 */
	@Override
	public void onDownload(int status, int completeCode, String downName) {
		DJILogUtil.E("onDownload,status="+status+",completeCode="+completeCode+",downName="+downName);
		switch (status) {
			case OfflineMapStatus.SUCCESS:
				changeOfflineMapTitle(OfflineMapStatus.SUCCESS);
				break;
			case OfflineMapStatus.LOADING:
				OfflineAMapActivity.this.mDownProgress = completeCode;
				break;
			case OfflineMapStatus.UNZIP:
				OfflineAMapActivity.this.mDownProgress = completeCode;
				changeOfflineMapTitle(OfflineMapStatus.UNZIP);
				break;
			case OfflineMapStatus.WAITING:
				break;
			case OfflineMapStatus.PAUSE:
				break;
			case OfflineMapStatus.STOP:
				break;
			case OfflineMapStatus.ERROR:
				break;
			default:
				break;
		}
		((BaseExpandableListAdapter) adapter).notifyDataSetChanged();
	}

	/**
	 * 更改离线地图下载状态文字
	 */
	private void changeOfflineMapTitle(int status) {
		if (mProvincePosition == 0 || mProvincePosition == 1 || mProvincePosition == 2) {
			mCityMap.get(mProvincePosition).get(mCityPosition).setState(status);
		} else {
			if (mCityPosition == 0) {
				for (int i = 0; i < mCityMap.get(mProvincePosition).size(); i++) {
					mCityMap.get(mProvincePosition).get(i).setState(status);//
				}
			} else {
				mCityMap.get(mProvincePosition).get(mCityPosition).setState(status);
			}
		}
	}

	/**
	 * 获取map 缓存和读取目录
	 */
	private String getSdCacheDir(Context context) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			java.io.File fExternalStorageDirectory = Environment
					.getExternalStorageDirectory();
			java.io.File autonaviDir = new java.io.File(
					fExternalStorageDirectory, "amapsdk");
			boolean result = false;
			if (!autonaviDir.exists()) {
				result = autonaviDir.mkdir();
			}
			java.io.File minimapDir = new java.io.File(autonaviDir,
					"offlineMap");
			if (!minimapDir.exists()) {
				result = minimapDir.mkdir();
			}
			return minimapDir.toString() + "/";
		} else {
			return "";
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mapView != null) {
			mapView.onDestroy();
		}
	}
}
