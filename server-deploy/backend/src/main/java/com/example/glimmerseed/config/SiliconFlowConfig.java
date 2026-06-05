package com.example.glimmerseed.config;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Configuration
public class SiliconFlowConfig {
    @Value("${siliconflow.base-url}")
    private String baseUrl;
    
    @Bean
    public OkHttpClient okHttpClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        
        return new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
    }
    
    @Bean
    public Retrofit retrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
    
    @Bean
    public SiliconFlowApi siliconFlowApi(Retrofit retrofit) {
        return retrofit.create(SiliconFlowApi.class);
    }
}