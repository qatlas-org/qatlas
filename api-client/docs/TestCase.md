# TestCase

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **Long** |  | 
**testSuiteId** | **Long** |  | 
**testSuiteName** | **String** |  |  [optional]
**name** | **String** |  | 
**referenceId** | **String** |  |  [optional]
**executionStatus** | [**ExecutionStatus**](ExecutionStatus.md) |  | 
**executionStartTime** | [**LocalDateTime**](LocalDateTime.md) |  | 
**executionEndTime** | [**LocalDateTime**](LocalDateTime.md) |  |  [optional]
**comments** | **String** |  |  [optional]
**testSteps** | [**List&lt;TestStep&gt;**](TestStep.md) |  |  [optional]
**testExecution** | [**TestExecution**](TestExecution.md) |  |  [optional]
**executionTime** | **Long** |  |  [optional]
**testStepCountWithWarnings** | **Integer** |  |  [optional]
**totalTestStepCount** | **Integer** |  |  [optional]
**failedTestStepCount** | **Integer** |  |  [optional]
**executedTestStepCount** | **Integer** |  |  [optional]
**passedTestStepCount** | **Integer** |  |  [optional]
