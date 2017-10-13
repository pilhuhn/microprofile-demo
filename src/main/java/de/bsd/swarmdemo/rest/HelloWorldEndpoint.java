package de.bsd.swarmdemo.rest;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Metric;
import org.eclipse.microprofile.metrics.annotation.Timed;

@RequestScoped
@Path("/hello")
public class HelloWorldEndpoint  {

	@Inject
	Counter aCounter;

	@Inject
  @Metric(absolute = true, tags = "type=histo")
	Histogram histogram;

	@PostConstruct
	public void startup() {
		aCounter.inc(42);
    System.out.println("+++ Post Construct");
  }

	@GET
	@Produces("text/plain")
	@Counted(description = "Counting of the Hello call", absolute = true, tags = {"app=shop","type=counter"})
	@Timed(name="helloTime", description = "Timing of the Hello call", absolute = true, tags = {"app=shop","type=timer"})
	public Response doGet() {
		aCounter.inc();
		return Response.ok("Hello from WildFly Swarm! " + aCounter.getCount()).build();
	}


}
