package org.onap.ccsdk.cds.blueprintsprocessor.healthapi.configuration

import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.HttpClient
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.HttpClientBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@Configuration
open class RestTemplateConfiguration {

    open val restTemplate: RestTemplate
        @Bean
        get() = RestTemplate(clientHttpRequestFactory)

    private val clientHttpRequestFactory: HttpComponentsClientHttpRequestFactory
        get() {
            val clientHttpRequestFactory = HttpComponentsClientHttpRequestFactory()

            clientHttpRequestFactory.httpClient = getHttpClient()

            return clientHttpRequestFactory
        }

    private fun getHttpClient(): HttpClient {
        val credentialsProvider = BasicCredentialsProvider()
        credentialsProvider.setCredentials(AuthScope.ANY,
                UsernamePasswordCredentials("ccsdkapps", "ccsdkapps"))
        return HttpClientBuilder
                .create()
                .setDefaultCredentialsProvider(credentialsProvider)
                .build()
    }
}
