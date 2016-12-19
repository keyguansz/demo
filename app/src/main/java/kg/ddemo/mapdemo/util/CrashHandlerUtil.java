package tiny.args.prof.dji.mapdemo.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import tiny.args.prof.dji.mapdemo.R;


/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,有该类来接管程序,并记录发送错误报告. * 
 * @author gj
 */
@Deprecated
public class CrashHandlerUtil implements UncaughtExceptionHandler {

	private static final String TAG = "CrashHandlerUtil";
	// 系统默认的UncaughtException处理类
	private Thread.UncaughtExceptionHandler mDefaultHandler;
	// CrashHandler实例
	private static volatile CrashHandlerUtil instance = null;
	// 程序的Context对象
	private Context _context;
	private final StringBuffer _sbDev = new StringBuffer(400);//需要同步，避免频繁扩容损失性能
	/** 保证只有一个CrashHandler实例 */
	private CrashHandlerUtil() {	}
	/** 单例模式(不用双检查，节省性能) */
	public static CrashHandlerUtil getInstance() {
		if (instance == null) {
			instance = new CrashHandlerUtil();
		}
		return instance;
	}

	/**
	 * 初始化
	 * 
	 * @param context
	 */
	public void init(Context context) {
		_context = context;
		// 获取系统默认的UncaughtException处理器
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		// 设置该CrashHandler为程序的默认处理器
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	/**
	 * 当UncaughtException发生时会转入该函数来处理
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(ex) && mDefaultHandler != null) {
			// 如果用户没有处理则让系统默认的异常处理器来处理
			mDefaultHandler.uncaughtException(thread, ex);
		} else {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				Log.e(TAG, "error : ", e);
			}
			// 退出程序
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(1);
		}
	}

	/**
	 * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
	 * 
	 * @param ex
	 * @return true:如果处理了该异常信息;否则返回false.
	 */
	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}
		if(_sbDev.length() == 0 ){//第一次发生crush，收集设备参数信息并且保存
			Map<String, String> infos = collect(_context);
			for (Map.Entry<String, String> entry : infos.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				_sbDev.append(key + "=" + value + "\n");
			}
		}
		save(ex);
		return true;
	}

	/**
	 * 收集设备参数信息	 *
	 * @param ctx
	 */
	private Map<String, String> collect(Context ctx) {
		// 用来存储设备信息和异常信息
		Map<String, String> infos = new HashMap<String, String>(40);//
		try {
			PackageManager pm = ctx.getPackageManager();
			PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
					PackageManager.GET_ACTIVITIES);
			if (pi != null) {
				infos.put("versionName", String.valueOf(pi.versionName));
				infos.put("versionCode", pi.versionCode + "");
			}
		} catch (NameNotFoundException e) {
			Log.e(TAG, "an error occured when collect package info", e);
		}
		Field[] fields = Build.class.getDeclaredFields();
		Field field;
		for (int i=0; i < fields.length; i++) {//
			field = fields[i];
			try {
				field.setAccessible(true);
				infos.put(field.getName(), String.valueOf(field.get(null)));
				Log.d(TAG, field.getName() + " : " + field.get(null));
			} catch (Exception e) {
				Log.e(TAG, "an error occured when collect crash info", e);
			}
		}
		return infos;
	}

	/**
	 * 保存错误信息到文件中 
	 * 
	 * @param ex
	 * @return 返回文件名称,便于将文件传送到服务器，只能用文本编辑器，如记事本，不能用手机浏览器（是按照网页解析的方式来工作的）打开！！！
	 */
	private void save(Throwable ex) {
		StringBuilder sb = new StringBuilder(100);//局部变量，不需要同步，用StringBuilder，避免频繁扩容损失性能
		String s = new SimpleDateFormat(
				"------------------[HH:mm:ss]-----------------\n")
				.format(System.currentTimeMillis());
		sb.append(s);
		sb.append(_sbDev);
		sb.append("\n");

		Writer writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		ex.printStackTrace(printWriter);
		Throwable cause = ex.getCause();
		while (cause != null) {
			cause.printStackTrace(printWriter);
			cause = cause.getCause();
		}
		printWriter.close();
		String result = writer.toString();
		sb.append(result);
		sb.append("---------------------------------------------\n");
		StringBuilder subject = new StringBuilder(30);
		subject.append(_context.getString(R.string.app_name));
		subject.append(new SimpleDateFormat("报错[YYYY-MM-DD HH:mm:ss]")
				.format(System.currentTimeMillis()));
		/*MailUtil.sendTo(subject.toString(), sb.toString());//发送到开发者邮件*/
	}



}
