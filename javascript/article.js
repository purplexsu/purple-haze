goog.provide('net.purplexsu.ArticlePage');

goog.require('net.purplexsu.Page');


/**
 * Article page class.
 * @constructor
 * @extends {net.purplexsu.Page}
 */
net.purplexsu.ArticlePage = function() {
  goog.base(this);
};
goog.inherits(net.purplexsu.ArticlePage, net.purplexsu.Page);


/** @override */
net.purplexsu.ArticlePage.prototype.renderInternal = function() {
  this.externalLinks_();
  this.constructEmail_();
};


/**
 * Add target=_blank to external links.
 * @private
 */
net.purplexsu.ArticlePage.prototype.externalLinks_ = function() {
 var anchors = goog.dom.getElementsByTagNameAndClass(goog.dom.TagName.A);
  for (var i = 0; i < anchors.length; i++) {
    var anchor = anchors[i];
    if (anchor.getAttribute("href") &&
        anchor.getAttribute("rel") == "external") {
      anchor.target = "_blank";
    }
  }
};


/**
 * Construct and set email address.
 * @private
 */
net.purplexsu.ArticlePage.prototype.constructEmail_ = function() {
  var anchor = goog.dom.getElement("contact");
  if (anchor) {
    anchor.href = "mailto" + ":purplexsu" + "@gmai" + "l.com";
  }
};


/**
 * Static function to call at the end of the JS.
 */
net.purplexsu.ArticlePage.render = function() {
  var page = new net.purplexsu.ArticlePage();
  page.render();
  goog.events.listen(window, goog.events.EventType.UNLOAD, function() {
    page.dispose();
  });
};


(function () {
  net.purplexsu.ArticlePage.render();
})();