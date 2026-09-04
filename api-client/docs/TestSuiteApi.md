# TestSuiteApi

All URIs are relative to *http://localhost:8080*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createTestSuite**](TestSuiteApi.md#createTestSuite) | **POST** /rs/test-suite | Create a test suite
[**getAllTestSuites**](TestSuiteApi.md#getAllTestSuites) | **GET** /rs/test-suite | Get list of test suites
[**getTestSuiteById**](TestSuiteApi.md#getTestSuiteById) | **GET** /rs/test-suite/{id} | Get a test suites by ID
[**getTestSuiteTestCases**](TestSuiteApi.md#getTestSuiteTestCases) | **GET** /rs/test-suite/{testSuiteId}/test-cases | Get list of test cases of a test suite filtered by execution status
[**updateTestSuite**](TestSuiteApi.md#updateTestSuite) | **PUT** /rs/test-suite | Update a test suite

<a name="createTestSuite"></a>
# **createTestSuite**
> TestSuite createTestSuite(body)

Create a test suite

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestSuiteApi;


TestSuiteApi apiInstance = new TestSuiteApi();
TestSuite body = new TestSuite(); // TestSuite | 
try {
    TestSuite result = apiInstance.createTestSuite(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestSuiteApi#createTestSuite");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**TestSuite**](TestSuite.md)|  |

### Return type

[**TestSuite**](TestSuite.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getAllTestSuites"></a>
# **getAllTestSuites**
> List&lt;TestSuite&gt; getAllTestSuites()

Get list of test suites

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestSuiteApi;


TestSuiteApi apiInstance = new TestSuiteApi();
try {
    List<TestSuite> result = apiInstance.getAllTestSuites();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestSuiteApi#getAllTestSuites");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List&lt;TestSuite&gt;**](TestSuite.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

<a name="getTestSuiteById"></a>
# **getTestSuiteById**
> TestSuite getTestSuiteById(id)

Get a test suites by ID

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestSuiteApi;


TestSuiteApi apiInstance = new TestSuiteApi();
Long id = 789L; // Long | 
try {
    TestSuite result = apiInstance.getTestSuiteById(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestSuiteApi#getTestSuiteById");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**|  | [enum: ]

### Return type

[**TestSuite**](TestSuite.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

<a name="getTestSuiteTestCases"></a>
# **getTestSuiteTestCases**
> List&lt;TestCase&gt; getTestSuiteTestCases(testSuiteId, status)

Get list of test cases of a test suite filtered by execution status

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestSuiteApi;


TestSuiteApi apiInstance = new TestSuiteApi();
Long testSuiteId = 789L; // Long | 
List<ExecutionStatus> status = Arrays.asList(new ExecutionStatus()); // List<ExecutionStatus> | 
try {
    List<TestCase> result = apiInstance.getTestSuiteTestCases(testSuiteId, status);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestSuiteApi#getTestSuiteTestCases");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **testSuiteId** | **Long**|  | [enum: ]
 **status** | [**List&lt;ExecutionStatus&gt;**](ExecutionStatus.md)|  | [optional]

### Return type

[**List&lt;TestCase&gt;**](TestCase.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

<a name="updateTestSuite"></a>
# **updateTestSuite**
> TestSuite updateTestSuite(body)

Update a test suite

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestSuiteApi;


TestSuiteApi apiInstance = new TestSuiteApi();
TestSuite body = new TestSuite(); // TestSuite | 
try {
    TestSuite result = apiInstance.updateTestSuite(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestSuiteApi#updateTestSuite");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**TestSuite**](TestSuite.md)|  |

### Return type

[**TestSuite**](TestSuite.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

