/*
 EcmDroid - Android Diagnostic Tool for Buell Motorcycles
 Copyright (C) 2012 by Michel Marti

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.ecmdroid;

import java.util.LinkedList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class DataChannelAdapter extends ArrayAdapter<Variable> {
	private static final String NO_SELECTION = "<none>";
	protected static final String TAG = "DataChannelAdapter";
	private LayoutInflater inflater;
	private VariableProvider variableProvider;
	private LinkedList<String> variables;
	private ECM ecm;
	private boolean autoRefresh = false;
	private Variable[] items;

	public DataChannelAdapter(Context ctx, ECM ecm, Variable[] items) {
		super(ctx, 0, items);
		this.items = items;
		inflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		variableProvider = VariableProvider.getInstance(ctx);
		variables = new LinkedList<String>(variableProvider.getScalarRtVariableNames(ecm.getId()));
		variables.add(0, NO_SELECTION);
		this.ecm = ecm;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		Variable variable = getItem(position);
		// Log.d(TAG, "getView("+ position +") = " + variable.getName());
		if (convertView == null) {
			// Log.d(TAG, "Creating new view");
			convertView = inflater.inflate(R.layout.datachannel, null);
			Spinner spinner = (Spinner) convertView.findViewById(R.id.dataChannelSpinner);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), android.R.layout.simple_spinner_item, variables);
			spinner.setAdapter(adapter);
			spinner.setPrompt(getContext().getString(R.string.choose_variable));
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			if (variable != null) {
				int pos = variables.indexOf(variable.getName());
				if (pos != -1) {
					spinner.setSelection(pos);
				}
			}
			spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
				public void onItemSelected(AdapterView<?> parent,
						View view, int pos, long id) {
					String selected = (String) parent.getItemAtPosition(pos);
					// Log.d(TAG, "Item Selected: " + selected + ", pos: " + pos + ", id: " + id + " parent pos: " + parent.getTag());
					Integer ppos = (Integer) parent.getTag();
					if (ppos != null) {
						Variable var = items[ppos];
						if (var == null && pos == 0) {
							return; // Already cleared
						}

						// Log.d(TAG, "var: " + var + ", pos: " + pos + ", selected: " + selected);
						if ( var == null || !selected.equals(var.getName())) {
							Log.d(TAG, "Setting " + selected + " at pos " + ppos);
							items[ppos] = (pos == 0 ? null : variableProvider.getRtVariable(ecm.getId(), selected));
							DataChannelAdapter.this.notifyDataSetChanged();
						}
					}
				}

				public void onNothingSelected(AdapterView<?> adapterview) {
				}
			});
		}

		Spinner spinner = (Spinner) convertView.findViewById(R.id.dataChannelSpinner);
		if (spinner != null) {
			spinner.setTag(Integer.valueOf(position));
			spinner.setEnabled(ecm.getId() != null && !autoRefresh);
			if (variable != null) {
				int pos = variables.indexOf(variable.getName());
				if (pos != -1) {
					spinner.setSelection(pos);
				}
			} else {
				spinner.setSelection(0);
			}
		}

		if (variable != null) {
			variable.refreshValue(ecm.getRealtimeData());
		}

		TextView    value = (TextView) convertView.findViewById(R.id.dataChannelText);
		ProgressBar pg  = (ProgressBar) convertView.findViewById(R.id.dataChannelProgress);
		if (value != null) {
			value.setText(variable == null ? "" : variable.getFormattedValue());
		}
		if (pg != null) {
			// Log.d(TAG, "Setting MAX " + variable.getName() + " to " + variable.getHigh());
			if (variable != null) {
				pg.setMax((int) variable.getHigh());
				try {
					int pi = (int) Double.parseDouble(variable.getRawValue().toString());
					// Log.d(TAG, "Setting progress to " + pi);
					pg.setProgress(pi);
				} catch (Exception e) {
					// Log.d(TAG, "Unable to parse value "+ variable.getRawValue());
				}
			} else {
				pg.setProgress(0);
			}
		}
		return convertView;
	}
	public boolean isAutoRefresh() {
		return autoRefresh;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}
	public void setAutoRefresh(boolean autoRefresh) {
		this.autoRefresh = autoRefresh;
	}
}