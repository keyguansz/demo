package key.android.demo.FitDemo.activity;

import android.app.Activity;
import android.os.Bundle;

import key.android.demo.FitDemo.R;

/**
 *@desc
 * FIT_XY：对原图宽高进行放缩，该放缩不保持原比例来填充满ImageView。
MATRIX：不改变原图大小从ImageView的左上角开始绘制，超过ImageView部分不再显示。
CENTER：对原图居中显示，超过ImageView部分不再显示。
CENTER_CROP：对原图居中显示后进行等比放缩处理，使原图最小边等于ImageView的相应边。
CENTER_INSIDE：若原图宽高小于ImageView宽高，这原图不做处理居中显示，否则按比例放缩原图宽(高)是之等于ImageView的宽(高)。
FIT_START：对原图按比例放缩使之等于ImageView的宽高，若原图高大于宽则左对齐否则上对其。
FIT_CENTER：对原图按比例放缩使之等于ImageView的宽高使之居中显示。
FIT_END：对原图按比例放缩使之等于ImageView的宽高，若原图高大于宽则右对齐否则下对其。
 TIP:CENTER_INSIDE最好，不失真，
 *@ref:http://www.jb51.net/article/85983.htm
 *
 *@author : key.guan @ 2017/3/4 16:04
 */
public class ScaleTypeActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scale_type);
        //ImageView
        String str=null;
        if(str == null){
            int ii = 0;
        }
    }
}
