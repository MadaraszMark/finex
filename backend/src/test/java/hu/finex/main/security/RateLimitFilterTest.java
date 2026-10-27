package hu.finex.main.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import hu.finex.main.config.FinexProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitFilterTest {

    private RateLimitFilter filter;

    @BeforeEach
    void setUp() {
        FinexProperties properties = new FinexProperties();
        properties.setRateLimitLoginPerMinute(3);
        properties.setRateLimitRegisterPerHour(2);
        filter = new RateLimitFilter(properties, new ObjectMapper().findAndRegisterModules());
    }

    @Test
    void login_shouldBeLimitedPerIp() throws Exception {
        for (int i = 0; i < 3; i++) {
            assertEquals(200, send("POST", "/auth/login", "10.0.0.1").getStatus());
        }

        MockHttpServletResponse blocked = send("POST", "/auth/login", "10.0.0.1");
        assertEquals(429, blocked.getStatus());
        assertEquals("60", blocked.getHeader("Retry-After"));
        assertTrue(blocked.getContentAsString(java.nio.charset.StandardCharsets.UTF_8).contains("Túl sok bejelentkezési kérés"));

        // Másik IP-címről továbbra is lehet
        assertEquals(200, send("POST", "/auth/login", "10.0.0.2").getStatus());
    }

    @Test
    void register_shouldBeLimitedPerIp() throws Exception {
        assertEquals(200, send("POST", "/auth/register", "10.0.0.1").getStatus());
        assertEquals(200, send("POST", "/auth/register", "10.0.0.1").getStatus());
        assertEquals(429, send("POST", "/auth/register", "10.0.0.1").getStatus());
    }

    @Test
    void otherEndpoints_shouldNotBeLimited() throws Exception {
        for (int i = 0; i < 10; i++) {
            assertEquals(200, send("GET", "/accounts", "10.0.0.1").getStatus());
        }
    }

    @Test
    void tooLargeRequest_shouldBeRejected() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/support-tickets");
        request.setRemoteAddr("10.0.0.1");
        request.setContent(new byte[(int) RateLimitFilter.MAX_REQUEST_BYTES + 1]);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(413, response.getStatus());
    }

    private MockHttpServletResponse send(String method, String path, String ip) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRemoteAddr(ip);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
