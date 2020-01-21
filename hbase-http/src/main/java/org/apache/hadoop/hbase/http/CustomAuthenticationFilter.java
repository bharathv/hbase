package org.apache.hadoop.hbase.http;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.hadoop.hbase.util.PrettyPrinter;
import org.apache.hadoop.security.authentication.server.AuthenticationFilter;
import org.apache.yetus.audience.InterfaceAudience;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An extension of AuthenticationFilter that has the ability to bypass authentication for certain
 * configured end points.
 */
@InterfaceAudience.Private
public class CustomAuthenticationFilter extends AuthenticationFilter {
  private static final Logger LOG = LoggerFactory.getLogger(CustomAuthenticationFilter.class);
  // init looks for csv for this key.
  public static final String AUTH_EXCLUDED_URIS = "hbase.auth.filter.excluded.uris";

  private final Set<String> excludeURIs = new HashSet<>();

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    super.init(filterConfig);
    String excludedURIs = filterConfig.getInitParameter(AUTH_EXCLUDED_URIS);
    if (excludedURIs != null) {
      this.excludeURIs.addAll(Arrays.asList(excludedURIs.split(",")));
    }
    LOG.info("SPNEGO auth bypassed for end points: " + PrettyPrinter.toString(excludeURIs));
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
      throws IOException, ServletException {
    HttpServletRequest httpRequest = (HttpServletRequest)request;
    if (excludeURIs.contains(httpRequest.getRequestURI())) {
      // Auth bypassed, continue with rest of the filter chain.
      filterChain.doFilter(request, response);
      return;
    }
    // Pass it along to the parent implementation since this end point is not configured to bypass.
    super.doFilter(request, response, filterChain);
  }
}
