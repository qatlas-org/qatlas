const urlParams = new URLSearchParams(window.location.search);
let executionId = urlParams.get("executionId");

let page = getUrlParam("page", 1);
let status = getUrlParam("status", "ALL");
let suite = getUrlParam("suite", "ALL");
let application = getUrlParam("application", "ALL");
let release = getUrlParam("release", "ALL");
let execution = getUrlParam("execution", "ALL");

let testCaseId = urlParams.get("testCaseId");

let statuses = new Map();
statuses
    .set('PLANNED', {
        colorCode: '#337ab7',
        name: 'Planned',
        icon: '/Logos/warning.png',
        iconStyle: 'fas fa-clock fa-lg'
    })
    .set('PROGRESS', {
        colorCode: '#f0ad4e',
        name: 'In progress',
        icon: '/Logos/warning.png',
        iconStyle: 'fas fa-arrow-alt-circle-right fa-lg'
    })
    .set('PASSED', {
        colorCode: '#5cb85c',
        name: 'Passed',
        icon: '/Logos/pass.ico',
        iconStyle: 'fas fa-check-circle fa-lg'
    })
    .set('FAILED', {
        colorCode: '#d9534f',
        name: 'Failed',
        icon: '/Logos/fail.ico',
        iconStyle: 'fas fa-times-circle fa-lg'
    })
    .set('WARNING', {
        colorCode: '#f0ad4e',
        name: 'Warning',
        icon: '/Logos/warning.png',
        iconStyle: 'fas fa-exclamation-triangle fa-lg'
    })
    .set('SKIPPED', {
        colorCode: '#f0ad4e',
        name: 'Skipped',
        icon: '/Logos/warning.png',
        iconStyle: 'fas fa-exclamation-triangle fa-lg'
    })
    .set('EXECUTED', {
        colorCode: '#f0ad4e',
        name: 'Executed',
        icon: '/Logos/pass.ico',
        iconStyle: 'fas fa-check-circle fa-lg'
    });

const isExecutionCompleted = function(executionStatus) {
    return executionStatus !== 'PLANNED'
        && executionStatus !== 'PROGRESS'
        && executionStatus !== 'SKIPPED';
}

function getUrlParam(paramName, defaultVal) {
    const paramVal = urlParams.get(paramName);
    if(arguments.length > 1 && defaultVal) {
        return paramVal != null && paramVal != '' ? paramVal : defaultVal;
    }
    return paramVal;
}

/*function updateTabsURLs() {
    if(executionId != null) {
        $("#IdExecutionSummary").attr("href", "ExecutionSummary.html?executionId=" + executionId);
        $("#dashboardLink").attr("href", "index.html?executionId=" + executionId);
    }
}*/

function updateTabsURLs() {
    if(executionId != null) {
        $("#IdExecutionSummary")
            .attr("href", "ExecutionSummary.html?executionId=" + executionId + "&page=" + page + "&status=" + status + "&suite=" + suite);
        $("#dashboardLink").attr("href", "index.html?executionId=" + executionId + "&application=" + application + "&release=" + release + "&execution=" + execution);
    }
}

//setInterval(page_refresh, 5*1000); //NOTE: period is passed in milliseconds

function startTime() {
    var today = new Date();
    var h = today.getHours();
    var m = today.getMinutes();
    var s = today.getSeconds();
    // add a zero in front of numbers<10
    m = checkTime(m);
    s = checkTime(s);
    //document.getElementById('txt').innerHTML=h+":"+m+":"+s;
    document.getElementById('txt').innerHTML = "   " + today;
    t = setTimeout(startTime, 500);
}
function checkTime(i) {
    if (i < 10) {
        i = "0" + i;
    }
    return i;
}

$(document).ready(function() {
    //$.getScript( "https://kit.fontawesome.com/1d83ed769a.js", function() {});
    startTime();
})

function statusImageFormatter(cellvalue, options, rowObject) {
    const statusObj = getStatusObj(rowObject.executionStatus);
    return getStatusIcon(statusObj);
}

const getStatusObj = function(status) {
    return statuses.get(status);
}

const getStatusIcon = function(status) {
    return '<i ' +
        'style="color: ' + status.colorCode + ';" ' +
        'title="' + status.name +
        '" class="' + status.iconStyle +
        '"/>';
}

function parseInteger(intVal) {
    return intVal != null ? parseInt(intVal) : 0;
}

const setAutoRefresh = function(interval) {
    if(arguments.length == 0) {
        interval = 300000;
    }
    setInterval(function() {
      window.location.reload();
    }, interval);
}

const milliSecondsFormatter = function(cellValue, options, rowObject) {
    if(cellValue) {
        return msToTime(cellValue);
    }
    return '';
}

function msToTime(duration) {
    var milliseconds = Math.floor(duration % 1000),
    seconds = Math.floor((duration / 1000) % 60),
    minutes = Math.floor((duration / (1000 * 60)) % 60),
    hours = Math.floor((duration / (1000 * 60 * 60)) % 24),
    days = Math.floor((duration / (1000 * 60 * 60 * 24)) % 365);

    return (days > 0 ? (days + 'd').padStart(5, ' ') : '') +
        (hours > 0 ? (hours + 'h').padStart(4, ' ') : '') +
        (minutes > 0 ? (minutes + 'm').padStart(4, ' ') : '') +
        (seconds + 's').padStart(4, ' ') +
        (milliseconds + 'ms').padStart(6, ' ');
}

const setExecutionDetails = function(testExecution) {
    const startTime = new Date(testExecution.startTime);
    const endTime = new Date(testExecution.endTime);
    //$("#SystemDate").html(startTime.toLocaleDateString());
    $("#RunStarted").html(startTime.toLocaleString());
    $("#RunEnded").html(testExecution.endTime ? endTime.toLocaleString() : '');
    $("#TotalExecutionTime").html(testExecution.endTime ? msToTime(endTime - startTime) : '');

    $("#ExecutedBy").html(testExecution.executedBy.toUpperCase());
    $("#BrowserType").html(testExecution.browser.toUpperCase());
    $("#ProjectName").html(testExecution.applicationName.toUpperCase());
    $("#TargettedEnvironment").html(testExecution.environmentName.toUpperCase());
    $("#Release").html(testExecution.applicationVersion.toUpperCase());
    $("#Run").html(testExecution.name.toUpperCase());
    //$("#ComputerName").html("Computer Name : " + ComputerName);
    //$("#DomainName").html("Domain Name  : Telecom");
    //$("#Tools").html("Automation Tools : Selenium WebDriver - JAVA");
}

const toggleClassName = function(element, className, callback) {
    $(element).addClass(className);
    $(element).on('animationend', function(){
        $(element).removeClass(className);
    });
    if(callback) {
        $(element).on('animationend', function(){
            callback.call();
        });
    }
}
