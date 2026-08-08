package com.akarsha.tenant;

import com.akarsha.core.entity.Salon;
import com.akarsha.core.repository.SalonRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class PublicTenantFilter extends OncePerRequestFilter {

    private final SalonRepository salonRepository;
    private static final Pattern PUBLIC_BOOKING_PATTERN = Pattern.compile("^/api/v1/public/booking/([^/]+)(/.*)?$");

    public PublicTenantFilter(SalonRepository salonRepository) {
        this.salonRepository = salonRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        Matcher matcher = PUBLIC_BOOKING_PATTERN.matcher(path);

        if (matcher.matches()) {
            String salonSlug = matcher.group(1);
            
            // Bypass TenantFilter for the salon lookup since Salon is TenantAware (wait, is Salon TenantAware? Let's check. 
            // Actually Salon uses subdomain. We must ensure we can look it up.)
            TenantContext.setCurrentTenant("SYSTEM_BYPASS");
            
            Optional<Salon> salonOpt = salonRepository.findBySubdomain(salonSlug);
            if (salonOpt.isPresent()) {
                TenantContext.setCurrentTenant(salonOpt.get().getSubdomain());
            } else {
                TenantContext.clear();
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Salon not found");
                return;
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (matcher.matches()) {
                TenantContext.clear();
            }
        }
    }
}
