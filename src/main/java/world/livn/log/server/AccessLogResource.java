package world.livn.log.server;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import com.goodkarmacompanies.commons.geocode.CountryGeocoder;
import com.goodkarmacompanies.commons.geocode.GeoName;
import com.goodkarmacompanies.commons.geocode.ReverseGeocoder;
import com.goodkarmacompanies.commons.geocode.ReverseGeocoder.Source;
import com.goodkarmacompanies.commons.geocode.naturalearth.Country;
import com.goodkarmacompanies.commons.util.LimitedLinkedHashMap;
import com.goodkarmacompanies.commons.util.Tuple2;
import com.javadocmd.simplelatlng.LatLng;
import com.maxmind.geoip2.record.City;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;


@Api(value="log")
@Path("log")
public class AccessLogResource {

	private void throwException(String message, Status status) {
		throw new WebApplicationException(Response.status(status).entity(message).type(MediaType.TEXT_PLAIN).build());
	}

	private static final int MAX_CACHED_GLOBAL_CODERS_CACHE = 20;
	private static final int MAX_CACHED_COUNTRY_SPECIFIC_CODERS_CACHE = 250; //These will be a lot smaller
	private static final long DEFAULT_MIN_POPULATION = 100000L;

	private static final ReverseGeocoder DEFAULT_GLOBAL_CODER;
	static {
		ReverseGeocoder rgc;
		try {
			rgc = new ReverseGeocoder(Source.CITIES_15000, true, AccessLogResource.DEFAULT_MIN_POPULATION);
		} catch (Exception fnfe) {
			rgc = null;
		}
		DEFAULT_GLOBAL_CODER = rgc;
	}

	private static Map<Long, ReverseGeocoder> globalCodersMap = Collections.synchronizedMap(new LimitedLinkedHashMap<>(AccessLogResource.MAX_CACHED_GLOBAL_CODERS_CACHE));
	private static Map<Tuple2<Long,String>, ReverseGeocoder> countrySpecificCodersMap = Collections.synchronizedMap(new LimitedLinkedHashMap<>(AccessLogResource.MAX_CACHED_COUNTRY_SPECIFIC_CODERS_CACHE));

	@ApiOperation(value="Retrieves the closest city to the specified latitude and longitude, with the option to restrict results to cities above a specific population. By default we limit results to country at specified lat/lng (reverts to global search if lat/lng is in international waters).", response=City.class)
	@GET
	@Path("closestCity")
	@Produces(MediaType.APPLICATION_JSON)
	public City getClosestCity(
			@ApiParam(value="Latitude in decimal notation.", required=true, allowEmptyValue=false, example="-33.856781") @QueryParam("lat") String latStr,
			@ApiParam(value="Longitude in decimal notation.", required=true, allowEmptyValue=false, example="151.214962") @QueryParam("lng") String lngStr,
			@ApiParam(value="Minimum population of returned city.", required=false, example=""+AccessLogResource.DEFAULT_MIN_POPULATION, defaultValue=""+AccessLogResource.DEFAULT_MIN_POPULATION) @QueryParam("minPop") @DefaultValue(""+AccessLogResource.DEFAULT_MIN_POPULATION) long minPop,
			@ApiParam(value="Set to true to widen search to other countries.", required=false, example="true", defaultValue="false") @QueryParam("crossBorders") @DefaultValue("false") boolean crossBorders,
			@ApiParam(value="Include alternate names.", required=false, example="true", defaultValue="false") @QueryParam("inclAltName") @DefaultValue("false") boolean inclAltNames
			) {

		if(minPop<0) {
			this.throwException("Parameter minPop cannot be negative.", Status.BAD_REQUEST);
		}

		if(StringUtils.isBlank(latStr)) {
			this.throwException("Parameter lat cannot be blank.", Status.BAD_REQUEST);
		}

		double lat = 0D;

		try {
			lat = Double.parseDouble(latStr);
		} catch (Exception nfe) {
			this.throwException("Parameter lat cannot be parsed to a decimal number.", Status.BAD_REQUEST);
		}

		if(lat > 90.0D || lat < -90.0) {
			this.throwException("Parameter lat must be between -90.0 and 90.0.", Status.BAD_REQUEST);
		}

		if(StringUtils.isBlank(lngStr)) {
			this.throwException("Parameter lng cannot be blank.", Status.BAD_REQUEST);
		}

		double lng = 0D;

		try {
			lng = Double.parseDouble(lngStr);
		} catch (Exception nfe) {
			this.throwException("Parameter lng cannot be parsed to a decimal number.", Status.BAD_REQUEST);
		}

		if(lng > 180.0D || lng < -180.0) {
			this.throwException("Parameter lng must be between -180.0 and 180.0.", Status.BAD_REQUEST);
		}

		final LatLng llIn = new LatLng(lat, lng);

		String countryCodeToFilter = null;
		if(!crossBorders) {
			Tuple2<Long, Country> tuple = CountryGeocoder.INSTANCE.getClosestCountry(llIn);
			if(tuple.a!=null && tuple.a <= 10000L) {
				countryCodeToFilter = tuple.b.getIso2();
			}
		}

		ReverseGeocoder rgc = null;

		try {

			if(!crossBorders && countryCodeToFilter!=null) {
				synchronized (AccessLogResource.countrySpecificCodersMap) {

					Tuple2<Long,String> key = Tuple2.of(minPop, countryCodeToFilter);

					rgc = AccessLogResource.countrySpecificCodersMap.get(key);

					if(rgc==null) {
						final Source source;

						if(minPop>=15000) {
							source = Source.CITIES_15000;
						} else if(minPop>=5000) {
							source = Source.CITIES_5000;
						} else {
							source = Source.CITIES_1000;
						}

						rgc = new ReverseGeocoder(source, true, minPop, countryCodeToFilter);
						AccessLogResource.countrySpecificCodersMap.put(key, rgc);
					}
				}
			}
			else {
				//Do global search
				if(minPop==AccessLogResource.DEFAULT_MIN_POPULATION) {
					rgc = AccessLogResource.DEFAULT_GLOBAL_CODER;
				}
				else {
					synchronized (AccessLogResource.globalCodersMap) {
						rgc = AccessLogResource.globalCodersMap.get(minPop);

						if(rgc==null) {
							final Source source;

							if(minPop>=15000) {
								source = Source.CITIES_15000;
							} else if(minPop>=5000) {
								source = Source.CITIES_5000;
							} else {
								source = Source.CITIES_1000;
							}

							rgc = new ReverseGeocoder(source, true, minPop);
							AccessLogResource.globalCodersMap.put(minPop, rgc);
						}
					}
				}
			}

			if(rgc==null) {
				this.throwException("Failed to initialise ReverseGeocoder.", Status.INTERNAL_SERVER_ERROR);
			}
		} catch (IOException e) {
			this.throwException("Failed to initialise ReverseGeocoder.", Status.INTERNAL_SERVER_ERROR);
		}



		GeoName geoName = null;

		try {
			geoName = rgc.nearestPlace(llIn);
		} catch (NullPointerException e) {
			this.throwException("No result matching search criteria.", Status.NOT_FOUND);
		} catch (Exception e) {
			this.throwException("ReverseGeocoder lookup failed.", Status.INTERNAL_SERVER_ERROR);
		}



		return null;

	}



}
