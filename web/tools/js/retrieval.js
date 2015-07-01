var tt = tt || {};

tt.retrieval = function () {

    var API = "http://warua.org:8080/retrieve?text=";

    var retrieveContent = function (url, obj) {
        $.ajax({
            url: API + url,
            method: 'GET',
            success: function(data){
                obj.html(data);
            },
            dataType: "html"
        });
    };

    return {
        retrieveContent: retrieveContent
    };
}();