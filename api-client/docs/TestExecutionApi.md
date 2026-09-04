# TestExecutionApi

All URIs are relative to *http://localhost:8080*

Method | HTTP request | Description
------------- | ------------- | -------------
[**archiveTestExecution**](TestExecutionApi.md#archiveTestExecution) | **PUT** /rs/test-execution/archive/{deleteAttachmentsOnly} | Archives the test execution(s)
[**createTestExecution**](TestExecutionApi.md#createTestExecution) | **POST** /rs/test-execution | Create a test execution
[**downloadAttachments**](TestExecutionApi.md#downloadAttachments) | **GET** /rs/test-execution/download-attachments | Download Test Step Attachment(s) of test execution(s)
[**getAllTestExecutions**](TestExecutionApi.md#getAllTestExecutions) | **GET** /rs/test-execution | Get list of all test executions
[**getTestExecutionById**](TestExecutionApi.md#getTestExecutionById) | **GET** /rs/test-execution/{id} | Get a test execution by ID
[**getTestExecutionTestCases**](TestExecutionApi.md#getTestExecutionTestCases) | **GET** /rs/test-execution/{testExecutionId}/test-cases | Get list of test cases of a test execution filtered by execution status
[**getTestSuites**](TestExecutionApi.md#getTestSuites) | **GET** /rs/test-execution/{executionId}/test-suites | Get list of all test suites of an execution
[**updateTestExecutionEndTime**](TestExecutionApi.md#updateTestExecutionEndTime) | **PUT** /rs/test-execution/{id} | Update a test execution end time

<a name="archiveTestExecution"></a>
# **archiveTestExecution**
> archiveTestExecution(body, deleteAttachmentsOnly)

Archives the test execution(s)

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestExecutionApi;


TestExecutionApi apiInstance = new TestExecutionApi();
List<Long> body = Arrays.asList(56L); // List<Long> | 
Boolean deleteAttachmentsOnly = true; // Boolean | 
try {
    apiInstance.archiveTestExecution(body, deleteAttachmentsOnly);
} catch (ApiException e) {
    System.err.println("Exception when calling TestExecutionApi#archiveTestExecution");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**List&lt;Long&gt;**](Long.md)|  |
 **deleteAttachmentsOnly** | **Boolean**|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined

<a name="createTestExecution"></a>
# **createTestExecution**
> TestExecution createTestExecution(body)

Create a test execution

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestExecutionApi;


TestExecutionApi apiInstance = new TestExecutionApi();
TestExecution body = new TestExecution(); // TestExecution | 
try {
    TestExecution result = apiInstance.createTestExecution(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestExecutionApi#createTestExecution");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**TestExecution**](TestExecution.md)|  |

### Return type

[**TestExecution**](TestExecution.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="downloadAttachments"></a>
# **downloadAttachments**
> StreamingResponseBody downloadAttachments(executionId)

Download Test Step Attachment(s) of test execution(s)

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestExecutionApi;


TestExecutionApi apiInstance = new TestExecutionApi();
List<Long> executionId = Arrays.asList(56L); // List<Long> | 
try {
    StreamingResponseBody result = apiInstance.downloadAttachments(executionId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestExecutionApi#downloadAttachments");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **executionId** | [**List&lt;Long&gt;**](Long.md)|  |

### Return type

[**StreamingResponseBody**](StreamingResponseBody.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/octet-stream

<a name="getAllTestExecutions"></a>
# **getAllTestExecutions**
> List&lt;TestExecution&gt; getAllTestExecutions()

Get list of all test executions

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestExecutionApi;


TestExecutionApi apiInstance = new TestExecutionApi();
try {
    List<TestExecution> result = apiInstance.getAllTestExecutions();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestExecutionApi#getAllTestExecutions");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List&lt;TestExecution&gt;**](TestExecution.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

<a name="getTestExecutionById"></a>
# **getTestExecutionById**
> TestExecution getTestExecutionById(id)

Get a test execution by ID

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestExecutionApi;


TestExecutionApi apiInstance = new TestExecutionApi();
Long id = 789L; // Long | 
try {
    TestExecution result = apiInstance.getTestExecutionById(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestExecutionApi#getTestExecutionById");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**|  | [enum: ]

### Return type

[**TestExecution**](TestExecution.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

<a name="getTestExecutionTestCases"></a>
# **getTestExecutionTestCases**
> List&lt;TestCase&gt; getTestExecutionTestCases(testExecutionId, status)

Get list of test cases of a test execution filtered by execution status

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestExecutionApi;


TestExecutionApi apiInstance = new TestExecutionApi();
Long testExecutionId = 789L; // Long | 
List<ExecutionStatus> status = Arrays.asList(new ExecutionStatus()); // List<ExecutionStatus> | 
try {
    List<TestCase> result = apiInstance.getTestExecutionTestCases(testExecutionId, status);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestExecutionApi#getTestExecutionTestCases");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **testExecutionId** | **Long**|  | [enum: ]
 **status** | [**List&lt;ExecutionStatus&gt;**](ExecutionStatus.md)|  | [optional]

### Return type

[**List&lt;TestCase&gt;**](TestCase.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

<a name="getTestSuites"></a>
# **getTestSuites**
> List&lt;TestSuite&gt; getTestSuites(executionId)

Get list of all test suites of an execution

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestExecutionApi;


TestExecutionApi apiInstance = new TestExecutionApi();
Long executionId = 789L; // Long | 
try {
    List<TestSuite> result = apiInstance.getTestSuites(executionId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestExecutionApi#getTestSuites");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **executionId** | **Long**|  | [enum: ]

### Return type

[**List&lt;TestSuite&gt;**](TestSuite.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

<a name="updateTestExecutionEndTime"></a>
# **updateTestExecutionEndTime**
> TestExecution updateTestExecutionEndTime(body, id)

Update a test execution end time

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestExecutionApi;


TestExecutionApi apiInstance = new TestExecutionApi();
LocalDateTime body = new LocalDateTime(); // LocalDateTime | 
Long id = 789L; // Long | 
try {
    TestExecution result = apiInstance.updateTestExecutionEndTime(body, id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestExecutionApi#updateTestExecutionEndTime");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**LocalDateTime**](LocalDateTime.md)|  |
 **id** | **Long**|  | [enum: ]

### Return type

[**TestExecution**](TestExecution.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

