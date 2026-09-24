package com.Harsh.Productivity.Os;
import com.Harsh.Productivity.Os.entity.User;
import com.Harsh.Productivity.Os.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;
@DataJpaTest
@ActiveProfiles("test")
public class TenantIsolationTest {
    @Autowired
    private UserRepository userRepository;
    @Test
    public void testUserHasTenantId() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("hashed");
        user.setTenantId("tenant-123");
        User saved = userRepository.save(user);
        assertNotNull(saved.getTenantId());
        assertEquals("tenant-123", saved.getTenantId());
    }
    @Test
    public void testDefaultTenantIdAssigned() {
        User user = new User();
        user.setName("Another User");
        user.setEmail("another@example.com");
        user.setPassword("hashed");
        // Don't set tenantId; it should default
        User saved = userRepository.save(user);
        assertEquals("default-tenant", saved.getTenantId());
    }
}