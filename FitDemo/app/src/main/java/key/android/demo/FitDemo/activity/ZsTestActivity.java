package key.android.demo.FitDemo.activity;

import android.app.Activity;
import android.content.ContentResolver;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;

import key.android.demo.FitDemo.R;

/**
 *@desc
 *@ref:zs600b layout：large>land>..
 *@author : key.guan @ 2017/4/18 18:02
 */
public class ZsTestActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zs_test);
        findViewById(R.id.test_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable d = ((ImageView)findViewById(R.id.test_img)).getDrawable();
                // normal:1920*1080:9.5M
                // x:540*960:9.5M
                //xx:360*640:2.5M
                //  Log.e("ZsTestActivity","w="+d.getH)
                int i = 0;
            }
        });
        final ContentResolver cr = getContentResolver();
        String strGo3 = Settings.Global.getString(cr, "dji_fps_go4_enable");
        String strGo4 = Settings.Global.getString(cr, "dji_fps_go4_enable");

    }
}
