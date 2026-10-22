package com.Harsh.Productivity.Os.ai.provider;
import com.Harsh.Productivity.Os.ai.gateway.AiProvider;
import com.Harsh.Productivity.Os.ai.gateway.ProviderProbe;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
public abstract class ChatModelProvider implements AiProvider {
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
    private final String id;
    private final String modelName;
    private final ChatModel chatModel;
    private final Map<Class<?>, Object> services = new ConcurrentHashMap<>();
    public ChatModelProvider(String id, String modelName, ChatModel chatModel) {
        this.id = id;
        this.modelName = modelName;
        this.chatModel = chatModel;
    }
    @Override
    public String id() {
        return id;
    }
    @Override
    public String modelName() {
        return modelName;
    }
    @Override
    public <S> S service(Class<S> serviceType) {
        return serviceType.cast(services.computeIfAbsent(serviceType, type -> AiServices.create(type, chatModel)));
    }
    @Override
    public ProviderProbe probeInference(){
        long start=System.nanoTime();
        try {
            String reply=chatModel.chat("Reply with single word OK.");
            boolean ok=reply!=null && !reply.isBlank();
            return new ProviderProbe(ok,elapsedMs(start),ok?"Model produced a response":"Model returned an empty response");
        }
        catch (RuntimeException e){
            return new ProviderProbe(false,elapsedMs(start),"Inference failed");
        }
    }
    protected static ProviderProbe httpGet(String url,String bearerToken,String mustContain){
        long start=System.nanoTime();
        try {
            HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(5)).GET();
            if(bearerToken!=null && !bearerToken.isBlank()){
                request.header("Authorization", "Bearer "+bearerToken);
            }
            HttpResponse<String> response=HTTP.send(request.build(),HttpResponse.BodyHandlers.ofString());
            long ms=elapsedMs(start);
            int status=response.statusCode();
            if(status==200){
                if(mustContain!=null && !response.body().contains(mustContain)){
                    return new ProviderProbe(false, ms, "Server reachable but the model is not installed");
                }
                return new ProviderProbe(true, ms, "Reachable");
            }
            if(status==401||status==403){
                return new ProviderProbe(false,ms,"Authentication failed");
            }
            return new ProviderProbe(false,ms,"Unexpected HTTP status"+status);
        }catch(InterruptedException e){
            Thread.currentThread().interrupt();
            return new ProviderProbe(false,elapsedMs(start),"Interrupted");
        }catch(Exception e){
            return new ProviderProbe(false,elapsedMs(start),"Unreachable(" + e.getClass().getSimpleName() + ")");
        }
    }
    private static long elapsedMs(long start){
        return (System.nanoTime() - start)/1_000_000;
    }
    protected static String stripTailingSlash(String s){
        return s!=null&&s.endsWith("/")?s.substring(0,s.length()-1):s;
    }
}
