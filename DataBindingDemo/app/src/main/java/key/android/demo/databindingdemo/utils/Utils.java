package key.android.demo.databindingdemo.utils;

import android.databinding.BindingAdapter;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import key.android.demo.databindingdemo.model.FontFamily;


/**
 *@desc   
 *@ref:
 *@author : key.guan @ 2017/2/4 22:21
 */
public class Utils {

    // 使用注解，无需手动调用此函数
    @BindingAdapter({"imageUrl", "error"})
    public static void loadImage(ImageView view, String url, Drawable error) {
        Glide.with(view.getContext()).load(url).error(error).into(view);
    }

    @BindingAdapter({"font"})
    public static void setFont(TextView textView, String fontName) {
        textView.setTypeface(FontFamily.getInstance().getFont(fontName, textView.getContext()));
    }
}
