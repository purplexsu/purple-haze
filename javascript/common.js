// JavaScript for entire site

function keyHandler(event) {
  var e = event || window.event;
  var code = e.charCode || e.keyCode;
  if (code == 37) {
    // left arrow key
    var p = document.getElementById("PreviousPage");
    if (p) {
      window.location = p.href || window.location;
    } else {
      p = document.getElementById("PreviousArticle");
      if (p) {
        window.location = p.href || window.location;
      }
    }
  } else if (code == 39) {
    // right arrow key
    var n = document.getElementById("NextPage");
    if (n) {
      window.location = n.href || window.location;
    } else {
      n = document.getElementById("NextArticle");
      if (n) {
        window.location = n.href || window.location;
      }
    }
  }
}

function enableArrowKey() {
  if (document.addEventListener) {
    document.addEventListener("keydown", keyHandler, false);
  } else {
    document.onkeydown = keyHandler;
  }
}

function Connection() {
  this.xmlHttp = null;
  if (window['XMLHttpRequest']) {
    this.xmlHttp = new XMLHttpRequest();
  } else if (window['ActiveXObject']) {
    this.xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
  }
};

Connection.prototype.isReady = function() {
  if (this.xmlHttp.readyState == 0 || this.xmlHttp.readyState == 4) {
    return true;
  }
  return false;
};

Connection.prototype.getResponseCode = function() {
  return this.xmlHttp.status;
};

Connection.prototype.getResponseXml = function() {
  return this.xmlHttp.responseXML;
};

Connection.prototype.get = function(url, callback, errorCallback) {
  if (this.xmlHttp != null) {
    if (!this.isReady()) {
      return;
    }
    try {
      this.xmlHttp.onreadystatechange = this.getGetHandler(callback, errorCallback);
      this.xmlHttp.open("GET", url, true);
      this.xmlHttp.send(null);
    } catch (err) {
      if (errorCallback) {
        errorCallback(err);
      }
    }
  }
};

Connection.prototype.getGetHandler = function (callback, errorCallback) {
  var connection = this;
  return function() {
    if (connection.xmlHttp.readyState == 4) {
      if (connection.xmlHttp.status == 200) {
        callback(connection.xmlHttp.responseText);
      } else if (errorCallback) {
        errorCallback(connection);
      }
    }
  };
};

function Cookie (name) {
  this.$name = name;
}

Cookie.prototype.store = function (daysToLive) {
  var cookieValue = "";
  for(var property in this) {
    if (property.charAt(0) == "$" || ((typeof this[property]) == "function")) {
      continue;
    }
    if (cookieValue != "") {
      cookieValue += "&";
    }
    cookieValue += (property + ":" + encodeURIComponent(this[property]));
  }
  var cookie = $name + "=" + cookieValue;
  if (!(daysToLive || daysToLive == 0)) {
    cookie += "; max-age=" + (daysToLive * 24 * 60 * 60);
  }
  document.cookie = cookie;
}

Cookie.prototype.read = function (rawText) {
  if(!document.cookie) {
    return;
  }
  var cookies = document.cookie.split(";");
  var cookie = null;
  for(var i = 0; i < cookies.length; i++){
    if (cookies[i].indexOf($name + "=") == 0) {
      cookie = cookies[i];
      break;
    }
  }
  if(cookie == null) {
    return;
  }
  var cookieValue = cookie.substring($name.length + 1);
  var pairs = cookieValue.split("&");
  for(var i = 0; i < pairs.length; i++) {
    var pair = pairs[i].split(":");
    this[pair[0]] = decodeURIComponent(pair[1]);
  }  
}
