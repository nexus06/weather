package com.puliware.watherminiproject.operations;

import java.lang.ref.WeakReference;
import java.util.List;

import com.puliware.watherminiproject.R;
import com.puliware.watherminiproject.activities.MainActivity;
import com.puliware.watherminiproject.aidl.WeatherCall;
import com.puliware.watherminiproject.aidl.WeatherData;
import com.puliware.watherminiproject.aidl.WeatherRequest;
import com.puliware.watherminiproject.aidl.WeatherResults;
import com.puliware.watherminiproject.services.WeatherServiceAsync;
import com.puliware.watherminiproject.services.WeatherServiceSync;
import com.puliware.watherminiproject.utils.GenericServiceConnection;
import com.puliware.watherminiproject.utils.Utils;
import com.puliware.watherminiproject.utils.WeatherDataArrayAdapter;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

/**
 * This class implements all the weather-related operations defined in the
 * WeatherOps interface.
 */
public class WeatherOpsImpl implements WeatherOps {
	/**
	 * Debugging tag used by the Android logger.
	 */
	protected final String TAG = getClass().getSimpleName();

	/**
	 * Used to enable garbage collection.
	 */
	protected WeakReference<MainActivity> mActivity;

	/**
	 * The ListView that will display the results to the user.
	 */
	protected WeakReference<ListView> mListView;

	/**
	 * Acronym entered by the user.
	 */
	protected WeakReference<EditText> mEditText;

	/**
	 * List of results to display (if any).
	 */
	protected List<WeatherData> mResults;

	/**
	 * A custom ArrayAdapter used to display the list of AcronymData objects.
	 */
	protected WeakReference<WeatherDataArrayAdapter> mAdapter;

	/**
	 * This GenericServiceConnection is used to receive results after binding to
	 * the WeatherServiceSync Service using bindService().
	 */
	private GenericServiceConnection<WeatherCall> mServiceConnectionSync;

	/**
	 * This GenericServiceConnection is used to receive results after binding to
	 * the WeatherServiceAsync Service using bindService().
	 */
	private GenericServiceConnection<WeatherRequest> mServiceConnectionAsync;

	/**
	 * This Handler is used to post Runnables to the UI from the mWeatherResults
	 * callback methods to avoid a dependency on the Activity, which may be
	 * destroyed in the UI Thread during a runtime configuration change.
	 */
	private final Handler mDisplayHandler = new Handler();

	/**
	 * The implementation of the AcronymResults AIDL Interface, which will be
	 * passed to the Acronym Web service using the
	 * AcronymRequest.expandAcronym() method.
	 * 
	 * This implementation of AcronymResults.Stub plays the role of Invoker in
	 * the Broker Pattern since it dispatches the upcall to sendResults().
	 * 
	 * is invoked by WeatherAsynk to return result back to the activity
	 */
	private final WeatherResults.Stub mWeathermResults = new WeatherResults.Stub() {

		@Override
		public void sendResults(final List<WeatherData> weatherDataList)
				throws RemoteException {

			// Since the Android Binder framework dispatches this
			// method in a background Thread we need to explicitly
			// post a runnable containing the results to the UI
			// Thread, where it's displayed. We use the
			// mDisplayHandler to avoid a dependency on the
			// Activity, which may be destroyed in the UI Thread
			// during a runtime configuration change.

			mDisplayHandler.post(new Runnable() {
				public void run() {
					displayResults(weatherDataList);
				}
			});

		}

		/**
		 * This method is invoked by the AcronymServiceAsync to return the
		 * results back to the AcronymActivity.
		 */

		@Override
		public void sendError(final String reason) throws RemoteException {
			mActivity.get().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Utils.showToast(mActivity.get(), reason);
				}
			});

		}

	};

	/**
	 * Constructor initializes the fields.
	 */
	public WeatherOpsImpl(MainActivity activity) {
		// Initialize the WeakReference.
		mActivity = new WeakReference<>(activity);

		// Finish the initialization steps.
		initializeViewFields();
		initializeNonViewFields();
	}

	/**
	 * Initialize the View fields, which are all stored as WeakReferences to
	 * enable garbage collection.
	 */
	private void initializeViewFields() {
		// Get references to the UI components.
		mActivity.get().setContentView(R.layout.main_activity);

		// Store the EditText that holds the urls entered by the user
		// (if any).
		mEditText = new WeakReference<>((EditText) mActivity.get()
				.findViewById(R.id.editText1));

		// Store the ListView for displaying the results entered.
		mListView = new WeakReference<>((ListView) mActivity.get()
				.findViewById(R.id.listView1));

		// Create a local instance of our custom Adapter for our
		// ListView.
		mAdapter = new WeakReference<>(new WeatherDataArrayAdapter(
				mActivity.get()));

		// Set the adapter to the ListView.
		mListView.get().setAdapter(mAdapter.get());

		// Display results if any (due to runtime configuration change).
		if (mResults != null)
			displayResults(mResults);
	}

	/**
	 * (Re)initialize the non-view fields (e.g., GenericServiceConnection
	 * objects).
	 */
	private void initializeNonViewFields() {
		mServiceConnectionSync = new GenericServiceConnection<WeatherCall>(
				WeatherCall.class);

		mServiceConnectionAsync = new GenericServiceConnection<WeatherRequest>(
				WeatherRequest.class);
	}

	/**
	 * Initiate the service binding protocol.
	 */
	@Override
	public void bindService() {
		Log.d(TAG, "calling bindService()");

		// Launch the Acronym Bound Services if they aren't already
		// running via a call to bindService(), which binds this
		// activity to the AcronymService* if they aren't already
		// bound.
		if (mServiceConnectionSync.getInterface() == null)
			mActivity
					.get()
					.getApplicationContext()
					.bindService(
							WeatherServiceSync.makeIntent(mActivity.get()),
							mServiceConnectionSync, Context.BIND_AUTO_CREATE);

		if (mServiceConnectionAsync.getInterface() == null)
			mActivity
					.get()
					.getApplicationContext()
					.bindService(
							WeatherServiceAsync.makeIntent(mActivity.get()),
							mServiceConnectionAsync, Context.BIND_AUTO_CREATE);
	}

	/**
	 * Initiate the service unbinding protocol.
	 */
	@Override
	public void unbindService() {
		if (mActivity.get().isChangingConfigurations())
			Log.d(TAG,
					"just a configuration change - unbindService() not called");
		else {
			Log.d(TAG, "calling unbindService()");

			// Unbind the Async Service if it is connected.
			if (mServiceConnectionAsync.getInterface() != null)
				mActivity.get().getApplicationContext()
						.unbindService(mServiceConnectionAsync);

			// Unbind the Sync Service if it is connected.
			if (mServiceConnectionSync.getInterface() != null)
				mActivity.get().getApplicationContext()
						.unbindService(mServiceConnectionSync);
		}
	}

	/**
	 * Called after a runtime configuration change occurs to finish the
	 * initialization steps.
	 */
	public void onConfigurationChange(MainActivity activity) {
		Log.d(TAG, "onConfigurationChange() called");

		// Reset the mActivity WeakReference.
		mActivity = new WeakReference<>(activity);

		// (Re)initialize all the View fields.
		initializeViewFields();
	}

	/*
	 * Initiate the asynchronous acronym lookup when the user presses the
	 * "Look Up Async" button. one-way call
	 */
	public void getCurrentWeatherAsync(View v) {
		final WeatherRequest weatherRequest = mServiceConnectionAsync
				.getInterface();

		if (weatherRequest != null) {
			// Get the acronym entered by the user.
			final String acronym = mEditText.get().getText().toString();

			resetDisplay();

			try {
				// Invoke a one-way AIDL call, which does not block
				// the client. The results are returned via the
				// sendResults() method of the mAcronymResults
				// callback object, which runs in a Thread from the
				// Thread pool managed by the Binder framework.
				weatherRequest.getCurrentWeather(acronym, mWeathermResults);
			} catch (RemoteException e) {
				Log.e(TAG, "RemoteException:" + e.getMessage());
			}
		} else {
			Log.d(TAG, "acronymRequest was null.");
		}
	}

	/*
	 * Initiate the synchronous acronym lookup when the user presses the
	 * "Look Up Sync" button. Two-way call
	 */
	public void getCurrentWeatherSync(View v) {

		// get the wether call version
		final WeatherCall weatherCall = mServiceConnectionSync.getInterface();

		if (weatherCall != null) {
			// Get the acronym entered by the user.
			final String acronym = mEditText.get().getText().toString();

			resetDisplay();

			// Use an anonymous AsyncTask to download the Weather data
			// in a separate thread and then display any results in
			// the UI thread.
			// Using AsyncTask to avoid UI blocking
			new AsyncTask<String, Void, List<WeatherData>>() {
				/**
				 * Acronym we're trying to expand.
				 */
				private String mWeather;

				/**
				 * Retrieve the expanded acronym results via a synchronous
				 * two-way method call, which runs in a background thread to
				 * avoid blocking the UI thread.
				 */
				protected List<WeatherData> doInBackground(String... acronyms) {
					try {
						mWeather = acronyms[0];
						return weatherCall.getCurrentWeather(mWeather);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					return null;
				}

				/**
				 * Display the results in the UI Thread.
				 */
				protected void onPostExecute(List<WeatherData> weatherDataList) {
					if (weatherDataList.size() > 0)
						displayResults(weatherDataList);
					else
						Utils.showToast(mActivity.get(),
								"Sorry, city not found");
				}
				// Execute the AsyncTask to expand the acronym without
				// blocking the caller.
			}.execute(acronym);
		} else {
			Log.d(TAG, "mWeatherCall was null.");
		}
	}

	/**
	 * Display the results to the screen.
	 * 
	 * @param results
	 *            List of Results to be displayed.
	 */
	private void displayResults(List<WeatherData> results) {
		mResults = results;

		// Set/change data set.
		mAdapter.get().clear();
		mAdapter.get().addAll(mResults);
		mAdapter.get().notifyDataSetChanged();
	}

	/**
	 * Reset the display prior to attempting to expand a new acronym.
	 */
	private void resetDisplay() {
		Utils.hideKeyboard(mActivity.get(), mEditText.get().getWindowToken());
		mResults = null;
		mAdapter.get().clear();
		mAdapter.get().notifyDataSetChanged();
	}

}
