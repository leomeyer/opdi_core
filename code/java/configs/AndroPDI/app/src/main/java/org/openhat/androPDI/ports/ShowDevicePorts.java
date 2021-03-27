//    This file is part of an OPDI reference implementation.
//    see: Open Protocol for Device Interaction
//
//    Copyright (C) 2011-2016 Leo Meyer (leo@leomeyer.de)
//    All rights reserved.

/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.openhat.androPDI.ports;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

import org.openhat.androPDI.AndroPDI;
import org.openhat.androPDI.AndroPDIDevice;
import org.openhat.androPDI.DeviceManager;
import org.openhat.androPDI.R;
import org.openhat.androPDI.gui.LoggingActivity;
import org.openhat.androPDI.portdetails.ShowPortDetails;
import org.openhat.androPDI.units.Units;
import org.openhat.opdi.devices.DeviceException;
import org.openhat.opdi.interfaces.IBasicProtocol;
import org.openhat.opdi.interfaces.IDevice;
import org.openhat.opdi.interfaces.IDeviceCapabilities;
import org.openhat.opdi.interfaces.IDeviceListener;
import org.openhat.opdi.interfaces.IProtocol;
import org.openhat.opdi.ports.AnalogPort;
import org.openhat.opdi.ports.CustomPort;
import org.openhat.opdi.ports.DialPort;
import org.openhat.opdi.ports.DigitalPort;
import org.openhat.opdi.ports.Port;
import org.openhat.opdi.ports.Port.PortType;
import org.openhat.opdi.ports.PortGroup;
import org.openhat.opdi.ports.SelectPort;
import org.openhat.opdi.ports.StreamingPort;
import org.openhat.opdi.protocol.DisconnectedException;
import org.openhat.opdi.protocol.PortAccessDeniedException;
import org.openhat.opdi.protocol.ProtocolException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/** This class implements an activity to show the ports of a connected device.
*/
public class ShowDevicePorts extends LoggingActivity implements IDeviceListener {

	private static final int COLOR_DEFAULT = Color.BLUE;
	private static final int COLOR_ERROR = Color.RED;

	class PortListAdapter extends ArrayAdapter<Port> {

		private List<Port> items;

		public PortListAdapter(int textViewResourceId, List<Port> items) {
			super(ShowDevicePorts.this, textViewResourceId, items);
			this.items = items;
		}

		// ListView optimization support
		// Note: Dealing with proper view types (i. e. one view type per port type) yields strange problems.
		// Dial ports won't update their labels on converted views even though the updateState method should run.
		// This method disables the ListView optimization and uses one view per port. However, this should
		// not be a problem because we're not dealing with hundreds or thousands of ports, anyway.

		@Override
		public int getViewTypeCount() {
			// return PortType.values().length;
			return items.size();
		}

		@Override
		public int getItemViewType(int position) {
			// return items.get(position).getType().ordinal();
			return position;
		}

		// end of ListView optimization support

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View result = null;		// do not reuse convert views because their types probably don't match!

			IPortViewAdapter adapter = null;
			if (result == null) {
				Port port = items.get(position);

				// does the port have a view adapter assigned?
				if (portAdapters.containsKey(port.getID())) {
					// assume it is already configured
					return portAdapters.get(port.getID()).getView(convertView);
				} else {

					// to display the port correctly, set its default unit
					// this will also influence the layout to use for the port
					port.setUnitFormat(Units.getDefaultFormat(ShowDevicePorts.this, port.getUnit()));
				}

				if (port instanceof DigitalPort) {
					adapter = new DigitalPortViewAdapter(ShowDevicePorts.this);
				}
				else if (port instanceof AnalogPort) {
					adapter = new AnalogPortViewAdapter(ShowDevicePorts.this);
				}
				else if (port instanceof SelectPort) {
					adapter = new SelectPortViewAdapter(ShowDevicePorts.this);
				}
				else if (port instanceof DialPort) {
					adapter = new DialPortViewAdapter(ShowDevicePorts.this);
				}
				else if (port instanceof StreamingPort) {
					adapter = new StreamingPortViewAdapter(ShowDevicePorts.this);
				}
				else if (port instanceof CustomViewPort) {
					adapter = ((CustomViewPort)port).getViewAdapter(ShowDevicePorts.this);
				}
				else if (port instanceof CustomPort) {
					adapter = new CustomPortViewAdapter(ShowDevicePorts.this);
				}

				if (adapter != null) {
					// configure the port adapter
					adapter.configure(port, ShowDevicePorts.this);

					// get the adapter view
					if (result == null) {
						result = adapter.getView(convertView);
					}
				}
				else {
					// no matching adapter found - create a simple default view
					if (result == null) {
						LayoutInflater vi = (LayoutInflater)ShowDevicePorts.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						result = vi.inflate(R.layout.default_port_row, null);
					}

					TextView tt = result.findViewById(R.id.toptext);
					TextView bt = result.findViewById(R.id.bottomtext);
					if (tt != null) {
						tt.setText(port.getName());
					}
					if (bt != null){
						bt.setText(port.getType() + " " + port.getDirCaps());
					}
				}
				// remember port adapter
				portAdapters.put(port.getID(), adapter);
			}

			result.setBackgroundColor(Color.TRANSPARENT);
			return result;
		}
	}
	
	private ShowDevicePorts instance;
	private ProgressBar mProgress;
    private AndroPDIDevice device;
    ListView ports_listview;
    private List<Port> portList;
    public HashMap<String, IPortViewAdapter> portAdapters = new HashMap<>();
    private PortListAdapter portListAdapter;
    PortAndAdapter portAndAdapter;

    private Spinner groupSelect;
    List<PortGroup> portGroups; 
    private String currentGroup = "";	// default: no group/all ports

    Handler mHandler = new Handler();

    /** A queue of operations to be performed sequentially.
	 * 
	 */
	protected BlockingQueue<PortAction> queue = new ArrayBlockingQueue<PortAction>(10);
	private TextView tvName; 
	private TextView tvInfo;
	
	protected Thread processorThread;
	
	protected boolean dispatchBlocked;
	
    BroadcastReceiver screenReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		// disconnect when the screen is turned off
    		if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF))
    			DeviceManager.getInstance().disconnectAllDevices();
    		// leave the activity
    		finish();
    	}
    };	
	
	class PortGroupSelectAdapter extends ArrayAdapter<PortGroup> {

        public PortGroupSelectAdapter(Context context, List<PortGroup> objects) {
            super(context, R.layout.port_group_select_row, objects);
        }

        @Override //don't override if you don't want the default spinner to be a two line view
        public View getView(int position, View convertView, ViewGroup parent) {
            return initView(position, convertView);
        }

        @Override
        public View getDropDownView(int position, View convertView,
                                    ViewGroup parent) {
            return initView(position, convertView);
        }

        private View initView(int position, View convertView) {
            if(convertView == null)
                convertView = View.inflate(getContext(),
                                           R.layout.port_group_select_row,
                                           null);
            TextView tvText1 = (TextView)convertView.findViewById(R.id.text1);
            tvText1.setText(getItem(position).getLabel());
            TextView tvText2 = (TextView)convertView.findViewById(R.id.text2);
            tvText2.setText("" /*getItem(position).getID()*/);
            return convertView;
        }
    }	

	/** Reloads the device capabilities and reconfigures all ports.
	 */
	class ReconfigureOperation extends PortAction {
		
		boolean setInitialGroup;
		public ReconfigureOperation(boolean setInitialGroup) {
			super();
			this.setInitialGroup = setInitialGroup;
		}
		
		@Override
		void perform() throws TimeoutException, ProtocolException, DeviceException, InterruptedException, DisconnectedException {
            IProtocol protocol = device.getProtocol();

			// clear previous view adapters
			// this avoids too much flickering of the GUI
			portAdapters.clear();

			final IDeviceCapabilities dc = protocol.getDeviceCapabilities();
			// get the state of all ports
			try {
				// query device capabilities
				dc.getPortStates();
			} catch (PortAccessDeniedException e) {
			}

	        // set group initially?
	        if (setInitialGroup)
	        	// set current group from device info, if present
	        	if (device.getDeviceInfo().getStartGroup() != null)
	        		currentGroup = device.getDeviceInfo().getStartGroup();
	        
	        // build group list
	    	portGroups = dc.getPortGroups(currentGroup);
	    	// insert "All groups" item
	    	portGroups.add(0, new PortGroup("" /* empty ID means all ports */, "All ports", "", 0));
	    	
	    	final List<PortGroup> groups = portGroups; 
	    	
            // update port information
            mHandler.post(new Runnable() {
				public void run() {
					PortGroupSelectAdapter adapter = new PortGroupSelectAdapter(instance, groups);
			    	
			    	groupSelect.setAdapter(adapter);
			    	
			    	// select current group
			    	int selection = 0;
			    	for (PortGroup group: groups) {
			    		if (group.getID().equals(currentGroup))
			    			break;
			    		selection++;
			    	}
			    	groupSelect.setOnItemSelectedListener(null);
			    	if (selection < groups.size())
			    		groupSelect.setSelection(selection, false);
			    	
			    	groupSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			    		@Override
			    		public void onItemSelected(AdapterView<?> parent,
			    				View view, int position, long id) {
			    			String oldGroup = currentGroup; 
			    			currentGroup = groups.get(position).getID();
			    			if (!currentGroup.equals(oldGroup))
				    			// reconfigure to filter the ports
				    	        queue.offer(new ReconfigureOperation(false));
			    		}
			    		
			    		@Override
			    		public void onNothingSelected(AdapterView<?> parent) {
			    			
			    		}
			    	});

			    	portList = dc.getPorts(currentGroup);
			        portListAdapter = new PortListAdapter(android.R.layout.simple_list_item_1, portList);
			        ports_listview.setAdapter(portListAdapter);
				}
            });
		}
	}

	/** This class performs the operations that are put in the queue asynchronously.
	 */
	class QueueProcessor implements Runnable {
		@Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    // attempt to take the next work item off the queue
                    // if we consume quicker than the producer then take
                    // will block until there is work to do.
                    final PortAction op = queue.take();
                    
                    // Log.d(AndroPDI.MASTER_NAME, "Processing action: " + op.getName());

                    // show the progress bar
                    mHandler.post(new Runnable() {
                        public void run() {
                            dispatchBlocked = true;
                            
                            //tvName.setVisibility(View.GONE);
                        	mProgress.setVisibility(View.VISIBLE);
                            mProgress.setProgress(0);
                        }
                    });
                    
                    // give the UI time to repaint
                    Thread.yield();

                    // do the work
                    try {
                    	op.startPerformAction();
                    	
						op.perform();

                    	// notify port view
	                    mHandler.post(new Runnable() {
	                        public void run() {
	                        	ShowDevicePorts.this.onActionCompleted(op);
	                        }
	                    });
                    } catch (DisconnectedException de) {
                    	// if disconnected, exit the thread and the view immediately
                    	ShowDevicePorts.this.finish();
                    	break;
                    } catch (final Throwable t) {
                    	t.printStackTrace();
                    	
						// notify adapter: an error occurred
						op.setError(t);
						
                    	// notify port view
	                    mHandler.post(new Runnable() {
	                        public void run() {
	                        	ShowDevicePorts.this.onActionError(op, t);
	                        }
	                    });				
					}

                    mHandler.post(new Runnable() {
                        public void run() {
                        	op.runOnUIThread();

                        	//tvName.setVisibility(View.VISIBLE);
                            // hide the progress bar
                        	mProgress.setVisibility(View.GONE);
                        	
                            dispatchBlocked = false;
                        }
                    });				
                }
            } catch (InterruptedException e) {
                // no need to re-set interrupted as we are outside loop
            }
        }
	}
		
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_device_ports);
        this.instance = this;
        
        // detect when the screen is turned off
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, filter);        

        // get the selected device
        String devId = getIntent().getStringExtra(AndroPDI.CURRENT_DEVICE_ID);
        device = DeviceManager.getInstance().findDeviceById(devId);
        if (device == null) {
        	Toast.makeText(this, R.string.devicecaps_no_device, Toast.LENGTH_SHORT).show();
        	finish();
        	return;
        }
        // the device must be connected
        if (!device.isConnected()) {
        	Toast.makeText(this, R.string.device_not_connected, Toast.LENGTH_SHORT).show();
        	finish();
        	return;
        }

        // the device must support the basic protocol
        if (!(device.getProtocol() instanceof IBasicProtocol)) {
        	Toast.makeText(this, R.string.devicecaps_not_supported, Toast.LENGTH_SHORT).show();
        	finish();
        	return;
        }
    	tvName = (TextView)findViewById(R.id.devicecaps_name);
    	tvName.setText(device.getDeviceName());
    	tvName.setVisibility(View.VISIBLE);
    	tvName.setTextColor(COLOR_DEFAULT);

    	tvInfo = (TextView)findViewById(R.id.devicecaps_textview);
    	tvInfo.setText(device.getDisplayAddress());

    	mProgress = (ProgressBar)findViewById(R.id.progress);
    	mProgress.setVisibility(View.GONE);
    	
    	groupSelect = (Spinner)findViewById(R.id.portGroupSelect);
        
    	ports_listview = (ListView)findViewById(R.id.devicecaps_listview);
        
        ports_listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        		// wrong port index
        		if (position >= portList.size()) return;
        		// streaming port clicked?
        		if (portList.get(position).getType() == PortType.STREAMING)
    				// show streaming port details
    				showPortDetails(portList.get(position));
        		// default port click handler: show context menu
        		else {
        			Port p = portList.get(position);
    				// show select port menu
					if (portAdapters.containsKey(p.getID()))
        				portAdapters.get(p.getID()).handleClick();
        		}
			}
		});
        
        // register this activity as a listener for the device
        device.addConnectionListener(this);
        
        // start asynchronous processing
        processorThread = new Thread(new QueueProcessor(), "QueueProcessor");
        processorThread.setDaemon(true); // don't hold the VM open for this thread
        processorThread.start();
        
        // query the device capabilities
        queue.offer(new ReconfigureOperation(true));
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
    	if (dispatchBlocked)
    		return true;
    	
    	return super.dispatchTouchEvent(ev);
    }
    
	private void showPortDetails(Port port) {
		// Create an intent to start the ShowPortDetails activity
		Intent intent = new Intent(this, ShowPortDetails.class);
		intent.putExtra(AndroPDI.CURRENT_DEVICE_ID, device.getId());
		intent.putExtra(AndroPDI.CURRENT_PORT_ID, port.getID());
		startActivity(intent);			
	}

    @Override
    public void onStart() {
        super.onStart();
    }    
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
    	if (isFinishing()) {
	    	if (portList != null) {
		    	// unbind all streaming ports that may have been autobound
		    	for (Port port: portList) {
		    		if (port instanceof StreamingPort) {
		    			addPortAction(new PortAction(port) {
		    				@Override
		    				void perform() throws TimeoutException,
		    						InterruptedException, DisconnectedException,
		    						DeviceException, ProtocolException {
		    					try {
									((StreamingPort)port).unbind();
								} catch (PortAccessDeniedException e) {
								}
		    				}
		    			});
		    		}
		    	}
	    	}
    	}
    	// unregister listener
    	device.removeConnectionListener(this);
    	// unregister receiver
    	unregisterReceiver(screenReceiver);
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }
    
    @Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {

		super.onCreateContextMenu(menu, v, menuInfo);
		  
		// context menu for port item created?
		if (portAndAdapter != null && v == ports_listview) {
			// let the adapter create the menu
			portAndAdapter.createContextMenu(menu, menuInfo);
		}
	}
    
    @Override
    public void onContextMenuClosed(Menu menu) {
    }

    
    /** Queues the specified action for asynchronous processing.
     * 
     * @param portAction
     * @return
     */
	public synchronized boolean addPortAction(PortAction portAction) {
		queue.offer(portAction);
		Log.d("androPDI", "addPortAction: " + portAction);
		return true;
	}

	// must be called on the UI thread!
	protected void onActionError(PortAction action, Throwable t) {
    	Toast.makeText(ShowDevicePorts.this, t.getMessage(), Toast.LENGTH_LONG).show();
    	tvInfo.setText(t.getMessage());
    	
    	// indicate device problem
    	tvName.setTextColor(COLOR_ERROR);
	}
	
	// must be called on the UI thread!
	protected void onActionCompleted(PortAction action) {
    	// reset device name color
		tvName.setTextColor(COLOR_DEFAULT);
    	tvInfo.setText(device.getDisplayAddress());
   	}
	
	protected void stopProcessor() {
		if (processorThread != null)
			processorThread.interrupt();
	}

	@Override
	public void connectionAborted(IDevice device) {
		stopProcessor();
		finish();
	}
	
	@Override
	public void connectionClosed(IDevice device) {
		// interrupt pending or current operations and exit
		stopProcessor();
		finish();
	}
	
	@Override
	public void connectionError(IDevice device, String message) {
		// interrupt pending or current operations and exit
		stopProcessor();
		showError(message);
		finish();
	}

	@Override
	public boolean getCredentials(IDevice device, String[] namePassword, Boolean[] save) {
		// not supported here
		return false;
	}
	
	@Override
	public void connectionFailed(IDevice device, String message) {
		// interrupt pending or current operations and exit
		stopProcessor();
		showError(message);
		finish();
	}
	
	@Override
	public void connectionInitiated(IDevice device) {
	}
	
	@Override
	public void connectionOpened(IDevice device) {
	}
	
	@Override
	public void connectionTerminating(IDevice device) {
	}
	
	@Override
	public void receivedDebug(IDevice device, String message) {
		// nothing to do here (main activity handles all debug messages)
	}
	
	@Override
	public void receivedReconfigure(IDevice device) {
        // query the device capabilities
        queue.offer(new ReconfigureOperation(false));
	}
	
	@Override
	public void receivedRefresh(IDevice device, String[] portIDs) {
		// ports not specified?
		if (portIDs == null || portIDs.length == 0) {
			// refresh all ports
			try {
				for (IPortViewAdapter adapter: portAdapters.values()) {
					adapter.refresh();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else
			// refresh all specified ports
			for (String portID: portIDs) {
				try {
					if (portAdapters.containsKey(portID)) {
						portAdapters.get(portID).refresh();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
	}
	
	@Override
	public void receivedError(IDevice device, String text) {
		// error on device or in connection; close the view
		finish();
	}	
	
	void showError(final String message) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(ShowDevicePorts.this, message, Toast.LENGTH_SHORT).show();
			}
		});
	}

	/** Wraps a port and an adapter together.
	 * 
	 * @author Leo
	 *
	 */
	class PortAndAdapter {
		Port port;
		IPortViewAdapter adapter;
		
		protected PortAndAdapter(Port port, IPortViewAdapter adapter) {
			super();
			this.port = port;
			this.adapter = adapter;
		}
		
		void createContextMenu(ContextMenu menu, ContextMenuInfo menuInfo) {
			adapter.createContextMenu(menu, menuInfo);
		}
	}

	public void receivedPortMessage(Port port, String message) {
		final String msg = this.device.getDeviceName() + "/" + port.getName() + ": " + message;
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(ShowDevicePorts.this, msg, Toast.LENGTH_LONG).show();
				addLogMessage(msg);
			}
		});
	}
}
