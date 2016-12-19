package com.yck.cc;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

public class CircleProgressBar extends ImageButton {

	private int maxProgress = 100;
	private int progress = 0;// 默认进度
	private int progressStrokeWidth = 12;
	// 画圆所在的距形区域
	RectF oval;
	Paint paint;

	public CircleProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO 自动生成的构造函数存根
		oval = new RectF();
		paint = new Paint();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO 自动生成的方法存根
		super.onDraw(canvas);
		int width = this.getWidth();
		int height = this.getHeight();

		if (width != height) {
			int min = Math.min(width, height);
			width = min;
			height = min;
		}

		paint.setAntiAlias(true); // 设置画笔为抗锯齿
		//canvas.drawColor(Color.TRANSPARENT); // 白色背景
		paint.setStrokeWidth(progressStrokeWidth); // 线宽
		paint.setStyle(Style.STROKE);//当前只绘制图形的轮廓，而Paint.Style.FILL表示填充图形

		oval.left = progressStrokeWidth / 2; // 左上角x
		oval.top = progressStrokeWidth / 2; // 左上角y
		oval.right = width - progressStrokeWidth / 2; // 左下角x
		oval.bottom = height - progressStrokeWidth / 2; // 右下角y
		//oval :指定圆弧的外轮廓矩形区域。startAngle: 圆弧起始角度，单位为度,
		//sweepAngle: 圆弧扫过的角度，顺时针方向paint: 绘制圆弧的画板属性，如颜色，是否填充等
		//canvas.drawArc(oval, -90, 360, false, paint); // 绘制白色圆圈，即进度条背景
		paint.setColor(Color.rgb(0xf6, 0xb3, 0x7f));// 设置画笔颜色
		canvas.drawArc(oval, -90, ((float) progress / maxProgress) * 360,
				false, paint); // 绘制进度圆弧

		//绘制文本
		/*paint.setStrokeWidth(1);
		String text = progress + "%";
		text = "";
		int textHeight = height / 4;
		paint.setTextSize(textHeight);
		int textWidth = (int) paint.measureText(text, 0, text.length());
		paint.setStyle(Style.FILL);
		canvas.drawText(text, width / 2 - textWidth / 2, height / 2
				+ textHeight / 2, paint);*/

	}

	public int getMaxProgress() {
		return maxProgress;
	}

	public void setMaxProgress(int maxProgress) {
		this.maxProgress = maxProgress;
	}

	public void setProgress(int progress) {
		this.progress = progress;
		this.invalidate();
	}

	/**
	 * 非ＵＩ线程调用
	 */
	public void setProgressNotInUiThread(int progress) {
		this.progress = progress;
		this.postInvalidate();
	}

}
