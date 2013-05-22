package com.example.database;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.SearchManager.OnCancelListener;
import android.app.SearchManager.OnDismissListener;

import android.content.ContentProviderOperation;

import android.content.Intent;
import android.content.OperationApplicationException;

import android.database.SQLException;

import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;

import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.ImageButton;
import android.widget.Spinner;

import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class MainActivity extends Activity implements OnItemSelectedListener {

	private Spinner customListSpinner;
	String[] DataToDB;
	String[] result_array;
	String Selecteditem;
	public String filter = "";
	boolean searchInvolved = false;
	private MenuItem mMenuItemSearch;
	private MenuItem mMenuItemYouth;

	public final Pattern EMAIL_ADDRESS_PATTERN = Pattern
			.compile("[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@"
					+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "(" + "\\."
					+ "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		initCustomListSpinner(filter, false);
		// setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		handleIntent(getIntent());

	}

	@Override
	public boolean onSearchRequested() {

		final SearchManager searchManager = (SearchManager) this
				.getSystemService(MainActivity.SEARCH_SERVICE);
		searchManager.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {

				if (filter == "")
					initCustomListSpinner("", false);

			}
		});

		searchManager.setOnCancelListener(new OnCancelListener() {

			@Override
			public void onCancel() {

				Log.e("MListe", "- Cancel-");

			}
		});

		return super.onSearchRequested();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
		Log.e("MListe", "- HandleIntent-" + intent.getAction());
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			doMySearch(query);
		}

	}

	private void doMySearch(String query) {
		// TODO Auto-generated method stub
		filter = query;
		initCustomListSpinner(query, false);

		Log.e("MListe", "- Query:-" + query);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		DatabaseHelper myDbHelper = new DatabaseHelper(this);
		myDbHelper.close();
	}

	private void initCustomListSpinner(String filter, boolean youth) {

		customListSpinner = (Spinner) findViewById(R.id.custom_list_spinner);
		List<CharSequence> choices = new ArrayList<CharSequence>();

		DatabaseHelper myDbHelper = new DatabaseHelper(this);
		try {

			myDbHelper.createDataBase();
			int dlength = 0;
			while (dlength == 0) {
				DataToDB = myDbHelper.ReadNRFromDB(filter, youth);
				dlength = DataToDB.length;
			}

			for (int i = 0; i < DataToDB.length; i++) {
				choices.add(DataToDB[i]);
			}

			myDbHelper.close();

		} catch (IOException ioe) {

			throw new Error("Unable to create database");

		}

		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
				this, android.R.layout.simple_spinner_item, choices);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		customListSpinner.setAdapter(adapter);
		customListSpinner.setOnItemSelectedListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		mMenuItemSearch = menu.getItem(0);
		mMenuItemYouth = menu.getItem(1);

		Log.e("MListe", "- OPtions menu created -");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String searchS;
		switch (item.getItemId()) {

		case R.id.search:
			filter = "";

			searchS = (String) mMenuItemSearch.getTitle();
			if (searchS.equals("Filter entfernen")) {
				mMenuItemSearch.setTitle("Suche / Filter");
				initCustomListSpinner(filter, false);

			} else {
				mMenuItemSearch.setTitle("Filter entfernen");
				onSearchRequested();
			}

			return true;
		case R.id.version:
			doVersion();
			return true;

		case R.id.stats:
			doStats();
			return true;

		case R.id.structure:
			doStructure();
			return true;

		case R.id.childs:
			filter = "";

			searchS = (String) mMenuItemYouth.getTitle();
			if (searchS.equals("Alle")) {
				mMenuItemYouth.setTitle("Jugendliche");
				initCustomListSpinner(filter, false);

			} else {
				mMenuItemYouth.setTitle("Alle");
				initCustomListSpinner(filter, true);
			}
			return true;
		}

		return false;
	}

	private void doStructure() {

		String stat = "";
		DatabaseHelper myDbHelper = new DatabaseHelper(this);
		try {

			myDbHelper.createDataBase();
			DataToDB = myDbHelper.ReadAlterFromDB();

			for (int i = 0; i < DataToDB.length; i++) {
				stat += DataToDB[i] + "\n";
			}

			myDbHelper.close();

		} catch (IOException ioe) {

			throw new Error("Unable to open database");

		}

		new AlertDialog.Builder(this)
				.setTitle("Altersstruktur")
				.

				setInverseBackgroundForced(false)
				.setMessage(
						"Aero-Club Hamburg e.V.\n" + stat
								+ "Database Version: " + DatabaseHelper.DB_NAME
								+ "\nKommerzielle Nutzung der Daten verboten!")
				.show();
	}

	private void doStats() {

		String stat = "";
		DatabaseHelper myDbHelper = new DatabaseHelper(this);
		try {

			myDbHelper.createDataBase();
			DataToDB = myDbHelper.ReadStatsFromDB();
			stat += "Mitglieder gesamt: " + DataToDB[0] + "\n";
			stat += "Aktive: " + DataToDB[1] + "\n";
			stat += "Jungendliche: " + DataToDB[2] + "\n";
			stat += "Ehrenmitglieder: " + DataToDB[3] + "\n";
			stat += "Fördermitglieder: " + DataToDB[4] + "\n";
			stat += "ruhende Mitglieder: " + DataToDB[5] + "\n";
			stat += "Austritte Ende 2013: " + DataToDB[6] + "\n";
			
			DataToDB = myDbHelper.ReadAverageAlterFromDB();
			
			stat += "Durchschnitssalter: " + DataToDB[0] + "\n\n";
			
			myDbHelper.close();
			
			

		} catch (IOException ioe) {

			throw new Error("Unable to open database");

		}

		new AlertDialog.Builder(this)
				.setTitle("Statistik")
				.

				setInverseBackgroundForced(false)
				.setMessage(
						"Aero-Club Hamburg e.V.\n" + stat
								+ "Database Version: " + DatabaseHelper.DB_NAME
								+ "\nKommerzielle Nutzung der Daten verboten!")
				.show();
	}

	private void doVersion() {

		new AlertDialog.Builder(this)
				.setTitle("Versionshinweis")
				.setInverseBackgroundForced(false)
				.setMessage(
						"Aero-Club Hamburg e.V.\n" + "Mitgliederliste V1.1\n"
								+ "(c) 2013 Ralf Rosche\n"
								+ "Database Version: " + DatabaseHelper.DB_NAME
								+ "\nKommerzielle Nutzung der Daten verboten!")
				.show();
	}

	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		Selecteditem = customListSpinner.getSelectedItem().toString();
		View lview = (View) findViewById(R.layout.main);
		RunDatabse(lview);
	}

	public void onNothingSelected(AdapterView<?> parent) {
	}

	private boolean checkEmail(String email) {
		return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
	}

	public void RunDatabse(View view) {
		DatabaseHelper myDbHelper = new DatabaseHelper(this);

		try {

			myDbHelper.createDataBase();
			DataToDB = myDbHelper.ReadFromDB(Selecteditem.trim());
			TextView svorname = (TextView) findViewById(R.id.vorname);
			final TextView sname = (TextView) findViewById(R.id.name);
			final TextView smobil = (TextView) findViewById(R.id.mobil);
			final TextView stelefon = (TextView) findViewById(R.id.telefon);
			final TextView semail = (TextView) findViewById(R.id.email);
			TextView sstrasse = (TextView) findViewById(R.id.strasse);
			TextView splz = (TextView) findViewById(R.id.plz);
			TextView sort = (TextView) findViewById(R.id.ort);
			TextView snummer = (TextView) findViewById(R.id.nummer);
			TextView sstatus = (TextView) findViewById(R.id.status);

			ImageButton smobileButton = (ImageButton) findViewById(R.id.mobileButton);
			ImageButton sphoneButton = (ImageButton) findViewById(R.id.phoneButton);
			ImageButton ssmsButton = (ImageButton) findViewById(R.id.smsButton);
			ImageButton semailButton = (ImageButton) findViewById(R.id.emailButton);
			ImageButton scontactButton = (ImageButton) findViewById(R.id.contactButton);

			svorname.setText(DataToDB[0]);
			sname.setText(DataToDB[1]);
			sstrasse.setText(DataToDB[2]);
			splz.setText(DataToDB[3]);
			sort.setText(DataToDB[4]);
			stelefon.setText(DataToDB[5]);
			smobil.setText(DataToDB[6]);
			semail.setText(DataToDB[7]);
			snummer.setText(DataToDB[8]);
			sstatus.setText(DataToDB[9]);

			myDbHelper.close();

			sphoneButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					String telefonNumber = stelefon.getText().toString();
					telefonNumber = telefonNumber.trim();
					telefonNumber = telefonNumber.replaceAll("/", "");
					telefonNumber = telefonNumber.replaceAll("-", "");
					telefonNumber = telefonNumber.replaceAll(" ", "");

					if (telefonNumber.length() > 5) {
						String uri = "tel:" + telefonNumber;
						Intent intent = new Intent(Intent.ACTION_CALL);
						intent.setData(Uri.parse(uri));

						startActivity(intent);
					}

				}
			});

			smobileButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					String phoneNumber = smobil.getText().toString();

					phoneNumber = phoneNumber.trim();
					phoneNumber = phoneNumber.replaceAll("/", "");
					phoneNumber = phoneNumber.replaceAll("-", "");
					phoneNumber = phoneNumber.replaceAll(" ", "");

					if (phoneNumber.length() > 5) {
						String uri = "tel:" + phoneNumber;
						Intent intent = new Intent(Intent.ACTION_CALL);
						intent.setData(Uri.parse(uri));

						startActivity(intent);
					}

				}
			});

			semailButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					String emailn = semail.getText().toString();

					if (checkEmail(emailn)) {
						String uri = "mailto:" + emailn;
						Intent intent = new Intent(Intent.ACTION_SENDTO, Uri
								.parse(uri));
						intent.putExtra("compose_mode", true);
						startActivity(intent);
					}

				}
			});

			ssmsButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					String phoneNumber = smobil.getText().toString();

					phoneNumber = phoneNumber.trim();
					phoneNumber = phoneNumber.replaceAll("/", "");
					phoneNumber = phoneNumber.replaceAll("-", "");
					phoneNumber = phoneNumber.replaceAll(" ", "");

					if (phoneNumber.length() > 5) {
						String uri = "smsto:" + phoneNumber;
						Intent intent = new Intent(Intent.ACTION_SENDTO, Uri
								.parse(uri));
						intent.putExtra("compose_mode", true);
						startActivity(intent);
					}

				}
			});
			scontactButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {
					String phoneNumber = smobil.getText().toString();
					String name = sname.getText().toString();
					phoneNumber = phoneNumber.trim();
					phoneNumber = phoneNumber.replaceAll("/", "");
					phoneNumber = phoneNumber.replaceAll("-", "");
					phoneNumber = phoneNumber.replaceAll(" ", "");

					String telefonNumber = stelefon.getText().toString();
					telefonNumber = telefonNumber.trim();
					telefonNumber = telefonNumber.replaceAll("/", "");
					telefonNumber = telefonNumber.replaceAll("-", "");
					telefonNumber = telefonNumber.replaceAll(" ", "");

					String emailn = semail.getText().toString();

					ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

					int rawContactID = ops.size();

					// Adding insert operation to operations list
					// to insert a new raw contact in the table
					// ContactsContract.RawContacts
					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.RawContacts.CONTENT_URI)
							.withValue(
									ContactsContract.RawContacts.ACCOUNT_TYPE,
									null)
							.withValue(RawContacts.ACCOUNT_NAME, null).build());

					// Adding insert operation to operations list
					// to insert display name in the table ContactsContract.Data
					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID,
									rawContactID)
							.withValue(ContactsContract.Data.MIMETYPE,
									StructuredName.CONTENT_ITEM_TYPE)
							.withValue(StructuredName.DISPLAY_NAME, name)
							.build());

					// Adding insert operation to operations list
					// to insert Mobile Number in the table
					// ContactsContract.Data
					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID,
									rawContactID)
							.withValue(ContactsContract.Data.MIMETYPE,
									Phone.CONTENT_ITEM_TYPE)
							.withValue(Phone.NUMBER, phoneNumber)
							.withValue(Phone.TYPE,
									CommonDataKinds.Phone.TYPE_MOBILE).build());

					// Adding insert operation to operations list
					// to insert Home Phone Number in the table
					// ContactsContract.Data
					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID,
									rawContactID)
							.withValue(ContactsContract.Data.MIMETYPE,
									Phone.CONTENT_ITEM_TYPE)
							.withValue(Phone.NUMBER, telefonNumber)
							.withValue(Phone.TYPE, Phone.TYPE_HOME).build());

					// Adding insert operation to operations list
					// to insert Home Email in the table ContactsContract.Data
					ops.add(ContentProviderOperation
							.newInsert(ContactsContract.Data.CONTENT_URI)
							.withValueBackReference(
									ContactsContract.Data.RAW_CONTACT_ID,
									rawContactID)
							.withValue(ContactsContract.Data.MIMETYPE,
									Email.CONTENT_ITEM_TYPE)
							.withValue(Email.ADDRESS, emailn)
							.withValue(Email.TYPE, Email.TYPE_HOME).build());

					try {
						// Executing all the insert operations as a single
						// database transaction
						getContentResolver().applyBatch(
								ContactsContract.AUTHORITY, ops);
						Toast.makeText(getBaseContext(),
								"Kontakt erfolgreich hinzugefügt",
								Toast.LENGTH_SHORT).show();
					} catch (RemoteException e) {
						e.printStackTrace();
					} catch (OperationApplicationException e) {
						e.printStackTrace();
					}
				}

			});

		} catch (IOException ioe) {

			throw new Error("Unable to create database");

		}

		try {

		} catch (SQLException sqle) {

			throw sqle;

		}
	}

}
