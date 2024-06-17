package com.chargev.emsp.utilities;

import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HttpGetWithBody {
    private HttpGetWithBody() {
    }

    public static String get(String url, String body) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("Content-Type", "application/json");
            httpGet.addHeader("Accept", "*/*");
            httpGet.setEntity(new org.apache.hc.core5.http.io.entity.StringEntity(body));
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity);
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static String delete(String url, String body) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpDelete httpDelete = new HttpDelete(url);
            httpDelete.addHeader("Content-Type", "application/json");
            httpDelete.addHeader("Accept", "*/*");
            httpDelete.setEntity(new org.apache.hc.core5.http.io.entity.StringEntity(body));
            try (CloseableHttpResponse response = httpclient.execute(httpDelete)) {
                HttpEntity entity = response.getEntity();
                return EntityUtils.toString(entity);
            }
        } catch (Exception e) {
        }
        return null;

    }
    
}
