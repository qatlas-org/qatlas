# TestCaseApi

All URIs are relative to *http://localhost:8080*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createTestCase**](TestCaseApi.md#createTestCase) | **POST** /rs/test-case | Create a test case
[**getComments**](TestCaseApi.md#getComments) | **GET** /rs/test-case/{id}/comments | Get comments of a test case
[**getTestCaseById**](TestCaseApi.md#getTestCaseById) | **GET** /rs/test-case/{id} | Get a test cases by it&#x27;s ID
[**getTestCaseTestSteps**](TestCaseApi.md#getTestCaseTestSteps) | **GET** /rs/test-case/{testCaseId}/test-steps | Get the list of Test Steps of a Test Case
[**getTestCases**](TestCaseApi.md#getTestCases) | **GET** /rs/test-case | Get list of all Test Cases
[**updateComments**](TestCaseApi.md#updateComments) | **PUT** /rs/test-case/{id}/comments | Update comments of a test case
[**updateTestCase**](TestCaseApi.md#updateTestCase) | **PUT** /rs/test-case | Update a test case
[**updateTestCaseStatus**](TestCaseApi.md#updateTestCaseStatus) | **PUT** /rs/test-case/{id}/status/{executionStatus} | Update execution status of a test case
[**updateTestCaseStatusAndExecutionEndTime**](TestCaseApi.md#updateTestCaseStatusAndExecutionEndTime) | **PUT** /rs/test-case/{id}/status/{executionStatus}/endTime/{executionEndTime} | Update execution status and end time of a test case
[**updateTestCaseStatusAndStartTime**](TestCaseApi.md#updateTestCaseStatusAndStartTime) | **PUT** /rs/test-case/{id}/status/{executionStatus}/startTime/{executionStartTime} | Update execution status and start time of a test case

<a name="createTestCase"></a>
# **createTestCase**
> TestCase createTestCase(body)

Create a test case

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestCaseApi;


TestCaseApi apiInstance = new TestCaseApi();
TestCase body = new TestCase(); // TestCase | 
try {
    TestCase result = apiInstance.createTestCase(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestCaseApi#createTestCase");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**TestCase**](TestCase.md)|  |

### Return type

[**TestCase**](TestCase.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getComments"></a>
# **getComments**
> String getComments(id)

Get comments of a test case

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestCaseApi;


TestCaseApi apiInstance = new TestCaseApi();
Long id = 789L; // Long | 
try {
    String result = apiInstance.getComments(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestCaseApi#getComments");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**|  | [enum: ]

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

<a name="getTestCaseById"></a>
# **getTestCaseById**
> TestCase getTestCaseById(id)

Get a test cases by it&#x27;s ID

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestCaseApi;


TestCaseApi apiInstance = new TestCaseApi();
Long id = 789L; // Long | 
try {
    TestCase result = apiInstance.getTestCaseById(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestCaseApi#getTestCaseById");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**|  | [enum: ]

### Return type

[**TestCase**](TestCase.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

<a name="getTestCaseTestSteps"></a>
# **getTestCaseTestSteps**
> List&lt;TestStep&gt; getTestCaseTestSteps(testCaseId)

Get the list of Test Steps of a Test Case

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestCaseApi;


TestCaseApi apiInstance = new TestCaseApi();
Long testCaseId = 789L; // Long | 
try {
    List<TestStep> result = apiInstance.getTestCaseTestSteps(testCaseId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestCaseApi#getTestCaseTestSteps");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **testCaseId** | **Long**|  | [enum: ]

### Return type

[**List&lt;TestStep&gt;**](TestStep.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

<a name="getTestCases"></a>
# **getTestCases**
> List&lt;TestCase&gt; getTestCases()

Get list of all Test Cases

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestCaseApi;


TestCaseApi apiInstance = new TestCaseApi();
try {
    List<TestCase> result = apiInstance.getTestCases();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestCaseApi#getTestCases");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List&lt;TestCase&gt;**](TestCase.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

<a name="updateComments"></a>
# **updateComments**
> String updateComments(body, id)

Update comments of a test case

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestCaseApi;


TestCaseApi apiInstance = new TestCaseApi();
String body = "body_example"; // String | 
Long id = 789L; // Long | 
try {
    String result = apiInstance.updateComments(body, id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestCaseApi#updateComments");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**String**](String.md)|  |
 **id** | **Long**|  | [enum: ]

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="updateTestCase"></a>
# **updateTestCase**
> TestCase updateTestCase(body)

Update a test case

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestCaseApi;


TestCaseApi apiInstance = new TestCaseApi();
TestCase body = new TestCase(); // TestCase | 
try {
    TestCase result = apiInstance.updateTestCase(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestCaseApi#updateTestCase");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**TestCase**](TestCase.md)|  |

### Return type

[**TestCase**](TestCase.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="updateTestCaseStatus"></a>
# **updateTestCaseStatus**
> TestCase updateTestCaseStatus(id, executionStatus)

Update execution status of a test case

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestCaseApi;


TestCaseApi apiInstance = new TestCaseApi();
Long id = 789L; // Long | 
ExecutionStatus executionStatus = new ExecutionStatus(); // ExecutionStatus | 
try {
    TestCase result = apiInstance.updateTestCaseStatus(id, executionStatus);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestCaseApi#updateTestCaseStatus");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**|  | [enum: ]
 **executionStatus** | [**ExecutionStatus**](.md)|  |

### Return type

[**TestCase**](TestCase.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

<a name="updateTestCaseStatusAndExecutionEndTime"></a>
# **updateTestCaseStatusAndExecutionEndTime**
> TestCase updateTestCaseStatusAndExecutionEndTime(id, executionStatus, executionEndTime)

Update execution status and end time of a test case

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestCaseApi;


TestCaseApi apiInstance = new TestCaseApi();
Long id = 789L; // Long | 
ExecutionStatus executionStatus = new ExecutionStatus(); // ExecutionStatus | 
LocalDateTime executionEndTime = new LocalDateTime(); // LocalDateTime | 
try {
    TestCase result = apiInstance.updateTestCaseStatusAndExecutionEndTime(id, executionStatus, executionEndTime);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestCaseApi#updateTestCaseStatusAndExecutionEndTime");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**|  | [enum: ]
 **executionStatus** | [**ExecutionStatus**](.md)|  |
 **executionEndTime** | **LocalDateTime**|  |

### Return type

[**TestCase**](TestCase.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

<a name="updateTestCaseStatusAndStartTime"></a>
# **updateTestCaseStatusAndStartTime**
> TestCase updateTestCaseStatusAndStartTime(id, executionStatus, executionStartTime)

Update execution status and start time of a test case

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestCaseApi;


TestCaseApi apiInstance = new TestCaseApi();
Long id = 789L; // Long | 
ExecutionStatus executionStatus = new ExecutionStatus(); // ExecutionStatus | 
LocalDateTime executionStartTime = new LocalDateTime(); // LocalDateTime | 
try {
    TestCase result = apiInstance.updateTestCaseStatusAndStartTime(id, executionStatus, executionStartTime);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestCaseApi#updateTestCaseStatusAndStartTime");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**|  | [enum: ]
 **executionStatus** | [**ExecutionStatus**](.md)|  |
 **executionStartTime** | **LocalDateTime**|  |

### Return type

[**TestCase**](TestCase.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

