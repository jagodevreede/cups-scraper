package org.example;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class CupsQueueParser {
    private final static Pattern tbodyPattern = Pattern.compile("(?i)<tbody[^>]*>(.*?)</tbody>", Pattern.DOTALL);
    private final static Pattern trPattern = Pattern.compile("(?i)<tr\\b[^>]*>");

    public String fetchCupsJobsHtml(String target) throws Exception {

        // Create all-trusting SSL context (insecure!)
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Disable hostname verification
        HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        URL url = new URL(target);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }

    public int countJobsInQueue(String html) {
        Matcher tbodyMatcher = tbodyPattern.matcher(html);
        String tbodyContent;
        if (tbodyMatcher.find()) {
            tbodyContent = tbodyMatcher.group(1);
        } else {
            tbodyContent = html;
        }

        Matcher trMatcher = trPattern.matcher(tbodyContent);

        int count = 0;
        while (trMatcher.find()) {
            count++;
        }

        return count;
    }
}
