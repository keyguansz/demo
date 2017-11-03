package tiny.args.prof.K.mapdemo;

import android.app.Application;
import android.os.RemoteException;
import android.util.Log;

import com.google.gson.Gson;

import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.collector.CrashReportData;
import org.acra.sender.EmailIntentSender;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.xutils.HttpManager;

import mapdemo.util.CrashHandlerUtil;

/**
* <p>ClassName:MyApplication类</p>
* <p>Description: 代表着整个应用程序</p>
* @author WangJinShan
* @version revision: 1.0 2016年3月2日下午11:27:49 
*/
@ReportsCrashes(formKey = "" 
			 	,mailTo = "yuanchuangke@foxmail.com" 
				,customReportContent = {
					ReportField.APP_VERSION_NAME, ReportField.APP_VERSION_CODE, ReportField.ANDROID_VERSION,
					ReportField.PHONE_MODEL, ReportField.CUSTOM_DATA, ReportField.STACK_TRACE, ReportField.LOGCAT }
				,mode = ReportingInteractionMode.SILENT, forceCloseDialogAfterToast = false)


public final class MyApplication extends Application{
	//unupdate:move to baseActivity?
	//globl var, shard by all the activity,
	private HttpManager _HttpManage;
	private Gson _gson;
	private final String _CHARSET = "GB2312";
	
	@Override
	public void onCreate() {
		super.onCreate();
		//初始化xUtils工具 初始化后才能使用
		// x.Ext.init(this);
		// x.Ext.setDebug(true); // 是否输出debug日志,如何设置超时时间呢


		// 百度地图初始化
	//	SDKInitializer.initialize(getApplicationContext());
		//SDKInitializer.initialize(getApplicationContext());
		try {
			com.amap.api.maps.MapsInitializer.initialize(getApplicationContext());
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		// 异常捕获方法1：ACRA配置
		//ACRA.init(this);
		//ErrorReporter.getInstance().setReportSender(new CrashReportSender());
		// 设置Thread Exception Handler
		// 异常捕获方法2：
		CrashHandlerUtil crashHandler = CrashHandlerUtil.getInstance();
		crashHandler.init(getApplicationContext());
		super.onCreate();
	}
	private class CrashReportSender implements ReportSender{
		private static final String _TAG = "CrashReportSender";
		@Override
		public void send(CrashReportData arg0) throws ReportSenderException {
			// TODO Auto-generated method stub
			EmailIntentSender emailSender = new EmailIntentSender(getApplicationContext());  
	        emailSender.send(arg0); 
	        Log.e(_TAG,arg0.toString());
	        // send crash report to host  
	      //  HttpPostSender httpSender = new HttpPostSender(formUri, null);  
	     //   httpSender.send(arg0);  
	        //send crash report to google  
	      //  GoogleFormSender googleSender =new GoogleFormSender();  
	       // googleSender.send(arg0); 	        
			
		}		
	}
	public Gson getGson() {
		return _gson;
	}

	
}
