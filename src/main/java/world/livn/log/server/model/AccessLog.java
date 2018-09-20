package world.livn.log.server.model;

import io.swagger.annotations.ApiModelProperty;

public class AccessLog {
	/**
	 * {"server_name":"central.livngds.com",
	 * "request":"GET \/livncentral\/intapi\/cache\/42717?startDate=2018-09-06&endDate=2018-09-12  HTTP\/1.1",
	 * "postman-token":null,"request_encoding":null,
	 * "remote_user":null,"request_content_type":null,
	 * "protocol":"HTTP\/1.1","response_content_type":"application\/json",
	 * "millis_commit":26,"@version":1,"response_encoding":"ISO-8859-1",
	 * "logger_name":"world.livn.tomcat.JSONAccessLogValve",
	 * "remote_addr":"50.116.2.140","auth_type":null,
	 * "source_host":"central.livngds.com","method":"GET","millis_process":26,
	 * "query":"?startDate=2018-09-06&endDate=2018-09-12",
	 * "remote_port":33162,
	 * "length":21,
	 * "local_addr":"45.33.63.90",
	 * "remote_host":"50.116.2.140","@timestamp":"2018-09-05T14:06:26.847Z",
	 * "thread_name":"http-bio-443-exec-5","request_path":"\/livncentral\/intapi\/cache\/42717","server_port":443,
	 * "user-agent":"Java\/1.8.0_171","status":200}
	 */
	@ApiModelProperty(value = "server name e.g.central.livngds.com", required = true)
	String server_name;
	@ApiModelProperty(value = "e.g.GET //livncentral//intapi//cache//42717?startDate=2018-09-06&endDate=2018-09-12  HTTP//1.1", required = true)
	String request;
	String remote_addr;
	String source_host;
	String query;
	String local_addr;
	String remote_host;
	String timestamp;
	String request_path;

}
