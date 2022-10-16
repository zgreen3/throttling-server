package ecom.market.throttlingserver.utils;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

public class HttpUtils {
    //NOTE: for implementation variants see:
    // https://stackoverflow.com/a/58682577 ,
    // https://stackoverflow.com/questions/1979419/how-to-get-client-ip-address-in-spring-bean ,
    // https://stackoverflow.com/questions/46611579/obtain-ip-address-from-clients-http-request-using-java ,
    // https://dirask.com/posts/Spring-Boot-get-client-IP-address-from-request-HttpServletRequest-pBv9Bp
    private static final String[] IP_HEADER_NAMES = {
            "REMOTE_ADDR",
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA"
    };

    public static String getRemoteIP(HttpServletRequest request) {
        if (request == null) {
            return "0.0.0.0";
        }
        String ip = Arrays.asList(IP_HEADER_NAMES)
                .stream()
                .map(request::getHeader)
                .filter(h -> h != null && h.length() != 0 && !"unknown".equalsIgnoreCase(h))
                .map(h -> h.split("\\s*,\\s*")[0])
                .reduce("", (h1, h2) -> h1 + ":" + h2);
        return ip != "" ? ip : request.getRemoteAddr();
    }

}
