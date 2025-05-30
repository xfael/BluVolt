package Bluvolt.bluvolt.service;


import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;


@Service
public class CookieService {

    public static void setCookie(HttpServletResponse response, String Key, String valor, int segundos) throws UnsupportedEncodingException {
        Cookie cookie = new Cookie(Key, URLEncoder.encode(valor,"UTF-8"));
        cookie.setMaxAge(segundos);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public static String getCookie(HttpServletRequest request, String key) throws UnsupportedEncodingException {
        String valor = Optional.ofNullable(request.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(cookie -> key.equals(cookie.getName()))
                        .findAny())
                .map(Cookie::getValue)
                .orElse(null);

        if (valor != null) {
            valor = URLDecoder.decode(valor, "UTF-8");
        }

        return valor;
    }

}