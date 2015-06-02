package com.puliware.watherminiproject.jsonweather;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

/**
 * Parses the Json weather data returned from the Weather Services API and
 * returns a List of JsonWeather objects that contain this data.
 */
public class WeatherJSONParser {
	/**
	 * Used for logging purposes.
	 */
	private final String TAG = this.getClass().getCanonicalName();

	/**
	 * Parse the @a inputStream and convert it into a List of JsonWeather
	 * objects.
	 */
	/**
	 * Parse the @a inputStream and convert it into a List of JsonWeather
	 * objects.
	 */
	public List<JsonWeather> parseJsonStream(InputStream inputStream)
			throws IOException {
		// TODO -- you fill in here.
		// Create a JsonReader for the inputStream.
		try (JsonReader reader = new JsonReader(new InputStreamReader(
				inputStream, "UTF-8"))) {
			Log.d(TAG, "Parsing the results returned as an array");

			// Handle the array returned from the weather Service.
			return parseJsonWeatherArray(reader);
		}
	}

	/**
	 * Parse a Json stream and convert it into a List of JsonWeather objects.
	 */
	public List<JsonWeather> parseJsonWeatherArray(JsonReader reader)
			throws IOException {
		// If the acronym wasn't expanded return null;
		if (reader.peek() == JsonToken.END_OBJECT) {
			return null;
		}
		// TODO -- you fill in here.
		List<JsonWeather> weather = new ArrayList<>();
		Log.d(TAG, "READING--->parseJsonWeatherArray ");
		weather.add(parseJsonWeather(reader));

		return weather;
	}

	/**
	 * Parse a Json stream and return a JsonWeather object.
	 */
	public JsonWeather parseJsonWeather(JsonReader reader) throws IOException {

		// TODO -- you fill in here.
		reader.beginObject();
		Log.d(TAG, "READING--->parseJsonWeatherArray ");
		JsonWeather weather = new JsonWeather();
		try {
			while (reader.hasNext()) {

				String name = reader.nextName();

				Log.d(TAG, "READING---> " + name);

				switch (name) {
				case JsonWeather.base_JSON:
					weather.setBase(reader.nextString());
					Log.d(TAG, "reading base " + weather.getBase());
					break;
				case JsonWeather.cod_JSON:
					weather.setCod(reader.nextLong());
					Log.d(TAG, "reading cod " + weather.getCod());
					break;
				case JsonWeather.message_JSON:
					weather.setMessage(reader.nextString());
					Log.d(TAG, "reading message " + weather.getMessage());
					break;
				case JsonWeather.dt_JSON:
					weather.setDt(reader.nextLong());
					Log.d(TAG, "reading Dt " + weather.getDt());
					break;
				case JsonWeather.id_JSON:
					weather.setId(reader.nextLong());
					Log.d(TAG, "reading Id " + weather.getId());
					break;
				case JsonWeather.main_JSON:
					weather.setMain(parseMain(reader));
					Log.d(TAG, "reading Main " + weather.getMain().toString());
					break;
				case JsonWeather.name_JSON:
					weather.setName(reader.nextString());
					Log.d(TAG, "reading Name " + weather.getName());
					break;
				case JsonWeather.sys_JSON:
					weather.setSys(parseSys(reader));
					Log.d(TAG, "reading Sys " + weather.getSys());
					break;
				case JsonWeather.weather_JSON:
					weather.setWeather(parseWeathers(reader));
					Log.d(TAG, "reading Weather " + weather.getWeather());
					break;
				case JsonWeather.wind_JSON:
					weather.setWind(parseWind(reader));
					Log.d(TAG, "reading Wind " + weather.getWind());
					break;

				default:
					reader.skipValue();
					Log.d(TAG, "ignoring " + name);
					break;
				}
			}
		} finally {
			reader.endObject();
		}
		return weather;
	}

	/**
	 * Parse a Json stream and return a List of Weather objects.
	 */
	public List<Weather> parseWeathers(JsonReader reader) throws IOException {
		reader.beginArray();
		try {
			List<Weather> parseWeathers = new ArrayList<>();
			while (reader.hasNext()) {
				parseWeathers.add(parseWeather(reader));
			}
			return parseWeathers;
		} finally {
			reader.endArray();
		}

	}

	/**
	 * Parse a Json stream and return a Weather object.
	 */
	public Weather parseWeather(JsonReader reader) throws IOException {
		reader.beginObject();
		Weather weather = new Weather();

		try {

			while (reader.hasNext()) {
				String name = reader.nextName();
				switch (name) {
				case Weather.icon_JSON:
					weather.setIcon(reader.nextString());
					Log.d(TAG, "reading Icon " + weather.getIcon());
					break;
				case Weather.description_JSON:
					weather.setDescription(reader.nextString());
					Log.d(TAG,
							"reading Description " + weather.getDescription());
					break;
				case Weather.id_JSON:
					weather.setId(reader.nextLong());
					Log.d(TAG, "reading Id " + weather.getId());
					break;
				default:
					reader.skipValue();
					Log.d(TAG, "ignoring " + name);
					break;
				}
			}
		} finally {
			reader.endObject();
		}
		return weather;
	}

	/**
	 * Parse a Json stream and return a Main Object.
	 */
	public Main parseMain(JsonReader reader) throws IOException {
		reader.beginObject();

		Main weatherMain = new Main();
		try {
			while (reader.hasNext()) {
				String name = reader.nextName();
				switch (name) {
				case Main.grndLevel_JSON:
					weatherMain.setGrndLevel(reader.nextDouble());
					break;
				case Main.humidity_JSON:
					weatherMain.setHumidity(reader.nextLong());
					break;
				case Main.pressure_JSON:
					weatherMain.setPressure(reader.nextDouble());
					break;
				case Main.seaLevel_JSON:
					weatherMain.setSeaLevel(reader.nextDouble());
					break;
				case Main.temp_JSON:
					weatherMain.setTemp(reader.nextDouble());
					break;
				case Main.tempMax_JSON:
					weatherMain.setTempMax(reader.nextDouble());
					break;
				case Main.tempMin_JSON:
					weatherMain.setTempMin(reader.nextDouble());
					break;
				default:
					reader.skipValue();
					Log.d(TAG, "ignoring " + name);
					break;
				}
			}
		} finally {
			reader.endObject();
		}
		return weatherMain;
	}

	/**
	 * Parse a Json stream and return a Wind Object.
	 */
	public Wind parseWind(JsonReader reader) throws IOException {
		// TODO -- you fill in here.
		reader.beginObject();
		Wind weatherMain = new Wind();
		try {
			while (reader.hasNext()) {
				String name = reader.nextName();
				switch (name) {
				case Wind.deg_JSON:
					weatherMain.setDeg(reader.nextDouble());
					break;
				case Wind.speed_JSON:
					weatherMain.setSpeed(reader.nextDouble());
					break;
				default:
					reader.skipValue();
					Log.d(TAG, "ignoring " + name);
					break;
				}
			}
		} finally {
			reader.endObject();
		}
		return weatherMain;
	}

	/**
	 * Parse a Json stream and return a Sys Object.
	 */
	public Sys parseSys(JsonReader reader) throws IOException {
		// TODO -- you fill in here.
		reader.beginObject();

		Sys weatherSys = new Sys();
		try {
			while (reader.hasNext()) {
				String name = reader.nextName();
				switch (name) {
				case Sys.country_JSON:
					weatherSys.setCountry(reader.nextString());

					break;
				case Sys.message_JSON:
					weatherSys.setMessage(reader.nextDouble());
					break;
				case Sys.sunrise_JSON:
					weatherSys.setSunrise(reader.nextLong());
					break;
				case Sys.sunset_JSON:
					weatherSys.setSunset(reader.nextLong());
					break;
				default:
					reader.skipValue();
					Log.d(TAG, "ignoring " + name);
					break;
				}
			}
		} finally {
			reader.endObject();
		}
		return weatherSys;

	}
}
