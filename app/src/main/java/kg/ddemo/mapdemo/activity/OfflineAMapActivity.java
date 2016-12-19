package kg.ddemo.mapdemo.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMapException;
import com.amap.api.maps.MapView;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.amap.api.maps.offlinemap.OfflineMapManager.OfflineMapDownloadListener;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.yck.cc.CustomDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kg.ddemo.mapdemo.R;


/**
 *@desc
 *@author : key.guan @ 2016/12/19 16:00
 */
public class OfflineAMapActivity extends Activity implements
		OfflineMapDownloadListener {
	private OfflineMapManager amapManager = null;// 离线地图下载控制器
	private List<OfflineMapProvince> provinceList = new ArrayList<OfflineMapProvince>();// 保存一级目录的省直辖市
	private HashMap<Object, List<OfflineMapCity>> cityMap = new HashMap<Object, List<OfflineMapCity>>();// 保存二级目录的市
	private int groupPosition;// 记录一级目录的position
	private int childPosition;// 记录二级目录的position
	private int completeCode;// 记录下载比例
	private boolean isStart = false;// 判断是否开始下载,true表示开始下载，false表示下载失败
	private boolean[] isOpen;// 记录一级目录是否打开

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.a_offline_map);
		init();
	}

	private MapView mapView;

	private void initView(){
		// 打开软件，是不需要出现软键盘的，因此需要隐藏掉
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		_iv_offline_map_back = (ImageView)findViewById(R.id.iv_offline_map_back);
		_b_city_list = (Button)findViewById(R.id.b_city_list);
		_b_local_map = (Button)findViewById(R.id.b_local_map);
		_b_offline_map_search = (Button)findViewById(R.id.b_offline_map_search);
		_b_map_start = (Button)findViewById(R.id.b_map_start);
		_b_map_stop = (Button)findViewById(R.id.b_map_stop);
		_b_map_del = (Button)findViewById(R.id.b_map_del);
		_ll_city_list = (LinearLayout)findViewById(R.id.ll_city_list);
		_ll_local_map = (LinearLayout)findViewById(R.id.ll_local_map);
		_et_map_city = (EditText)findViewById(R.id.et_map_city);
		_tv_map_state = (TextView)findViewById(R.id.tv_map_state);

		ListView hotCityList = (ListView) findViewById(R.id.lv_hot_city);
		ArrayList<String> hotCities = new ArrayList<String>();
		// 获取热门城市列表
		final ArrayList<MKOLSearchRecord> records1 = mOffline.();
		if (records1 != null) {
			for (MKOLSearchRecord r : records1) {
				hotCities.add(r.cityName + "(" + r.cityID + ")" + "   --"
						+ this.formatDataSize(r.size));
			}
		}
		ListAdapter hAdapter = (ListAdapter) new ArrayAdapter<String>(this,
				R.layout.offline_map_city_item, hotCities);
		hotCityList.setAdapter(hAdapter);
		hotCityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
									long id) {
				// TODO Auto-generated method stub
				cityId = records1.get(position).cityID;
				cityName = records1.get(position).cityName;
				clickOfflineMap();
			}
		});
		ListView allCityList = (ListView) findViewById(R.id.lv_all_city);
		// 获取所有支持离线地图的城市
		ArrayList<String> allCities = new ArrayList<String>();
		final ArrayList<MKOLSearchRecord> records2 = mOffline.getOfflineCityList();
		if (records1 != null) {
			for (MKOLSearchRecord r : records2) {
				allCities.add(r.cityName + "(" + r.cityID + ")" + "   --"
						+ this.formatDataSize(r.size));
			}
		}
		ListAdapter aAdapter = (ListAdapter) new ArrayAdapter<String>(this,
				R.layout.offline_map_city_item, allCities);
		allCityList.setAdapter(aAdapter);
		allCityList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
									long id) {
				// TODO Auto-generated method stub
				cityId = records2.get(position).cityID;
				cityName = records2.get(position).cityName;
				clickOfflineMap();
			}
		});
		_ll_local_map.setVisibility(View.GONE);
		_ll_city_list.setVisibility(View.VISIBLE);

		// 获取已下过的离线地图信息
		localMapList = mOffline.getAllUpdateInfo();
		if (localMapList == null) {
			localMapList = new ArrayList<MKOLUpdateElement>();
		}

		ListView localMapListView = (ListView) findViewById(R.id.lv_local_map);
		lAdapter = new LocalMapAdapter();
		localMapListView.setAdapter(lAdapter);


		_iv_offline_map_back.setOnClickListener(this);
		_b_city_list.setOnClickListener(this);
		_b_local_map.setOnClickListener(this);
		_b_offline_map_search.setOnClickListener(this);
		_b_map_start.setOnClickListener(this);
		_b_map_stop.setOnClickListener(this);
		_b_map_del.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		int n_id = v.getId();
		if(R.id.iv_offline_map_back == n_id){
			finish();
		}else if(R.id.b_local_map == n_id){
			_b_local_map.setBackgroundResource(R.drawable.left_while_borde_rounded_focused);
			_b_local_map.setTextColor(getResources().getColor(R.color.theme_color));
			_b_city_list.setBackgroundResource(R.drawable.right_while_borde_rounded);
			_b_city_list.setTextColor(getResources().getColor(R.color.white));
			_ll_city_list.setVisibility(View.GONE);//显示下载管理
			_ll_local_map.setVisibility(View.VISIBLE);
		}else if(R.id.b_city_list == n_id){
			_b_local_map.setBackgroundResource(R.drawable.left_while_borde_rounded);
			_b_local_map.setTextColor(getResources().getColor(R.color.white));
			_b_city_list.setBackgroundResource(R.drawable.right_while_borde_rounded_focused);
			_b_city_list.setTextColor(getResources().getColor(R.color.theme_color));
			_ll_city_list.setVisibility(View.VISIBLE);//显示城市列表
			_ll_local_map.setVisibility(View.GONE);
		}else if(R.id.b_offline_map_search == n_id){
			ArrayList<MKOLSearchRecord> records = mOffline.searchCity(_et_map_city
					.getText().toString());
			if (records == null || records.size() != 1) {
				Toast.makeText(OfflineMapActivity.this,"抱歉，未搜索到结果", Toast.LENGTH_LONG)
						.show();
				return;
			}
			cityId = records.get(0).cityID;//搜索成功，获取城市ID
			cityName = records.get(0).cityName;
			clickOfflineMap();
		}else if(R.id.b_map_start == n_id){
			mOffline.start(cityId);
			Toast.makeText(OfflineMapActivity.this, "开始下载离线地图. cityid: " + cityId, Toast.LENGTH_SHORT)
					.show();
			updateView();
		}else if(R.id.b_map_stop == n_id){
			mOffline.pause(cityId);
			Toast.makeText(OfflineMapActivity.this, "暂停下载离线地图. cityid: " + cityId, Toast.LENGTH_SHORT)
					.show();
			updateView();
		}else if(R.id.b_map_del == n_id){
			mOffline.remove(cityId);
			Toast.makeText(OfflineMapActivity.this, "删除离线地图. cityid: " + cityId, Toast.LENGTH_SHORT)
					.show();
			updateView();
		}
	}
	private void clickOfflineMap(){
		CustomDialog.Builder customBuilder = new
				CustomDialog.Builder(OfflineMapActivity.this);
		customBuilder.setMessage("现在开始下载 " + cityName + " 的离线地图吗？")
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				})
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
						_ll_city_list.setVisibility(View.GONE);//显示下载管理
						_ll_local_map.setVisibility(View.VISIBLE);
						mOffline.start(cityId);
						Toast.makeText(OfflineMapActivity.this, "开始下载离线地图. cityid: " + cityId, Toast.LENGTH_SHORT)
								.show();
						updateView();
					}
				});
		dialog = customBuilder.create();
		dialog.show();
	}
	@Override
	public void onGetOfflineMapState(int type, int state) {
		switch (type) {
			case MKOfflineMap.TYPE_DOWNLOAD_UPDATE: {
				MKOLUpdateElement update = mOffline.getUpdateInfo(state);
				// 处理下载进度更新提示
				if (update != null) {
					_tv_map_state.setText(String.format("%s : %d%%", update.cityName,
							update.ratio));
					updateView();
				}
			}
			break;
			case MKOfflineMap.TYPE_NEW_OFFLINE:
				// 有新离线地图安装
				Log.d("OfflineDemo", String.format("add offlinemap num:%d", state));
				break;
			case MKOfflineMap.TYPE_VER_UPDATE:
				// 版本更新提示
				// MKOLUpdateElement e = mOffline.getUpdateInfo(state);

				break;
			default:
				break;
		}
	}

	public void updateView() {
		localMapList = mOffline.getAllUpdateInfo();
		if (localMapList == null) {
			localMapList = new ArrayList<MKOLUpdateElement>();
		}
		lAdapter.notifyDataSetChanged();
	}
	/**
	 * 离线地图管理列表适配器
	 */
	public class LocalMapAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return localMapList.size();
		}

		@Override
		public Object getItem(int index) {
			return localMapList.get(index);
		}

		@Override
		public long getItemId(int index) {
			return index;
		}

		@Override
		public View getView(int index, View view, ViewGroup arg2) {
			MKOLUpdateElement e = (MKOLUpdateElement) getItem(index);
			view = View.inflate(OfflineMapActivity.this,
					R.layout.offline_localmap_list_item, null);
			initViewItem(view, e);
			return view;
		}

		void initViewItem(View view, final MKOLUpdateElement e) {
			Button display = (Button) view.findViewById(R.id.b_offline_display);
			Button remove = (Button) view.findViewById(R.id.b_offline_remove);
			TextView title = (TextView) view.findViewById(R.id.tv_offline_title);
			TextView update = (TextView) view.findViewById(R.id.tv_offline_update);
			TextView ratio = (TextView) view.findViewById(R.id.tv_offline_ratio);
			ratio.setText(e.ratio + "%");
			title.setText(e.cityName);
			if (e.update) {
				update.setText("可更新");
			} else {
				update.setText("最新");
			}
			if (e.ratio != 100) {
				display.setEnabled(false);
			} else {
				display.setEnabled(true);
			}
			remove.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View arg0) {
					mOffline.remove(e.cityID);
					updateView();
				}
			});
			display.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.putExtra("x", e.geoPt.longitude);
					intent.putExtra("y", e.geoPt.latitude);
					intent.setClass(OfflineMapActivity.this, OfflineMapDisplayActivity.class);
					startActivity(intent);
				}
			});
		}

	}
	@Override
	protected void onPause() {
		MKOLUpdateElement temp = mOffline.getUpdateInfo(cityId);
		if (temp != null && temp.status == MKOLUpdateElement.DOWNLOADING) {
			mOffline.pause(cityId);
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	public String formatDataSize(int size) {
		String ret = "";
		if (size < (1024 * 1024)) {
			ret = String.format("%dK", size / 1024);
		} else {
			ret = String.format("%.1fM", size / (1024 * 1024.0));
		}
		return ret;
	}

	@Override
	protected void onDestroy() {
		/**
		 * 退出时，销毁离线地图模块
		 */
		mOffline.destroy();
		super.onDestroy();
	}
}
