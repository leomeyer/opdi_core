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
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import org.joda.time.LocalDateTime;
import org.openhat.androPDI.R;
import org.openhat.androPDI.ports.editors.DialPortDateTimeEditor;
import org.openhat.opdi.devices.DeviceException;
import org.openhat.opdi.ports.DialPort;
import org.openhat.opdi.ports.Port;
import org.openhat.opdi.protocol.DisconnectedException;
import org.openhat.opdi.protocol.PortAccessDeniedException;
import org.openhat.opdi.protocol.ProtocolException;
import org.openhat.opdi.units.DisplayHint;

import java.util.concurrent.TimeoutException;

/** A port view adapter for dial ports.
 * 
 * @author Leo
 *
 */
class DialPortViewAdapter implements IPortViewAdapter {
	
	protected final ShowDevicePorts showDevicePorts;

	Context context;

	private TextView tvToptext;
	private TextView tvBottomtext;
	private TextView tvMax;
	private TextView tvMin;
	private TextView tvCur;
	private SeekBar sbSeek;
	private ImageView ivPortIcon;
	
	DialPort dPort;
	
	// cache values
	boolean positionValid;
	long position;
	long minValue;
	long maxValue;
	long step;
    boolean stateError;
    boolean inaccurate;
    
	protected DialPortViewAdapter(ShowDevicePorts showDevicePorts) {
		super();
		this.showDevicePorts = showDevicePorts;
	}

	public View getView(View convertView) {
		View cachedView;
		if (convertView == null) {
			LayoutInflater vi = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			// get layout property from UnitFormat definition
			String layoutName = dPort.getUnitFormat().getProperty("layout", "dial_port_row");
			// get layout identifier
			int layoutID = context.getResources().getIdentifier(layoutName, "layout", context.getPackageName());
			// inflate the identified layout
			cachedView = vi.inflate(layoutID, null);
		} else
			cachedView = convertView;
				
        tvToptext = (TextView) cachedView.findViewById(R.id.toptext);
        tvBottomtext = (TextView) cachedView.findViewById(R.id.bottomtext);
        tvMax = (TextView) cachedView.findViewById(R.id.dial_max_value);
        tvMin = (TextView) cachedView.findViewById(R.id.dial_min_value);
        tvCur = (TextView) cachedView.findViewById(R.id.dial_current_value);
        sbSeek = (SeekBar) cachedView.findViewById(R.id.seekBar);
        ivPortIcon = (ImageView) cachedView.findViewById(R.id.port_icon);

        if (tvToptext != null) tvToptext.setText(dPort.getName());
        if (tvBottomtext != null) tvBottomtext.setText(dPort.getType() + " " + dPort.getDirCaps());
        
        showDevicePorts.addPortAction(new PortAction(DialPortViewAdapter.this) {
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

        return cachedView;
	}
	
	public void openMenu() {
		this.showDevicePorts.portAndAdapter = this.showDevicePorts.new PortAndAdapter(dPort, this);
		this.showDevicePorts.mHandler.post(new Runnable() {
			@Override
			public void run() {
			    DialPortViewAdapter.this.showDevicePorts.registerForContextMenu(DialPortViewAdapter.this.showDevicePorts.ports_listview);
			    DialPortViewAdapter.this.showDevicePorts.openContextMenu(DialPortViewAdapter.this.showDevicePorts.ports_listview);
			    DialPortViewAdapter.this.showDevicePorts.unregisterForContextMenu(DialPortViewAdapter.this.showDevicePorts.ports_listview);
			}
		});
	}
	
	@Override
	public void handleClick() {
		openMenu();
	}

	protected void queryState() throws TimeoutException, InterruptedException, DisconnectedException, DeviceException, ProtocolException {
		position = dPort.getMinimum();
		positionValid = false;
		stateError = dPort.hasError();
		if (stateError)
			return;

		try {
			position = dPort.getPosition();
			positionValid = true;
		} catch (PortAccessDeniedException e) {
			positionValid = false;
		}
        maxValue = dPort.getMaximum();
        minValue = dPort.getMinimum();
        step = dPort.getStep();
        stateError = dPort.hasError();
        inaccurate = dPort.getExtendedState("inaccurate", "false").equals("true");
    }
	
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
	public void configure(Port port, Context context) {
		this.dPort = (DialPort)port;
		this.context = context;
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
				dPort.refresh();
				queryState();
			}
		});
	}

	// must be called on the UI thread!
	public void updateState() {
		if (stateError) {
			// indicate an error
	        Drawable drawable = context.getResources().getDrawable(R.drawable.dial_port_error);
			if (ivPortIcon != null) ivPortIcon.setImageDrawable(drawable);
			if (ivPortIcon != null) ivPortIcon.setOnClickListener(null);		// no context menu

			if (tvBottomtext != null) tvBottomtext.setText(dPort.getErrorMessage());
			if (tvMax != null) tvMax.setText("");
			if (tvMin != null) tvMin.setText("");
			if (tvCur != null) tvCur.setText("");
			if (sbSeek != null) sbSeek.setEnabled(false);
			if (sbSeek != null) sbSeek.setOnSeekBarChangeListener(null);
			return;
		}
		
		// set the proper icon
		if (ivPortIcon != null) {
			// default icon
			Drawable drawable = context.getResources().getDrawable(R.drawable.dial_port);
			String iconName = dPort.getUnitFormat().getProperty("icon", dPort.getExtendedInfo("icon", ""));
			if (!iconName.equals("")) {
				// get icon identifier
				int iconID = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
				if (iconID == 0)
					throw new IllegalArgumentException("Drawable resource not found: " + iconName);							
					
				drawable = context.getResources().getDrawable(iconID);
			}
			
			ivPortIcon.setImageDrawable(drawable);
			
			// context menu when clicking
			ivPortIcon.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					handleClick();
				}
			});
		}
		
			
		if (tvBottomtext != null) tvBottomtext.setText("");
		if (tvMax != null) tvMax.setText("" + maxValue);
		if (tvMin != null) tvMin.setText("" + minValue);
		if (tvCur != null) {
			tvCur.setText(dPort.getUnitFormat().format(position));
			DisplayHint dh = dPort.getUnitFormat().getDisplayHint(position);
			if (dh.activityState == DisplayHint.ActivityState.ACTIVE)
				tvCur.setTextColor(Color.BLACK);
			else
				tvCur.setTextColor(Color.LTGRAY);
			if ("posNeg".equals(dPort.getColorScheme())) {
				if (position < 0)
					tvCur.setTextColor(Color.RED);
				else
					tvCur.setTextColor(Color.rgb(0, 200, 0));
			}
			if (inaccurate) {
				int col = tvCur.getCurrentTextColor();
				tvCur.setTextColor(Color.argb(127, Color.red(col), Color.green(col), Color.blue(col)));
			}
		}

		if (sbSeek != null) sbSeek.setOnSeekBarChangeListener(null);
		if (sbSeek != null) sbSeek.setMax((int)maxValue - (int)minValue);
		if (sbSeek != null) sbSeek.setProgress((int)position - (int)minValue);
		if (sbSeek != null) sbSeek.setEnabled(!dPort.isReadonly());

		if (sbSeek != null) sbSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			boolean ignoreNextSet;
			
			private void setValue(final int val) {
				DialPortViewAdapter.this.showDevicePorts.addPortAction(new PortAction(DialPortViewAdapter.this) {
					@Override
					void perform() throws TimeoutException, InterruptedException, DisconnectedException, DeviceException, ProtocolException {
						try {
							dPort.setPosition((int)val + (int)dPort.getMinimum());
						} catch (PortAccessDeniedException e) {
						}
						queryState();
					}
				});
			}
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				try {
					ignoreNextSet = true;
					setValue(seekBar.getProgress());
				} catch (Exception e) {
					DialPortViewAdapter.this.showDevicePorts.showError(e.toString());
				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (fromUser) return;
				if (ignoreNextSet) {
					ignoreNextSet = false;
					return;
				}
				try {
					ignoreNextSet = true;
					setValue(seekBar.getProgress());
				} catch (Exception e) {
					DialPortViewAdapter.this.showDevicePorts.showError(e.toString());
				}
			}
		});
	}		
	
	@Override
	public void createContextMenu(ContextMenu menu,
			ContextMenuInfo menuInfo) {

		MenuInflater inflater = this.showDevicePorts.getMenuInflater();
		
		try {
			// inflate the context menu 
			inflater.inflate(R.menu.dial_port_menu, menu);
			
			// selectively enable/disable menu items
			MenuItem item;

			item = menu.findItem(R.id.menuitem_port_reload);
			item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					return showDevicePorts.addPortAction(new PortAction(DialPortViewAdapter.this) {
						@Override
						void perform() throws TimeoutException, InterruptedException, DisconnectedException, DeviceException, ProtocolException {
							dPort.refresh();
							queryState();
						}
					});
				}
			});
			
			final long portMinValue = dPort.getMinimum();
			final long portMaxValue = dPort.getMaximum();
			
			item = menu.findItem(R.id.menuitem_dial_set_value);
			
			if (dPort.isReadonly())
				item.setVisible(false);
			else
				item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						// should use editor?
						if (dPort.getUnitFormat().hasEditor()) {
							String editor = dPort.getUnitFormat().getProperty("editor", null);
							
							if ("DateTimeEditor".equals(editor)) {
								// build and show the DateTimeEditor
						        FragmentManager fm = showDevicePorts.getFragmentManager();
						        DialPortDateTimeEditor dialog;
								try {
									dialog = new DialPortDateTimeEditor().init(dPort.getUnitFormat().convertToLocalDate(dPort.getPosition()),
										// callback when new value has been confirmed
										new DialPortDateTimeEditor.DismissedListener() {
											@Override
											public void dismissed(LocalDateTime date) {
												final long val = dPort.getUnitFormat().convertFromLocalDate(date);
												if ((val >= portMinValue) && (val <= portMaxValue))
													// set value
													DialPortViewAdapter.this.showDevicePorts.addPortAction(new PortAction(DialPortViewAdapter.this) {
														@Override
														void perform()
																throws TimeoutException,
																InterruptedException,
																DisconnectedException,
																DeviceException,
																ProtocolException {
															// set the port value
															// this corrects to the nearest step
															try {
																dPort.setPosition(val);
															} catch (PortAccessDeniedException e) {
															}
															queryState();
														}							
													});												
											}
									});
							        dialog.show(fm, "fragment_edit_name");
							        return true;
								} catch (Exception e) {
									e.printStackTrace();
									return false;
								}
							} else
								throw new RuntimeException("Unknown editor specified by unit " + dPort.getUnit() + " of port " + dPort.getID() + ": " + editor);
							
						} else {
							// open standard value input dialog
		
							final Dialog dl = new Dialog(showDevicePorts);
							dl.setTitle("Dial port value");
							dl.setContentView(R.layout.dialog_analog_value);
							
							final EditText value = (EditText) dl.findViewById(R.id.edittext_value);
							try {
								value.setText("" + dPort.getPosition());
							} catch (Exception e1) {
								// can't read the value
								return false;
							}
		
							Button bOk = (Button) dl.findViewById(R.id.button_ok);
							bOk.setOnClickListener(new View.OnClickListener() {
								public void onClick(View v) {
									// close dialog
									dl.dismiss();
									
									// parse value
									final long val;
									try {
										val = Long.parseLong(value.getText().toString());
									} catch (Exception e) {
										// no number entered
										return;
									}
									if ((val >= portMinValue) && (val <= portMaxValue))
										// set value
										DialPortViewAdapter.this.showDevicePorts.addPortAction(new PortAction(DialPortViewAdapter.this) {
											@Override
											void perform()
													throws TimeoutException,
													InterruptedException,
													DisconnectedException,
													DeviceException,
													ProtocolException {
												// set the port value
												// this corrects to the nearest step
												try {
													dPort.setPosition(val);
												} catch (PortAccessDeniedException e) {
												}
												queryState();
											}							
										});
								}
							});
							Button bCancel = (Button) dl.findViewById(R.id.button_cancel);
							bCancel.setOnClickListener(new View.OnClickListener() {
								public void onClick(View v) {
									dl.dismiss();
								}
							});
						
							dl.show();				
							return true;
						}
				}
			});
		} catch (Exception e) {
			this.showDevicePorts.showError("Can't show the context menu");
			menu.close();
		}
	}

	@Override
	public void showMessage(String message) {
		showDevicePorts.receivedPortMessage(this.dPort, message);
		
	}

}