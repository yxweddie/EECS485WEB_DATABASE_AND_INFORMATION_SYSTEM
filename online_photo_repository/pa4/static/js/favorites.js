"use strict";
var Home_prefix = '/v4y9q6ko6r/pa4';

function Favorite(element1,element2,element3,picid,favoriteNumber,favoriteUsername){
	this.element1  = element1;
	this.element2  = element2;
	if (element3 !== null){
		this.element3  = element3;
		element3.addEventListener("click", this, false); 
	}
	this.picid     = picid;
	element1.innerHTML = favoriteNumber;
	element2.innerHTML = favoriteUsername;
	
}

Favorite.prototype.handleEvent = function (e) {
	if (e.type == "click"){
		this.update();
	}
};

Favorite.prototype.change = function(value1, value2){
	this.element1.innerHTML = value1;
	this.element2.innerHTML = value2;
};

Favorite.prototype.update = function(){
	var username = document.getElementById("username").innerHTML;
	console.log(username);
	makeFavoritePostRequest(this.picid, username,function(resp){
		console.log('favorites POST successful, submitted the like');
	});
};

function makeFavoriteRequest(picid, callback){
	qwest.get(Home_prefix + '/pic/favorites?id=' + picid)
		.then(function (xhr, resp){
			callback(resp);
		});
}

function makeFavoritePostRequest(picid, username, callback){
	var data = {
		"id": picid,
		"username" : username
	};
	qwest.post(Home_prefix + '/pic/favorites', data, {
		dataType: 'json',
		responseType: 'json'
	}).then(function (xhr, resp) {
		callback(resp);
		makeFavoriteRequest(picid,function(resp){
			document.getElementById("favoriteNumber").innerHTML = resp['num_favorites'];
			document.getElementById("favoriteUsername").innerHTML = resp['latest_favorite'];
		});
	});
}

function initFavorite(picid) {
	var favoriteNumber   = document.getElementById("favoriteNumber");
	var favoriteUsername = document.getElementById("favoriteUsername");
	var favoriteButton   = document.getElementById("favoriteButton");
	var favoriteBinding  = new Favorite(favoriteNumber,favoriteUsername,favoriteButton,picid);
	
	makeFavoriteRequest(picid,function(resp){
		favoriteBinding.change(resp['num_favorites'],resp['latest_favorite']);
	});

	setInterval(function () {
		makeFavoriteRequest(picid,function(resp){
			favoriteBinding.change(resp['num_favorites'], resp['latest_favorite']);
		});
	}, 10000);
}

