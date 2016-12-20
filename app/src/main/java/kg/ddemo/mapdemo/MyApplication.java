package kg.ddemo.mapdemo;///

import android.app.Application;
import android.content.Context;
import android.os.RemoteException;

import kg.ddemo.mapdemo.util.CrashHandlerUtil;

/**
* <p>ClassName:MyApplication类</p>
* <p>Description: 代表着整个应用程序</p>
* @author WangJinShan
* @version revision: 1.0 2016年3月2日下午11:27:49 
*/
public final class MyApplication extends Application{
	@Override
	public void onCreate() {
		super.onCreate();

		try {
			com.amap.api.maps.MapsInitializer.initialize(getApplicationContext());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		CrashHandlerUtil crashHandler = CrashHandlerUtil.getInstance();
		crashHandler.init(getApplicationContext());
		super.onCreate();
	}

	private static Context mAppContext;
	public MyApplication() {
		mAppContext = this;
	}

	public static Context getAppContext() {
		return mAppContext;
	}





	
}
