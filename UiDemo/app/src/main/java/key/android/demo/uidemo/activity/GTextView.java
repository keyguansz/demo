package key.android.demo.uidemo.activity;



import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.TextView;

public class GTextView extends TextView {

    private boolean isPad = false;

    public GTextView(Context context)
    {
        this(context, null);
    }
  

    public GTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);


    }

    public GTextView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

    }





    private LinearGradient mLinearGradient;
    private Paint mPaint;
    private int mViewWidth = 0;
    private Rect mTextBound = new Rect();
 
    @Override
    protected void onDraw(Canvas canvas) {
        mViewWidth = getMeasuredWidth();
        mPaint = getPaint();
        String mTipText = getText().toString();
        mPaint.getTextBounds(mTipText, 0, mTipText.length(), mTextBound);
        mLinearGradient = new LinearGradient(0, 0, mViewWidth, 0,
                new int[] {  0xFF429321, 0xFFB4EC51 },
                null, Shader.TileMode.REPEAT);
        mPaint.setShader(mLinearGradient);
        canvas.drawText(mTipText, getMeasuredWidth() / 2 - mTextBound.width() / 2, getMeasuredHeight() / 2 +   mTextBound.height()/2, mPaint);
    }

    @Override
    public boolean isFocused()
    {
        return true;
    }

}

