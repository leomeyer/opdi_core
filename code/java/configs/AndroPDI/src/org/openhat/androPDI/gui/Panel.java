//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.androPDI.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class Panel extends SurfaceView implements SurfaceHolder.Callback {
	
	/** Drawing callback
	 * 
	 * @author Leo
	 *
	 */
	public interface RunOnDraw {
		
		/** Draw on the canvas */
		public void drawOn(Canvas canvas);
	}

	private RunOnDraw runOnDraw;
	
    public Panel(Context context, AttributeSet attrs) {
        super(context, attrs);
	    getHolder().addCallback(this);
	    setFocusable(true);
	}
    
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		
	}


	public RunOnDraw getRunOnDraw() {
		return runOnDraw;
	}

	public void setRunOnDraw(RunOnDraw runOnDraw) {
		this.runOnDraw = runOnDraw;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (runOnDraw != null)
			runOnDraw.drawOn(canvas);
	}

}
