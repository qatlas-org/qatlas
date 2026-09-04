package org.qatlas.client.handler;

import org.qatlas.client.handler.ApiClient;

import org.qatlas.client.model.ExecutionStatus;
import java.time.LocalDateTime;
import org.qatlas.client.model.StreamingResponseBody;
import org.qatlas.client.model.TestCase;
import org.qatlas.client.model.TestExecution;
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

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaClientCodegen", date = "2021-06-02T18:40:14.833+02:00[Europe/Berlin]")@Component("org.qatlas.client.handler.TestExecutionApi")
public class TestExecutionApi {
    private ApiClient apiClient;

    public TestExecutionApi() {
        this(new ApiClient());
    }

    @Autowired
    public TestExecutionApi(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * Archives the test execution(s)
     * 
     * <p><b>200</b> - OK
     * @param body The body parameter
     * @param deleteAttachmentsOnly The deleteAttachmentsOnly parameter
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public void archiveTestExecution(List<Long> body, Boolean deleteAttachmentsOnly) throws RestClientException {
        Object postBody = body;
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'body' when calling archiveTestExecution");
        }
        // verify the required parameter 'deleteAttachmentsOnly' is set
        if (deleteAttachmentsOnly == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'deleteAttachmentsOnly' when calling archiveTestExecution");
        }
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("deleteAttachmentsOnly", deleteAttachmentsOnly);
        String path = UriComponentsBuilder.fromPath("/rs/test-execution/archive/{deleteAttachmentsOnly}").buildAndExpand(uriVariables).toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();

        final String[] accepts = {  };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = { 
            "application/json"
         };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        String[] authNames = new String[] {  };

        ParameterizedTypeReference<Void> returnType = new ParameterizedTypeReference<Void>() {};
        apiClient.invokeAPI(path, HttpMethod.PUT, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Create a test execution
     * 
     * <p><b>201</b> - Created
     * @param body The body parameter
     * @return TestExecution
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TestExecution createTestExecution(TestExecution body) throws RestClientException {
        Object postBody = body;
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'body' when calling createTestExecution");
        }
        String path = UriComponentsBuilder.fromPath("/rs/test-execution").build().toUriString();
        
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

        ParameterizedTypeReference<TestExecution> returnType = new ParameterizedTypeReference<TestExecution>() {};
        return apiClient.invokeAPI(path, HttpMethod.POST, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Download Test Step Attachment(s) of test execution(s)
     * 
     * <p><b>200</b> - OK
     * @param executionId The executionId parameter
     * @return StreamingResponseBody
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public StreamingResponseBody downloadAttachments(List<Long> executionId) throws RestClientException {
        Object postBody = null;
        // verify the required parameter 'executionId' is set
        if (executionId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'executionId' when calling downloadAttachments");
        }
        String path = UriComponentsBuilder.fromPath("/rs/test-execution/download-attachments").build().toUriString();
        
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders headerParams = new HttpHeaders();
        final MultiValueMap<String, Object> formParams = new LinkedMultiValueMap<String, Object>();
        queryParams.putAll(apiClient.parameterToMultiValueMap(ApiClient.CollectionFormat.valueOf("multi".toUpperCase()), "executionId", executionId));

        final String[] accepts = { 
            "application/octet-stream"
         };
        final List<MediaType> accept = apiClient.selectHeaderAccept(accepts);
        final String[] contentTypes = {  };
        final MediaType contentType = apiClient.selectHeaderContentType(contentTypes);

        String[] authNames = new String[] {  };

        ParameterizedTypeReference<StreamingResponseBody> returnType = new ParameterizedTypeReference<StreamingResponseBody>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Get list of all test executions
     * 
     * <p><b>200</b> - OK
     * @return List&lt;TestExecution&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<TestExecution> getAllTestExecutions() throws RestClientException {
        Object postBody = null;
        String path = UriComponentsBuilder.fromPath("/rs/test-execution").build().toUriString();
        
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

        ParameterizedTypeReference<List<TestExecution>> returnType = new ParameterizedTypeReference<List<TestExecution>>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Get a test execution by ID
     * 
     * <p><b>200</b> - OK
     * @param id The id parameter
     * @return TestExecution
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TestExecution getTestExecutionById(Long id) throws RestClientException {
        Object postBody = null;
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'id' when calling getTestExecutionById");
        }
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("id", id);
        String path = UriComponentsBuilder.fromPath("/rs/test-execution/{id}").buildAndExpand(uriVariables).toUriString();
        
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

        ParameterizedTypeReference<TestExecution> returnType = new ParameterizedTypeReference<TestExecution>() {};
        return apiClient.invokeAPI(path, HttpMethod.GET, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
    /**
     * Get list of test cases of a test execution filtered by execution status
     * 
     * <p><b>200</b> - OK
     * @param testExecutionId The testExecutionId parameter
     * @param status The status parameter
     * @return List&lt;TestCase&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<TestCase> getTestExecutionTestCases(Long testExecutionId, List<ExecutionStatus> status) throws RestClientException {
        Object postBody = null;
        // verify the required parameter 'testExecutionId' is set
        if (testExecutionId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'testExecutionId' when calling getTestExecutionTestCases");
        }
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("testExecutionId", testExecutionId);
        String path = UriComponentsBuilder.fromPath("/rs/test-execution/{testExecutionId}/test-cases").buildAndExpand(uriVariables).toUriString();
        
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
     * Get list of all test suites of an execution
     * 
     * <p><b>200</b> - OK
     * @param executionId The executionId parameter
     * @return List&lt;TestSuite&gt;
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public List<TestSuite> getTestSuites(Long executionId) throws RestClientException {
        Object postBody = null;
        // verify the required parameter 'executionId' is set
        if (executionId == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'executionId' when calling getTestSuites");
        }
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("executionId", executionId);
        String path = UriComponentsBuilder.fromPath("/rs/test-execution/{executionId}/test-suites").buildAndExpand(uriVariables).toUriString();
        
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
     * Update a test execution end time
     * 
     * <p><b>200</b> - OK
     * @param body The body parameter
     * @param id The id parameter
     * @return TestExecution
     * @throws RestClientException if an error occurs while attempting to invoke the API
     */
    public TestExecution updateTestExecutionEndTime(LocalDateTime body, Long id) throws RestClientException {
        Object postBody = body;
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'body' when calling updateTestExecutionEndTime");
        }
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Missing the required parameter 'id' when calling updateTestExecutionEndTime");
        }
        // create path and map variables
        final Map<String, Object> uriVariables = new HashMap<String, Object>();
        uriVariables.put("id", id);
        String path = UriComponentsBuilder.fromPath("/rs/test-execution/{id}").buildAndExpand(uriVariables).toUriString();
        
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

        ParameterizedTypeReference<TestExecution> returnType = new ParameterizedTypeReference<TestExecution>() {};
        return apiClient.invokeAPI(path, HttpMethod.PUT, queryParams, postBody, headerParams, formParams, accept, contentType, authNames, returnType);
    }
}
