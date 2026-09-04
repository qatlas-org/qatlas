# IsAliveControllerApi

All URIs are relative to *http://localhost:8080*

Method | HTTP request | Description
------------- | ------------- | -------------
[**isStarted**](IsAliveControllerApi.md#isStarted) | **GET** /rs/isAlive | 

<a name="isStarted"></a>
# **isStarted**
> String isStarted()



### Example
```java
// Import classes:
//import com.github.ekahyukti.testreportsclient.handler.ApiException;
//import com.github.ekahyukti.testreportsclient.handler.IsAliveControllerApi;


IsAliveControllerApi apiInstance = new IsAliveControllerApi();
try {
    String result = apiInstance.isStarted();
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling IsAliveControllerApi#isStarted");
    e.printStackTrace();
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

**String**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: */*

