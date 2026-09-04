# ApplicationApi

All URIs are relative to *http://localhost:8080*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createApplication**](ApplicationApi.md#createApplication) | **POST** /rs/application | Create an application
[**deleteApplication**](ApplicationApi.md#deleteApplication) | **DELETE** /rs/application/{id} | Delete an application
[**getAllApplications**](ApplicationApi.md#getAllApplications) | **GET** /rs/application | List all the applications
[**getApplicationById**](ApplicationApi.md#getApplicationById) | **GET** /rs/application/{id} | Get an application by ID
[**getTestExecutions**](ApplicationApi.md#getTestExecutions) | **GET** /rs/application/{applicationId}/test-executions | Get list of test executions of an application
[**updateApplication**](ApplicationApi.md#updateApplication) | **PUT** /rs/application | Update an application

<a name="createApplication"></a>
# **createApplication**
> Application createApplication(body)

Create an application

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.ApplicationApi;


ApplicationApi apiInstance = new ApplicationApi();
Application body = new Application(); // Application | 
try {
    Application result = apiInstance.createApplication(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ApplicationApi#createApplication");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Application**](Application.md)|  |

### Return type

[**Application**](Application.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="deleteApplication"></a>
# **deleteApplication**
> deleteApplication(id)

Delete an application

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.ApplicationApi;


ApplicationApi apiInstance = new ApplicationApi();
Long id = 789L; // Long | 
try {
    apiInstance.deleteApplication(id);
} catch (ApiException e) {
    System.err.println("Exception when calling ApplicationApi#deleteApplication");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="getAllApplications"></a>
# **getAllApplications**
> List&lt;Application&gt; getAllApplications()

List all the applications

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.ApplicationApi;


ApplicationApi apiInstance = new ApplicationApi();
try {
    List<Application> result = apiInstance.getAllApplications();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ApplicationApi#getAllApplications");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List&lt;Application&gt;**](Application.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

<a name="getApplicationById"></a>
# **getApplicationById**
> Application getApplicationById(id)

Get an application by ID

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.ApplicationApi;


ApplicationApi apiInstance = new ApplicationApi();
Long id = 789L; // Long | 
try {
    Application result = apiInstance.getApplicationById(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ApplicationApi#getApplicationById");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**|  |

### Return type

[**Application**](Application.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

<a name="getTestExecutions"></a>
# **getTestExecutions**
> List&lt;TestExecution&gt; getTestExecutions(applicationId)

Get list of test executions of an application

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.ApplicationApi;


ApplicationApi apiInstance = new ApplicationApi();
Long applicationId = 789L; // Long | 
try {
    List<TestExecution> result = apiInstance.getTestExecutions(applicationId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ApplicationApi#getTestExecutions");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **applicationId** | **Long**|  | [enum: ]

### Return type

[**List&lt;TestExecution&gt;**](TestExecution.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

<a name="updateApplication"></a>
# **updateApplication**
> Application updateApplication(body)

Update an application

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.ApplicationApi;


ApplicationApi apiInstance = new ApplicationApi();
Application body = new Application(); // Application | 
try {
    Application result = apiInstance.updateApplication(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling ApplicationApi#updateApplication");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Application**](Application.md)|  |

### Return type

[**Application**](Application.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

