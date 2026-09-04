# TestStepApi

All URIs are relative to *http://localhost:8080*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createTestStep**](TestStepApi.md#createTestStep) | **POST** /rs/test-step | Create a test step
[**getAllTestSteps**](TestStepApi.md#getAllTestSteps) | **GET** /rs/test-step | Get list of all test steps
[**getTestStepById**](TestStepApi.md#getTestStepById) | **GET** /rs/test-step/{id} | Get test step by ID

<a name="createTestStep"></a>
# **createTestStep**
> TestStep createTestStep(body)

Create a test step

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestStepApi;


TestStepApi apiInstance = new TestStepApi();
TestStep body = new TestStep(); // TestStep | 
try {
    TestStep result = apiInstance.createTestStep(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestStepApi#createTestStep");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**TestStep**](TestStep.md)|  |

### Return type

[**TestStep**](TestStep.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="getAllTestSteps"></a>
# **getAllTestSteps**
> List&lt;TestStep&gt; getAllTestSteps()

Get list of all test steps

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestStepApi;


TestStepApi apiInstance = new TestStepApi();
try {
    List<TestStep> result = apiInstance.getAllTestSteps();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestStepApi#getAllTestSteps");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List&lt;TestStep&gt;**](TestStep.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

<a name="getTestStepById"></a>
# **getTestStepById**
> TestStep getTestStepById(id)

Get test step by ID

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.TestStepApi;


TestStepApi apiInstance = new TestStepApi();
Long id = 789L; // Long | 
try {
    TestStep result = apiInstance.getTestStepById(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling TestStepApi#getTestStepById");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**|  | [enum: ]

### Return type

[**TestStep**](TestStep.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

