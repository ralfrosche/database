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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class EditMember extends Activity {
	private DatabaseHelper myDbHelper = new DatabaseHelper(this);
	String id = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_member);
		id = getIntent().getStringExtra("query");

		if (!id.equals("")) {
			String[] result_array;
			result_array = id.split(",");
			id = result_array[2];
			// Log.e("mliste",""+id);
		}

		Map<String, String> data = new HashMap<String, String>();

		Class<id> clazz = R.id.class;

		try {
			myDbHelper.createDataBase();
			data = myDbHelper.getRecord("mitglieder", id, "mitgliedsnummer");
			if (data.size() > 0) {

				for (Entry<String, String> e : data.entrySet()) {
					String key = e.getKey();
					String value = e.getValue();

					Field f;
					try {

						f = clazz.getField(key);

						try {
							// Log.e("key:",""+key);
							int id;
							if (key.equals("status")) {
								Spinner spinner = (Spinner) findViewById(R.id.status);

								ArrayAdapter myAdap = (ArrayAdapter) spinner
										.getAdapter();
								for (int index = 0, count = myAdap.getCount(); index < count; ++index) {
									if (myAdap.getItem(index).equals(value)) {
										spinner.setSelection(index);
										break;
									}
								}

							} else if (key.equals("verband")) {
								Spinner spinner = (Spinner) findViewById(R.id.verband);
								ArrayAdapter myAdap = (ArrayAdapter) spinner
										.getAdapter();
								for (int index = 0, count = myAdap.getCount(); index < count; ++index) {
									if (myAdap.getItem(index).equals(value)) {
										spinner.setSelection(index);
										break;
									}
								}

							} 
							
						else if (key.equals("Anrede")) {
							Spinner spinner = (Spinner) findViewById(R.id.Anrede);
							ArrayAdapter myAdap = (ArrayAdapter) spinner
									.getAdapter();
							for (int index = 0, count = myAdap.getCount(); index < count; ++index) {
								if (myAdap.getItem(index).equals(value)) {
									spinner.setSelection(index);
									break;
								}
							}

						}
							else if (key.equals("versicherung")) {
								Spinner spinner = (Spinner) findViewById(R.id.versicherung);
								ArrayAdapter myAdap = (ArrayAdapter) spinner
										.getAdapter();
								if (value.equals("2")) {
									value = "3 Mio.";
								} else { 
									value = "1 Mio.";
								}
								for (int index = 0, count = myAdap.getCount(); index < count; ++index) {
									if (myAdap.getItem(index).equals(value)) {
										spinner.setSelection(index);
										break;
									}
								}

							}
							
							else if (key.equals("platzarbeit")) {
								CheckBox checkBox = (CheckBox) findViewById(R.id.platzarbeit);

								if (value.equals("1")) {
									checkBox.setChecked(true);
								} else {
									checkBox.setChecked(false);
								}
							} else if (key.equals("newsletter")) {
								CheckBox checkBox = (CheckBox) findViewById(R.id.newsletter);

								if (value.equals("1")) {
									checkBox.setChecked(true);
								} else {
									checkBox.setChecked(false);
								}
							} else if (key.equals("pdf")) {
								CheckBox checkBox = (CheckBox) findViewById(R.id.pdf);

								if (value.equals("1")) {
									checkBox.setChecked(true);
								} else {
									checkBox.setChecked(false);
								}
							} else if (key.equals("abbuchung")) {
								CheckBox checkBox = (CheckBox) findViewById(R.id.abbuchung);

								if (value.equals("1")) {
									checkBox.setChecked(true);
								} else {
									checkBox.setChecked(false);
								}

							} else if (key.equals("daec_newspaper")) {
								CheckBox checkBox = (CheckBox) findViewById(R.id.daec_newspaper);

								if (value.equals("1")) {
									checkBox.setChecked(true);
								} else {
									checkBox.setChecked(false);
								}
							}

							else if (key.equals("profil_zeigen")) {
								CheckBox checkBox = (CheckBox) findViewById(R.id.profil_zeigen);

								if (value.equals("1")) {
									checkBox.setChecked(true);
								} else {
									checkBox.setChecked(false);
								}
							}

							else if (key.equals("jugendlicher")) {
								CheckBox checkBox = (CheckBox) findViewById(R.id.jugendlicher);

								if (value.equals("1")) {
									checkBox.setChecked(true);
								} else {
									checkBox.setChecked(false);
								}
							} else if (key.equals("beitragsfrei")) {
								CheckBox checkBox = (CheckBox) findViewById(R.id.beitragsfrei);

								if (value.equals("1")) {
									checkBox.setChecked(true);
								} else {
									checkBox.setChecked(false);
								}
							} else if (key.equals("versicherungsfrei")) {
								CheckBox checkBox = (CheckBox) findViewById(R.id.versicherungsfrei);

								if (value.equals("1")) {
									checkBox.setChecked(true);
								} else {
									checkBox.setChecked(false);
								}
							}
							else if (key.equals("fremdkonto")) {
								CheckBox checkBox = (CheckBox) findViewById(R.id.fremdkonto);

								if (value.equals("1")) {
									checkBox.setChecked(true);
								} else {
									checkBox.setChecked(false);
								}
							}

							else {
								id = f.getInt(null);
								EditText tv = (EditText) findViewById(id);
								tv.setText(value);
							}
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

				}
			}
			myDbHelper.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// Log.e("mliste", "" + prefs);

		Button cancelButton = (Button) findViewById(R.id.cancelbutton);
		Button saveButton = (Button) findViewById(R.id.savebutton);
		cancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				finish();
			}

		});
		Button deleteButton = (Button) findViewById(R.id.deletebutton);
		deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				
				try {
					myDbHelper.createDataBase();
					Integer mitgliedsnummer = Integer.parseInt(id);
					if (mitgliedsnummer >0) 
						myDbHelper.deleteMember(mitgliedsnummer);

				} catch (IOException e) {
					e.printStackTrace();
				}
				
				MainActivity.updateView = true;
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
					header = myDbHelper.getTableHeader("mitglieder");
					myDbHelper.close();

				} catch (IOException e) {
					e.printStackTrace();
				}
				int length = header.size();
				for (int i = 0; i < length; i++) {

					Field f;
					try {
						String key = header.get(i).toString();
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
						if (key.equals("verband")) {
							Spinner spinner = (Spinner) findViewById(R.id.verband);
							String value = spinner.getSelectedItem().toString();
							update.put(key, value);
						} else if (key.equals("status")) {
							Spinner spinner = (Spinner) findViewById(R.id.status);
							String value = spinner.getSelectedItem().toString();
							update.put(key, value);
						} else if (key.equals("versicherung")) {
							Spinner spinner = (Spinner) findViewById(R.id.versicherung);
							String value = spinner.getSelectedItem().toString();
							if (value.equals("3 Mio.")) {
								value = "2";
							}
							else{
								value = "1";
							}
							update.put(key, value);
						} 
						else if (key.equals("Anrede")) {
							Spinner spinner = (Spinner) findViewById(R.id.Anrede);
							String value = spinner.getSelectedItem().toString();
							update.put(key, value);
						}
						
						
						else if (key.equals("platzarbeit")) {
							CheckBox checkBox = (CheckBox) findViewById(R.id.platzarbeit);
							String value = "0";
							if (checkBox.isChecked()) {
								value = "1";
							} else {
								value = "0";

							}
							update.put(key, value);
						} else if (key.equals("newsletter")) {
							CheckBox checkBox = (CheckBox) findViewById(R.id.newsletter);
							String value = "0";
							if (checkBox.isChecked()) {
								value = "1";
							} else {
								value = "0";

							}
							update.put(key, value);
						} else if (key.equals("pdf")) {
							CheckBox checkBox = (CheckBox) findViewById(R.id.pdf);
							String value = "0";
							if (checkBox.isChecked()) {
								value = "1";
							} else {
								value = "0";

							}
							update.put(key, value);
						} else if (key.equals("platzarbeit")) {
							CheckBox checkBox = (CheckBox) findViewById(R.id.platzarbeit);
							String value = "0";
							if (checkBox.isChecked()) {
								value = "1";
							} else {
								value = "0";

							}
							update.put(key, value);
						} else if (key.equals("abbuchung")) {
							CheckBox checkBox = (CheckBox) findViewById(R.id.abbuchung);
							String value = "0";
							if (checkBox.isChecked()) {
								value = "1";
							} else {
								value = "0";

							}
							update.put(key, value);
						}
						
						
						else if (key.equals("daec_newspaper")) {
							CheckBox checkBox = (CheckBox) findViewById(R.id.daec_newspaper);
							String value = "0";
							if (checkBox.isChecked()) {
								value = "1";
							} else {
								value = "0";

							}
							update.put(key, value);
						}
						
						else if (key.equals("daec_newspaper")) {
							CheckBox checkBox = (CheckBox) findViewById(R.id.daec_newspaper);
							String value = "0";
							if (checkBox.isChecked()) {
								value = "1";
							} else {
								value = "0";

							}
							update.put(key, value);
						}
						else if (key.equals("profil_zeigen")) {
							CheckBox checkBox = (CheckBox) findViewById(R.id.profil_zeigen);
							String value = "0";
							if (checkBox.isChecked()) {
								value = "1";
							} else {
								value = "0";

							}
							update.put(key, value);
						}
						else if (key.equals("jugendlicher")) {
							CheckBox checkBox = (CheckBox) findViewById(R.id.jugendlicher);
							String value = "0";
							if (checkBox.isChecked()) {
								value = "1";
							} else {
								value = "0";

							}
							update.put(key, value);
						}
						
						else if (key.equals("beitragsfrei")) {
							CheckBox checkBox = (CheckBox) findViewById(R.id.beitragsfrei);
							String value = "0";
							if (checkBox.isChecked()) {
								value = "1";
							} else {
								value = "0";

							}
							update.put(key, value);
						}
						else if (key.equals("fremdkonto")) {
							CheckBox checkBox = (CheckBox) findViewById(R.id.fremdkonto);
							String value = "0";
							if (checkBox.isChecked()) {
								value = "1";
							} else {
								value = "0";

							}
							update.put(key, value);
						}
						else if (key.equals("versicherungsfrei")) {
							CheckBox checkBox = (CheckBox) findViewById(R.id.versicherungsfrei);
							String value = "0";
							if (checkBox.isChecked()) {
								value = "1";
							} else {
								value = "0";

							}
							update.put(key, value);
						}
						
						
						
						
						
						else {

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
				myDbHelper.update("mitglieder", update, id, "mitgliedsnummer",
						true);
				MainActivity.updateView = true;
				finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.edit_member, menu);
		return true;
	}

}
