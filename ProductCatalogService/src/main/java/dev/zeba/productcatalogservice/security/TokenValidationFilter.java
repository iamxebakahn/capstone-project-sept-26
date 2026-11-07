package dev.zeba.productcatalogservice.security;

import dev.zeba.productcatalogservice.dto.AuthUserDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TokenValidationFilter extends OncePerRequestFilter {

    private final String authServiceUrl; //to read authUrl
    private final RestTemplate restTemplate; //for rest template object

    public TokenValidationFilter(@Value("${auth.service.url}")String authServiceUrl,
                                 RestTemplateBuilder restTemplateBuilder) {
        this.authServiceUrl = authServiceUrl;
        this.restTemplate = restTemplateBuilder.build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        //all get end points to be public - no authentication
        //any role can do post/put - need to be authenticated (valid token with any role)
        //delete - admin role only
        if (request.getMethod().equals("GET")) {
            // no need of security context verification/validation: pass request to next filter
            filterChain.doFilter(request, response);
            return;
        }

        //if not get req, call authservice to validate token
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        AuthUserDto authUserDto;
        //if valid header call authservice
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", header);
            //can use get (coz of /get in authcontroller )but using excehange
            ResponseEntity<AuthUserDto> answer =
                    restTemplate.exchange(this.authServiceUrl + "/auth/validateToken", HttpMethod.GET, new HttpEntity<>(headers), AuthUserDto.class);
            authUserDto = answer.getBody();
        } catch (HttpClientErrorException e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());//token is invalid
            return;
        } catch (RestClientException e) {
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value()); //service not working/service is down
            return;
        }

        //response/req (by default empty) client sends -> call to authservice -> answer returned by authservice -> based on answer status in response is to be set
        //2 ways to interact with microservice: syn: rest template/fienclient direct call w/o response
        //asynch: kafka

        if (request.getMethod().equals("DELETE") && !authUserDto.getRole().equals("ADMIN")) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return;
        }
        //attach filter in front of request coming to productcatalog; no manual token validation call authservice api for token validation
        //Tomcat runs on 8080, both services(productcatalog and authservice) run on 8080! 1 port listens to 1 request hence productcatalog on 8080
        //authservice shift to 8081 (server.port=8081 added to properties in userauth)
        //connect to service with its URL ; add in application properties

        filterChain.doFilter(request, response);

        //authservice is handling authentication of which entity in system? user hence authservice is called identity service
        //business logic not owned by authservice only token validation is owned by authservice
    }
}

/*Instead of On Prem , we go for cloud machine - virtually via internal (GCP< Azure < AWS)
 spin computing resource in AWS  functionaly: EC2 (no manual config)
 elastic beanstalk: deploy spring -> takes code, does everything - infrastructure management etc
1. set up relational db (RDS in AWS) (terraform in enterprise env and not in AWS) (IAAS - Infra as code, terraform script to create infra for auditability n accountablity)
VPC = virtual pvt cloud : complete logical separation b/w 2 accounts hence VPC for 2 accounts : physically might be on same account
JARs = upload code on elastic beanstalk, no need of m/c; we compile code first in our m/c app and upload it in elastic bean stalk
compiled code = JAR of code : Java archive : compression file of code
JAR types - 1. Fat: classes, configs, embedded Tomcats, classes, run environment, dependencies, app server 2. Normal: just classes
we upload fat JAR
deploy UserAuth first
mvn clean package -DskipTests: to generate jar (-DskipTests: to stop generating tests while generating jar; haven't written on how to open connection to db or mock db, hence cant have these test cases running n jar will fail)
target has all the jar: to be uploaded on elastic beanstalk
target -> test-classes  -> .jar to be uploaded .jar > .jar.original larger size (fat is larger than original)
how to set env variables in AWS db? export command ! eg.server.port -->> SERVER_PORT (all caps and . is now _)\
spring profiles: prod db and staging env db same ? diff!! spring profiles sets diff resources based on env
separate production related configuration in application-prod.properties : profile activating this ? prod
in application,properties show-sql in prod should be false coz we don't wanna see sql logs in prod, coz it consumes memory
datasource.url, datasource.username, datasource.password will change : only those which will change will go in application-prod.properties
fire query on db - connect to workbench: only resource in vpc can connect to db nothing from outside; hence change inbound rule to allow outside traffic to come in, so we can connect local workbench
never allow external traffic to prod db
something becomes publically accessible: how: external IP is created which can be connected by outside world - 1. traffic from VPC
2. traffic from localhost workbench
cidr block: 172.31.0.0/16: everything in vpc can access my database
(when internet connection/movbile data changes - mysql stops: coz myip changes)
move to prod and delete lcoal rule
once public traffic is disabled only way to interact with db: through spring apps! via java code; someone gets hold of spring app too, they can make change
create unique password for 2 dbs (user, productcatalog) and user can't access any other instance of db in aws

elasticbeanstock: creates ec2 instance, manage app, manage infra (everything related to infra)
if not elastic bean stock how is infra managed: follow manual steps (spin ec2 instance, select os in it, install java version, deploy app)
give a role to app(spring) to do all the steps you would have done manually : IAM role attached to elasticbeanstock
Health Check End Point: via actuate in AWS (inbuilt in Spring)

db username in spring: is not admin, in aws: admin (coz of permission to user with username admin and password to access product catalog and user auth db)
 coz if username of spring is known to anyone: can do bad things to app, spoof entire db

 prioritiies of picking up variable values while setting up prod profile
 environment variables>app-prod.properties>app.properties

ddl-auto=validate, db is empty someone's to create tables how is going to be created in 1st attempt
1. manually write it using flyway; hence in environment varirables in aws pre deployment ddl-auto =update ; post deployment remove it

log file is generated, but is stored in s3
despite ok, health - ec2 being healthy - application working ! no - 502 Bad Gateway (App not started) : nginx:like Tomcat is app server, elastic beanstock attaches nginx (web server/reverse proxy: takes req from internet and forwards it to app) in front of our app
 why nginx : coz everything not offline anymore, internet involved here: 1 person on 2g, other on 5g - so 2g will get 1 packet or take time to get all packets of a request than 5g person, hence nginx is needed to buffer i.e collect all packets together and send it to app, coz there's no load balance n all, it has other jobs as well like decompression
 collecting + collating packets and sending to spring app is nginx hjob; nginx listens to 5000 port; but service is running on 8081

 */