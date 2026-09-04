# EnvironmentApi

All URIs are relative to *http://localhost:8080*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createEnvironment**](EnvironmentApi.md#createEnvironment) | **POST** /rs/environment | Create an environment
[**deleteEnvironment**](EnvironmentApi.md#deleteEnvironment) | **DELETE** /rs/environment/{id} | Delete an environment
[**getAllEnvironments**](EnvironmentApi.md#getAllEnvironments) | **GET** /rs/environment | List all environments
[**getEnvironmentById**](EnvironmentApi.md#getEnvironmentById) | **GET** /rs/environment/{id} | Get an environment by ID
[**updateEnvironment**](EnvironmentApi.md#updateEnvironment) | **PUT** /rs/environment | Update an environment

<a name="createEnvironment"></a>
# **createEnvironment**
> Environment createEnvironment(body)

Create an environment

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.EnvironmentApi;


EnvironmentApi apiInstance = new EnvironmentApi();
Environment body = new Environment(); // Environment | 
try {
    Environment result = apiInstance.createEnvironment(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling EnvironmentApi#createEnvironment");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Environment**](Environment.md)|  |

### Return type

[**Environment**](Environment.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

<a name="deleteEnvironment"></a>
# **deleteEnvironment**
> deleteEnvironment(id)

Delete an environment

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.EnvironmentApi;


EnvironmentApi apiInstance = new EnvironmentApi();
Long id = 789L; // Long | 
try {
    apiInstance.deleteEnvironment(id);
} catch (ApiException e) {
    System.err.println("Exception when calling EnvironmentApi#deleteEnvironment");
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

<a name="getAllEnvironments"></a>
# **getAllEnvironments**
> List&lt;Environment&gt; getAllEnvironments()

List all environments

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.EnvironmentApi;


EnvironmentApi apiInstance = new EnvironmentApi();
try {
    List<Environment> result = apiInstance.getAllEnvironments();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling EnvironmentApi#getAllEnvironments");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**List&lt;Environment&gt;**](Environment.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

<a name="getEnvironmentById"></a>
# **getEnvironmentById**
> Environment getEnvironmentById(id)

Get an environment by ID

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.EnvironmentApi;


EnvironmentApi apiInstance = new EnvironmentApi();
Long id = 789L; // Long | 
try {
    Environment result = apiInstance.getEnvironmentById(id);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling EnvironmentApi#getEnvironmentById");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **Long**|  |

### Return type

[**Environment**](Environment.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

<a name="updateEnvironment"></a>
# **updateEnvironment**
> Environment updateEnvironment(body)

Update an environment

### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.EnvironmentApi;


EnvironmentApi apiInstance = new EnvironmentApi();
Environment body = new Environment(); // Environment | 
try {
    Environment result = apiInstance.updateEnvironment(body);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling EnvironmentApi#updateEnvironment");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Environment**](Environment.md)|  |

### Return type

[**Environment**](Environment.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: */*

