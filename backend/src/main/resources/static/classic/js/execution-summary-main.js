$(document).ready(function(){
    updateTabsURLs();
    getTestRunDetails();
    getTestSuites();
    setAutoRefresh();
    initCommentsModal();
});

function getTestRunDetails(latestTestRun) {
    $.ajax({
        url: "/rs/test-execution/" + executionId,
        method: "GET",
        context: document.body
    }).done(function(result) {
        console.log(result);
        loadTestRunDetails(result);
    });
}

function getTestSuites() {
    $.get( "/rs/test-execution/" + executionId + "/test-cases", function(data) {
        loadTestSuitesGrid(data);
    });
}

function loadTestRunDetails(testExecution) {
    setExecutionDetails(testExecution);
}

const initCommentsModal = function() {
    $('#commentsModal').on('show.bs.modal', function (e) {
        const testCaseId = $(e.relatedTarget).attr('test-case-id');
        $.get( "/rs/test-case/" + testCaseId + "/comments", function(data) {
            toggleCommentsElement(data);
            initCommentsButtons(testCaseId);
        });
    });
}

const initCommentsButtons = function(testCaseId) {
    $('#testCaseIdToUpdate').val(testCaseId);
    $('#commentsModal .modal-footer .btn-danger').attr('disabled', false);
    $('#commentsModal .modal-footer .btn-primary').attr('disabled', false);
    $('#commentsModal .modal-footer .btn-danger')
        .click(deleteComments);
    $('#commentsModal .modal-footer .btn-primary')
        .click(saveComments);
}

const saveComments = function() {
    putComments(getComments());
}

const deleteComments = function() {
    putComments(' ');
}

const putComments = function(comments) {
    $('#commentsModal .modal-footer .btn-danger').attr('disabled', true);
    $('#commentsModal .modal-footer .btn-primary').attr('disabled', true);
    if(isCommentsEditable()) {
        toggleCommentsElement();
    }
    const testCaseId = $('#testCaseIdToUpdate').val();
    $.ajax({
        url: "/rs/test-case/" + testCaseId + "/comments",
        method: 'PUT',
        contentType: 'application/json',
        data: comments
    }).done(function(data) {
        toggleCommentsElement(data);
        $('#commentsModal .modal-footer .btn-danger').attr('disabled', false);
        $('#commentsModal .modal-footer .btn-primary').attr('disabled', false);
        const commentsIcon = $('#MainReport #' + testCaseId + ' td[aria-describedby="MainReport_actions"] a[link-for="comments"] i');
        if(data && data.trim().length > 0) {
            $(commentsIcon).attr('style', 'color: blue');
            $(commentsIcon).attr('title', data);
        } else {
            $(commentsIcon).attr('style', '');
        }
        toggleClassName($('#commentsModal .modal-body'), 'successful');
    }).fail(function() {
        console.log('PUT request failed...');
        toggleClassName($('#commentsModal .modal-body'), 'failure');
    });
}

const isCommentsEditable = function() {
    const comments = $('#commentsModal .modal-body .content');
    return $(comments).is('textarea');
}

const getComments  = function() {
    const comments = $('#commentsModal .modal-body .content');
    const isEditable = isCommentsEditable();
    return isEditable ? $(comments).val() : $(comments).text();
}

const toggleCommentsElement = function() {
    const modalBody = $('#commentsModal .modal-body');
    let comments = $(modalBody).find('.content');
    let isEditable;
    let commentsVal;
    if(arguments.length > 0) {
        isEditable = true;
        commentsVal = arguments[0];
    } else {
        isEditable = $(comments).is('textarea');
        commentsVal = getComments();
    }
    modalBody.empty();
    comments = $(isEditable ? '<div>' : '<textarea>', {
        class: 'content',
        style: 'width: 35vw;height: 100px;overflow: auto',
    });
    modalBody.append(comments);
    if(isEditable) {
        comments.text(commentsVal);
        $(comments).click(function() {
            toggleCommentsElement();
        });
    } else {
        comments.val(commentsVal);
    }
}

function loadTestSuitesGrid(testCases) {
    $("#MainReport").jqGrid({
        datatype: 'local',
        data: testCases,
        autowidth: true,
        shrinkToFit: true,
        page: page,
//        colMenu : false,
        colModel: [
            {   label : "Test Suite",
                name: 'testSuiteName',
                width: 20,
                align: 'center',
                stype: "select",
                searchoptions: {
                    defaultValue: suite,
                    autosearch: true,
                    dataEvents: [
                        { type: 'change',
                            fn: function(e) {
                                console.log('suite filter changed');
                                suite = e.currentTarget.value;
                                updateTabsURLs();
                            }
                        }
                    ],
                    dataInit:function(el){
                        $("option:contains(" + suite + ")",el).attr("selected", "selected");
                        setTimeout(function(){
                            $(el).trigger('change');
                        },500);
                    },
                    /*attr: {
                        style: "width:150%;padding:0;max-width:150%;float:center"
                    },*/
                    value: ":[ALL];" +
                        Array.from(
                            new Set(
                                testCases.map(function(e) {
                                    return e.testSuiteName + ":" + e.testSuiteName;
                                })
                            )
                        ).join(";")
                }
            },
            {   label : "Test Case",
                name: 'name',
                width: 20,
                align: 'center',
                searchoptions: {
                    // dataInit is the client-side event that fires upon initializing the toolbar search field for a column
                    // use it to place a third party control to customize the toolbar
                    /*attr: {
                        style: "width:1000%;padding:0;max-width:1000%;float:center"
                    },*/
                    sopt : ['cn']
                }
            },
            {   label : "Reference",
                name: 'referenceId',
                width: 30,
                align: 'center',
                searchoptions: {
                    // dataInit is the client-side event that fires upon initializing the toolbar search field for a column
                    // use it to place a third party control to customize the toolbar
                    /*attr: {
                        style: "width:1000%;padding:0;max-width:1000%;float:center"
                    },*/
                    sopt : ['cn']
                }
            },
            /*{
                label: "System IP",
                name: 'SystemIP',
                width: 30,
                align: 'center',
                // stype defines the search type control - in this case HTML select (dropdownlist)
                stype: "select",
                // searchoptions value - name values pairs for the dropdown - they will appear as options

                searchoptions: {
                    attr: {
                        style: "width:150%;padding:0;max-width:150%;float:center"
                    },
                    value: strSystemIP } //":[All];255.255.254.0:255.255.254.0;255.255.254.10:255.255.254.10"
            },
            {
                label: "Test Title",
                name: 'TestTitle',
                width: 200,
                searchoptions: {
                    // dataInit is the client-side event that fires upon initializing the toolbar search field for a column
                    // use it to place a third party control to customize the toolbar
                    attr: {
                        style: "width:1000%;padding:0;max-width:1000%;float:center"
                    },
                    sopt : ['cn']
                }
            },
            {   label : "Browser",
                //sorttype: 'integer',
                name: 'Browser',
                width: 30,
                align: 'center',
                searchoptions : { sopt:['cn']}
            },*/
            {
                label : "Duration",
                name: 'executionTime',
                width: 10,
                classes: 'align-right-with-padding',
                formatter: milliSecondsFormatter,
                searchoptions: {
                    // dataInit is the client-side event that fires upon initializing the toolbar search field for a column
                    // use it to place a third party control to customize the toolbar
                    sopt: ['cn'],

                }
            },
            {
                label : "Status",
                name: 'executionStatus',
                width: 10,
                align: 'center',
                classes: 'status-cell',
                formatter: statusImageFormatter,
                stype: "select",
                searchoptions: {
                    defaultValue: status,
                    dataEvents: [
                        { type: 'change',
                            fn: function(e) {
                                console.log('state filter changed');
                                status = e.currentTarget.value;
                                updateTabsURLs();
                            }
                        }
                    ],
                    dataInit: function(ele) {
                        $("option:contains(" + status + ")",ele).attr("selected", "selected");
                    },
                    value: ":[ALL];" +
                        Array.from(statuses.keys()).map(function(e) {
                            return e + ":" + statuses.get(e).name;
                        }).join(";")
                }
            },
            {
                label : "Actions",
                name: 'actions',
                width: 10,
                align: 'center',
                index: 'DetailResults',
                search: false,
                formatter: actionsLinkFormatter,
                formatoptions: { baseLinkUrl: 'javascript:', showAction: "Link('", addParam: "');"}
            }
        ],
        loadonce: true,
        viewrecords: true,
        width: 780,
        align: 'center',
        height: 550,
        rowNum: 25,
        pager: "#gridpager",
//        inFilterSeparator: ",",
        loadComplete: function () {
            initJqGrid();
            loadTestStepsDialog();
        },
        onSelectRow: function(id) {
            console.log('TestCase ID: ' + id);
            const target = $(event.target);
            if($(this).getLocalRow(id).executionStatus === 'PLANNED') {
                console.log('TestCase execution not started');
            } else if(event && event.target && $(event.target).attr('aria-describedby') === 'MainReport_referenceId') {
                console.log('TestCase referenceId selected');
            } else if(event && ((event.target && $(event.target).attr('aria-describedby') === 'MainReport_actions')
                || (event.target && $(event.target).parent() && $(event.target).parent().parent() && $(event.target).parent().parent().attr('aria-describedby') === 'MainReport_actions'))) {
                console.log('Clicked on actions');
            } else {
                location.href = 'DetailReport.html?executionId=' + executionId + '&testCaseId=' + id;
            }
        }
    });
}

function ChangeUrl(page, url) {
    if (typeof (history.pushState) != "undefined") {
        var obj = { Page: page, Url: url };
        history.pushState(obj, obj.Page, obj.Url);
    } else {
        alert("Browser does not support HTML5.");
    }
}

const initJqGrid = function() {
    var page = getUrlParam('page');
    if (typeof(page) == 'undefined') {
        page = 1;
        var symbol = (window.location.href.indexOf("?") > -1) ? '&': '?';
        ChangeUrl('Page'+page, window.location.href += symbol + "page="+page);
    }

    var totalJQGridPages = $("#MainReport").jqGrid('getGridParam', 'lastpage');

    $("span.ui-icon").click(function () {
        var button = $(this), oldPage = page;
        if (button.hasClass('ui-icon-seek-first')) {
            page = 1;
        }
        else if (button.hasClass('ui-icon-seek-prev') && page > 1) {
            page = parseInt(page) - 1;
        }
        else if (button.hasClass('ui-icon-seek-next') && page < totalJQGridPages) {
            page = parseInt(page) + 1;
        }
        else if (button.hasClass('ui-icon-seek-end')) {
            page = totalJQGridPages;
        }

        ChangeUrl('Page'+page, window.location.href.replace("page="+oldPage, "page="+page));
    });
    // activate the toolbar searching
    $('#MainReport').jqGrid('filterToolbar', {
        beforeSearch: function() {
            if(status === 'EXECUTED') {
                let postData = $('#MainReport').jqGrid('getGridParam', 'postData');
                let filters = JSON.parse(postData.filters);
                let rules = filters.rules.filter(i => i.field !== 'executionStatus');
                rules.push({
                    field: 'executionStatus',
                    data: 'PLANNED',
                    op: 'ne'
                });
                rules.push({
                    field: 'executionStatus',
                    data: 'PROGRESS',
                    op: 'ne'
                });
                filters.rules = rules;
                postData.filters = JSON.stringify(filters);
                $('#MainReport').jqGrid('setGridParam', 'postData', postData);
            }
            return false;
        }
    });

    /*$("#MainReport").jqGrid('navGrid','#gridpager',
        {
            edit:false,
            add:false,
            del:false
        },
        {},
        {},
        {},
        {
            multipleSearch: false,
            multipleGroup: false,
            showQuery: true
        }
    );*/

    $('#MainReport').jqGrid('navButtonAdd', '#myGrid_toppager', {
        caption: "<select id='gridFilter' onchange='ChangeGridView()'><option>Inbox</option><option>Sent Messages</option></select>",
        title: "Apply Filter",
        onClickButton: function () {
        }
    });
}

function actionsLinkFormatter(cellvalue, options, testCase) {
    if(isExecutionCompleted(testCase.executionStatus)) {
        let returnStr = '';
        if(hasAttachments(testCase.testSteps)) {
            returnStr += '<a class="action-icon action-test-steps" testCaseId="' + testCase.id + '" title="test steps slide"><i class="fas fa-tv"/></a>';
        }
        let colorStr = '';
        let commentsLinkTitle = 'Add Comments';
        if(testCase.comments && testCase.comments.trim().length > 0) {
            colorStr = 'color: blue';
            commentsLinkTitle = testCase.comments;
        }
        return  returnStr +
            '<a href="#" link-for="comments" title="' + commentsLinkTitle + '" test-case-id="' + testCase.id + '" data-toggle="modal" data-target="#commentsModal"><i style="' + colorStr + '" class="far fa-comment-alt"></i></a>';
    }
    return '';
}

const hasAttachments = function(testSteps) {
    return testSteps
        && testSteps.length > 0
        && testSteps
            .map(i => i.attachments)
            .flat().length > 0;
}

function ChangeGridView() {
    var gridViewFilter = $("#gridFilter").val();
    $('#myGrid').setGridParam({ datatype: 'json', url: '../../Controller/ActionJSON', postData: { msgFilter: gridViewFilter } });
    $('#myGrid').trigger("reloadGrid");
};
