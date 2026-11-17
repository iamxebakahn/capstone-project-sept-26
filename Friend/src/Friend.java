import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

// A pretend friend for practice. Run it with:  java Friend.java
// Remote control:  http://localhost:9000/set?delay=3000&fail=70&code=503
public class Friend {

    static volatile int delayMs = 0;      // how slow the friend is
    static volatile int failPercent = 0;  // how often the friend fails
    static volatile int failCode = 503;   // the HTTP code it fails with

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(9000), 0);
        server.setExecutor(Executors.newCachedThreadPool());   // many calls at the same time

        server.createContext("/set", ex -> {
            String query = ex.getRequestURI().getQuery() == null ? "" : ex.getRequestURI().getQuery();
            for (String pair : query.split("&")) {
                if (pair.startsWith("delay=")) delayMs = Integer.parseInt(pair.substring(6));
                if (pair.startsWith("fail=")) failPercent = Integer.parseInt(pair.substring(5));
                if (pair.startsWith("code=")) failCode = Integer.parseInt(pair.substring(5));
            }
            reply(ex, 200, "friend is now: delay=" + delayMs + " ms, fail=" + failPercent + " %, code=" + failCode);
        });
        // pretends to be our auth service
        server.createContext("/auth/validateToken", ex ->
                answer(ex, "{\"email\":\"friend@shopnest.com\",\"role\":\"ADMIN\"}"));
        // pretends to be Groq
        server.createContext("/groq", ex ->
                answer(ex, "{\"choices\":[{\"message\":{\"content\":\"Pretend pitch from the friend!\"}}]}"));

        server.start();
        System.out.println("Friend is listening on http://localhost:9000");
    }

    static void answer(HttpExchange ex, String json) throws IOException {
        ex.getRequestBody().readAllBytes();
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ignored) { }
        boolean fail = ThreadLocalRandom.current().nextInt(100) < failPercent;
        System.out.println(ex.getRequestURI().getPath() + " -> " + (fail ? failCode : 200));
        if (fail) reply(ex, failCode, "no, sorry");
        else reply(ex, 200, json);
    }

    static void reply(HttpExchange ex, int status, String text) throws IOException {
        byte[] bytes = text.getBytes();
        ex.getResponseHeaders().add("Content-Type", "application/json");
        ex.sendResponseHeaders(status, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }
}