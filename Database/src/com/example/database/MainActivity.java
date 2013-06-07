package com.example.database;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.app.SearchManager.OnCancelListener;
import android.app.SearchManager.OnDismissListener;

import android.content.ContentProviderOperation;

import android.content.Intent;
import android.content.OperationApplicationException;

import android.database.Cursor;
import android.database.SQLException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.MediaStore;

import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

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
	String SelectedPosition;
	String SelectedID = "0";
	public String filter = "";
	ImageView imageViewList;
	boolean searchInvolved = false;
	private MenuItem mMenuItemSearch;
	private MenuItem mMenuItemYouth;
	static final int DATE_DIALOG_ID = 0;
	private static final int SELECT_PICTURE = 1;
	String image_path = "";

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
		imageViewList = (ImageView) findViewById(R.id.imageViewList);

		imageViewList.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
						Intent.createChooser(intent, "Select Picture"),
						SELECT_PICTURE);

			}
		});

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			Bitmap newImage = null;
			Bitmap resizedBitmap = null;
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"ddMMyyyyHHmmss", Locale.GERMAN);

			// image_path = "mitglieder_liste/" + dateFormat.format(new Date())
			// + ".jpg";
			image_path = "mitglieder_liste/" + SelectedID + ".jpg";

			Log.e("MLISTE", "+++ readDataID:" + SelectedID);

			File sdCard = Environment.getExternalStorageDirectory();
			File dir = new File(sdCard.getAbsolutePath() + "/mitglieder_liste");
			dir.mkdir();

			if (requestCode == SELECT_PICTURE) {

				int or = 0;
				Uri selectedImageUri = data.getData();
				try {
					newImage = decodeUri(selectedImageUri);

					Cursor cursor = getContentResolver()
							.query(selectedImageUri,
									new String[] { MediaStore.Images.ImageColumns.ORIENTATION },
									null, null, null);

					if (cursor.getCount() != 1) {
						or = 0;
					} else {

						cursor.moveToFirst();

						or = cursor.getInt(0);

						Log.e("MLISTE", "+++ orientation:" + or);
					}

					int width = newImage.getWidth();
					int height = newImage.getHeight();
					Matrix matrix = new Matrix();
					if (or > 0)
						matrix.postRotate(or);
					resizedBitmap = Bitmap.createBitmap(newImage, 0, 0, width,
							height, matrix, true);
					saveBitmap(resizedBitmap, image_path);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				imageViewList = (ImageView) findViewById(R.id.imageViewList);
				imageViewList.setImageBitmap(resizedBitmap);
			}

		}

	}

	private void saveBitmap(Bitmap newImage, String image_path) {
		imageViewList = (ImageView) findViewById(R.id.imageViewList);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		newImage.compress(Bitmap.CompressFormat.JPEG, 40, bytes);

		File f = new File(Environment.getExternalStorageDirectory()
				+ File.separator + image_path);
		FileOutputStream fo = null;
		try {
			f.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// write the bytes in file

		try {
			fo = new FileOutputStream(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			fo.write(bytes.toByteArray());
			fo.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// remember close de FileOutput

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
		SelectedPosition = String.valueOf(customListSpinner
				.getSelectedItemPosition());

		RunDatabase();
	}

	public void onNothingSelected(AdapterView<?> parent) {
	}

	private boolean checkEmail(String email) {
		return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
	}

	public void RunDatabase() {
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
			SelectedID = DataToDB[8];
			sstatus.setText(DataToDB[9]);

			myDbHelper.close();

			image_path = "mitglieder_liste/" + SelectedID + ".jpg";
			imageViewList = (ImageView) findViewById(R.id.imageViewList);
			Log.e("MListe", "- image:" + image_path);
			if (!image_path.equals("")) {
				Uri mUri;

				File sdCard = Environment.getExternalStorageDirectory();
				String f = sdCard.getAbsolutePath() + "/" + image_path;
				File filecheck = new File(f);
				if (filecheck.exists()) {
					mUri = Uri.parse(f);
					imageViewList.setImageURI(mUri);
				} else {
					imageViewList.setImageResource(R.drawable.noimageavailable);
				}

			}

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

	private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
		BitmapFactory.Options o = new BitmapFactory.Options();
		o.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(
				getContentResolver().openInputStream(selectedImage), null, o);
		final int REQUIRED_SIZE = 300;
		int width_tmp = o.outWidth, height_tmp = o.outHeight;
		int scale = 1;
		while (true) {
			if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
				break;
			}
			width_tmp /= 2;
			height_tmp /= 2;
			scale *= 2;
		}
		BitmapFactory.Options o2 = new BitmapFactory.Options();
		o2.inSampleSize = scale;
		return BitmapFactory.decodeStream(
				getContentResolver().openInputStream(selectedImage), null, o2);

	}

}
