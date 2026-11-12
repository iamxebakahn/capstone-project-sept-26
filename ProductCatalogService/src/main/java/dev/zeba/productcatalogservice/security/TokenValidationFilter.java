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

CI/CD pipelines : continous deployment instead of uploading JARs again and again
New Age Software dev:
Agent: Open Code Skills / Claude Skills / Rovo (Conceptually everything remains same in every env; response might be diff for diff agent)
Agent???
chatgpt->upload file -> response -> answer it/verify (loop)

Agent = model + harness + tools
LLM Model vs agent /Plain AI model vs agent -> LLM: textual response only; Agents: does task-edit/delete/add/opens browser n so on
model = LLM model: I gives text, it reads it, prepares answer
ex: explain maven file
agent needs to search file; search functionality is tool, reads file to understand and anwser
agents dont have all powers: restricted powers; set of rules/permissions contained in harness; model feeds reposnse in harness - harness check rules and if it has powers it invokes tool to do the job
access google drive: harness check rules and calls tools
ex: explain maven file;agent needs to search file-> harness calls search tool->harness then takes tool response and calls models with response from tool/feedbacks the LLM
entire proceess is automated / in loop
claud code/open code/rovo ?? what are they?? they are harness specifically (if i am layman i would call it model)
companies dont build models: only 2 companies build models: OpenAI, Anthropic: Rest other companies are building harness around them

Framework to follow while doing agentic development:
Define (problem) ->Delegate (AI model)->Verify (response)->Decide(what to do next)

2 modes to work with agent:
build: can actually invoke the tools
plan: gives text responses, doesn't invoke any editable tools (do work in this, once you satisfied move to build mode)

 Context Window/Request Context: everything given to agent in one chat session/one chat session memory. As context increases, model degrades! (hallucination) (generates false response to satisfy context you have given)
 Safety First Guardrails:
 * always have git enabled in project
 * always ensure permissions you have given to agent (delete no way! avoid destructive permissions)
 * ask, allow, deny permission (agent should always ask my permission, automatically does it: searching file/opening non secret file free to execute, says i cant do it for things i havenot defined)
 * try to teach agent habits of your team (habits = skills)

to define rule; create opencode.json (follows a certain schema)
deleting - shell command/terminal command
resource - commnad /resource permission needs to be applied to  (rm - delete commands in mac/linux); effect - ask/deny/allow
git remove/find delete - other delete commands
/sessions: stores sessions
/export: gives export of sessions in json or anyother way (in cmd open -R file path)
NOTES.md created: in case of leak ensure no secret info is kept . most new models will not reveal the hidden instructions for AI, but our free basic one did
Prompt injection points need to be taken care of
Customers can exploit chatbot
Run the unit tests and tell me if they pass.:
-DSKIPTESTS coz context loads file expecting data base test cases - we did not have, so its not a failure - its more of an expected result
AGENTS.md: to teach model habits of my team, i write agents.md (eg. to ignore failure of context loads)
prompt from me -> opencode opens AGENTS.md in context of a session -> then takes my content for processing
Rules/Good practices to write around Agents.md:
-short (file/rules to be small sized)- iterative process(keep improving it) - (opencode /init creates 1st version of AGENTS.md)
in case of lot of test cases/skills : create subagents

//Frontend make it!!!  Signup, Login, Search : Railway.com /Vercel/ netlify
tools: shell/edit/read
opencode.json (claude.json (its a configuration file)): specify specific rules for permission n all (allow rules)
prompt injection used by ppl to bypass rules to abuse system, doesnt understand intent : agent writes 1st version, we work on it
/init for building agents file not json file
-> run the tests: db related tests were failing coz i didnt set up context, model shold have pre context that my team doensot want db related tests to run
precontext/team habits to agent -> written in agents.md (loaded before every chat session)
agents.md - should be small else it will hallucinate -> after certain time it will ignore if it is large

plan mode -> patch api -> first problem -> it was overengineering and mockito bean -> reiterate plan -> build mode -> build feauter -> TDD ask it to write 4-5 tests and then write code according to those tests (can hallucinate here too) -> get a 2nd review/create agentic reviewer

Skills & Subagents:
ask agent to review code in ssame session? no ! it will justify what its done! get the review in new chat session give it context and location and ask it to review (painful)
better way - make a subagent: analogy -> threads in a process, agents can spin up subagents and the benefit -> context of subagent is empty
make a subagent with name of reviewer, security reviewer (security parts)
to put subagents and skills: create .opencode folder -> subfolders agents, skills (/subagents or /skills for first version from agent)
Agents.md : precontext loaded with every chat session: disadvantage! even for smaller contxt, everything is loaded : for every segment pre context could be different -> define skill for a perticuklar type of task: agent only loads skill when it is needed not in all chats
precontext invoked on demand : skills
we can have scripts, templates, assets inside skill.
can create as many skills as possible, wont hallucinate agent
integrate via commands the agents in your workflow!
Build a Readme of your project, ask agent to act as interviewer, suggest features which can be built on top of it (PM)
how to invoke agent as a command:
Smoke Test?? sort of pre prod testing, if all APIs are working or not - test live path (path customers will go through)
no matter what code change happens, my basic features 6-7 APIs should keep on working, on every commit and push  trigger github workflows in which i can run a command to run a smoke test- to ensure no commit is disturbing main path
opencode run calls agent as command, because i have screen right now, i am running open code, but in CI/CD pipelines without screen we use command to invoke agents
pipeline is sequence of shell commands we exectue, in that last would be commands to run agent with so n so prompt
requests.md : smoketest file
previously python scripts to invoke end points with credentials and tokens; write agent command in workflow run requests.md as smoke test -> give command to generate token -> agent generates token and check end points whether smoke is passing or not

MCPs: make our own. + integrate others as well
(Top 3 challenges for new engineers- K.T, Documentation, Lot of knowledge undocumented, Our own systems - ticket where issue is-> logs -> painful (trace id, when the issue was, bug, search logs of that itme line, multiple systems travelling multiple logs hence, downstream service -navigation issue / time consuming)
Solve these above issues with MCPs
ask agent to design doc on confluence/notion ! cannot do it by default, ask it to read JIRA ticket -> by default no it cant read, ask it to go to diff dashboard and check logs -> no!
basic skills - read, edit, shell - cant open external systems, cant navigate, cant create JIRA ticket, cant open logs, cant write docs on my behalf on notion/confluence
design to feature ready: figma
MCP : problem solves -> access to external systems and work on my behalf (normally can't work unless integrated)
integrate apple calendar with google calendar -> integration possible ->

Opencode, codex, claude code, rover, agent -> JIRA, Confluence/Notion, Splunk (to read logs), storage/DB, talk to external service too (20 capabilities need to be built)
since there's agent capability to be integrated hence we are able to integrate it, similary RHS tools also need to have some capability to be integrated
N*M problem: m is huge (1000s of app to connect agent to) to avoid this, define common standard between them / interface analogy: anthropic defined MCP: open standard (25/11/24)
12/25 - moved to agentic foundation(under Linux foundation) [like http MCP is also an open standard]
all external systems if they want AI agents to operate on them, they have to expose MCP server. MCP server is agent agnostic
now N*M becomes N+M, architecturally adapter pattern
model never directly talks to MCP server

Architecture of how E-2-E MCP works
catelog service -> MySQL DB -> as of today opencode cannot fire queries on db
if i want certain capability to my agent i will have to write MCP server for catalog; agent/model doesnt talk to MCP server, harness talks to MCP
calling order

Client       Host+MCP Client                                         MCP Server                                                         Server API
Model->     Opencode ->                                       MCP server ->                                                           cataloge ->     DB
        (edit,read,shell)             (defines all capabilities AI agents to have: getProducts() from DB, deleteProducts)
         asks MCP server
     what all capabilities it can offer
    stores all those capabilities along with default capabilities
    register tools exposed in MCP Server


layman: MCP is Rest API server, server which can be called by AI agent, since AI is involved and making calls and not human making calls hence MCP and not http
local servers: communicate via stdio
remote servers: JSON - RPC 2.0 (v.specific GRPC)
 */

/*
business , environmental
maths, financial
+
FMVA (12k, Fin Analyst, 2 lev, 100, Accounts/Maths-    )- CFI, CA, CFA, ACCA

MBA

 */