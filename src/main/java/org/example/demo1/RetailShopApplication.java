package org.example.demo1;

import org.example.demo1.resources.*;
import org.example.demo1.security.JWTFilter;
import org.example.demo1.security.JWTRequired;
import org.example.demo1.security.JWTUtility;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

@ApplicationPath("/api")
public class RetailShopApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(ProductResource.class);
        classes.add(CustomerResource.class);
        classes.add(AuthResource.class);
        classes.add(OrdersResource.class);
        classes.add(JWTRequired.class);
        classes.add(JWTFilter.class);
        classes.add(JWTUtility.class);
        classes.add(NotificationsResource.class);



        return classes;
    }

}
