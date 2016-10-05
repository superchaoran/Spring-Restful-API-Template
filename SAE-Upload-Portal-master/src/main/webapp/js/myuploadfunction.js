$(function () {
    $('#fileupload').fileupload({
        dataType: 'json',
        
        done: function (e, data) {
        	$("tr:has(td)").remove();
            $.each(data.result.reverse(), function (index, file) {
                $("#uploaded-files").append(
                		$('<tr/>')
                		.append($('<td/>').text(file.fileName))
                		.append($('<td/>').text(file.fileSize))
                		.append($('<td/>').text(file.fileType))
                		.append($('<td/>').html(file.fileLink))
                		.append($('<td/>').text(file.fileStatus))
                		.append($('<td/>').text(new Date(file.fileDate)))
                		)//end $("#uploaded-files").append()
            }); 
        },
        
        progressall: function (e, data) {
	        var progress = parseInt(data.loaded / data.total * 100, 10);
	        $('#progress .bar').css(
	            'width',
	            progress + '%'
	        );
   		},
   		
		dropZone: $('#dropzone')
    });
});