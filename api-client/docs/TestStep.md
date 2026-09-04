# TestStep

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **Long** |  | 
**testCaseId** | **Long** |  | 
**testCaseName** | **String** |  |  [optional]
**description** | **String** |  | 
**objectName** | **String** |  |  [optional]
**operation** | **String** |  |  [optional]
**result** | **String** |  |  [optional]
**executionTime** | **Long** |  |  [optional]
**executionStatus** | [**ExecutionStatusEnum**](#ExecutionStatusEnum) |  | 
**attachments** | [**List&lt;TestStepAttachment&gt;**](TestStepAttachment.md) |  |  [optional]

<a name="ExecutionStatusEnum"></a>
## Enum: ExecutionStatusEnum
Name | Value
---- | -----
PLANNED | &quot;PLANNED&quot;
PROGRESS | &quot;PROGRESS&quot;
PASSED | &quot;PASSED&quot;
FAILED | &quot;FAILED&quot;
WARNING | &quot;WARNING&quot;
