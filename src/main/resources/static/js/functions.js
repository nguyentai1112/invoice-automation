/* Function to handle the update */
function updateInvoice(invoiceId) {
  const newEmail = $('#email-dropdown-' + invoiceId).val();
  const status = $('#invoice-status-' + invoiceId).val();
  let update_url = '/app/invoices/' + invoiceId + '/update?email=' + newEmail
      + '&status=' + status;

  if (newEmail === '') {
    update_url = '/app/invoices/' + invoiceId + '/update?status=' + status;
  }

  console.log("newEmail", newEmail);
  console.log("update_url", update_url);
  // You can use AJAX to send the updated data to the server and perform the update
  // AJAX call using jQuery
  $.ajax({
    type: 'PUT',
    url: update_url,
    // data: {newInvoiceNo: newInvoiceNo},
    success: function (response) {
      console.log(response);
      console.log(
          "Updating Invoice ID " + invoiceId + " with new email: " + newEmail);
      // Add your update logic here
      fetchInvoices();
      // Handle success (e.g., display a success message)
    },
    error: function (error) {
      console.error(error);
      // Handle error (e.g., display an error message)
    }
  });

}

function deleteInvoice(invoiceId) {
  var isConfirmed = window.confirm('Are you sure you want to delete this invoice?');
  if (!isConfirmed) {
    return;
  }
  // You can use AJAX to send the updated data to the server and perform the update

  // AJAX call using jQuery
  $.ajax({
    type: 'DELETE',
    url: '/app/invoices/delete/' + invoiceId,
    // data: {newInvoiceNo: newInvoiceNo},
    success: function (response) {
      console.log(response);
      // Add your update logic here
      fetchInvoices();
      // Handle success (e.g., display a success message)
    },
    error: function (error) {
      console.error(error);
      // Handle error (e.g., display an error message)
    }
  });
}

// Function to fetch email data from API
// Function to fetch email data from API
function fetchEmailData() {
  return $.ajax({
    url: '/app/receivers/list',
    method: 'GET'
  });
}

// Function to update the email dropdown based on fetched data

function updateEmailDropdown(dropdown, emails) {
  dropdown.empty(); // Clear existing options

  // Add the "Select Email" option
  dropdown.append('<option value="">Select Email</option>');

  // Iterate over the fetched emails and add options to the dropdown
  $.each(emails, function (index, email) {
    dropdown.append(
        '<option value="' + email.email + '">' + email.email + '</option>');
  });
}

// Function to fetch invoices based on selected email
function fetchInvoices() {
  var selectedEmail = $('#main-email-dropdown').val();
  var selectedQuarter = $('#quarterDropdown').val();
  var url = '/app/invoices/list?status=SUCCESS&email=' + selectedEmail
      + '&quarter='
      + selectedQuarter;
  var loadingSpinner = $("#loadingSpinner");
  var invoiceTable = $("#invoiceTable");
  var tbody = $('#invoiceGrid');
  console.log("url", url);

  // Show loading spinner before making the AJAX request
  invoiceTable.hide();
  loadingSpinner.show();
  tbody.empty(); // Clear existing rows

  // Make an AJAX request to fetch invoices based on the selected email
  $.ajax({
    url: '/app/invoices/list?status=SUCCESS&email=' + selectedEmail
        + '&quarter='
        + selectedQuarter, // Replace with your API endpoint
    type: 'GET',
    success: function (data) {
      // Populate the grid with the fetched invoices
      updateInvoiceGrid(data);
      loadingSpinner.hide();
      invoiceTable.show();


    },
    error: function (error) {
      console.error('Error fetching invoices:', error);
    },
    complete: function () {
      // Hide loading spinner after the AJAX request completes (success or error)
      loadingSpinner.hide();
    }
  });

}

// Function to update the invoice grid based on fetched data
function updateInvoiceGrid(data) {
  var tbody = $('#invoiceGrid');

  tbody.empty(); // Clear existing rows

  // Iterate over the fetched invoices and add rows to the grid
  $.each(data, function (index, invoice) {
    var row = '<tr>' +
        '<td>' +
        '<a href="/app/invoices/' + invoice.id + '/details' + '">' + invoice.id
        + '</a>' +
        '</td>' +
        '<td>' + formatInvoiceDate(invoice.invoiceDate) + '</td>' +
        '<td>' + invoice.invoiceNo + '</td>' +

        '<td>' + invoice.seller + '</td>' +
        '<td>' + invoice.sellerTaxCode + '</td>' +
        '<td>' + invoice.buyer + '</td>' +
        '<td>' + formatDecimal(invoice.subTotal) + '</td>' +
        '<td>' + formatDecimal(invoice.vatAmount) + '</td>' +
        '<td>' + formatDecimal(invoice.totalPayment) + '</td>' +
        '<td>' + invoice.receiver + '</td>' +
        '<td>' + invoice.originalReceiver + '</td>' +
        '<td>' +
        '<select class="emailDropdown" id="email-dropdown-' + invoice.id + '">'
        +
        '<option value=""> Select Email</option>' +
        '</select>' +
        '</td>' +
        '<td>' +
        '<select class="invoice-status" id="invoice-status-' + invoice.id + '">'
        +
        '<option value="' + invoice.invoiceStatus + '">' + invoice.invoiceStatus
        + '</option>' +
        '<option value="CANCELLED"> CANCELLED</option>' +
        +'</td>' +

        '<td>' + (invoice.cc !== null ? invoice.cc : '') + '</td>' +
        '<td>' + formatTime(invoice.createdTime) + '</td>' +
        '<td>' + formatTime(invoice.modifiedTime) + '</td>' +
        '<td>' +
        '<button type="button" onclick="updateInvoice(' + invoice.id
        + ')">Update' +
        '</button></td>' +
        '<td>' +
        '<button type="button" onclick="deleteInvoice(' + invoice.id
        + ')">Delete' +
        '</button></td>' +
        '</tr>';

    tbody.append(row);
  });

  $('.emailDropdown').each(function () {
    updateEmailDropdown($(this), emailData);
  });
}

// Helper function to format decimal values
function formatDecimal(value) {
  return new Intl.NumberFormat('en-US', {style: 'decimal'}).format(value);
}

// Helper function to format invoice date
function formatInvoiceDate(date) {
  console.log("date", date);
  const dateComponents = date.split(/[T\+-]/);
  const year = dateComponents[0];
  const month = dateComponents[1];
  const day = dateComponents[2];
  return `${day}-${month}-${year}`;
// Format the date

}

// Helper function to format modified time
function formatTime(modifiedTime) {
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  }).format(new Date(modifiedTime));
}
