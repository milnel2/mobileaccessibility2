package edu.washington.cs.streetsignreader;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

/**
 * Extends View. Lets us draw on top of the camera preview surface holder.
 * Modularized to support other uses.
 * 
 * @author shurui
 */
public class DrawOnTop extends View {

	/**
	 * Constructor: Takes the context of the current application.
	 * 
	 * @param context
	 */
	public DrawOnTop(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/* Draws our preview lines. */
	@Override
	protected void onDraw(Canvas canvas) {
		setKeepScreenOn(true);
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(Color.MAGENTA);

		for (int i = 1; i < 11; i++) {
			// draws horizontal
			canvas.drawLine(0, this.getHeight() / 2 + i, this.getWidth() + i,
					this.getHeight() / 2 + i, paint);
			// draws vertical
			canvas.drawLine(this.getWidth() / 2 + i, 0,
					this.getWidth() / 2 + i, this.getHeight() + i, paint);
		}
		// WHAT A HACK <3
		// canvas.drawLine(this.getWidth()/2, 0, this.getHeight(),
		// this.getWidth()*32, paint);
		super.onDraw(canvas);
	}
}
