package org.qatlas.client.handler;

import org.qatlas.client.handler.ApiClient;

import org.qatlas.client.model.ExecutionStatus;
import org.qatlas.client.model.TestCase;
import org.qatlas.client.model.TestSuite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-06-02T18:40:14.833+02:00[Europe/Berlin]")@Component("org.qatlas.client.handler.TestSuiteApi")
public class TestSuiteApi {
    private ApiClient apiClient;

    public TestSuiteApi() {
        this(new ApiClient());
    }

    @Autowired
    public TestSuiteApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Create a test suite
     * 
     * <p><b>201</b> - Created
     * @param body The body parameter
     * @return TestSuite
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TestSuite createTestSuite(TestSuite body) throws RestClientException {
        Object postBody = body;
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'body' when calling createTestSuite");
        }
        String path = UriComponentsBuilder.fromPath("/rs/test-suite").build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] accepts = { 
            "*/*"
         };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = { 
            "application/json"
         };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        String[] authNames = new String[] {  };

        ParameterizedTypeReference<TestSuite> returnType = new ParameterizedTypeReference<TestSuite>() {};
        return apiClient.invokeAPI(path, HttpMethod.POST, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Get list of test suites
     * 
     * <p><b>200</b> - OK
     * @return List&lt;TestSuite&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<TestSuite> getAllTestSuites() throws RestClientException {
        Object postBody = null;
        String path = UriComponentsBuilder.fromPath("/rs/test-suite").build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] accepts = { 
            "*/*"
         };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = {  };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        String[] authNames = new String[] {  };

        ParameterizedTypeReference<List<TestSuite>> returnType = new ParameterizedTypeReference<List<TestSuite>>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Get a test suites by ID
     * 
     * <p><b>200</b> - OK
     * @param id The id parameter
     * @return TestSuite
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TestSuite getTestSuiteById(Long id) throws RestClientException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'id' when calling getTestSuiteById");
        }
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("id", id);
        String path = UriComponentsBuilder.fromPath("/rs/test-suite/{id}").buildAndExpand(uriVariables).toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] accepts = { 
            "*/*"
         };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = {  };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        String[] authNames = new String[] {  };

        ParameterizedTypeReference<TestSuite> returnType = new ParameterizedTypeReference<TestSuite>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Get list of test cases of a test suite filtered by execution status
     * 
     * <p><b>200</b> - OK
     * @param testSuiteId The testSuiteId parameter
     * @param status The status parameter
     * @return List&lt;TestCase&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<TestCase> getTestSuiteTestCases(Long testSuiteId, List<ExecutionStatus> status) throws RestClientException {
        Object postBody = null;
        // verify the required parameter 'testSuiteId' is set
        if (testSuiteId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'testSuiteId' when calling getTestSuiteTestCases");
        }
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("testSuiteId", testSuiteId);
        String path = UriComponentsBuilder.fromPath("/rs/test-suite/{testSuiteId}/test-cases").buildAndExpand(uriVariables).toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase()), "status", status));

        final String[] accepts = { 
            "*/*"
         };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = {  };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        String[] authNames = new String[] {  };

        ParameterizedTypeReference<List<TestCase>> returnType = new ParameterizedTypeReference<List<TestCase>>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Update a test suite
     * 
     * <p><b>200</b> - OK
     * @param body The body parameter
     * @return TestSuite
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TestSuite updateTestSuite(TestSuite body) throws RestClientException {
        Object postBody = body;
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'body' when calling updateTestSuite");
        }
        String path = UriComponentsBuilder.fromPath("/rs/test-suite").build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] accepts = { 
            "*/*"
         };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = { 
            "application/json"
         };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        String[] authNames = new String[] {  };

        ParameterizedTypeReference<TestSuite> returnType = new ParameterizedTypeReference<TestSuite>() {};
        return apiClient.invokeAPI(path, HttpMethod.PUT, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
}
