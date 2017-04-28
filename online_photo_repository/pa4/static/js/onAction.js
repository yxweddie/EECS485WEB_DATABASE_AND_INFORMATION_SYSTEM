var Home_prefix = '/v4y9q6ko6r/pa4';
function onPost(did){
	var data = {
		"did": did,
		"single_display" : true
	};
	$.post(Home_prefix + "/search", data);
}