package c2.search.netlas.target.metasploit;

import c2.search.netlas.annotation.Detect;
import c2.search.netlas.annotation.Test;
import c2.search.netlas.annotation.Wire;
import c2.search.netlas.scheme.Host;
import c2.search.netlas.scheme.Response;
import c2.search.netlas.scheme.Version;
import c2.search.netlas.target.NetlasWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

@Detect(name = "Metasploit")
public class Metasploit {
  @Wire protected Host host;
  @Wire protected NetlasWrapper netlasWrapper;

  @Test
  public Response checkDefaultBodyResponse() throws JsonMappingException, JsonProcessingException {
    String body = "";
    body = netlasWrapper.getBody();
    String defaultBody = "It works!";
    String defaultTagPayload = "echo";
    return new Response(body.contains(defaultBody) || body.contains(defaultTagPayload));
  }

  private boolean checkJarm(String body, List<String> jarms) {
    for (String jarm : jarms) {
      if (body.contains(jarm)) {
        return true;
      }
    }
    return false;
  }

  @Test
  public Response checkJarm() throws JsonMappingException, JsonProcessingException {
    List<String> jarmv5 = List.of("07d14d16d21d21d07c42d43d000000f50d155305214cf247147c43c0f1a823");
    List<String> jarmv6 =
        List.of(
            "07d19d12d21d21d07c42d43d000000f50d155305214cf247147c43c0f1a823",
            "07b03b12b21b21b07b07b03b07b21b23aeefb38b723c523befb314af6e95ac",
            "07c03c12c21c21c07c07c03c07c21c23aeefb38b723c523befb314af6e95ac",
            "07d19d12d21d21d00007d19d07d21d0ae59125bcd90b8876b50928af8f6cd4");

    String responseJarm = "";
    responseJarm = netlasWrapper.getJarm();

    String minVersion = null;
    boolean detect = false;
    if (checkJarm(responseJarm, jarmv5)) {
      minVersion = "5.x.x";
      detect = true;
    }
    if (checkJarm(responseJarm, jarmv6)) {
      minVersion = "6.x.x";
      detect = true;
    }
    return new Response(detect, new Version(null, minVersion));
  }

  @Test
  public Response checkHeaders() throws JsonMappingException, JsonProcessingException {
    List<String> servers = netlasWrapper.getServers();
    String defaultServer = "apache";
    boolean checkDefaultServer = false;
    for (String server : servers) {
      if (server.toLowerCase().contains(defaultServer)) {
        checkDefaultServer = true;
        break;
      }
    }

    int defaultStatus = 200;
    int status = 0;
    status = netlasWrapper.getStatusCode();
    boolean checkDefaultStatus = defaultStatus == status;

    return new Response(checkDefaultServer && checkDefaultStatus);
  }

  @Test
  public Response checkBindShell() {
    String id = "i_am_a_shell";
    String responseMessage = "";
    try {
      Socket socket = new Socket(host.getTarget(), host.getPort());

      OutputStream output = socket.getOutputStream();
      InputStream input = socket.getInputStream();

      String message = "echo " + id;
      output.write(message.getBytes());
      output.flush();

      byte[] response = new byte[1024];
      int responseLength = input.read(response);
      responseMessage = new String(response, 0, responseLength);

      socket.close();
    } catch (IOException e) {
      return new Response(false);
    }

    return new Response(responseMessage.trim().equals(id));
  }
}
