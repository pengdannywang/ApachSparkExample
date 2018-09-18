package world.livn.log.server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import world.livn.log.server.model.AccessLog;


@Api(value="log")
@Path("log")
public class AccessLogResource {

	private void throwException(String message, Status status) {
		throw new WebApplicationException(Response.status(status).entity(message).type(MediaType.TEXT_PLAIN).build());
	}



	static {

	}


	@ApiOperation(value="List all accessLog", response=AccessLog.class)
	@GET
	@Path("accessLog")
	@Produces(MediaType.APPLICATION_JSON)
	public AccessLog getClosestCity(
			@ApiParam(value="Latitude in decimal notation.", required=true, allowEmptyValue=false, example="-33.856781") @QueryParam("lat") String latStr,
			@ApiParam(value="Longitude in decimal notation.", required=true, allowEmptyValue=false, example="151.214962") @QueryParam("lng") String lngStr
			) {




		return null;

	}



}
