/* 
 Author : "Yixiang Wu"
*/
function remove_user_privilege(url_prefix, album_id, username){
    var data = {};
    data['album_id'] = album_id;
    data['username'] = username;
    var url = url_prefix + "/album/edit/remove_access";
    $.ajax({
        method: "POST",
        url: url,
        data: JSON.stringify(data, null, '\t'),
        contentType: 'application/json; charset=UTF-8',
        success: function(result){
            if(result == 'True')
            {
                location.reload();
                alert("Successful remove!");
            }
            else
                alert("Fail, try again.");
        }
    })

}


function add_user_privilege(url_prefix, album_id){
    var data = {};
    data['album_id'] = album_id;
    data['username'] = $("#new_username").find("option:selected").text();
    var url = url_prefix + "/album/edit/add_access";
    $.ajax({
        method: "POST",
        url: url,
        data: JSON.stringify(data, null, '\t'),
        contentType: 'application/json; charset=UTF-8',
        success: function(result){
            if(result == 'True')
            {
                location.reload();
                alert("Successful add!");
            }
            else
                alert("Fail, try again.");
        }
    })

}


function modify_album_info(url_prefix, album_id){
    // use jQuery to catch the input from website
    var data = {};
    data['album_id'] = album_id;
    data['name'] = $("#album_name").val();
    data['status'] = $("#album_status").find("option:selected").text();
    var url = url_prefix + "/album/edit/info";
    $.ajax({
        method: "POST",
        url: url,
        data: JSON.stringify(data, null, '\t'),
        contentType: 'application/json; charset=UTF-8',
        success: function(result){
            if(result == 'True')
                alert("Successful modify!");
            else
                alert("Fail, try again.");
        }
    })
}


function modify_pic_caption(url_prefix, picid){
    var data = {};
    data['picid'] = picid;
    data['caption'] = $("#new_caption").val();
    var url = url_prefix + "/pic/caption";
    $.ajax({
        method: "POST",
        url: url,
        data: JSON.stringify(data, null, '\t'),
        contentType: 'application/json; charset=UTF-8',
        success: function(result){
            if(result == 'True')
            {
                location.reload();
                alert("Successful modify!");
            }
            else
                alert("Fail, try again.");
        }
    })
}

console.log("Hello, EECS485")