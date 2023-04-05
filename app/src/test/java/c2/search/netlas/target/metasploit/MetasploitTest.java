package c2.search.netlas.target.metasploit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import c2.search.netlas.scheme.Host;
import c2.search.netlas.scheme.Response;
import c2.search.netlas.target.NetlasWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MetasploitTest {
  @Mock private Host host;
  @Mock private NetlasWrapper netlasWrapper;
  @Mock private SocketConnection socketConnection;
  @InjectMocks private Metasploit metasploit;

  @BeforeEach
  public void setUp() throws IOException {
    metasploit.init();
    metasploit.setHost(host);
    metasploit.setNetlasWrapper(netlasWrapper);
    metasploit.setSocketConnection(socketConnection);
  }

  @Test
  @DisplayName("Test checkDefaultBodyResponse() with default body")
  public void testCheckDefaultBodyResponseWithDefaultBody() throws IOException {
    String defaultBody = "<html><body><h1>It works!</h1></body></html>";
    when(netlasWrapper.getBody()).thenReturn(defaultBody);
    Response response = metasploit.checkDefaultBodyResponse();
    assertNotNull(response);
    assertTrue(response.isSuccess());
  }

  @Test
  @DisplayName("Test checkDefaultBodyResponse() with non-default body")
  public void testCheckDefaultBodyResponseWithNonDefaultBody() throws IOException {
    String nonDefaultBody = "<html><body><h1>It doesn't work!</h1></body></html>";
    when(netlasWrapper.getBody()).thenReturn(nonDefaultBody);
    Response response = metasploit.checkDefaultBodyResponse();
    assertNotNull(response);
    assertFalse(response.isSuccess());
  }

  @Test
  @DisplayName("Test checkJarm() with JARM version 5")
  public void testCheckJarmWithJarmV5() throws IOException {
    String jarmv5 = "07d14d16d21d21d07c42d43d000000f50d155305214cf247147c43c0f1a823";
    when(netlasWrapper.getJarm()).thenReturn(jarmv5);
    Response response = metasploit.checkJarm();
    assertNotNull(response);
    assertTrue(response.isSuccess());
  }

  @Test
  @DisplayName("Test checkJarm() with JARM version 6")
  public void testCheckJarmWithJarmV6() throws IOException {
    String jarmv6 = "07d19d12d21d21d07c42d43d000000f50d155305214cf247147c43c0f1a823";
    when(netlasWrapper.getJarm()).thenReturn(jarmv6);
    Response response = metasploit.checkJarm();
    assertNotNull(response);
    assertTrue(response.isSuccess());
  }

  @Test
  public void testCheckHeaders() throws JsonMappingException, JsonProcessingException {
    List<String> defaultServers = List.of("apache");
    when(netlasWrapper.getServers()).thenReturn(defaultServers);
    when(netlasWrapper.getStatusCode()).thenReturn(200);
    Response response = metasploit.checkHeaders();
    assertNotNull(response);
    assertTrue(response.isSuccess());
  }

  @Test
  public void testCheckBindShell() throws IOException {
    Host mockHost = new Host("localhost", 8080);
    boolean testPassed = true;

    when(socketConnection.sendAndReceive()).thenReturn("i_am_a_shell");
    Response expectedResponse = new Response(true);
    Response result = new Response(true);
    when(socketConnection.sendAndReceive()).thenReturn("i_am_a_shell");

    Metasploit metasploit = new Metasploit();
    metasploit.setHost(mockHost);
    metasploit.setNetlasWrapper(netlasWrapper);
    metasploit.setSocketConnection(socketConnection);

    try {
      result = metasploit.checkBindShell();
    } catch (IOException e) {
      testPassed = false;
    }

    assertEquals(expectedResponse.isSuccess(), result.isSuccess());
    assertTrue(testPassed);
  }
}
