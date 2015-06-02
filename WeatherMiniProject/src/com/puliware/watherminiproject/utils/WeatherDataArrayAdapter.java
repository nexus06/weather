package com.puliware.watherminiproject.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.puliware.watherminiproject.R;
import com.puliware.watherminiproject.aidl.WeatherData;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Custom ArrayAdapter for the Weather class, which makes each row of the
 * ListView have a more complex layout than just a single textview (which is the
 * default for ListViews).
 */
public class WeatherDataArrayAdapter extends ArrayAdapter<WeatherData> {
	/**
	 * Construtor that declares which layout file is used as the layout for each
	 * row.
	 */
	public WeatherDataArrayAdapter(Context context) {
		super(context, R.layout.weather_view);
	}

	/**
	 * Construtor that declares which layout file is used as the layout for each
	 * row.
	 */
	public WeatherDataArrayAdapter(Context context, List<WeatherData> objects) {
		super(context, R.layout.weather_view, objects);
	}

	/**
	 * Method used by the ListView to "get" the "view" for each row of data in
	 * the ListView.
	 * 
	 * @param position
	 *            The position of the item within the adapter's data set of the
	 *            item whose view we want. convertView The old view to reuse, if
	 *            possible. Note: You should check that this view is non-null
	 *            and of an appropriate type before using. If it is not possible
	 *            to convert this view to display the correct data, this method
	 *            can create a new view. Heterogeneous lists can specify their
	 *            number of view types, so that this View is always of the right
	 *            type (see getViewTypeCount() and getItemViewType(int)).
	 * @param parent
	 *            The parent that this view will eventually be attached to
	 * @return A View corresponding to the data at the specified position.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final WeatherData data = getItem(position);
		// recicle view
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(
					R.layout.weather_view, parent, false);
		}

		Log.d(WeatherDataArrayAdapter.class.getName(), data.toString());

		// place name
		TextView resultPlace = (TextView) convertView
				.findViewById(R.id.place_name);
		resultPlace.setText(data.mName);

		TextView resultDate = (TextView) convertView.findViewById(R.id.date);

		// date
		Date date = new Date(data.mDate * 1000);
		SimpleDateFormat df2 = new SimpleDateFormat("MMMM,dd");
		String dateText = df2.format(date);
		resultDate.setText(dateText);

		// temp
		TextView resultTemp = (TextView) convertView.findViewById(R.id.degrees);
		DecimalFormat df = new DecimalFormat("#.00");

		resultTemp.setText(df.format(data.mTemp - 272.15) + " ºC");

		new DownloadImageTask(
				(ImageView) convertView.findViewById(R.id.iconImage))
				.execute("http://openweathermap.org/img/w/" + data.mIcon
						+ ".png");

		TextView resultDescription = (TextView) convertView
				.findViewById(R.id.icon_description);

		resultDescription.setText(data.mDescription);

		TextView humidity = (TextView) convertView.findViewById(R.id.humidity);

		humidity.setText("Humidity: " + data.mHumidity + "%");

		TextView wind = (TextView) convertView.findViewById(R.id.wind);

		wind.setText("Wind: " + (data.mSpeed * 3600) / 1000);

		// TextView resultTV =
		// (TextView) convertView.findViewById(R.id.degrees);
		// TextView dbRefsTV =
		// (TextView) convertView.findViewById(R.id.date);
		// TextView yearAddedTV =
		// (TextView) convertView.findViewById(R.id.place_name);

		// resultTV.setText(data.mName);
		// dbRefsTV.setText("" + data.mHumidity);
		// yearAddedTV.setText("" + data.mSunrise);

		return convertView;
	}

	private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
		ImageView bmImage;

		public DownloadImageTask(ImageView bmImage) {
			this.bmImage = bmImage;
		}

		protected Bitmap doInBackground(String... urls) {
			String urldisplay = urls[0];
			Bitmap mIcon11 = null;
			try {
				InputStream in = new java.net.URL(urldisplay).openStream();
				mIcon11 = BitmapFactory.decodeStream(in);
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
				e.printStackTrace();
			}
			return mIcon11;
		}

		protected void onPostExecute(Bitmap result) {
			bmImage.setImageBitmap(Bitmap.createScaledBitmap(result, 200, 200,
					true));
		}
	}
}
