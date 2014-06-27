goog.provide('net.purplexsu.CommentPage');

goog.require('net.purplexsu.Page');


/**
 * Comment page class.
 * @constructor
 * @extends {net.purplexsu.Page}
 */
net.purplexsu.CommentPage = function() {
  goog.base(this);

  this.sitePattern_ = new RegExp("^(http://|https://|ftp://).*\..*$");

  this.textPattern_ = new RegExp("http://|https://", "g");
};
goog.inherits(net.purplexsu.CommentPage, net.purplexsu.Page);


// JavaScript for comment files:
/**
 * @type {string}
 */
net.purplexsu.CommentPage.INPUT_ID_ALERT =
    "\u8bf7\u586b\u5199\u60a8\u7684ID\u3002";


/**
 * @type {string}
 */
net.purplexsu.CommentPage.EMPTY_TEXT_AREA_COMMENT_ALERT =
    "\u7559\u8a00\u4e0d\u80fd\u4e3a\u7a7a\u3002";


/**
 * @type {string}
 */
net.purplexsu.CommentPage.INVALID_TEXT_AREA_COMMENT_ALERT =
    "\u4e3a\u9632\u6b62\u5783\u573e\u7559" +
    "\u8a00\uff0c\u60a8\u53ea\u80fd\u5728\u6bcf\u6b21\u7559\u8a00" +
    "\u91cc\u5f15\u7528\u4e00\u4e2a\u8d85\u94fe\u63a5\u5730\u5740" +
    "\uff08http://\uff09\u3002\n\u540c\u65f6\uff0c\u8fd9\u4e2a\u5730" +
    "\u5740\u53ea\u4f1a\u4ee5\u7eaf\u6587\u672c\u7684\u5f62\u5f0f\u663e\u793a\u3002";


/**
 * @type {string}
 */
net.purplexsu.CommentPage.INPUT_CAPTCHA_ALERT =
    "\u8bf7\u586b\u5199\u9a8c\u8bc1\u7801\u3002";


/** @override */
net.purplexsu.CommentPage.prototype.renderInternal = function() {
  var commentForm = goog.dom.getElement("CommentForm");
  if (commentForm) {
    this.handler.listen(commentForm, goog.events.EventType.SUBMIT, this.verifyForm_);
  }
  var commentInput = goog.dom.getElement("comment");
  if (commentInput) {
    commentInput.value = "";
  }
  var captcha = goog.dom.getElement("captcha_img");
  //this.handler.listen(captcha, goog.events.EventType.CLICK, this.refreshCaptcha_);
  this.refreshCaptcha_();
};


/**
 * Verify every input value of the comment form.
 * @param {goog.events.Event} e The SUBMIT event.
 * @private
 */
net.purplexsu.CommentPage.prototype.verifyForm_ = function(e) {
  var idInput = goog.dom.getElement("id");
  if (idInput && (idInput.value == null || idInput.value == "")) {
    window.alert(net.purplexsu.CommentPage.INPUT_ID_ALERT);
    idInput.focus();
    e.preventDefault();
    return;
  }
  var siteInput = goog.dom.getElement("site");
  if (siteInput && siteInput.value != null && siteInput.value != "") {
    if (!this.sitePattern_.test(siteInput.value)) {
      siteInput.value = "http://" + siteInput.value;
    }
  }
  var commentInput = goog.dom.getElement("comment");
  if (commentInput) {
    var text = commentInput.value;
    if (text == null || text == "") {
      window.alert(net.purplexsu.CommentPage.EMPTY_TEXT_AREA_COMMENT_ALERT);
      commentInput.focus();
      e.preventDefault();
      return;
    } else {
      var count = 0;
      while(this.textPattern_.exec(text) != null){
        count++;
      }
      if(count > 1){
        window.alert(net.purplexsu.CommentPage.INVALID_TEXT_AREA_COMMENT_ALERT);
        commentInput.focus();
        e.preventDefault();
        return;
      }
    }
  }
  var captchaInput = goog.dom.getElement("captcha");
  if (captchaInput && (captchaInput.value == null || captchaInput.value == "")) {
    window.alert(net.purplexsu.CommentPage.INPUT_CAPTCHA_ALERT);
    captchaInput.focus();
    e.preventDefault();
    return;
  }
};


/**
 * Refresh captcha image after being clicked.
 * @private
 */
net.purplexsu.CommentPage.prototype.refreshCaptcha_ = function() {
  var captcha = goog.dom.getElement("captcha_img");
  captcha.src = "../captcha.php?" + new Date().getTime();
};

/**
 * Static function to call at the end of the JS.
 */
net.purplexsu.CommentPage.render = function() {
  var page = new net.purplexsu.CommentPage();
  page.render();
  goog.events.listen(window, goog.events.EventType.UNLOAD, function() {
    page.dispose();
  });
};


(function () {
  net.purplexsu.CommentPage.render();
})();
