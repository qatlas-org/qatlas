package org.qatlas.client.handler;

import org.qatlas.client.handler.ApiClient;

import org.qatlas.client.model.ExecutionStatus;
import java.time.LocalDateTime;
import org.qatlas.client.model.TestCase;
import org.qatlas.client.model.TestStep;

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

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-06-02T18:40:14.833+02:00[Europe/Berlin]")@Component("org.qatlas.client.handler.TestCaseApi")
public class TestCaseApi {
    private ApiClient apiClient;

    public TestCaseApi() {
        this(new ApiClient());
    }

    @Autowired
    public TestCaseApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Create a test case
     * 
     * <p><b>201</b> - Created
     * @param body The body parameter
     * @return TestCase
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TestCase createTestCase(TestCase body) throws RestClientException {
        Object postBody = body;
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'body' when calling createTestCase");
        }
        String path = UriComponentsBuilder.fromPath("/rs/test-case").build().toUriString();
        
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

        ParameterizedTypeReference<TestCase> returnType = new ParameterizedTypeReference<TestCase>() {};
        return apiClient.invokeAPI(path, HttpMethod.POST, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Get comments of a test case
     * 
     * <p><b>200</b> - OK
     * @param id The id parameter
     * @return String
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public String getComments(Long id) throws RestClientException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'id' when calling getComments");
        }
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("id", id);
        String path = UriComponentsBuilder.fromPath("/rs/test-case/{id}/comments").buildAndExpand(uriVariables).toUriString();
        
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

        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Get a test cases by it&#x27;s ID
     * 
     * <p><b>200</b> - OK
     * @param id The id parameter
     * @return TestCase
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TestCase getTestCaseById(Long id) throws RestClientException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'id' when calling getTestCaseById");
        }
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("id", id);
        String path = UriComponentsBuilder.fromPath("/rs/test-case/{id}").buildAndExpand(uriVariables).toUriString();
        
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

        ParameterizedTypeReference<TestCase> returnType = new ParameterizedTypeReference<TestCase>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Get the list of Test Steps of a Test Case
     * 
     * <p><b>200</b> - OK
     * @param testCaseId The testCaseId parameter
     * @return List&lt;TestStep&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<TestStep> getTestCaseTestSteps(Long testCaseId) throws RestClientException {
        Object postBody = null;
        // verify the required parameter 'testCaseId' is set
        if (testCaseId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'testCaseId' when calling getTestCaseTestSteps");
        }
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("testCaseId", testCaseId);
        String path = UriComponentsBuilder.fromPath("/rs/test-case/{testCaseId}/test-steps").buildAndExpand(uriVariables).toUriString();
        
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

        ParameterizedTypeReference<List<TestStep>> returnType = new ParameterizedTypeReference<List<TestStep>>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Get list of all Test Cases
     * 
     * <p><b>200</b> - OK
     * @return List&lt;TestCase&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<TestCase> getTestCases() throws RestClientException {
        Object postBody = null;
        String path = UriComponentsBuilder.fromPath("/rs/test-case").build().toUriString();
        
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

        ParameterizedTypeReference<List<TestCase>> returnType = new ParameterizedTypeReference<List<TestCase>>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Update comments of a test case
     * 
     * <p><b>200</b> - OK
     * @param body The body parameter
     * @param id The id parameter
     * @return String
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public String updateComments(String body, Long id) throws RestClientException {
        Object postBody = body;
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'body' when calling updateComments");
        }
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'id' when calling updateComments");
        }
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("id", id);
        String path = UriComponentsBuilder.fromPath("/rs/test-case/{id}/comments").buildAndExpand(uriVariables).toUriString();
        
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

        ParameterizedTypeReference<String> returnType = new ParameterizedTypeReference<String>() {};
        return apiClient.invokeAPI(path, HttpMethod.PUT, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Update a test case
     * 
     * <p><b>200</b> - OK
     * @param body The body parameter
     * @return TestCase
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TestCase updateTestCase(TestCase body) throws RestClientException {
        Object postBody = body;
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'body' when calling updateTestCase");
        }
        String path = UriComponentsBuilder.fromPath("/rs/test-case").build().toUriString();
        
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

        ParameterizedTypeReference<TestCase> returnType = new ParameterizedTypeReference<TestCase>() {};
        return apiClient.invokeAPI(path, HttpMethod.PUT, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Update execution status of a test case
     * 
     * <p><b>200</b> - OK
     * @param id The id parameter
     * @param executionStatus The executionStatus parameter
     * @return TestCase
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TestCase updateTestCaseStatus(Long id, ExecutionStatus executionStatus) throws RestClientException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'id' when calling updateTestCaseStatus");
        }
        // verify the required parameter 'executionStatus' is set
        if (executionStatus == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'executionStatus' when calling updateTestCaseStatus");
        }
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("id", id);
        uriVariables.put("executionStatus", executionStatus);
        String path = UriComponentsBuilder.fromPath("/rs/test-case/{id}/status/{executionStatus}").buildAndExpand(uriVariables).toUriString();
        
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

        ParameterizedTypeReference<TestCase> returnType = new ParameterizedTypeReference<TestCase>() {};
        return apiClient.invokeAPI(path, HttpMethod.PUT, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Update execution status and end time of a test case
     * 
     * <p><b>200</b> - OK
     * @param id The id parameter
     * @param executionStatus The executionStatus parameter
     * @param executionEndTime The executionEndTime parameter
     * @return TestCase
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TestCase updateTestCaseStatusAndExecutionEndTime(Long id, ExecutionStatus executionStatus, LocalDateTime executionEndTime) throws RestClientException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'id' when calling updateTestCaseStatusAndExecutionEndTime");
        }
        // verify the required parameter 'executionStatus' is set
        if (executionStatus == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'executionStatus' when calling updateTestCaseStatusAndExecutionEndTime");
        }
        // verify the required parameter 'executionEndTime' is set
        if (executionEndTime == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'executionEndTime' when calling updateTestCaseStatusAndExecutionEndTime");
        }
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("id", id);
        uriVariables.put("executionStatus", executionStatus);
        uriVariables.put("executionEndTime", executionEndTime);
        String path = UriComponentsBuilder.fromPath("/rs/test-case/{id}/status/{executionStatus}/endTime/{executionEndTime}").buildAndExpand(uriVariables).toUriString();
        
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

        ParameterizedTypeReference<TestCase> returnType = new ParameterizedTypeReference<TestCase>() {};
        return apiClient.invokeAPI(path, HttpMethod.PUT, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Update execution status and start time of a test case
     * 
     * <p><b>200</b> - OK
     * @param id The id parameter
     * @param executionStatus The executionStatus parameter
     * @param executionStartTime The executionStartTime parameter
     * @return TestCase
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TestCase updateTestCaseStatusAndStartTime(Long id, ExecutionStatus executionStatus, LocalDateTime executionStartTime) throws RestClientException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'id' when calling updateTestCaseStatusAndStartTime");
        }
        // verify the required parameter 'executionStatus' is set
        if (executionStatus == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'executionStatus' when calling updateTestCaseStatusAndStartTime");
        }
        // verify the required parameter 'executionStartTime' is set
        if (executionStartTime == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'executionStartTime' when calling updateTestCaseStatusAndStartTime");
        }
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("id", id);
        uriVariables.put("executionStatus", executionStatus);
        uriVariables.put("executionStartTime", executionStartTime);
        String path = UriComponentsBuilder.fromPath("/rs/test-case/{id}/status/{executionStatus}/startTime/{executionStartTime}").buildAndExpand(uriVariables).toUriString();
        
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

        ParameterizedTypeReference<TestCase> returnType = new ParameterizedTypeReference<TestCase>() {};
        return apiClient.invokeAPI(path, HttpMethod.PUT, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
}
