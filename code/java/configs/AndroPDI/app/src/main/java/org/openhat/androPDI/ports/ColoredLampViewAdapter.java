//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.androPDI.ports;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.rarepebble.colorpicker.ColorPickerView;

import org.openhat.androPDI.AndroPDI;
import org.openhat.androPDI.R;
import org.openhat.opdi.devices.DeviceException;
import org.openhat.opdi.ports.CustomPort;
import org.openhat.opdi.ports.Port;
import org.openhat.opdi.protocol.DisconnectedException;
import org.openhat.opdi.protocol.PortAccessDeniedException;
import org.openhat.opdi.protocol.ProtocolException;

import java.util.concurrent.TimeoutException;

/** A port view adapter for colored lamps.
 * 
 * @author Leo
 *
 */
public abstract class ColoredLampViewAdapter<T extends CustomPort> implements IPortViewAdapter {

	protected enum LampState {
		UNKNOWN,
		OFF,
		ON
	}

	protected final ShowDevicePorts showDevicePorts;
	final static int MENU_OUTER = 0;

	Context context;

	int menutype;
	private ImageView ivPortIcon;
	private Button btnColor;
	private ImageView ivStateIcon;
	private SeekBar sbSeek;
	private TextView tvBottomtext;
	boolean isSeekbarTracking;
	boolean ignoreNextSet;

	protected T port;

	// view state values
	protected boolean stateError;
	protected LampState state = LampState.UNKNOWN;
	protected int color = -1;
	protected int brightness = -1;

	public ColoredLampViewAdapter(T port, ShowDevicePorts showDevicePorts) {
		this.port = port;
		this.showDevicePorts = showDevicePorts;
	}

	public View getView(View convertView) {
		View view;
		if (convertView == null) {
			LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// inflate the layout
			view = vi.inflate(R.layout.coloredlamp_port_row, null);
		} else
			view = convertView;
		
        btnColor = view.findViewById(R.id.btn_color);
		sbSeek = view.findViewById(R.id.seekBar);
        tvBottomtext = (TextView) view.findViewById(R.id.bottomtext);
        ivPortIcon = (ImageView) view.findViewById(R.id.port_icon);
        ivStateIcon = (ImageView) view.findViewById(R.id.state_icon);

        btnColor.setText(port.getName());
        tvBottomtext.setText("");

        showDevicePorts.addPortAction(new PortAction(ColoredLampViewAdapter.this) {
			@Override
			void perform() throws TimeoutException, InterruptedException, DisconnectedException, DeviceException, ProtocolException {
				queryState();
				showDevicePorts.mHandler.post(new Runnable() {
					@Override
					public void run() {
						updateState();
					}
				});
			}
		});
        
        return view;
	}
	
	void openMenu(int menu) {
		this.menutype = menu;
		this.showDevicePorts.portAndAdapter = this.showDevicePorts.new PortAndAdapter(port, this);
		this.showDevicePorts.mHandler.post(() -> {
			ColoredLampViewAdapter.this.showDevicePorts.registerForContextMenu(ColoredLampViewAdapter.this.showDevicePorts.ports_listview);
			ColoredLampViewAdapter.this.showDevicePorts.openContextMenu(ColoredLampViewAdapter.this.showDevicePorts.ports_listview);
			ColoredLampViewAdapter.this.showDevicePorts.unregisterForContextMenu(ColoredLampViewAdapter.this.showDevicePorts.ports_listview);
		});
	}
	
	protected void queryState() throws TimeoutException, InterruptedException, DisconnectedException, DeviceException, ProtocolException{
		state = LampState.UNKNOWN;
		color = -1;
		stateError = port.hasError();
		if (stateError)
			return;

        // attempt to retrieve the values
        try {
            String value = port.getValue();
            parseValue(value);
		} catch (PortAccessDeniedException e) {
		}
        stateError = port.hasError();
	}

	/** Fill the state variables state and color from the port's value.
	 *
	 * @param value
	 */
	protected abstract void parseValue(String value);

	@Override
	public void startPerformAction() {
		// reset error state
		stateError = false;
	}
	
	@Override
	public void setError(Throwable t) {
		stateError = true;
	}
	
	@Override
	public void refresh() {
		showDevicePorts.addPortAction(new PortAction(this) {
			@Override
			void perform()
					throws TimeoutException,
					InterruptedException,
					DisconnectedException,
					DeviceException,
					ProtocolException {
				// reload the port state
				ColoredLampViewAdapter.this.port.refresh();
				queryState();
			}
		});
	}

	protected void pickColor() {
		final Dialog dialog = new Dialog(showDevicePorts);
		dialog.setContentView(R.layout.color_picker);
		dialog.setTitle(port.getName());

		final ColorPickerView picker = dialog.findViewById(R.id.picker);
		picker.setColor(color);
		picker.showAlpha(false);
		picker.showHex(false);
		dialog.findViewById(R.id.button_cancel).setOnClickListener(view -> dialog.dismiss());
		dialog.findViewById(R.id.button_ok).setOnClickListener(view -> {
			dialog.dismiss();
			setColor(picker.getColor());
		});

		dialog.show();
	}

	// must be called on the UI thread!
	public void updateState() {
		if (stateError) {
			ivPortIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.digital_port_error));
			ivPortIcon.setOnClickListener(null);
			ivStateIcon.setImageDrawable(context.getResources().getDrawable(R.drawable.led_yellow));
			ivStateIcon.setOnClickListener(null);
			tvBottomtext.setText(port.getErrorMessage());
			btnColor.setBackgroundColor(Color.TRANSPARENT);
			btnColor.setTextColor(Color.BLACK);
			btnColor.setEnabled(false);
			sbSeek.setEnabled(false);
			sbSeek.setOnSeekBarChangeListener(null);
    		return;
		}

		// set the proper icon
		Drawable portIcon = null;
		String bText = "";

		// default icon
		String iconName = port.getExtendedInfo("icon","");
		if (iconName != "") {
			// get icon identifier
			int iconID = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
			if (iconID == 0)
				throw new IllegalArgumentException("Drawable resource not found: " + iconName);							
				
			portIcon = context.getResources().getDrawable(iconID);
		}

		// use text from device if present
		bText = port.getExtendedState("text", bText);

		btnColor.setEnabled(true);
		if (color == -1) {
			btnColor.setBackgroundColor(Color.TRANSPARENT);
			btnColor.setTextColor(Color.BLACK);
		} else {
			btnColor.setBackgroundColor(color);
			// adjust text color based on background brightness
			// https://stackoverflow.com/questions/1855884/determine-font-color-based-on-background-color
			double luminance = (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
			if (luminance > 0.5)
				btnColor.setTextColor(Color.BLACK);
			else
				btnColor.setTextColor(Color.WHITE);
		}

		ivPortIcon.setImageDrawable(portIcon);
		tvBottomtext.setText(bText);
		
		Drawable stateIcon = null;
		if (!port.isReadonly()) {
        	switch (state) {
        	case UNKNOWN:
        		// TODO icon for UNKNOWN
        		stateIcon = context.getResources().getDrawable(R.drawable.switch_off);
        		btnColor.setOnClickListener(null);
        		break;
        	case OFF:
        		stateIcon = context.getResources().getDrawable(R.drawable.switch_off);
				ivStateIcon.setOnClickListener(v -> ColoredLampViewAdapter.this.showDevicePorts.addPortAction(new PortAction(ColoredLampViewAdapter.this) {
					@Override
					void perform() {
						// switch on
						try {
							switchOn();
						} catch (PortAccessDeniedException e) {
							state = LampState.UNKNOWN;
						}
					}
				}));
				btnColor.setOnClickListener(null);
				break;
        	case ON:
        		stateIcon = context.getResources().getDrawable(R.drawable.switch_on);
				ivStateIcon.setOnClickListener(v -> ColoredLampViewAdapter.this.showDevicePorts.addPortAction(new PortAction(ColoredLampViewAdapter.this) {
					@Override
					void perform(){
						// switch off
						try {
							switchOff();
						} catch (PortAccessDeniedException e) {
							state = LampState.UNKNOWN;
						}
					}
				}));
				btnColor.setOnClickListener(view -> {
					pickColor();
				});
        		break;
        	}
		}

		ivStateIcon.setImageDrawable(stateIcon);

		// context menu when clicking
		ivPortIcon.setOnClickListener(v -> handleClick());

		//sbSeek.setOnSeekBarChangeListener(null);
//		sbSeek.setMin(0);
		sbSeek.setMax(255);
		if (!isSeekbarTracking || ignoreNextSet) {
			Log.d("androPDI", "Setting brightness to: " + brightness);
			sbSeek.setProgress(brightness);
		}
		sbSeek.setEnabled(!port.isReadonly());

		sbSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			private void setValue(final int val) {
				showDevicePorts.addPortAction(new PortAction(ColoredLampViewAdapter.this) {
					@Override
					void perform() {
						try {
							setBrightness(val);
						} catch (PortAccessDeniedException e) {
						}
					}
				});
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				try {
					ignoreNextSet = true;
					isSeekbarTracking = false;
					setValue(seekBar.getProgress());
				} catch (Exception e) {
					showDevicePorts.showError(e.toString());
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				isSeekbarTracking = true;
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
										  boolean fromUser) {
				if (fromUser)
					return;
				if (ignoreNextSet) {
					ignoreNextSet = false;
					return;
				}
				try {
					ignoreNextSet = true;
					setValue(seekBar.getProgress());
				} catch (Exception e) {
					showDevicePorts.showError(e.toString());
				}
			}
		});
	}

	protected abstract void switchOn() throws PortAccessDeniedException;

	protected abstract void switchOff() throws PortAccessDeniedException;

	protected abstract void setBrightness(int brightness) throws PortAccessDeniedException;

	@Override
	public void configure(Port port, Context context) {
		this.port = (T)port;
		this.context = context;
	}
	
	@Override
	public void handleClick() {
		openMenu(MENU_OUTER);
	}

	@Override
	public void createContextMenu(ContextMenu menu,
			ContextMenuInfo menuInfo) {
		MenuInflater inflater = this.showDevicePorts.getMenuInflater();
		
		try {
			if (menutype == MENU_OUTER) {
				// inflate the outer menu 
				inflater.inflate(R.menu.coloredlamp_port_menu, menu);

				// selectively enable/disable menu items
				MenuItem item;

				item = menu.findItem(R.id.menuitem_port_reload);
				item.setOnMenuItemClickListener(item1 -> showDevicePorts.addPortAction(new PortAction(ColoredLampViewAdapter.this) {
					@Override
					void perform() throws TimeoutException, InterruptedException, DisconnectedException, DeviceException, ProtocolException {
						ColoredLampViewAdapter.this.port.refresh();
						queryState();
					}
				}));

				boolean canChange = !port.isReadonly();

				item = menu.findItem(R.id.menuitem_coloredlamp_toggle);
				item.setEnabled(canChange);
				item.setOnMenuItemClickListener(item12 -> {
					ColoredLampViewAdapter.this.toggle();
					return true;
				});

				item = menu.findItem(R.id.menuitem_coloredlamp_select_color);
				item.setEnabled(canChange && state == LampState.ON);
				item.setOnMenuItemClickListener(item12 -> {
					ColoredLampViewAdapter.this.pickColor();
					return true;
				});
			}
		} catch (Exception e) {
			this.showDevicePorts.showError("Can't show the context menu");
			menu.close();
		}
	}

	protected abstract void setColor(int newColor);

	protected abstract void toggle();

	@Override
	public void showMessage(String message) {
		showDevicePorts.receivedPortMessage(this.port, message);
	}
}
