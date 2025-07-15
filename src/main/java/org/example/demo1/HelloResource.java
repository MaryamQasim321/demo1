package org.example.demo1;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.*;

@Path("/hello-world")


public class HelloResource {

    @GET
    @Produces("text/plain")
    public String sayHello() {
        return "Hello, World!";
    }

    @GET
    @Path("/{name}")
    @Produces("text/plain")
    public String greet(@PathParam("name") String name) {
        return "Hello, " + name;
    }
}
