package com.shitsend;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SendshitActivity extends Activity {

	// private Map<String, String> id_data = new HashMap<String, String>();

	/**
	 * Called when the activity is first created.
	 * 
	 * @return
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

		new uploadTask().execute();

	}

	int timestamp = 0;

	private class getLineTask extends AsyncTask<Void, Void, Void> {

		//int timestamp = 0;

		@Override
		protected Void doInBackground(Void... params) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(
					"http://dolan.bouncemen.net/robust_timestamp.php");

			try {
				// Add your data
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						2);
				nameValuePairs.add(new BasicNameValuePair("sensor", "1"));
				nameValuePairs.add(new BasicNameValuePair("id", "30B57BFBB820273207483858911A00377BD558A3"));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				// Execute HTTP Post Request
				HttpResponse response = httpclient.execute(httppost);

				BufferedReader in = new BufferedReader(new InputStreamReader(
						response.getEntity().getContent()));
				StringBuffer sb = new StringBuffer("");
				String line = "";
				String NL = System.getProperty("line.separator");
				while ((line = in.readLine()) != null) {
					sb.append(line + NL);
				}
				in.close();
				String page = sb.toString();
				System.out.println(page);

				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}

			return null;
		}

		protected Void onPostExecute() {
			doRobustUpload();
			return null;
		}

	}

	private class uploadTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			long start = System.currentTimeMillis();
			new getLineTask().execute();
			long end = System.currentTimeMillis();
			System.out.println("Execution speed " + (end - start) + " ms\n");
			return null;
		}
	}

	private void doRobustUpload() {

		String dirPath = Environment.getExternalStorageDirectory().getPath()
				+ "/sensorData/";

		File dir = new File(dirPath);
		File[] fileList = dir.listFiles();

		for (File f : fileList) {
			doUploadFile(f, 1);
		}

	}

	private void doUploadFile(File f, int line) {

		HttpURLConnection conn = null;
		DataOutputStream dos = null;
		DataInputStream inStream = null;

		String urlString = "http://dolan.bounceme.net/robust_upload.php"; // if i'm at work and dolan is home
		// String urlString = "http://192.168.206.31/robust_upload.php"; // both at work
		// String urlString = "http://192.168.1.45/robust_upload.php"; //both at home

		String lineEnd = "\r\n";
		String twoHyphens = "--";
		String boundary = "*****";

		/*
		 * int bytesRead, bytesAvailable, bufferSize; 
		 * byte[] buffer; 
		 * int maxBufferSize = 1 * 1024 * 1024;
		 */

		Scanner file_scanner = null;

		try {
			file_scanner = new Scanner(f);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		while (file_scanner.hasNext()) {

			if (!hasActiveInternetConnection()) {
				// write code to pause here
				break;

			}

			String currentline = null;

			for (int i = 0; i < 10; i++) {
				currentline += file_scanner.next();
			}

			Log.d("GB", currentline);

			// upload each line
			try {
				// ------------------ CLIENT REQUEST
				Log.d("MediaPlayer", "Inside second Method");

				// open a URL connection to the Servlet
				URL url = new URL(urlString);

				// Open a HTTP connection to the URL
				conn = (HttpURLConnection) url.openConnection();

				// Allow Inputs
				conn.setDoInput(true);

				// Allow Outputs
				conn.setDoOutput(true);

				// Don't use a cached copy.
				conn.setUseCaches(false);

				// Use a post method.
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Connection", "Keep-Alive");
				// System.setProperty("http.keepAlive", "false");
				conn.setRequestProperty("Content-Type",
						"multipart/form-data;boundary=" + boundary);

				dos = new DataOutputStream(conn.getOutputStream());

				// write header
				String key = "u"; 
				dos.writeBytes(twoHyphens + boundary + lineEnd);
				dos.writeBytes("Content-Disposition: form-data; name=" + key
						+ lineEnd);
				dos.writeBytes(lineEnd);
				dos.writeBytes(currentline);
				dos.writeBytes(lineEnd);

				dos.flush();
				dos.close();

			} catch (MalformedURLException ex) {
				Log.d("MediaPlayer", "error: " + ex.getMessage(), ex);
			}

			catch (IOException ioe) {
				Log.d("MediaPlayer", "error: " + ioe.getMessage(), ioe);
			}

			// get server response
			try {
				inStream = new DataInputStream(conn.getInputStream());
				String str;

				while ((str = inStream.readLine()) != null) {
					Log.d("MediaPlayer", "Server Response: " + str);
				}

				inStream.close();
			}

			catch (IOException ioex) {
				Log.d("MediaPlayer", "error: " + ioex.getMessage(), ioex);
			}
		}
		
		if(file_scanner.hasNext()) {  //if there is a next line in the file
			Log.d("GB", "upload not finished");
			
			int pausetime=1000; //pause for 1000 milli sec
			while (!hasActiveInternetConnection()){ //detect internet connectivity
				try {
					Thread.sleep(pausetime); //pauses loop 
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.d("GB", "trying to connect. pausing for: " + pausetime + " seconds");
				if (pausetime < 6000) {
					pausetime*=2; //doubles pausetime if no internet connectivity
				} else {
					pausetime = 6000; //caps pause time at 1min (checks every minute)
				}
			}
			
			new getLineTask().execute(); //gets line and resumes upload 
		}

		Log.d("MediaPlayer", "Done uploading everything");
	}

	public boolean hasActiveInternetConnection() {
		// boolean networkavailable = isNetworkAvailable();
		// if (networkavailable) {
		try {
			HttpURLConnection urlc = (HttpURLConnection) (new URL(
					"http://www.google.com").openConnection());
			urlc.setRequestProperty("User-Agent", "Test");
			urlc.setRequestProperty("Connection", "close");
			urlc.setConnectTimeout(3000); //maybe want to up this threshold?
			urlc.connect();
			if (urlc.getResponseCode() == 200) {
				Log.d("GB", "there is internet");
				return true;
			} else {
				Log.d("GB", "there is no internet");
			}
		} catch (IOException e) {
			Log.e("GB", "Error checking internet connection", e);
		}
		// } else {
		// Log.d("GB", "No network available!");
		// }
		return false;
	}
	
	private String getID() {
		TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}
	
	

}
