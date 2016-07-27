package com.example.database;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.example.database.R.id;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

public class EditPrefs extends Activity {
	private DatabaseHelper myDbHelper = new DatabaseHelper(this);
	private String pref_id = "";
	private Map<String, String> shadowPrefs = new HashMap<String, String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_prefs);
		getWindow().setSoftInputMode(
			    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		Map<String, String> prefs = new HashMap<String, String>();
		
		Class<id> clazz = R.id.class;

		try {
			myDbHelper.createDataBase();
			prefs = myDbHelper.getPreferncies();
			if (prefs.size() > 0) {
				shadowPrefs = prefs;

				for (Entry<String, String> e : prefs.entrySet()) {
					String key = e.getKey();
					String value = e.getValue();
					if (!key.equals("id")) {
						Field f;
						try {

							f = clazz.getField(key);

							try {
								int id;
								
									id = f.getInt(null);
									EditText tv = (EditText) findViewById(id);
									tv.setText(value);
								

							} catch (IllegalArgumentException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (IllegalAccessException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}

						} catch (NoSuchFieldException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} else {
						pref_id = value;
					}

				}
			}
			myDbHelper.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


		Button cancelButton = (Button) findViewById(R.id.cancelbutton);
		Button saveButton = (Button) findViewById(R.id.savebutton);
		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}

		});

		saveButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Class<id> clazz = R.id.class;
				ArrayList<String> header = new ArrayList<String>();
				Map<String, String> update = new HashMap<String, String>();
				try {
					myDbHelper.createDataBase();
					header = myDbHelper.getTableHeader("preferencies");
					myDbHelper.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
				int length = header.size();
				for (int i = 0; i < length; i++) {

					Field f;
					try {
						String key = header.get(i).toString();

						if (!key.equals("id")) {
							f = clazz.getField(key);
							int id = 0;
							try {
								id = f.getInt(null);
							} catch (IllegalArgumentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IllegalAccessException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							EditText tv = (EditText) findViewById(id);
							String value = tv.getText().toString();
							update.put(key, value);

						}
					} catch (NoSuchFieldException e) {
						Log.e("mliste", "" + header.get(i).toString());
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				myDbHelper.update("preferencies", update, shadowPrefs, pref_id, "id", false);
				finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_prefs, menu);
		return true;
	}

}
