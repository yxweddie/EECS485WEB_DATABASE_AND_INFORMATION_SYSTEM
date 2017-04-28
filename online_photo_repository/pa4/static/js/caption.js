/**
 * Created by ray on 10/24/15.
 */

"use strict";
var Home_prefix = '/v4y9q6ko6r/pa4';

function Caption(element, picid, caption) {
    this.element = element;
    this.picid = picid;
    element.value = caption; // objects in Javascript are assigned by reference, so this works
    element.addEventListener("change", this, false);
}

Caption.prototype.handleEvent = function (e) {
    if (e.type === "change") {
        this.update(this.element.value);
    }
};

Caption.prototype.change = function (value) {
    this.data = value;
    if (value)
        this.element.value = value;
    else
        this.element.value = "";
    console.log('Caption GET successful.');
};

Caption.prototype.update = function (caption) {
    makeCaptionPostRequest(this.picid, caption, function () {
        console.log('Caption POST successful.');
    });
};


function makeCaptionRequest(picid, cb) {
    qwest.get(Home_prefix + '/pic/caption?id='+picid)
        .then(function (xhr, resp) {
            cb(resp);
        });
}

function makeCaptionPostRequest(picid, caption, cb) {
  var data = {
    'id': picid,
    'caption': caption
  };

  qwest.post(Home_prefix + '/pic/caption', data, {
    dataType: 'json',
    responseType: 'json'
  }).then(function(xhr, resp) {
      cb(resp);
  });
}

function initCaption(picid) {
    var caption_node = document.getElementById("caption");
    var captionBinding = new Caption(caption_node, picid);

    makeCaptionRequest(picid, function (resp) {
        captionBinding.change(resp['caption']);
    });

    setInterval(function () {
        makeCaptionRequest(picid, function (resp) {
            captionBinding.change(resp['caption']);
        });
    }, 7000);
}
