package de.bsd.microprofiledemo.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.annotation.Timed;

@RequestScoped
@Path("/hello")
public class HelloWorldEndpoint  {

	@Inject
	@ConfigProperty(name = "mp.what", defaultValue = "Hello")
	String what;

	@Inject
	@Metric(absolute = true, description = "How often are calls to /health tried")
	Counter retryCount;

	@PostConstruct
	public void startup() {
  }

	@GET
	@Produces("text/plain")
	@Counted(description = "Counting of the Hello call", absolute = true, tags = {"app=shop","type=counter"}, monotonic
			= true)
	@Timed(name="helloTime", description = "Timing of the Hello call", absolute = true, tags = {"app=shop","type=timer"})
	public Response doGet() {
		return Response.ok(what + " from WildFly Swarm! ").build();
	}


	@GET
	@Path("/health")
	@Produces("text/plain")
	@Counted(name = "gh_count", absolute = true, monotonic = true)
	@Retry
	@Timeout(300)
	@Fallback(fallbackMethod = "unhealthy")
	public Response getHealth() throws Exception {

			URL url = new URL("http://localhost:8080/hello/slow");
			retryCount.inc();

		try {
			URLConnection urlConnection = url.openConnection();
			urlConnection.setDoInput(true);
			InputStream stream = urlConnection.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(stream));
			StringBuilder builder = new StringBuilder();

			String s;
			while ((s = br.readLine()) != null) {
				builder.append(s);
			}
			stream.close();
			return Response.ok(builder.toString()).build();
		} catch (IOException e) {
			System.err.println(e.getMessage());
			throw e;
		}

	}

	// Fallback from FT
	@SuppressWarnings("unused")
	public Response unhealthy() {

	  return Response.status(Response.Status.EXPECTATION_FAILED).build();
  }

  @GET
	@Path("/slow")
	@Timed(absolute = true)
  public Response sometimesSlow() {

		boolean isSlow = false;
		if (Math.random() < 0.7) {  // TODO get factor from config
			try {
				isSlow = true;
				Thread.sleep(800);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return Response.ok("/slow was called and it was " + (isSlow ? "slow" : "fast")).build();

	}


	@POST
	@Path("/unhealthy")
	public Response setUnhealthy() {
		MyApplication.isHealthy = false;

		return Response.ok("This is the unhealthy fallback").build();
	}

}
