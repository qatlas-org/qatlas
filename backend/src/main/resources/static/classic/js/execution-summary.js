$(document).ready(function() {
    $('#testStepsModal').on('show.bs.modal', function (e) {
      $('.carousel').carousel();
    });
});

const loadTestStepsDialog = function() {
    $('.action-test-steps').on('click', function(event) {
        const testCaseId = $(this).attr('testCaseId');
        getTestSteps(testCaseId);
    });
}

const getTestSteps = function(testCaseId) {
    $.get( "/rs/test-case/" + testCaseId, function(data) {
        showTestStepsModal(data);
    });
}

const showTestStepsModal = function(testCase) {
    const modal = $('#testStepsModal');
    modal.find('.carousel-indicators').empty();
    modal.find('.carousel-inner').empty();
    const modalTitle = $(modal).find('.modal-title');
    modalTitle.empty();
    modalTitle.append('Test Case: ');
    const testCaseTitle = $('<small>');
    testCaseTitle.text(testCase.name);
    modalTitle.append(testCaseTitle);
    testCase
        .testSteps
        .forEach(testStep => {
            processTestStepForCarousel(testStep);
        });
    $('#testStepsModal').modal({show:true});
}

const processTestStepForCarousel = function(testStep) {
    testStep
        .attachments
        .forEach(attachment => {
            addTestStepAttachmentToCarousel(attachment, testStep);
        });
}

const addTestStepAttachmentToCarousel = function(testStepAttachment, testStep) {
    const modal = $('#testStepsModal');
    const indicator = modal
        .find('.carousel-indicators');
    indicator
        .append(
            getIndicator(indicator.children().length)
        );
    const inner = modal
        .find('.carousel-inner');
    inner
        .append(
            appendCarouselItem(testStepAttachment, testStep, inner.children().length)
        );
}

const getIndicator = function(slideTo) {
    const indicator = $('<li>', {class: slideTo == 0 ? 'active' : ''});
    $(indicator)
        .attr('data-target', '#testStepsCarousel')
        .attr('data-slide-to', slideTo);
    return indicator;
}

const appendCarouselItem = function(attachment, testStep, slideTo) {
    const classVal = 'card item' + (slideTo == 0 ? ' active' : '');
    const item = $('<div>', {class: classVal});

    if(attachment.attachmentType === 'SNAPSHOT') {
        const image = $('<img>', {
            class: 'card-img-top slide-' + slideTo,
            src: '/attachment/' + attachment.attachmentRelativePath,
            alt: attachment.attachmentType
        });
        $(item).append(image);
    } else {
        const aLink = $('<a>', {
            target: '_blank',
            href: '/attachment/' + attachment.attachmentRelativePath
        });
        const fileIcon = $('<i>', {class: 'fas fa-file-alt fa-10x'});
        $(aLink).append(fileIcon);
        $(item).css('text-align', 'center');
        $(item).append(aLink);
    }

    const cardBody = $('<div>', {class: 'card-body'});

    cardBody.append(getStepDetailsTable(testStep));

    item.append(cardBody);

    return item;
}

const getStepDetailsTable = function(testStep) {
    const table = $('<table>');
    let row = $('<tr>');
    let td = $('<td>');
    let tdVal = $('<h4>', {class: 'card-title'});
    tdVal.text('Step Description');
    td.append(tdVal);
    row.append(td);

    td = $('<td>');
    tdVal = $('<h4>', {class: 'card-title'});
    tdVal.text(':');
    td.append(tdVal);
    row.append(td);

    td = $('<td>');
    tdVal = $('<h4>', {class: 'card-title'});
    let tdContent = $('<small>');
    tdContent.text(testStep.description);
    tdVal.append(tdContent);
    td.append(tdVal);
    row.append(td);

    table.append(row);

    row = $('<tr>');
    td = $('<td>');
    tdVal = $('<h4>', {class: 'card-title'});
    tdVal.text('Actual Result');
    td.append(tdVal);
    row.append(td);

    td = $('<td>');
    tdVal = $('<h4>', {class: 'card-title'});
    tdVal.text(':');
    td.append(tdVal);
    row.append(td);

    td = $('<td>');
    tdVal = $('<h4>', {class: 'card-title'});
    tdContent = $('<small>');
    tdContent.text(testStep.result);
    tdVal.append(tdContent);
    td.append(tdVal);
    row.append(td);

    table.append(row);
    return table;
}

