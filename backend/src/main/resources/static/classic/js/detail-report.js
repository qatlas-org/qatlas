$(document).ready(function () {
    updateTabsURLs();
    startTime();
    getTestCaseDetails();
});

function loadTestSteps(testSteps) {
    let index = 1;
    testSteps.forEach(function (e) {
        e.sNo = index++;
    })
    $("#MainReport").jqGrid({
        datatype: 'local',
        data: testSteps,
        autowidth: true,
        shrinkToFit: true,
        page: page,
        colMenu: false,
        sortable: false,
        colModel: [{
            label: "Step No",
            //sorttype: 'integer',
            name: 'sNo',
            width: 25,
            sortable: false,
            align: 'center'
        },
            {
                label: "Step Description",
                name: 'description',
                width: 70,
                align: 'left',
                sortable: false
            },
            {
                label: "Object Name",
                name: 'objectName',
                width: 70,
                sortable: false
            },
            {
                label: "Action/Operation",
                name: 'operation',
                width: 40,
                align: 'center',
                sortable: false
            },
            {
                label: "Actual Result",
                name: 'result',
                width: 70,
                align: 'left',
                sortable: false
            },
            {
                label: "Duration",
                name: 'executionTime',
                width: 30,
                classes: 'align-right-with-padding',
                formatter: milliSecondsFormatter,
                sortable: false
            },
            {
                label: "Status",
                name: 'executionStatus',
                width: 20,
                align: 'center',
                formatter: statusImageFormatter,
                sortable: false
                // searchoptions: {
                //  // dataInit is the client-side event that fires upon initializing the toolbar search field for a column
                //  // use it to place a third party control to customize the toolbar
                //	sopt : ['cn']
                //   }
            },

            {
                label: "Snapshot",
                name: '',
                width: 30,
                align: 'center',
                formatter: filesLinkFormatter,
                index: 'DetailResults',
                sortable: false
            }
        ],
        loadonce: true,
        viewrecords: true,
        width: 780,
        align: 'center',
        height: 550,
        rowNum: 20,
        pager: "#gridpager"
    });
}

function getTestSteps(testCaseId) {
    $.get("/rs/test-case/" + testCaseId + "/test-steps", function(data) {
        loadTestSteps(data);
    });
}

function getTestCaseDetails() {
    $.get( "/rs/test-case/" + testCaseId, function(data) {
        getTestSteps(testCaseId);
        loadTestRunDetails(data.testExecution);
        loadTestCaseDetails(data);
    });
}

function loadTestRunDetails(testExecution) {
    $("#ExecutedBy").html(testExecution.executedBy.toUpperCase());
    $("#BrowserType").html(testExecution.browser.toUpperCase());
    $("#ProjectName").html(testExecution.applicationName.toUpperCase());
    $("#TargettedEnvironment").html(testExecution.environmentName.toUpperCase());
    $("#SystemIP").html(testExecution.systemIp);
}

function loadTestCaseDetails(testCase) {
    const startTime = new Date(testCase.executionStartTime);
    const executionEnd = new Date(testCase.executionEndTime);

    $("#TestCaseName").html(testCase.name.toUpperCase());
    //$("#SystemDate").html(startTime.toLocaleDateString());
    $("#RunStarted").html(startTime.toLocaleString());
    $("#RunEnded").html(testCase.executionEndTime ? executionEnd.toLocaleString() : '');
    $("#TotalExecutionTime").html(milliSecondsFormatter(testCase.executionTime));
    $("#StepsPassed").html(parseInteger(testCase.passedTestStepCount));
    $("#StepsFailed").html(parseInteger(testCase.failedTestStepCount));
    $("#StepsWarnings").html(parseInteger(testCase.testStepCountWithWarnings));
}


function callFunctionFromScript(jsonTestCaseID) {
    $('#MainReport').jqGrid('navButtonAdd', '#myGrid_toppager', {
        caption: "<select id='gridFilter' onchange='ChangeGridView()'><option>Inbox</option><option>Sent Messages</option></select>",
        title: "Apply Filter",
        onClickButton: function() {}
    });
}

function testStepStatusFormatter(cellvalue, options, rowObject) {
    return "<img title='" + cellvalue + "' src='" + statuses.get(rowObject.executionStatus).icon + "' />";
}

function filesLinkFormatter(cellvalue, options, rowObject) {
    return "<ul class='attachment-icons'>" +
        rowObject.attachments.map(function (e) {
            var cssClassName = e.attachmentType === 'SNAPSHOT' ? 'fas fa-file-image' : 'fas fa-file-alt';
            return '<li><a title="' + e.attachmentType + '" class="' + cssClassName + '" target="_blank" href="/attachment/' + e.attachmentRelativePath + '"></a></li>';
        }).join("") + "</ul>";
    //return "<a class='file-list-tooltip' title='" + titleVal + "' href='#'>Show Details</a>";
}

function GetTimeInSeconds(DurationTime) {
    var str = DurationTime.split(":");
    var k = str[0] * 3600 + str[1] * 60 + str[2] * 1;
    return k / 60;
}

function PudateHTMLParameters(passCount, failCount, warningCount, RunStarted, RunEnded, RunTime) {
    //document.getElementById("TestsPassed").innerHTML = "Total no of Tests Passed : " + passCount;
    //document.getElementById("TestsFailed").innerHTML = "Total no of Tests failed : " + failCount;
    //document.getElementById("TotalScripts").innerHTML = " Total no of Tests Executed : "+ (eval(passCount ) + eval(failCount)+ eval(warningCount));
    //document.getElementById("TestsWarnings").innerHTML = "Total no of Tests completed with Warning : " + warningCount;
}




function ChangeGridView() {
    var gridViewFilter = $("#gridFilter").val();
    $('#myGrid').setGridParam({
        datatype: 'json',
        url: '../../Controller/ActionJSON',
        postData: {
            msgFilter: gridViewFilter
        }
    });
    $('#myGrid').trigger("reloadGrid");
};